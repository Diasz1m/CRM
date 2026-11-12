# CRM Backend

API REST em Java 25 com Spring Boot, Spring Security, PostgreSQL e Flyway. Permite acompanhar leads e gerenciar produtos, com acesso restrito a um administrador.

## Rodar localmente

Você precisa do JDK 25 ou superior, Maven 3.6.3 ou superior e Docker com Docker Compose. O Compose inicia o PostgreSQL na porta local 5432. O Flyway aplica a migração na primeira inicialização.

```bash
docker compose up -d --wait
read -rsp 'Senha do administrador (mínimo de 12 caracteres): ' CRM_ADMIN_PASSWORD
printf '\n'
export CRM_ADMIN_PASSWORD
mvn spring-boot:run
```

A API fica em `http://localhost:8080`. O usuário padrão é `admin` e pode ser alterado com `CRM_ADMIN_USERNAME`. O Spring Security mantém o usuário em memória e usa PBKDF2 para verificar a senha. Para mudar as credenciais, altere as variáveis e reinicie a aplicação.

Para usar uma instância própria de PostgreSQL, configure `CRM_DB_URL` (URL JDBC), `CRM_DB_USER` e `CRM_DB_PASSWORD`. Os valores padrão do banco correspondem ao Compose local. Em uma implantação HTTPS, configure `CRM_SECURE_COOKIE=true`.

## Endpoints

- `GET /api/auth/csrf` entrega o token CSRF da sessão.
- `POST /api/auth/login` recebe `username` e `password` como formulário URL-encoded. Retorna `204` no sucesso e `401` com credenciais inválidas.
- `GET /api/auth/me` retorna o usuário autenticado.
- `POST /api/auth/logout` encerra a sessão e retorna `204`.
- `POST /api/leads` cadastra um lead e retorna `201`, o registro e o cabeçalho `Location`.
- `GET /api/leads` lista do mais recente para o mais antigo, com filtros opcionais `q` (trecho do nome, sem distinção entre maiúsculas e minúsculas) e `stage`. Use `page` a partir de zero e `size` de até 100. A resposta contém `content` e `page`.
- `GET /api/leads/{id}` consulta um lead.
- `PUT /api/leads/{id}` atualiza os campos do lead. Envie o nome e pelo menos um contato. Campos opcionais omitidos são limpos, e a etapa atual é preservada quando `stage` é omitido.

As etapas são `NEW`, `CONTACTED`, `PROPOSAL_SENT`, `WON` e `LOST`. Um novo lead começa em `NEW` se nenhuma etapa for informada. `nextActionDate` aceita uma data no formato `AAAA-MM-DD`.

### Produtos

- `POST /api/products` cadastra um produto e retorna `201` com o registro. Envie `name` (até 150 caracteres) e `price` (valor não negativo com até dez dígitos inteiros e duas casas decimais). `description` é opcional e aceita até 1.000 caracteres.
- `GET /api/products?page=0&size=20` lista os produtos do mais novo para o mais antigo. A resposta contém `content` e `page`. O tamanho máximo da página é 100.
- `DELETE /api/products/{id}` remove o produto do banco e retorna `204`. Repetir a chamada para o mesmo ID também retorna `204`.

Exemplo de corpo para o cadastro:

```json
{"name":"Consultoria","description":"Sessão de uma hora","price":150.00}
```

### Relatório de leads

`GET /api/reports/leads?from=2026-11-01&to=2026-11-11` retorna o total de leads cadastrados no período e a contagem por etapa atual. Requer a sessão de administrador.

O backend encaminha essa chamada ao microserviço `crm-reports`, cuja execução e configuração estão descritas em [crm-reports/README.md](../crm-reports/README.md). Configure a mesma `CRM_REPORTS_PASSWORD` nos dois processos. `CRM_REPORTS_URL` define o endereço do serviço e usa `http://127.0.0.1:8081` por padrão. `CRM_REPORTS_USERNAME` usa `crm-reports` por padrão. O backend pode iniciar com a senha de relatórios vazia. Nesse caso, chamadas de relatórios retornam `503`.

O serviço consulta as contagens em `GET /internal/leads/stage-counts?start=...&end=...`, com instantes ISO-8601 em UTC e fim exclusivo. Essa rota exige HTTP Basic com as credenciais de serviço. O acesso aos relatórios pelo navegador utiliza a sessão de administrador no backend. Falhas de comunicação entre os serviços retornam `503`. Em produção, configure endereços internos e transporte HTTPS para a comunicação entre serviços.

Os parâmetros `from` e `to` são obrigatórios, no formato `AAAA-MM-DD`, com anos entre 0001 e 9999. Ambos os dias são incluídos no fuso `America/Sao_Paulo`. Datas inválidas, parâmetros ausentes ou data inicial posterior à final retornam `400`.

```json
{
  "from": "2026-11-01",
  "to": "2026-11-11",
  "timeZone": "America/Sao_Paulo",
  "total": 3,
  "byStage": {
    "NEW": 2,
    "CONTACTED": 0,
    "PROPOSAL_SENT": 0,
    "WON": 1,
    "LOST": 0
  }
}
```

Todas as etapas aparecem, inclusive com zero. Um período vazio retorna total zero. O filtro usa a data de cadastro e a contagem reflete a etapa atual de cada lead. Se a etapa mudar, consultas posteriores do mesmo período refletirão essa alteração.

## Experimentar a API

Em outro terminal Bash, use o mesmo usuário e senha da inicialização. Os comandos usam `curl` e `jq`.

```bash
base=http://localhost:8080
cookies=$(mktemp)

csrf_header() {
  curl -fsS -b "$cookies" -c "$cookies" "$base/api/auth/csrf" |
    jq -er '.headerName + ": " + .token'
}

read -rsp 'Senha do administrador: ' admin_password
printf '\n'
csrf=$(csrf_header)

printf '%s' "$admin_password" |
  curl -fsS -b "$cookies" -c "$cookies" -H "$csrf" \
    --data-urlencode 'username=admin' --data-urlencode 'password@-' \
    "$base/api/auth/login"
unset admin_password

csrf=$(csrf_header)
curl -fsS -b "$cookies" -H "$csrf" -H 'Content-Type: application/json' \
  --data '{"name":"Ana Silva","email":"ana@example.com","source":"Indicação","nextAction":"Ligar"}' \
  "$base/api/leads"

curl -fsS -b "$cookies" "$base/api/leads?stage=NEW&q=ana&page=0&size=10"
curl -fsS -b "$cookies" "$base/api/reports/leads?from=2026-11-01&to=2026-11-11"
curl -fsS -b "$cookies" -H "$csrf" -X POST "$base/api/auth/logout"
rm -f "$cookies"
```

O cookie de sessão é usado nas chamadas autenticadas. Envie também o token CSRF no cabeçalho informado por `GET /api/auth/csrf` em chamadas de escrita, incluindo login e logout. Busque um token novo após login ou logout. Para um frontend em desenvolvimento, um proxy de `/api` no Vite permite usar o mesmo fluxo de cookies.

# CRM Reports

Microserviço de relatórios em Java 25 e Spring Boot. Executa na porta 8081 e calcula o relatório de leads consultando uma API interna do CRM. Os projetos Maven do backend e de relatórios podem ser compilados e executados independentemente.

## Executar localmente

Inicie o PostgreSQL pelo Compose do backend. Defina uma senha de serviço com pelo menos 12 caracteres e use o mesmo valor nos dois processos.

No terminal do backend, dentro de `crm-backend`, execute

```bash
read -rsp 'Senha do administrador: ' CRM_ADMIN_PASSWORD
printf '\n'
read -rsp 'Senha do serviço de relatórios: ' CRM_REPORTS_PASSWORD
printf '\n'
export CRM_ADMIN_PASSWORD CRM_REPORTS_PASSWORD
mvn spring-boot:run
```

Em outro terminal, dentro de `crm-reports`, execute

```bash
read -rsp 'Mesma senha do serviço de relatórios: ' CRM_REPORTS_PASSWORD
printf '\n'
export CRM_REPORTS_PASSWORD
mvn spring-boot:run
```

`CRM_REPORTS_USERNAME` usa `crm-reports` por padrão e deve coincidir nos dois processos. `CRM_BACKEND_URL` define o endereço do CRM, com padrão `http://127.0.0.1:8080`. `CRM_REPORTS_PORT` altera a porta deste serviço, com padrão 8081. No backend, configure `CRM_REPORTS_URL` caso o endereço do serviço seja alterado.

## Relatório de leads

O frontend acessa `GET /api/reports/leads?from=2026-11-01&to=2026-11-11` na porta 8080, usando a sessão de administrador do CRM. O backend chama o serviço de relatórios na porta 8081 com HTTP Basic. O serviço converte as datas em instantes e consulta as contagens por etapa em `/internal/leads/stage-counts` no CRM, também com HTTP Basic. A senha de serviço deve ficar nos processos do servidor.

O retorno contém `from`, `to`, `timeZone`, `total` e `byStage`. Todas as etapas aparecem, inclusive com zero. As datas são obrigatórias, usam o formato `AAAA-MM-DD` e aceitam anos entre 0001 e 9999. Ambos os dias são incluídos no fuso `America/Sao_Paulo`. O filtro usa a data de cadastro e conta a etapa atual de cada lead.

Datas inválidas ou invertidas retornam `400`. Credenciais de serviço ausentes ou inválidas retornam `401` ao acessar o serviço diretamente. Falhas na consulta ao CRM retornam `503`. O backend também responde `503` quando o serviço de relatórios está inacessível ou rejeita suas credenciais.

A consulta no CRM agrega as contagens em uma única instrução no PostgreSQL. Esta versão depende da disponibilidade do CRM durante a geração do relatório e reflete os dados atuais. Em produção, use endereços internos e HTTPS entre os serviços.

## Compilar

```bash
mvn verify
```

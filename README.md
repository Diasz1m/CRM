# CRM

CRM com gerenciamento de leads, catálogo de produtos e relatório de leads por período.

## Projetos

- [crm-backend](crm-backend/README.md) contém a API em Java 25 e Spring Boot, autenticação de administrador, gerenciamento de leads e produtos e migrações PostgreSQL com Flyway. Executa na porta 8080.
- [crm-frontend](crm-frontend/README.md) contém o painel em Vue 3, TypeScript e Vite, com login e telas de leads e produtos. Em desenvolvimento, executa normalmente na porta 5173.
- [crm-reports](crm-reports/README.md) contém o microserviço Spring Boot de relatórios. Executa na porta 8081 e consulta contagens por etapa através da API interna do backend.

## Executar localmente

Instale Java 25, Maven, Node.js compatível com o frontend e Docker com Docker Compose.

1. Inicie o PostgreSQL com `docker compose up -d --wait` dentro de `crm-backend`.
2. Configure `CRM_ADMIN_PASSWORD` no backend e `CRM_REPORTS_PASSWORD` com o mesmo valor no backend e no serviço de relatórios. As senhas devem ter pelo menos 12 caracteres.
3. Execute `mvn spring-boot:run` em terminais separados dentro de `crm-backend` e `crm-reports`.
4. Dentro de `crm-frontend`, execute `npm ci` e `npm run dev`.
5. Abra o endereço informado pelo Vite e entre com o usuário `admin` e a senha configurada.

Os READMEs de cada projeto descrevem as variáveis de ambiente, os endpoints e os comandos de compilação.

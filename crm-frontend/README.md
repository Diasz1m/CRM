# CRM Frontend

Projeto Vue 3 + TypeScript + Vite para a API do CRM em Spring Boot. A interface tem login de administrador, listagem paginada, busca por nome, filtro por etapa e formulário para cadastrar ou editar leads.

## Desenvolvimento local

1. Inicie o backend e confirme que ele responde em `http://localhost:8080`.
2. Nesta pasta, com Node.js 20.19+ na linha 20 ou Node.js 22.12+, rode:

   ```bash
   npm ci
   npm run dev
   ```

3. Abra o endereço informado pelo Vite, normalmente `http://localhost:5173`. Use o usuário e a senha configurados no backend, normalmente o usuário `admin`.

O Vite encaminha `/api` para `http://127.0.0.1:8080` por meio de [server.proxy](https://vite.dev/config/server-options.html#server-proxy). Ajuste o alvo em `vite.config.ts` se o backend estiver em outro endereço. O navegador envia o cookie de sessão automaticamente nas chamadas ao frontend. O cliente da API busca um token CSRF na sessão antes de cada escrita, incluindo login, logout e cadastro.

## Estrutura

- `src/App.vue` verifica a sessão, mostra o login e monta a área comercial.
- `src/api.ts` centraliza as chamadas HTTP e o tratamento de sessão e CSRF.
- `src/leads.ts` descreve os dados e as etapas dos leads.
- `src/components/` contém a listagem e o formulário.

`npm run typecheck` verifica TypeScript. `npm run build` verifica os tipos e gera `dist/`. Para servir esse build em produção, publique os arquivos estáticos e encaminhe `/api` para o Spring na mesma origem.

# Arquitetura

O projeto é dividido em duas aplicações.

## Frontend

O Angular é responsável por:

- renderizar vitrine, login, cadastro, anúncios e chat;
- guardar o token JWT no `localStorage`;
- enviar o token nas requisições HTTP pelo interceptor;
- chamar a API REST do backend;
- abrir uma conexão WebSocket para receber mensagens em tempo real.

Principais pastas:

```txt
frontend/src/app/pages       telas da aplicação
frontend/src/app/services    comunicação com backend e autenticação
frontend/src/app/models      tipos usados pelo frontend
frontend/src/environments    URLs e configuração por ambiente
```

## Backend

O Spring Boot é responsável por:

- autenticar usuários;
- gerar e validar JWT;
- proteger rotas privadas;
- gerenciar usuários, produtos e chats;
- persistir dados no PostgreSQL;
- publicar mensagens novas via WebSocket/STOMP.

Principais camadas:

```txt
controller   expõe endpoints HTTP
service      concentra regras de negócio
repository   acessa o banco com Spring Data JPA
entity       representa tabelas do banco
dto          define entrada e saída da API
config       segurança, CORS e WebSocket
```

## Autenticação

1. O usuário envia e-mail e senha para `POST /login`.
2. O backend valida as credenciais com Spring Security.
3. O backend retorna um JWT.
4. O frontend salva o token em `localStorage`.
5. O interceptor Angular envia `Authorization: Bearer <token>` nas chamadas privadas.
6. O filtro de segurança do backend valida o token e autentica a requisição.

## Produtos

Produtos são criados por usuários autenticados. Cada produto possui vendedor, preço, descrição, status e imagens. A vitrine lista apenas produtos com status disponível.

## Conversas

Uma conversa é criada a partir de um produto. O comprador não pode iniciar conversa com o próprio anúncio. Cada chat liga comprador, vendedor e produto.

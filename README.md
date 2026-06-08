# Desapego Marketplace

Marketplace full stack para compra e venda de produtos entre usuários, com anúncios, autenticação JWT, upload de imagens e chat entre comprador e vendedor.

## Funcionalidades

- Cadastro e login de usuários.
- Vitrine pública de produtos disponíveis.
- Criação, edição e exclusão de anúncios.
- Upload de múltiplas imagens via Cloudinary.
- Página "Meus anúncios" para gerenciamento do vendedor.
- Chat por produto entre comprador e vendedor.
- Listagem de conversas recentes.
- Remoção de conversa da lista do usuário.
- Atualização em tempo real das mensagens com WebSocket/STOMP.

## Tecnologias

**Backend**

- Java 17
- Spring Boot 3
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL
- WebSocket/STOMP

**Frontend**

- Angular
- TypeScript
- RxJS
- Bootstrap
- Cloudinary

## Estrutura

```txt
desapego-marketplace/
  backend/   API Spring Boot
  frontend/  Aplicação Angular
  docs/      Notas de arquitetura e estudo
```

## Como rodar localmente

### 1. Banco de dados

Crie um banco PostgreSQL chamado `marketplace`.

Configuração local padrão:

```txt
host: localhost
porta: 5432
banco: marketplace
usuario: postgres
senha: postgres
```

Esses valores podem ser alterados por variáveis de ambiente. Veja `.env.example`.

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run
```

No Windows:

```bash
cd backend
mvnw.cmd spring-boot:run
```

API local:

```txt
http://localhost:8080
```

### 3. Frontend

Configure `frontend/src/environments/environment.ts` com seus dados locais:

```ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  wsUrl: 'ws://localhost:8080',
  cloudinaryCloudName: 'seu-cloud-name',
  cloudinaryUploadPreset: 'seu-upload-preset',
};
```

Depois rode:

```bash
cd frontend
npm install
npm run start
```

Frontend local:

```txt
http://localhost:4200
```

## Como o chat funciona

O envio da mensagem é feito por REST para aproveitar o fluxo normal de autenticação JWT:

```txt
Frontend -> POST /api/chats/{chatId}/mensagens -> Backend salva no banco
```

Depois de salvar, o backend publica a mensagem no tópico WebSocket:

```txt
/topic/chat/{chatId}
```

Assim, REST cuida de autenticação, validação e persistência; WebSocket/STOMP cuida da atualização em tempo real.

## Variáveis importantes

Backend:

```env
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
JWT_SECRET=
APP_BASE_URL=
APP_CORS_ALLOWED_ORIGINS=
```

Frontend:

```ts
apiUrl: 'https://url-do-backend'
wsUrl: 'wss://url-do-backend'
cloudinaryCloudName: 'cloud-name'
cloudinaryUploadPreset: 'upload-preset'
```

## Deploy

Sugestão de hospedagem gratuita:

- Frontend: Vercel
- Backend: Koyeb, Render ou outro host Java
- Banco: Supabase, Neon ou outro PostgreSQL gerenciado

Para produção, lembre-se de:

- usar uma chave forte em `JWT_SECRET`;
- configurar `APP_CORS_ALLOWED_ORIGINS` com a URL pública do frontend;
- trocar `apiUrl` e `wsUrl` em `environment.prod.ts`;
- configurar Cloudinary com um upload preset restrito;
- não versionar arquivos `.env`.

## Documentação de estudo

- [Arquitetura](docs/arquitetura.md)
- [Fluxo do chat](docs/chat.md)

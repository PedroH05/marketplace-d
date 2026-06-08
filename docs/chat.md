# Fluxo do chat

O chat usa REST e WebSocket juntos.

## Criação do chat

Quando o usuário clica em "Entrar em contato", o frontend chama:

```txt
POST /api/chats
```

O backend verifica:

- se o usuário está autenticado;
- se o produto existe;
- se o produto está disponível;
- se o usuário não é o vendedor do próprio produto;
- se já existe uma conversa para aquele comprador, vendedor e produto.

Se já existir, o backend reaproveita o chat. Se não existir, cria um novo.

## Envio de mensagem

O envio é feito por REST:

```txt
POST /api/chats/{chatId}/mensagens
```

Isso facilita autenticação e persistência:

- o JWT vai no header HTTP;
- o Spring Security identifica o usuário;
- o serviço valida se o usuário participa do chat;
- a mensagem é salva no banco.

Depois de salvar, o backend publica a mensagem:

```txt
/topic/chat/{chatId}
```

## Recebimento em tempo real

Ao abrir uma conversa, o frontend se inscreve no tópico:

```txt
/topic/chat/{chatId}
```

Quando o backend publica uma nova mensagem, os usuários conectados naquela conversa recebem a atualização sem recarregar a página.

## Remover conversa

Remover uma conversa não apaga o histórico do banco. O backend apenas oculta o chat para o usuário logado. Se uma nova mensagem chegar, a conversa volta a aparecer.

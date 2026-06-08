package com.pedro.marketplace.config;

import com.pedro.marketplace.entity.Usuario;
import com.pedro.marketplace.repository.ChatRepository;
import com.pedro.marketplace.repository.UsuarioRepository;
import com.pedro.marketplace.service.TokenService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String CHAT_TOPIC_PREFIX = "/topic/chat/";

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final ChatRepository chatRepository;

    public WebSocketAuthInterceptor(TokenService tokenService,
                                    UsuarioRepository usuarioRepository,
                                    ChatRepository chatRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
        this.chatRepository = chatRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            autenticar(accessor);

            return org.springframework.messaging.support.MessageBuilder.createMessage(message.getPayload(),
                                                                        accessor.getMessageHeaders());
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            validarInscricao(accessor);
        }

        if (StompCommand.SEND.equals(accessor.getCommand())) {
            exigirUsuarioAutenticado(accessor);
        }

        return message;
    }

    private void autenticar(StompHeaderAccessor accessor) {
        String token = recuperarToken(accessor);
        String email = tokenService.validarToken(token);

        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Token inválido para conexão WebSocket.");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Usuário não encontrado para conexão WebSocket."));

        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        accessor.setUser(authentication);
    }

    private void validarInscricao(StompHeaderAccessor accessor) {
        Principal usuarioAutenticado = exigirUsuarioAutenticado(accessor);
        String destination = accessor.getDestination();

        if (destination == null || !destination.startsWith(CHAT_TOPIC_PREFIX)) {
            return;
        }

        Long chatId = extrairChatId(destination);
        Long usuarioId = buscarUsuarioId(usuarioAutenticado);

        if (!chatRepository.existsByIdAndParticipanteId(chatId, usuarioId)) {
            throw new AccessDeniedException("Você não tem permissão para acompanhar este chat.");
        }
    }

    private Principal exigirUsuarioAutenticado(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();

        if (user == null) {
            throw new AccessDeniedException("Usuário não autenticado no WebSocket.");
        }

        return user;
    }

    private Long buscarUsuarioId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken authentication
                && authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario.getId();
        }

        return usuarioRepository.findByEmail(principal.getName())
                .map(Usuario::getId)
                .orElseThrow(() -> new AccessDeniedException("Usuário não encontrado para conexão WebSocket."));
    }

    private Long extrairChatId(String destination) {
        String valor = destination.substring(CHAT_TOPIC_PREFIX.length());

        try {
            return Long.valueOf(valor);
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Destino de chat inválido.");
        }
    }

    private String recuperarToken(StompHeaderAccessor accessor) {
        String authorization = primeiroHeader(accessor, "Authorization");

        if (authorization == null || authorization.isBlank()) {
            authorization = primeiroHeader(accessor, "authorization");
        }

        if (authorization == null || authorization.isBlank()) {
            throw new AccessDeniedException("Token não enviado na conexão WebSocket.");
        }

        return authorization.replace("Bearer ", "").trim();
    }

    private String primeiroHeader(StompHeaderAccessor accessor, String nome) {
        List<String> headers = accessor.getNativeHeader(nome);
        return headers == null || headers.isEmpty() ? null : headers.get(0);
    }
}

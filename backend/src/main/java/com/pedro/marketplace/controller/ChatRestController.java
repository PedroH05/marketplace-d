package com.pedro.marketplace.controller;

import com.pedro.marketplace.dto.ChatOutputDTO;
import com.pedro.marketplace.dto.ChatResumoDTO;
import com.pedro.marketplace.dto.CriarChatDTO;
import com.pedro.marketplace.dto.EnviarMensagemDTO;
import com.pedro.marketplace.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatRestController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatRestController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public ResponseEntity<Long> iniciarChat(@RequestBody @Valid CriarChatDTO dto) {
        Long chatId = chatService.buscarOuCriarChat(dto);
        return ResponseEntity.ok(chatId);
    }

    @GetMapping
    public ResponseEntity<List<ChatResumoDTO>> listarChats() {
        return ResponseEntity.ok(chatService.listarChatsDoUsuarioLogado());
    }

    @GetMapping("/{chatId}/historico")
    public ResponseEntity<List<ChatOutputDTO>> obterHistorico(@PathVariable Long chatId) {
        List<ChatOutputDTO> historico = chatService.buscarHistorico(chatId);
        return ResponseEntity.ok(historico);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> removerChatDaLista(@PathVariable Long chatId) {
        chatService.ocultarChatDoUsuarioLogado(chatId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{chatId}/mensagens")
    public ResponseEntity<ChatOutputDTO> enviarMensagem(@PathVariable Long chatId,
                                                        @RequestBody @Valid EnviarMensagemDTO dto) {
        ChatOutputDTO output = chatService.salvarMensagem(chatId, dto.mensagem());
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, output);
        return ResponseEntity.ok(output);
    }
}

package com.pedro.marketplace.service;

import com.pedro.marketplace.dto.ChatOutputDTO;
import com.pedro.marketplace.dto.ChatResumoDTO;
import com.pedro.marketplace.dto.CriarChatDTO;
import com.pedro.marketplace.entity.Chat;
import com.pedro.marketplace.entity.Mensagem;
import com.pedro.marketplace.entity.Produto;
import com.pedro.marketplace.entity.StatusProduto;
import com.pedro.marketplace.entity.Usuario;
import com.pedro.marketplace.exception.EstadoInvalidoException;
import com.pedro.marketplace.exception.RecursoNaoEncontradoException;
import com.pedro.marketplace.repository.ChatRepository;
import com.pedro.marketplace.repository.MensagemRepository;
import com.pedro.marketplace.repository.ProdutoRepository;
import com.pedro.marketplace.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final MensagemRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;

    public ChatService(ChatRepository chatRepository,
                       MensagemRepository mensagemRepository,
                       UsuarioRepository usuarioRepository,
                       ProdutoRepository produtoRepository) {
        this.chatRepository = chatRepository;
        this.mensagemRepository = mensagemRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public ChatOutputDTO salvarMensagem(Long chatId, String mensagem) {
        return salvarMensagem(chatId, mensagem, buscarUsuarioLogado());
    }

    private ChatOutputDTO salvarMensagem(Long chatId, String mensagem, Usuario remetente) {
        Chat chat = buscarChatPermitido(chatId, remetente);
        String texto = normalizarMensagem(mensagem);

        chat.setOcultoParaComprador(false);
        chat.setOcultoParaVendedor(false);

        Mensagem novaMensagem = new Mensagem(chat, remetente, texto);
        Mensagem mensagemSalva = mensagemRepository.saveAndFlush(novaMensagem);

        return montarOutput(mensagemSalva);
    }

    @Transactional(readOnly = true)
    public List<ChatOutputDTO> buscarHistorico(Long chatId) {
        Usuario usuarioLogado = buscarUsuarioLogado();
        buscarChatPermitido(chatId, usuarioLogado);

        return mensagemRepository.findByChatIdOrderByDataEnvioAsc(chatId)
                .stream()
                .map(this::montarOutput)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatResumoDTO> listarChatsDoUsuarioLogado() {
        Usuario usuarioLogado = buscarUsuarioLogado();

        return chatRepository.findByParticipanteId(usuarioLogado.getId())
                .stream()
                .map(chat -> montarResumo(chat, usuarioLogado))
                .sorted(Comparator.comparing(
                        ChatResumoDTO::dataUltimaMensagem,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    @Transactional
    public void ocultarChatDoUsuarioLogado(Long chatId) {
        Usuario usuarioLogado = buscarUsuarioLogado();
        Chat chat = buscarChatPermitido(chatId, usuarioLogado);

        if (Objects.equals(chat.getComprador().getId(), usuarioLogado.getId())) {
            chat.setOcultoParaComprador(true);
        } else {
            chat.setOcultoParaVendedor(true);
        }
    }

    @Transactional
    public Long buscarOuCriarChat(CriarChatDTO dto) {
        Usuario comprador = buscarUsuarioLogado();

        Produto produto = produtoRepository.findById(dto.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado!"));

        if (produto.getStatus() != StatusProduto.DISPONIVEL) {
            throw new EstadoInvalidoException("Este produto não está disponível para contato.");
        }

        Long vendedorId = produto.getVendedor().getId();
        Long compradorId = comprador.getId();

        if (Objects.equals(vendedorId, compradorId)) {
            throw new EstadoInvalidoException("Você não pode iniciar conversa no próprio anúncio.");
        }

        return chatRepository.findByProdutoIdAndCompradorIdAndVendedorId(dto.produtoId(), compradorId, vendedorId)
                .map(chat -> {
                    chat.setOcultoParaComprador(false);
                    return chat.getId();
                })
                .orElseGet(() -> criarChat(produto, comprador, vendedorId));
    }

    private Long criarChat(Produto produto, Usuario comprador, Long vendedorId) {
        Usuario vendedor = usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vendedor não encontrado!"));

        try {
            Chat novoChat = new Chat(produto, comprador, vendedor);
            return chatRepository.save(novoChat).getId();
        } catch (DataIntegrityViolationException ex) {
            return chatRepository.findByProdutoIdAndCompradorIdAndVendedorId(
                            produto.getId(),
                            comprador.getId(),
                            vendedorId
                    )
                    .map(Chat::getId)
                    .orElseThrow(() -> ex);
        }
    }

    private Chat buscarChatPermitido(Long chatId, Usuario usuario) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chat não encontrado com o ID: " + chatId));

        if (!ehParticipante(chat, usuario.getId())) {
            throw new AccessDeniedException("Você não tem permissão para acessar este chat.");
        }

        return chat;
    }

    private Usuario buscarUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        return buscarUsuarioPorEmail(authentication.getName());
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));
    }

    private boolean ehParticipante(Chat chat, Long usuarioId) {
        return Objects.equals(chat.getComprador().getId(), usuarioId)
                || Objects.equals(chat.getVendedor().getId(), usuarioId);
    }

    private String normalizarMensagem(String mensagem) {
        String texto = mensagem == null ? "" : mensagem.trim();

        if (texto.isBlank()) {
            throw new EstadoInvalidoException("A mensagem não pode ser vazia.");
        }

        if (texto.length() > 1000) {
            throw new EstadoInvalidoException("A mensagem deve ter no máximo 1000 caracteres.");
        }

        return texto;
    }

    private ChatOutputDTO montarOutput(Mensagem mensagem) {
        return new ChatOutputDTO(
                mensagem.getId(),
                mensagem.getRemetente().getId(),
                mensagem.getRemetente().getNome(),
                mensagem.getTexto(),
                mensagem.getDataEnvio()
        );
    }

    private ChatResumoDTO montarResumo(Chat chat, Usuario usuarioLogado) {
        Mensagem ultimaMensagem = mensagemRepository.findTopByChatIdOrderByDataEnvioDesc(chat.getId())
                .orElse(null);
        Usuario outroUsuario = Objects.equals(chat.getComprador().getId(), usuarioLogado.getId())
                ? chat.getVendedor()
                : chat.getComprador();

        return new ChatResumoDTO(
                chat.getId(),
                usuarioLogado.getId(),
                chat.getProduto().getId(),
                chat.getProduto().getNome(),
                primeiraImagem(chat.getProduto()),
                chat.getProduto().getStatus().name(),
                outroUsuario.getId(),
                outroUsuario.getNome(),
                outroUsuario.getNick(),
                ultimaMensagem == null ? null : ultimaMensagem.getRemetente().getId(),
                ultimaMensagem == null ? null : ultimaMensagem.getTexto(),
                dataUltimaMensagem(ultimaMensagem)
        );
    }

    private LocalDateTime dataUltimaMensagem(Mensagem mensagem) {
        return mensagem == null ? null : mensagem.getDataEnvio();
    }

    private String primeiraImagem(Produto produto) {
        if (produto.getImagemUrls() != null && !produto.getImagemUrls().isEmpty()) {
            return produto.getImagemUrls().stream()
                    .filter(url -> url != null && !url.isBlank())
                    .findFirst()
                    .orElse(produto.getImagemUrl());
        }

        return produto.getImagemUrl();
    }

    @Transactional(readOnly = true)
    public boolean usuarioPodeAcessarChat(Long chatId, String emailUsuario) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        return chatRepository.existsByIdAndParticipanteId(chatId, usuario.getId());
    }

    @Transactional(readOnly = true)
    public boolean usuarioPodeAcessarChat(Long chatId, Long usuarioId) {
        return chatRepository.existsByIdAndParticipanteId(chatId, usuarioId);
    }

    @Transactional(readOnly = true)
    public Long buscarIdUsuarioPorEmail(String emailUsuario) {
        return buscarUsuarioPorEmail(emailUsuario).getId();
    }

}

package com.pedro.marketplace.service;

import com.pedro.marketplace.dto.ProdutoRequestDTO;
import com.pedro.marketplace.dto.ProdutoResponseDTO;
import com.pedro.marketplace.entity.Produto;
import com.pedro.marketplace.entity.StatusProduto;
import com.pedro.marketplace.entity.Usuario;
import com.pedro.marketplace.exception.EstadoInvalidoException;
import com.pedro.marketplace.exception.PermissaoNegadaException;
import com.pedro.marketplace.exception.RecursoNaoEncontradoException;
import com.pedro.marketplace.repository.ChatRepository;
import com.pedro.marketplace.repository.MensagemRepository;
import com.pedro.marketplace.repository.ProdutoRepository;
import com.pedro.marketplace.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ChatRepository chatRepository;
    private final MensagemRepository mensagemRepository;
    private final AdminService adminService;
    private final TokenService tokenService;

    public ProdutoService(ProdutoRepository produtoRepository,
                          UsuarioRepository usuarioRepository,
                          ChatRepository chatRepository,
                          MensagemRepository mensagemRepository,
                          AdminService adminService,
                          TokenService tokenService) {
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.chatRepository = chatRepository;
        this.mensagemRepository = mensagemRepository;
        this.adminService = adminService;
        this.tokenService = tokenService;
    }

    @Transactional
    public Produto criarProduto(ProdutoRequestDTO dto) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Usuario vendedor = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vendedor não encontrado!"));

        Produto produto = new Produto();
        produto.setNome(dto.nome());
        produto.setPreco(dto.preco());
        produto.setDescricao(dto.descricao());
        produto.setVendedor(vendedor);
        produto.setStatus(StatusProduto.DISPONIVEL);

        List<String> imagens = normalizarImagemUrls(dto);
        produto.setImagemUrls(imagens);
        produto.setImagemUrl(imagens.isEmpty() ? null : imagens.get(0));

        return produtoRepository.save(produto);
    }

    public List<ProdutoResponseDTO> listarDisponiveis() {
        return produtoRepository.findByStatus(StatusProduto.DISPONIVEL)
                .stream()
                .map(ProdutoResponseDTO::new)
                .toList();
    }

    public List<ProdutoResponseDTO> listarProdutosDoVendedor(Long vendedorId) {
        buscarVendedor(vendedorId);

        return produtoRepository.findByVendedorId(vendedorId)
                .stream()
                .map(ProdutoResponseDTO::new)
                .toList();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado!"));
    }

    @Transactional
    public Produto atualizar(Long id, ProdutoRequestDTO dto) {
        Produto produto = buscarPorId(id);
        validarDonoDoProduto(produto);

        produto.setNome(dto.nome());
        produto.setPreco(dto.preco());
        produto.setDescricao(dto.descricao());

        List<String> imagens = normalizarImagemUrls(dto);
        produto.setImagemUrls(imagens);
        produto.setImagemUrl(imagens.isEmpty() ? null : imagens.get(0));

        return produtoRepository.save(produto);
    }

    @Transactional
    public ProdutoResponseDTO venderProduto(Long id) {
        Produto produto = buscarPorId(id);
        validarDonoDoProduto(produto);

        if (produto.getStatus() == StatusProduto.VENDIDO) {
            throw new EstadoInvalidoException("Este produto já foi vendido!");
        }

        produto.setStatus(StatusProduto.VENDIDO);
        return new ProdutoResponseDTO(produto);
    }

    @Transactional
    public void excluir(Long id) {
        excluir(id, null, null);
    }

    @Transactional
    public void excluir(Long id, Authentication authentication, String authorizationHeader) {
        Produto produto = buscarPorId(id);
        validarDonoOuAdmin(produto, authentication, authorizationHeader);
        if (!produtoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Produto não encontrado!");
        }

        excluirConversasDoProduto(id);
        produtoRepository.delete(produto);
    }

    private void excluirConversasDoProduto(Long produtoId) {
        List<Long> chatIds = chatRepository.findIdsByProdutoId(produtoId);
        if (chatIds.isEmpty()) {
            return;
        }

        mensagemRepository.deleteByChatIds(chatIds);
        chatRepository.deleteByProdutoId(produtoId);
    }

    public List<ProdutoResponseDTO> listarProdutosDoVendedorPorEmail(String email) {
        Usuario vendedor = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vendedor não encontrado!"));

        return produtoRepository.findByVendedorId(vendedor.getId())
                .stream()
                .map(ProdutoResponseDTO::new)
                .toList();
    }

    private Usuario buscarVendedor(Long vendedorId) {
        return usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vendedor não encontrado!"));
    }

    private List<String> normalizarImagemUrls(ProdutoRequestDTO dto) {
        List<String> imagens = new ArrayList<>();

        if (dto.imagemUrls() != null) {
            imagens.addAll(dto.imagemUrls().stream()
                    .filter(url -> url != null && !url.isBlank())
                    .toList());
        }

        if (imagens.isEmpty() && dto.imagemUrl() != null && !dto.imagemUrl().isBlank()) {
            imagens.add(dto.imagemUrl());
        }

        return imagens;
    }

    private String emailUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new PermissaoNegadaException("Usuário não autenticado.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Usuario usuario) {
            return usuario.getEmail();
        }

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return authentication.getName();
    }

    private void validarDonoDoProduto(Produto produto) {
        String email = emailUsuarioAutenticado();
        if (!produto.getVendedor().getEmail().equalsIgnoreCase(email)) {
            throw new PermissaoNegadaException("Você só pode alterar seus próprios anúncios.");
        }
    }

    private void validarDonoOuAdmin(Produto produto) {
        validarDonoOuAdmin(produto, null, null);
    }

    private void validarDonoOuAdmin(Produto produto, Authentication authentication, String authorizationHeader) {
        String email = emailUsuarioAutenticado(authentication, authorizationHeader);
        boolean dono = produto.getVendedor().getEmail().equalsIgnoreCase(email);

        if (!dono && !adminService.ehAdmin(email)) {
            throw new PermissaoNegadaException("Você não tem permissão para excluir este anúncio.");
        }
    }

    private String emailUsuarioAutenticado(Authentication authentication, String authorizationHeader) {
        String emailDaAutenticacao = emailDaAutenticacao(authentication);
        if (emailDaAutenticacao != null) {
            return emailDaAutenticacao;
        }

        String emailDoToken = emailDoToken(authorizationHeader);
        if (emailDoToken != null) {
            return emailDoToken;
        }

        return emailUsuarioAutenticado();
    }

    private String emailDaAutenticacao(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Usuario usuario) {
            return usuario.getEmail();
        }

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        String email = authentication.getName();
        return email == null || email.isBlank() ? null : email;
    }

    private String emailDoToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }

        String token = authorizationHeader.replace("Bearer ", "").trim();
        String email = tokenService.validarToken(token);
        return email == null || email.isBlank() ? null : email;
    }

}

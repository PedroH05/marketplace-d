package com.pedro.marketplace.controller;

import com.pedro.marketplace.dto.ProdutoRequestDTO;
import com.pedro.marketplace.dto.ProdutoResponseDTO;
import com.pedro.marketplace.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criarProduto(@Valid @RequestBody ProdutoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ProdutoResponseDTO(produtoService.criarProduto(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(new ProdutoResponseDTO(produtoService.buscarPorId(id)));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listarDisponiveis() {
        return ResponseEntity.ok(produtoService.listarDisponiveis());
    }

    @GetMapping("/vendedor/email/{email}")
    public ResponseEntity<List<ProdutoResponseDTO>> listarPorVendedorEmail(@PathVariable String email) {
        return ResponseEntity.ok(produtoService.listarProdutosDoVendedorPorEmail(email));
    }

    @GetMapping("/vendedor/{vendedorId}")
    public ResponseEntity<List<ProdutoResponseDTO>> listarPorVendedor(@PathVariable Long vendedorId) {
        return ResponseEntity.ok(produtoService.listarProdutosDoVendedor(vendedorId));
    }

    @PatchMapping("/{id}/vender")
    public ResponseEntity<ProdutoResponseDTO> venderProduto(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.venderProduto(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody ProdutoRequestDTO dto) {
        return ResponseEntity.ok(new ProdutoResponseDTO(produtoService.atualizar(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

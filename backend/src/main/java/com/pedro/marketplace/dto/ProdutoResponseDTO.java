package com.pedro.marketplace.dto;

import com.pedro.marketplace.entity.Produto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        BigDecimal preco,
        String descricao,
        String status,
        String imagemUrl,
        List<String> imagemUrls,
        VendedorDTO vendedor
) {
    public ProdutoResponseDTO(Produto produto) {
        this(
                produto.getId(),
                produto.getNome(),
                produto.getPreco(),
                produto.getDescricao(),
                produto.getStatus().name(),
                produto.getImagemUrl(),
                montarImagemUrls(produto),
                new VendedorDTO(produto.getVendedor().getId(), produto.getVendedor().getNome(), produto.getVendedor().getNick())
        );
    }

    private static List<String> montarImagemUrls(Produto produto) {
        List<String> urls = new ArrayList<>();

        if (produto.getImagemUrls() != null) {
            urls.addAll(produto.getImagemUrls().stream()
                    .filter(url -> url != null && !url.isBlank())
                    .toList());
        }

        if (urls.isEmpty() && produto.getImagemUrl() != null && !produto.getImagemUrl().isBlank()) {
            urls.add(produto.getImagemUrl());
        }

        return urls;
    }

    public record VendedorDTO(Long id, String nome, String nick) {}
}

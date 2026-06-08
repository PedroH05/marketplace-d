package com.pedro.marketplace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_produto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    private StatusProduto status;

    @ManyToOne
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @Column(name = "imagem_url")
    private String imagemUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "tb_produto_imagens",
            joinColumns = @JoinColumn(name = "produto_id")
    )
    @OrderColumn(name = "ordem")
    @Column(name = "imagem_url", columnDefinition = "TEXT")
    private List<String> imagemUrls = new ArrayList<>();
}

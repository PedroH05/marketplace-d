package com.pedro.marketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(
        name = "chats",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_produto_comprador_vendedor",
                columnNames = {"produto_id", "comprador_id", "vendedor_id"}
        )
)
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "comprador_id", nullable = false)
    private Usuario comprador;

    @ManyToOne
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @Column(name = "oculto_para_comprador", nullable = false, columnDefinition = "boolean default false")
    private boolean ocultoParaComprador = false;

    @Column(name = "oculto_para_vendedor", nullable = false, columnDefinition = "boolean default false")
    private boolean ocultoParaVendedor = false;

    public Chat() {}

    public Chat(Produto produto, Usuario comprador, Usuario vendedor) {
        this.produto = produto;
        this.comprador = comprador;
        this.vendedor = vendedor;
    }

}

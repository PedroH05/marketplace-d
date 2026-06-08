package com.pedro.marketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "mensagens")
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "remetente_id", nullable = false)
    private Usuario remetente;

    @Column(nullable = false, length = 1000)
    private String texto;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataEnvio;

    public Mensagem() {}

    public Mensagem(Chat chat, Usuario remetente, String texto) {
        this.chat = chat;
        this.remetente = remetente;
        this.texto = texto;
    }

}

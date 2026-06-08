package com.pedro.marketplace.dto;

import java.time.LocalDateTime;

public record ChatResumoDTO(
        Long id,
        Long usuarioLogadoId,
        Long produtoId,
        String produtoNome,
        String produtoImagemUrl,
        String produtoStatus,
        Long outroUsuarioId,
        String outroUsuarioNome,
        String outroUsuarioNick,
        Long ultimaMensagemRemetenteId,
        String ultimaMensagem,
        LocalDateTime dataUltimaMensagem
) {}

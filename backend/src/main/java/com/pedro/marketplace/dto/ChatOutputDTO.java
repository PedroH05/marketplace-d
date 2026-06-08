package com.pedro.marketplace.dto;

import java.time.LocalDateTime;

public record ChatOutputDTO(
        Long id,
        Long remetenteId,
        String remetenteNome,
        String mensagem,
        LocalDateTime dataEnvio
) {}

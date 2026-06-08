package com.pedro.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnviarMensagemDTO(
        @NotBlank
        @Size(max = 1000)
        String mensagem
) {}

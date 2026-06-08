package com.pedro.marketplace.dto;

import jakarta.validation.constraints.NotNull;

public record CriarChatDTO(
        @NotNull
        Long produtoId
) {}

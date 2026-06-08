package com.pedro.marketplace.dto;

import jakarta.validation.constraints.*;

public record UsuarioRequestDTO(

        @NotBlank(message = "O nome não pode ficar em branco")
        @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$", message = "O nome deve conter apenas letras")
        String nome,

        @NotBlank(message = "O nick não pode ficar em branco")
        String nick,

        @NotBlank(message = "O e-mail não pode ficar em branco")
        @Email(message = "Digite um e-mail válido (ex: usuario@email.com)")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String senha

) {}
package com.pedro.marketplace.dto;

import com.pedro.marketplace.entity.Usuario;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String nick,
        String email
) {
    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getNick(), usuario.getEmail());
    }
}
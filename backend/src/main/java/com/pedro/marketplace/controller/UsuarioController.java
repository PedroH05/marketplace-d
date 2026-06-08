package com.pedro.marketplace.controller;

import com.pedro.marketplace.dto.UsuarioRequestDTO;
import com.pedro.marketplace.dto.UsuarioResponseDTO;
import com.pedro.marketplace.entity.Usuario;
import com.pedro.marketplace.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@Valid @RequestBody UsuarioRequestDTO dto) {
        Usuario salvo = usuarioService.criarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioResponseDTO(salvo));
    }
}
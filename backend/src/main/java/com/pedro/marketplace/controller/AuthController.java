package com.pedro.marketplace.controller;

import com.pedro.marketplace.dto.UsuarioLogadoDTO;
import com.pedro.marketplace.entity.Usuario;
import com.pedro.marketplace.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AdminService adminService;

    public AuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioLogadoDTO> usuarioLogado(Authentication authentication) {
        String email = emailDoUsuario(authentication);
        return ResponseEntity.ok(new UsuarioLogadoDTO(email, adminService.ehAdmin(email)));
    }

    private String emailDoUsuario(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuario nao autenticado.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Usuario usuario) {
            return usuario.getEmail();
        }

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return authentication.getName();
    }
}

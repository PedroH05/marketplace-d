package com.pedro.marketplace.controller;

import com.pedro.marketplace.dto.LoginRequestDTO;
import com.pedro.marketplace.dto.TokenResponseDTO;
import com.pedro.marketplace.entity.Usuario;
import com.pedro.marketplace.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class AutenticacaoController {

    private final AuthenticationManager manager;
    private final TokenService tokenService;

    public AutenticacaoController(AuthenticationManager manager, TokenService tokenService) {
        this.manager = manager;
        this.tokenService = tokenService;
    }

    @PostMapping
    public ResponseEntity<TokenResponseDTO> efetuarLogin(@RequestBody @Valid LoginRequestDTO dto) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());
        var authentication = manager.authenticate(authenticationToken);
        var tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());

        return ResponseEntity.ok(new TokenResponseDTO(tokenJWT));
    }
}

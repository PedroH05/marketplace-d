package com.pedro.marketplace.service;

import com.pedro.marketplace.dto.UsuarioRequestDTO;
import com.pedro.marketplace.entity.Usuario;
import com.pedro.marketplace.exception.RegistroDuplicadoException;
import com.pedro.marketplace.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario criarUsuario(UsuarioRequestDTO dto) {
        verificarDuplicidade(dto);
        return usuarioRepository.save(mapear(dto));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado!"));
    }

    private void verificarDuplicidade(UsuarioRequestDTO dto) {
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new RegistroDuplicadoException("Este e-mail já está em uso!");
        }
    }

    private Usuario mapear(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setNick(dto.nick());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        return usuario;
    }
}

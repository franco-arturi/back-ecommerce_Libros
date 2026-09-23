package com.uade.e_commerce_ju.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.uade.e_commerce_ju.repository.UsuarioRepository;

/**
 * Le indica a Spring Security de donde sacar los usuarios: de nuestra base, buscando por email.
 * Lo usan el AuthenticationManager (login) y el JwtAuthenticationFilter (cada request).
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String emailNormalizado = email == null ? "" : email.trim().toLowerCase();
        return usuarioRepository.findByEmail(emailNormalizado)
            .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con ese email"));
    }
}

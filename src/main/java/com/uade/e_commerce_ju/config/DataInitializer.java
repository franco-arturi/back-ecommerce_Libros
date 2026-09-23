package com.uade.e_commerce_ju.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uade.e_commerce_ju.model.Role;
import com.uade.e_commerce_ju.model.Usuario;
import com.uade.e_commerce_ju.repository.UsuarioRepository;

/**
 * Tareas que corren una vez al arrancar la aplicacion:
 *  1. Encripta con BCrypt las passwords que hayan quedado en texto plano
 *     (datos de data-demo.sql o usuarios creados antes de agregar seguridad).
 *  2. Crea el usuario administrador si todavia no existe, para poder probar
 *     los endpoints restringidos al rol ADMIN.
 */
@Configuration
public class DataInitializer {

    private static final String PREFIJO_BCRYPT = "$2";

    @Bean
    public CommandLineRunner inicializarUsuarios(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.admin.email}") String adminEmail,
        @Value("${app.admin.password}") String adminPassword
    ) {
        return args -> {
            usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getPassword() != null
                    && !usuario.getPassword().startsWith(PREFIJO_BCRYPT))
                .forEach(usuario -> {
                    usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
                    usuarioRepository.save(usuario);
                });

            String email = adminEmail.trim().toLowerCase();
            if (!usuarioRepository.existsByEmail(email) && !usuarioRepository.existsByUsername("admin")) {
                usuarioRepository.save(Usuario.builder()
                    .username("admin")
                    .email(email)
                    .password(passwordEncoder.encode(adminPassword))
                    .nombre("Admin")
                    .apellido("Sistema")
                    .role(Role.ADMIN)
                    .build());
            }
        };
    }
}

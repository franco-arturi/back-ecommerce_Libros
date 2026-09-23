package com.uade.e_commerce_ju.dto.usuario;

import com.uade.e_commerce_ju.model.Usuario;

/**
 * Datos publicos de un usuario. No incluye la password.
 */
public record UsuarioResponseDTO(
    Long id,
    String username,
    String email,
    String nombre,
    String apellido,
    String rol
) {

    public static UsuarioResponseDTO desde(Usuario usuario) {
        return new UsuarioResponseDTO(
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.obtenerRol().name()
        );
    }
}

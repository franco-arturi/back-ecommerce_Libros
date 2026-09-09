package com.uade.e_commerce_ju.dto.usuario;

public record UsuarioResponseDTO(
    Long id,
    String username,
    String email,
    String nombre,
    String apellido
) {
}

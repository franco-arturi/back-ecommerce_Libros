package com.uade.e_commerce_ju.dto.usuario;

public record UsuarioRegistroDTO(
    String username,
    String email,
    String password,
    String nombre,
    String apellido
) {
}

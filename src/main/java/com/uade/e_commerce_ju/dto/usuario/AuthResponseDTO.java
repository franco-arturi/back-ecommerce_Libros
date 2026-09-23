package com.uade.e_commerce_ju.dto.usuario;

public record AuthResponseDTO(
    String token,
    UsuarioResponseDTO usuario
) {
}

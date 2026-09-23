package com.uade.e_commerce_ju.dto.auth;

import com.uade.e_commerce_ju.dto.usuario.UsuarioResponseDTO;

/**
 * Respuesta del login: el JWT que el cliente debe enviar en el header
 * "Authorization: Bearer {token}" y los datos del usuario autenticado.
 */
public record AuthResponseDTO(
    String token,
    String tipo,
    long expiraEnSegundos,
    UsuarioResponseDTO usuario
) {
}

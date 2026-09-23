package com.uade.e_commerce_ju.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce_ju.dto.auth.AuthResponseDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioLoginDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioRegistroDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioResponseDTO;
import com.uade.e_commerce_ju.service.AuthService;

/**
 * Endpoints publicos de autenticacion.
 *  POST /api/auth/register -> 201 Created con los datos del usuario (sin password).
 *  POST /api/auth/login    -> 200 OK con el JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> registrar(@RequestBody UsuarioRegistroDTO request) {
        UsuarioResponseDTO usuario = authService.registrar(request);
        return ResponseEntity
            .created(URI.create("/api/usuarios/" + usuario.id()))
            .body(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody UsuarioLoginDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

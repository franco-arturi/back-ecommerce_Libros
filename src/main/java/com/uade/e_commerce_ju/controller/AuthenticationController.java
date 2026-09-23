package com.uade.e_commerce_ju.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce_ju.dto.usuario.AuthResponseDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioLoginDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioRegistroDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioResponseDTO;
import com.uade.e_commerce_ju.service.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(@RequestBody UsuarioRegistroDTO request) {
        UsuarioResponseDTO usuario = authenticationService.register(request);
        return ResponseEntity
            .created(URI.create("/api/usuarios/" + usuario.id()))
            .body(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody UsuarioLoginDTO request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }
}

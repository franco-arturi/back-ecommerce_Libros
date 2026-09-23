package com.uade.e_commerce_ju.service;

import java.util.regex.Pattern;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce_ju.dto.usuario.AuthResponseDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioLoginDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioRegistroDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioResponseDTO;
import com.uade.e_commerce_ju.exception.ArgumentoInvalidoException;
import com.uade.e_commerce_ju.exception.CredencialesInvalidasException;
import com.uade.e_commerce_ju.exception.RecursoDuplicadoException;
import com.uade.e_commerce_ju.model.Role;
import com.uade.e_commerce_ju.model.Usuario;
import com.uade.e_commerce_ju.repository.UsuarioRepository;
import com.uade.e_commerce_ju.security.JwtService;

@Service
@Transactional
public class AuthenticationService {

    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String CREDENCIALES_INVALIDAS = "Email o password incorrectos";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public UsuarioResponseDTO register(UsuarioRegistroDTO request) {
        if (request == null) {
            throw new ArgumentoInvalidoException("El cuerpo de la solicitud es obligatorio");
        }

        String username = obligatorio(request.username(), "username", 50);
        String email = normalizarEmail(obligatorio(request.email(), "email", 120));
        String password = obligatorio(request.password(), "password", 100);
        String nombre = obligatorio(request.nombre(), "nombre", 60);
        String apellido = obligatorio(request.apellido(), "apellido", 60);

        if (!FORMATO_EMAIL.matcher(email).matches()) {
            throw new ArgumentoInvalidoException("El email no tiene un formato valido");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el email " + email);
        }
        if (usuarioRepository.existsByUsername(username)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el username " + username);
        }

        Usuario usuario = Usuario.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .nombre(nombre)
            .apellido(apellido)
            .role(Role.USER)
            .build();

        return UsuarioResponseDTO.from(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(UsuarioLoginDTO request) {
        if (request == null || esVacio(request.email()) || esVacio(request.password())) {
            throw new CredencialesInvalidasException(CREDENCIALES_INVALIDAS);
        }

        String email = normalizarEmail(request.email());

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
            );
        } catch (AuthenticationException ex) {
            throw new CredencialesInvalidasException(CREDENCIALES_INVALIDAS);
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new CredencialesInvalidasException(CREDENCIALES_INVALIDAS));

        String token = jwtService.generateToken(usuario);
        return new AuthResponseDTO(token, UsuarioResponseDTO.from(usuario));
    }

    private String obligatorio(String valor, String campo, int maximo) {
        if (esVacio(valor)) {
            throw new ArgumentoInvalidoException("El campo " + campo + " es obligatorio");
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new ArgumentoInvalidoException(
                "El campo " + campo + " no puede superar los " + maximo + " caracteres"
            );
        }
        return limpio;
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}

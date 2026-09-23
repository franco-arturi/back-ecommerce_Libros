package com.uade.e_commerce_ju.service;

import java.util.regex.Pattern;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce_ju.dto.auth.AuthResponseDTO;
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

/**
 * Logica de registro y login.
 *  - Registro: valida los datos, verifica duplicados, encripta la password con BCrypt
 *    y guarda el usuario con rol USER.
 *  - Login: delega la validacion de credenciales al AuthenticationManager de Spring Security
 *    y, si son correctas, genera un JWT.
 */
@Service
public class AuthService {

    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String CREDENCIALES_INVALIDAS = "Email o password incorrectos";
    private static final int PASSWORD_MINIMO = 6;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
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

    @Transactional
    public UsuarioResponseDTO registrar(UsuarioRegistroDTO request) {
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
        if (password.length() < PASSWORD_MINIMO) {
            throw new ArgumentoInvalidoException(
                "La password debe tener al menos " + PASSWORD_MINIMO + " caracteres"
            );
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el email " + email);
        }
        if (usuarioRepository.existsByUsername(username)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el username " + username);
        }

        // El rol no viene del request: nadie puede auto-registrarse como ADMIN.
        Usuario usuario = Usuario.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .nombre(nombre)
            .apellido(apellido)
            .role(Role.USER)
            .build();

        return UsuarioResponseDTO.desde(usuarioRepository.save(usuario));
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
        } catch (AuthenticationException e) {
            // Mismo mensaje para email inexistente o password incorrecta:
            // no le damos pistas a quien intenta adivinar cuentas.
            throw new CredencialesInvalidasException(CREDENCIALES_INVALIDAS);
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new CredencialesInvalidasException(CREDENCIALES_INVALIDAS));

        return new AuthResponseDTO(
            jwtService.generarToken(usuario),
            "Bearer",
            jwtService.getExpiracionSegundos(),
            UsuarioResponseDTO.desde(usuario)
        );
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

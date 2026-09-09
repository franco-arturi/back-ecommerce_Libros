package com.uade.e_commerce_ju.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce_ju.dto.usuario.UsuarioLoginDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioRegistroDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioResponseDTO;
import com.uade.e_commerce_ju.exception.ArgumentoInvalidoException;
import com.uade.e_commerce_ju.exception.CredencialesInvalidasException;
import com.uade.e_commerce_ju.exception.RecursoDuplicadoException;
import com.uade.e_commerce_ju.exception.RecursoNoEncontradoException;
import com.uade.e_commerce_ju.model.Usuario;
import com.uade.e_commerce_ju.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService {
//estas valid en un futuro son del front
    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String CREDENCIALES_INVALIDAS = "Email o password incorrectos";
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

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
        if (usuarioRepository.existsByEmail(email)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el email " + email);
        }
        if (usuarioRepository.existsByUsername(username)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el username " + username);
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);

        return crearRespuesta(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO login(UsuarioLoginDTO request) {
        if (request == null || esVacio(request.email()) || esVacio(request.password())) {
            throw new CredencialesInvalidasException(CREDENCIALES_INVALIDAS);
        }

        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(request.email()))
            .orElseThrow(() -> new CredencialesInvalidasException(CREDENCIALES_INVALIDAS));

        if (!request.password().equals(usuario.getPassword())) {
            throw new CredencialesInvalidasException(CREDENCIALES_INVALIDAS);
        }

        return crearRespuesta(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
            .map(this::crearRespuesta)
            .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        if (id == null) {
            throw new RecursoNoEncontradoException("El usuario es obligatorio");
        }
        return usuarioRepository.findById(id)
            .map(this::crearRespuesta)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
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

    private UsuarioResponseDTO crearRespuesta(Usuario usuario) {
        return new UsuarioResponseDTO(
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getNombre(),
            usuario.getApellido()
        );
    }
}

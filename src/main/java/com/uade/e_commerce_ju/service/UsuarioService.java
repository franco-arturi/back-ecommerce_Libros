package com.uade.e_commerce_ju.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce_ju.dto.usuario.UsuarioResponseDTO;
import com.uade.e_commerce_ju.exception.RecursoNoEncontradoException;
import com.uade.e_commerce_ju.repository.UsuarioRepository;

/**
 * Consultas de usuarios. El registro y el login se movieron a AuthService.
 */
@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
            .map(UsuarioResponseDTO::desde)
            .toList();
    }

    public UsuarioResponseDTO obtenerPorId(Long id) {
        if (id == null) {
            throw new RecursoNoEncontradoException("El usuario es obligatorio");
        }
        return usuarioRepository.findById(id)
            .map(UsuarioResponseDTO::desde)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
    }
}

package com.uade.e_commerce_ju.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.e_commerce_ju.dto.usuario.UsuarioResponseDTO;
import com.uade.e_commerce_ju.exception.RecursoNoEncontradoException;
import com.uade.e_commerce_ju.model.Role;
import com.uade.e_commerce_ju.model.Usuario;
import com.uade.e_commerce_ju.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository);
    }

    @Test
    void obtenerPorIdFallaSiNoExiste() {
        when(usuarioRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.obtenerPorId(5L))
            .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void obtenerPorIdFallaSiElIdEsNulo() {
        assertThatThrownBy(() -> usuarioService.obtenerPorId(null))
            .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void obtenerPorIdDevuelveElUsuarioMapeado() {
        Usuario usuario = Usuario.builder()
            .id(1L).username("juanp").email("juan@test.com")
            .password("HASH").nombre("Juan").apellido("Perez").role(Role.USER)
            .build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioResponseDTO response = usuarioService.obtenerPorId(1L);

        assertThat(response.email()).isEqualTo("juan@test.com");
        assertThat(response.rol()).isEqualTo(Role.USER.name());
    }
}

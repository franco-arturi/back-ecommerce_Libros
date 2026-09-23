package com.uade.e_commerce_ju.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.e_commerce_ju.dto.categoria.CategoriaCreateDTO;
import com.uade.e_commerce_ju.dto.categoria.CategoriaResponseDTO;
import com.uade.e_commerce_ju.exception.ArgumentoInvalidoException;
import com.uade.e_commerce_ju.exception.RecursoDuplicadoException;
import com.uade.e_commerce_ju.exception.RecursoEnUsoException;
import com.uade.e_commerce_ju.exception.RecursoNoEncontradoException;
import com.uade.e_commerce_ju.model.Categoria;
import com.uade.e_commerce_ju.repository.CategoriaRepository;
import com.uade.e_commerce_ju.repository.LibroRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private LibroRepository libroRepository;

    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaService(categoriaRepository, libroRepository);
    }

    @Test
    void crearGuardaCategoriaCuandoElNombreNoEstaRepetido() {
        CategoriaCreateDTO request = new CategoriaCreateDTO("Ficcion", "Libros de ficcion");
        when(categoriaRepository.existsByNombreIgnoreCase("Ficcion")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria categoria = invocation.getArgument(0);
            categoria.setId(10L);
            return categoria;
        });

        CategoriaResponseDTO response = categoriaService.crear(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.nombre()).isEqualTo("Ficcion");
    }

    @Test
    void crearFallaSiElNombreYaExiste() {
        CategoriaCreateDTO request = new CategoriaCreateDTO("Ficcion", null);
        when(categoriaRepository.existsByNombreIgnoreCase("Ficcion")).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.crear(request))
            .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    void crearFallaSiElNombreEsVacio() {
        CategoriaCreateDTO request = new CategoriaCreateDTO("   ", null);

        assertThatThrownBy(() -> categoriaService.crear(request))
            .isInstanceOf(ArgumentoInvalidoException.class);
    }

    @Test
    void obtenerPorIdFallaSiNoExiste() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.obtenerPorId(99L))
            .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminarFallaSiTieneLibrosAsociados() {
        Categoria categoria = new Categoria(1L, "Ficcion", null);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(libroRepository.existsByCategoriaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.eliminar(1L))
            .isInstanceOf(RecursoEnUsoException.class);

        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void eliminarBorraCategoriaSinLibrosAsociados() {
        Categoria categoria = new Categoria(1L, "Ficcion", null);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(libroRepository.existsByCategoriaId(1L)).thenReturn(false);

        categoriaService.eliminar(1L);

        verify(categoriaRepository).delete(categoria);
    }
}

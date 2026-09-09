package com.uade.e_commerce_ju.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.e_commerce_ju.dto.libro.AgregarImagenDTO;
import com.uade.e_commerce_ju.dto.libro.ImagenLibroDTO;
import com.uade.e_commerce_ju.exception.ArgumentoInvalidoException;
import com.uade.e_commerce_ju.exception.RecursoDuplicadoException;
import com.uade.e_commerce_ju.exception.RecursoNoEncontradoException;
import com.uade.e_commerce_ju.model.ImagenLibro;
import com.uade.e_commerce_ju.model.Libro;
import com.uade.e_commerce_ju.repository.ImagenLibroRepository;
import com.uade.e_commerce_ju.repository.LibroRepository;

@ExtendWith(MockitoExtension.class)
class ImagenLibroServiceTests {

    private static final String URL_VALIDA = "https://images.demo.com/portada.jpg";

    @Mock
    private ImagenLibroRepository imagenLibroRepository;

    @Mock
    private LibroRepository libroRepository;

    private ImagenLibroService imagenLibroService;

    @BeforeEach
    void setUp() {
        imagenLibroService = new ImagenLibroService(imagenLibroRepository, libroRepository);
    }

    @Test
    void agregaUnaImagenAlLibro() {
        Libro libro = libro(1L);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(imagenLibroRepository.existsByLibroIdAndUrl(1L, URL_VALIDA)).thenReturn(false);
        when(imagenLibroRepository.save(any(ImagenLibro.class)))
            .thenReturn(imagen(7L, libro, URL_VALIDA));

        ImagenLibroDTO response = imagenLibroService.agregar(1L, new AgregarImagenDTO(URL_VALIDA));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.url()).isEqualTo(URL_VALIDA);

        ArgumentCaptor<ImagenLibro> capturada = ArgumentCaptor.forClass(ImagenLibro.class);
        verify(imagenLibroRepository).save(capturada.capture());
        assertThat(capturada.getValue().getLibro()).isEqualTo(libro);
        assertThat(capturada.getValue().getUrl()).isEqualTo(URL_VALIDA);
    }

    @Test
    void guardaLaUrlSinEspaciosSobrantes() {
        Libro libro = libro(1L);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(imagenLibroRepository.existsByLibroIdAndUrl(1L, URL_VALIDA)).thenReturn(false);
        when(imagenLibroRepository.save(any(ImagenLibro.class)))
            .thenReturn(imagen(7L, libro, URL_VALIDA));

        imagenLibroService.agregar(1L, new AgregarImagenDTO("   " + URL_VALIDA + "   "));

        ArgumentCaptor<ImagenLibro> capturada = ArgumentCaptor.forClass(ImagenLibro.class);
        verify(imagenLibroRepository).save(capturada.capture());
        assertThat(capturada.getValue().getUrl()).isEqualTo(URL_VALIDA);
    }

    @Test
    void rechazaAgregarImagenAUnLibroInexistente() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> imagenLibroService.agregar(99L, new AgregarImagenDTO(URL_VALIDA)))
            .isInstanceOf(RecursoNoEncontradoException.class);

        verify(imagenLibroRepository, never()).save(any(ImagenLibro.class));
    }

    @Test
    void rechazaUnaUrlVacia() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro(1L)));

        assertThatThrownBy(() -> imagenLibroService.agregar(1L, new AgregarImagenDTO("   ")))
            .isInstanceOf(ArgumentoInvalidoException.class);

        verify(imagenLibroRepository, never()).save(any(ImagenLibro.class));
    }

    @Test
    void rechazaUnaUrlSinDominio() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro(1L)));

        assertThatThrownBy(() -> imagenLibroService.agregar(1L, new AgregarImagenDTO("portada.jpg")))
            .isInstanceOf(ArgumentoInvalidoException.class);

        verify(imagenLibroRepository, never()).save(any(ImagenLibro.class));
    }

    @Test
    void rechazaUnaUrlQueNoEsHttp() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro(1L)));

        assertThatThrownBy(() ->
            imagenLibroService.agregar(1L, new AgregarImagenDTO("ftp://images.demo.com/portada.jpg"))
        ).isInstanceOf(ArgumentoInvalidoException.class);

        verify(imagenLibroRepository, never()).save(any(ImagenLibro.class));
    }

    @Test
    void rechazaUnaImagenRepetidaEnElMismoLibro() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro(1L)));
        when(imagenLibroRepository.existsByLibroIdAndUrl(1L, URL_VALIDA)).thenReturn(true);

        assertThatThrownBy(() -> imagenLibroService.agregar(1L, new AgregarImagenDTO(URL_VALIDA)))
            .isInstanceOf(RecursoDuplicadoException.class);

        verify(imagenLibroRepository, never()).save(any(ImagenLibro.class));
    }

    @Test
    void listaLasImagenesDelLibro() {
        Libro libro = libro(1L);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(imagenLibroRepository.findByLibroIdOrderByIdAsc(1L)).thenReturn(List.of(
            imagen(1L, libro, URL_VALIDA),
            imagen(2L, libro, "https://images.demo.com/contratapa.jpg")
        ));

        List<ImagenLibroDTO> imagenes = imagenLibroService.listar(1L);

        assertThat(imagenes).extracting(ImagenLibroDTO::id).containsExactly(1L, 2L);
        assertThat(imagenes).extracting(ImagenLibroDTO::url)
            .containsExactly(URL_VALIDA, "https://images.demo.com/contratapa.jpg");
    }

    @Test
    void eliminaUnaImagenDelLibro() {
        Libro libro = libro(1L);
        ImagenLibro imagen = imagen(5L, libro, URL_VALIDA);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(imagenLibroRepository.findByIdAndLibroId(5L, 1L)).thenReturn(Optional.of(imagen));

        imagenLibroService.eliminar(1L, 5L);

        verify(imagenLibroRepository).delete(imagen);
    }

    @Test
    void noEliminaUnaImagenQueEsDeOtroLibro() {
        when(libroRepository.findById(2L)).thenReturn(Optional.of(libro(2L)));
        when(imagenLibroRepository.findByIdAndLibroId(5L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> imagenLibroService.eliminar(2L, 5L))
            .isInstanceOf(RecursoNoEncontradoException.class);

        verify(imagenLibroRepository, never()).delete(any(ImagenLibro.class));
    }

    private Libro libro(Long id) {
        Libro libro = new Libro();
        libro.setId(id);
        libro.setTitulo("Clean Code");
        return libro;
    }

    private ImagenLibro imagen(Long id, Libro libro, String url) {
        ImagenLibro imagen = new ImagenLibro();
        imagen.setId(id);
        imagen.setLibro(libro);
        imagen.setUrl(url);
        return imagen;
    }
}

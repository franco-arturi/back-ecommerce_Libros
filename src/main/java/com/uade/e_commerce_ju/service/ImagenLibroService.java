package com.uade.e_commerce_ju.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce_ju.dto.libro.AgregarImagenDTO;
import com.uade.e_commerce_ju.dto.libro.ImagenLibroDTO;
import com.uade.e_commerce_ju.exception.ArgumentoInvalidoException;
import com.uade.e_commerce_ju.exception.RecursoDuplicadoException;
import com.uade.e_commerce_ju.exception.RecursoNoEncontradoException;
import com.uade.e_commerce_ju.model.ImagenLibro;
import com.uade.e_commerce_ju.model.Libro;
import com.uade.e_commerce_ju.repository.ImagenLibroRepository;
import com.uade.e_commerce_ju.repository.LibroRepository;

@Service
@Transactional
public class ImagenLibroService {

    private static final int LARGO_MAXIMO_URL = 500;

    private final ImagenLibroRepository imagenLibroRepository;
    private final LibroRepository libroRepository;

    public ImagenLibroService(
        ImagenLibroRepository imagenLibroRepository,
        LibroRepository libroRepository
    ) {
        this.imagenLibroRepository = imagenLibroRepository;
        this.libroRepository = libroRepository;
    }

    //listar las imagenes de un libro, ordenadas por id
    @Transactional(readOnly = true)
    public List<ImagenLibroDTO> listar(Long libroId) {
        buscarLibro(libroId);
        return imagenLibroRepository.findByLibroIdOrderByIdAsc(libroId).stream()
            .map(this::crearRespuesta)
            .toList();
    }

    //agregar una imagen al libro validando que el libro exista y que la url sea valida
    public ImagenLibroDTO agregar(Long libroId, AgregarImagenDTO request) {
        if (request == null) {
            throw new ArgumentoInvalidoException("El cuerpo de la solicitud es obligatorio");
        }

        Libro libro = buscarLibro(libroId);
        String url = validarUrl(request.url());

        if (imagenLibroRepository.existsByLibroIdAndUrl(libroId, url)) {
            throw new RecursoDuplicadoException("El libro ya tiene una imagen con esa url");
        }

        ImagenLibro imagen = new ImagenLibro();
        imagen.setLibro(libro);
        imagen.setUrl(url);

        return crearRespuesta(imagenLibroRepository.save(imagen));
    }

    //eliminar una imagen verificando que pertenezca al libro indicado
    public void eliminar(Long libroId, Long imagenId) {
        buscarLibro(libroId);

        if (imagenId == null) {
            throw new RecursoNoEncontradoException("La imagen es obligatoria");
        }

        ImagenLibro imagen = imagenLibroRepository.findByIdAndLibroId(imagenId, libroId)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No existe la imagen con id " + imagenId + " en el libro " + libroId
            ));

        imagenLibroRepository.delete(imagen);
    }

    private Libro buscarLibro(Long libroId) {
        if (libroId == null) {
            throw new RecursoNoEncontradoException("El libro es obligatorio");
        }
        return libroRepository.findById(libroId)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el libro con id " + libroId));
    }

    //la url tiene que ser absoluta, http o https, y con host
    private String validarUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new ArgumentoInvalidoException("El campo url es obligatorio");
        }

        String limpio = url.trim();
        if (limpio.length() > LARGO_MAXIMO_URL) {
            throw new ArgumentoInvalidoException(
                "El campo url no puede superar los " + LARGO_MAXIMO_URL + " caracteres"
            );
        }

        URI uri;
        try {
            uri = new URI(limpio);
        } catch (URISyntaxException excepcion) {
            throw new ArgumentoInvalidoException("La url de la imagen no tiene un formato valido");
        }

        if (!uri.isAbsolute() || uri.getHost() == null) {
            throw new ArgumentoInvalidoException("La url de la imagen debe ser absoluta e incluir el dominio");
        }

        String esquema = uri.getScheme().toLowerCase(Locale.ROOT);
        if (!esquema.equals("http") && !esquema.equals("https")) {
            throw new ArgumentoInvalidoException("La url de la imagen debe usar http o https");
        }

        return limpio;
    }

    private ImagenLibroDTO crearRespuesta(ImagenLibro imagen) {
        return new ImagenLibroDTO(imagen.getId(), imagen.getUrl());
    }
}

package com.uade.e_commerce_ju.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce_ju.model.ImagenLibro;

/**
 * Acceso a las imagenes de un libro. Las consultas filtran siempre por libro
 * para no devolver ni borrar imagenes de otro libro.
 */
public interface ImagenLibroRepository extends JpaRepository<ImagenLibro, Long> {

    List<ImagenLibro> findByLibroIdOrderByIdAsc(Long libroId);

    Optional<ImagenLibro> findByIdAndLibroId(Long id, Long libroId);

    boolean existsByLibroIdAndUrl(Long libroId, String url);

    // se usa al eliminar un libro: sus imagenes se borran antes que el
    void deleteByLibroId(Long libroId);
}

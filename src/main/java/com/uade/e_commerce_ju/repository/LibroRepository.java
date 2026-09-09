package com.uade.e_commerce_ju.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.uade.e_commerce_ju.model.Libro;
import java.util.List;

/**
 * JpaRepository Provides CRUD operations and additional query methods for the Categoria
 * save, update, delete, findById, findAll, etc. de la tabla Categorias
 * CategoriaRepository
 */

public interface LibroRepository extends JpaRepository<Libro, Long> {

    @Query("SELECT l FROM Libro l WHERE " +
           "(:categoriaId IS NULL OR l.categoria.id = :categoriaId) AND " +
           "(:titulo IS NULL OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) " +
           "ORDER BY l.titulo ASC")
    List<Libro> buscarConFiltrosYOrden(
        @Param("categoriaId") Long categoriaId,
        @Param("titulo") String titulo
    );

    boolean existsByCategoriaId(Long categoriaId);
}

package com.uade.e_commerce_ju.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce_ju.model.Categoria;

/**
 * JpaRepository Provides CRUD operations and additional query methods for the Categoria
 * save, update, delete, findById, findAll, etc. de la tabla Categorias
 * CategoriaRepository
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    
}

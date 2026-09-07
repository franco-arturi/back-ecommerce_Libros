package com.uade.e_commerce_ju.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce_ju.model.ItemCarrito;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    Optional<ItemCarrito> findByCarritoIdAndLibroId(Long carritoId, Long libroId);

    Optional<ItemCarrito> findByIdAndCarritoUsuarioId(Long id, Long usuarioId);
}

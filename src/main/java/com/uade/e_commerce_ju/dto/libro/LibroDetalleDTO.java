package com.uade.e_commerce_ju.dto.libro;

import java.util.List;

import com.uade.e_commerce_ju.dto.categoria.CategoriaResponseDTO;

public record LibroDetalleDTO(
    Long id,
    String titulo,
    String autor,
    double precio,
    Integer stock,
    String descripcion,
    CategoriaResponseDTO categoria,
    List<ImagenLibroDTO> imagenes,
    Long vendedorId
) {

}

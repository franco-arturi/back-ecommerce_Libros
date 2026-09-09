package com.uade.e_commerce_ju.dto.libro;

import com.uade.e_commerce_ju.dto.categoria.CategoriaResponseDTO;

public record LibroListadoDTO(
    Long id,
    String titulo,
    String autor,
    double precio,
    CategoriaResponseDTO categoria
) {
}

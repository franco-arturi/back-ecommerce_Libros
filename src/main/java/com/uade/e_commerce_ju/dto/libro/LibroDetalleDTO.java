package com.uade.e_commerce_ju.dto.libro;

import java.util.List;

public record LibroDetalleDTO(
    Long id,
    String titulo,
    String autor,
    double precio,
    Integer stock,
    String descripcion,
    String categoria,
    List<ImagenLibroDTO> imagenes
) {
    
}

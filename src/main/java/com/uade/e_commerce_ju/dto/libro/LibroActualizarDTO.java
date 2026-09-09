package com.uade.e_commerce_ju.dto.libro;

public record LibroActualizarDTO(
    Long vendedorId,
    String titulo,
    String autor,
    String descripcion,
    Double precio,
    String categoria,
    String imagenes
) {
}

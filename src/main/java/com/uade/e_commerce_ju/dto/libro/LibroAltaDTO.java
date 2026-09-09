package com.uade.e_commerce_ju.dto.libro;

public record LibroAltaDTO(
    Long vendedorId,
    String titulo,
    String autor,
    String descripcion,
    Double precio,
    Integer stock,
    Long categoriaId
) {
}

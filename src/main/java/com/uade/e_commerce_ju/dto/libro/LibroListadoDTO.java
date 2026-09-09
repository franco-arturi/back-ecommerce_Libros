package com.uade.e_commerce_ju.dto.libro;

public record LibroListadoDTO(
    Long id,
    String titulo,
    String autor,
    double precio,
    String categoria
){
    
}


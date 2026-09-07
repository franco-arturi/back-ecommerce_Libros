package com.uade.e_commerce_ju.dto.carrito;

import java.math.BigDecimal;

public record ItemCarritoDTO(
    Long id,
    Long libroId,
    String titulo,
    BigDecimal precioUnitario,
    Integer cantidad,
    BigDecimal subtotal
) {
}

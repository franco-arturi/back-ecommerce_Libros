package com.uade.e_commerce_ju.dto.carrito;

import java.math.BigDecimal;
import java.util.List;

public record CarritoResponseDTO(
    Long id,
    Long usuarioId,
    List<ItemCarritoDTO> items,
    BigDecimal total
) {
}

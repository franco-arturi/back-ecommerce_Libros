package com.uade.e_commerce_ju.dto.carrito;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckoutResponseDTO {
    private BigDecimal total;
    private String mensaje;
}

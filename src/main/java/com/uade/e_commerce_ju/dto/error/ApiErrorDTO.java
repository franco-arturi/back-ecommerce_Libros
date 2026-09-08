package com.uade.e_commerce_ju.dto.error;

import java.time.Instant;

public record ApiErrorDTO(
    Instant timestamp,
    int status,
    String error,
    String mensaje,
    String path
) {
}

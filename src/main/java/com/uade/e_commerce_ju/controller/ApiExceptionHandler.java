package com.uade.e_commerce_ju.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.uade.e_commerce_ju.dto.error.ApiErrorDTO;
import com.uade.e_commerce_ju.exception.CantidadInvalidaException;
import com.uade.e_commerce_ju.exception.RecursoNoEncontradoException;
import com.uade.e_commerce_ju.exception.StockInsuficienteException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiErrorDTO> manejarNoEncontrado(
        RecursoNoEncontradoException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(CantidadInvalidaException.class)
    public ResponseEntity<ApiErrorDTO> manejarCantidadInvalida(
        CantidadInvalidaException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ApiErrorDTO> manejarStockInsuficiente(
        StockInsuficienteException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorDTO> manejarJsonInvalido(
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es invalido", request);
    }

    private ResponseEntity<ApiErrorDTO> crearRespuesta(
        HttpStatus status,
        String mensaje,
        HttpServletRequest request
    ) {
        ApiErrorDTO error = new ApiErrorDTO(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            mensaje,
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }
}

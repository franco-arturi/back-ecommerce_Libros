package com.uade.e_commerce_ju.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uade.e_commerce_ju.dto.error.ApiErrorDTO;
import com.uade.e_commerce_ju.exception.ArgumentoInvalidoException;
import com.uade.e_commerce_ju.exception.CantidadInvalidaException;
import com.uade.e_commerce_ju.exception.CredencialesInvalidasException;
import com.uade.e_commerce_ju.exception.OperacionNoAutorizadaException;
import com.uade.e_commerce_ju.exception.RecursoDuplicadoException;
import com.uade.e_commerce_ju.exception.RecursoEnUsoException;
import com.uade.e_commerce_ju.exception.RecursoNoEncontradoException;
import com.uade.e_commerce_ju.exception.StockInsuficienteException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiErrorDTO> manejarNoEncontrado(
        RecursoNoEncontradoException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ApiErrorDTO> manejarRecursoDuplicado(
        RecursoDuplicadoException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(RecursoEnUsoException.class)
    public ResponseEntity<ApiErrorDTO> manejarRecursoEnUso(
        RecursoEnUsoException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.CONFLICT, exception.getMessage(), request);
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

    @ExceptionHandler(ArgumentoInvalidoException.class)
    public ResponseEntity<ApiErrorDTO> manejarArgumentoInvalido(
        ArgumentoInvalidoException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiErrorDTO> manejarCredencialesInvalidas(
        CredencialesInvalidasException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
    }

    @ExceptionHandler(OperacionNoAutorizadaException.class)
    public ResponseEntity<ApiErrorDTO> manejarOperacionNoAutorizada(
        OperacionNoAutorizadaException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.FORBIDDEN, exception.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorDTO> manejarAutenticacion(
        AuthenticationException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.UNAUTHORIZED, "Email o password incorrectos", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDTO> manejarAccesoDenegado(
        AccessDeniedException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operacion", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorDTO> manejarJsonInvalido(
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es invalido", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorDTO> manejarMetodoNoSoportado(
        HttpRequestMethodNotSupportedException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(
            HttpStatus.METHOD_NOT_ALLOWED,
            "El metodo " + exception.getMethod() + " no esta soportado para este recurso",
            request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorDTO> manejarContentTypeNoSoportado(
        HttpMediaTypeNotSupportedException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "El Content-Type debe ser application/json",
            request
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorDTO> manejarRecursoInexistente(
        NoHandlerFoundException exception,
        HttpServletRequest request
    ) {
        return crearRespuesta(HttpStatus.NOT_FOUND, "El recurso solicitado no existe", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> manejarValidacionDeBody(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        String mensaje = exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .orElse("El cuerpo de la solicitud contiene datos invalidos");
        return crearRespuesta(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> manejarErrorInesperado(
        Exception exception,
        HttpServletRequest request
    ) {
        log.error("Error inesperado en {}", request.getRequestURI(), exception);
        return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request);
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

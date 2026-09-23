package com.uade.e_commerce_ju.security;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Los errores de seguridad ocurren en la cadena de filtros, antes de llegar a los controllers,
 * por eso no los atrapa ApiExceptionHandler. Esta clase devuelve el mismo formato JSON
 * (timestamp, status, error, mensaje, path) para que el front reciba errores uniformes:
 *  - 401 cuando falta el token o es invalido.
 *  - 403 cuando el usuario esta autenticado pero no tiene el rol necesario.
 */
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        escribirError(response, request, HttpStatus.UNAUTHORIZED,
            "Debe iniciar sesion para acceder a este recurso");
    }

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        escribirError(response, request, HttpStatus.FORBIDDEN,
            "No tiene permisos para realizar esta operacion");
    }

    private void escribirError(
        HttpServletResponse response,
        HttpServletRequest request,
        HttpStatus status,
        String mensaje
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String json = String.format(
            "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"mensaje\":\"%s\",\"path\":\"%s\"}",
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            mensaje,
            escaparJson(request.getRequestURI())
        );
        response.getWriter().write(json);
    }

    private String escaparJson(String valor) {
        return valor == null ? "" : valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

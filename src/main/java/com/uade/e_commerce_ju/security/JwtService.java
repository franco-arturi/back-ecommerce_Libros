package com.uade.e_commerce_ju.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.uade.e_commerce_ju.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Genera y valida los JSON Web Tokens.
 * El token lleva el email como "subject" y el id y rol como claims, firmado con HMAC-SHA.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(
        @Value("${jwt.secret}") String secretoBase64,
        @Value("${jwt.expiration-ms}") long expiracionMs
    ) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
            .subject(usuario.getEmail())
            .claim("id", usuario.getId())
            .claim("rol", usuario.obtenerRol().name())
            .issuedAt(ahora)
            .expiration(new Date(ahora.getTime() + expiracionMs))
            .signWith(clave)
            .compact();
    }

    /**
     * Devuelve el email del token. Si la firma no es valida o el token vencio,
     * JJWT lanza una JwtException (la maneja JwtAuthenticationFilter).
     */
    public String extraerEmail(String token) {
        return leerClaims(token).getSubject();
    }

    public long getExpiracionSegundos() {
        return expiracionMs / 1000;
    }

    private Claims leerClaims(String token) {
        return Jwts.parser()
            .verifyWith(clave)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}

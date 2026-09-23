package com.uade.e_commerce_ju.model;

/**
 * Roles de la aplicacion. Se guardan como texto en la columna "role" de la tabla usuarios
 * y Spring Security los expone con el prefijo "ROLE_" (ROLE_USER, ROLE_ADMIN).
 */
public enum Role {
    /** Usuario comun: compra y publica libros. Es el rol que se asigna al registrarse. */
    USER,
    /** Administrador: accede a endpoints de gestion (por ejemplo, el listado de usuarios). */
    ADMIN
}

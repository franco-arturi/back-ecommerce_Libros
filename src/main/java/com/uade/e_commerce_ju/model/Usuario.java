package com.uade.e_commerce_ju.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Usuario. Implementa UserDetails para que Spring Security pueda usarla
 * directamente en la autenticacion (email + password encriptada con BCrypt).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "usuarios",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_usuario_username", columnNames = "username")
    }
)
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String username;

    @Column(length = 120)
    private String email;

    /** Hash BCrypt (60 caracteres). Nunca se guarda ni se devuelve en texto plano. */
    @Column(length = 100)
    private String password;

    @Column(length = 60)
    private String nombre;

    @Column(length = 60)
    private String apellido;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Role role = Role.USER;

    /**
     * Rol efectivo del usuario. Los registros creados antes de agregar la columna "role"
     * la tienen en null, por eso se toma USER como valor por defecto.
     */
    public Role obtenerRol() {
        return role != null ? role : Role.USER;
    }

    // ---- Contrato UserDetails ----
    // getPassword() y getUsername() los genera Lombok a partir de los campos.
    // El login se hace por email: ver UsuarioDetailsService.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + obtenerRol().name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

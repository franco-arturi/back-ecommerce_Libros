package com.uade.e_commerce_ju.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce_ju.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}

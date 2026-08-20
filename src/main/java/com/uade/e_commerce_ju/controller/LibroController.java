package com.uade.e_commerce_ju.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce_ju.model.Libro;
import com.uade.e_commerce_ju.service.LibroService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/Libros")
public class LibroController {
    private final LibroService libroService;

    LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    // GET http://localhost:8080/api/Libros/1
    @GetMapping("/{id}")
    public Libro getLibroById(@PathVariable Long id) {
        return libroService.getLibroById(id);
    }

}

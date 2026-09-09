package com.uade.e_commerce_ju.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce_ju.dto.libro.LibroDetalleDTO;
import com.uade.e_commerce_ju.dto.libro.LibroListadoDTO;
import com.uade.e_commerce_ju.dto.libro.LibroListadoDTO;
import com.uade.e_commerce_ju.model.Libro;
import com.uade.e_commerce_ju.service.LibroService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/libros")
public class LibroController {
    private final LibroService libroService;

    LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public ResponseEntity<List<LibroListadoDTO>> getLibros(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String titulo) {
        
    
        List<LibroListadoDTO> libros = libroService.getLibros(categoria, titulo);
        return ResponseEntity.ok(libros);
    }


    // GET http://localhost:8080/api/Libros/1 (manejo de error 404)
    @GetMapping("/{id}")
    public ResponseEntity<LibroDetalleDTO> getLibroById(@PathVariable Long id) {
        
        LibroDetalleDTO libro = libroService.getLibroById(id);
        if (libro == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(libro);
    }

}

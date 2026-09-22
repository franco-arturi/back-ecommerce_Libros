package com.uade.e_commerce_ju.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.uade.e_commerce_ju.dto.categoria.CategoriaResponseDTO;
import com.uade.e_commerce_ju.service.CategoriaService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



// http://localhost:8080/api/Categorias
@RestController
@RequestMapping("/api/Categorias")
public class CategoriaController {

    private final CategoriaService CategoriaService;

    CategoriaController(CategoriaService CategoriaService) {
        this.CategoriaService = CategoriaService;
    }


    // get http://localhost:8080/api/Categorias
    @GetMapping()
    public ResponseEntity<List<CategoriaResponseDTO>> getAllCategorias() {
        return ResponseEntity.ok(CategoriaService.getAllCategorias());
    }

    // get http://localhost:8080/api/Categorias/1
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> getCategoriaById(@PathVariable Long id) {
        return CategoriaService.getCategoriaById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    
}

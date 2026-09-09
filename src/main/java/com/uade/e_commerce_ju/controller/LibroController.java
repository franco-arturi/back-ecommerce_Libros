package com.uade.e_commerce_ju.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce_ju.dto.libro.LibroActualizarDTO;
import com.uade.e_commerce_ju.dto.libro.LibroActualizarStockDTO;
import com.uade.e_commerce_ju.dto.libro.LibroAltaDTO;
import com.uade.e_commerce_ju.dto.libro.LibroDetalleDTO;
import com.uade.e_commerce_ju.dto.libro.LibroListadoDTO;
import com.uade.e_commerce_ju.service.LibroService;

@RestController
@RequestMapping("/api/libros")
public class LibroController {
    private final LibroService libroService;

    LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public ResponseEntity<List<LibroListadoDTO>> getLibros(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String titulo) {

        List<LibroListadoDTO> libros = libroService.getLibros(categoriaId, titulo);
        return ResponseEntity.ok(libros);
    }

    // GET http://localhost:8080/api/libros/1 (manejo de error 404)
    @GetMapping("/{id}")
    public ResponseEntity<LibroDetalleDTO> getLibroById(@PathVariable Long id) {
        LibroDetalleDTO libro = libroService.getLibroById(id);
        if (libro == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(libro);
    }

    // alta de una publicacion
    @PostMapping
    public ResponseEntity<LibroDetalleDTO> crear(@RequestBody LibroAltaDTO request) {
        LibroDetalleDTO libro = libroService.crear(request);
        return ResponseEntity.created(URI.create("/api/libros/" + libro.id())).body(libro);
    }

    // modificar datos generales de una publicacion (solo el vendedor original)
    @PutMapping("/{id}")
    public ResponseEntity<LibroDetalleDTO> actualizar(
            @PathVariable Long id,
            @RequestBody LibroActualizarDTO request) {
        return ResponseEntity.ok(libroService.actualizar(id, request));
    }

    // actualizar unicamente el stock (solo el vendedor original)
    @PutMapping("/{id}/stock")
    public ResponseEntity<LibroDetalleDTO> actualizarStock(
            @PathVariable Long id,
            @RequestBody LibroActualizarStockDTO request) {
        return ResponseEntity.ok(libroService.actualizarStock(id, request));
    }

    // eliminar una publicacion (solo el vendedor original)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam Long vendedorId) {
        libroService.eliminar(id, vendedorId);
        return ResponseEntity.noContent().build();
    }
}

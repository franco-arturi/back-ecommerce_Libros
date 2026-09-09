package com.uade.e_commerce_ju.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce_ju.dto.libro.AgregarImagenDTO;
import com.uade.e_commerce_ju.dto.libro.ImagenLibroDTO;
import com.uade.e_commerce_ju.service.ImagenLibroService;

// http://localhost:8080/api/libros/{libroId}/imagenes
@RestController
@RequestMapping("/api/libros/{libroId}/imagenes")
public class ImagenLibroController {

    private final ImagenLibroService imagenLibroService;

    public ImagenLibroController(ImagenLibroService imagenLibroService) {
        this.imagenLibroService = imagenLibroService;
    }

    // GET http://localhost:8080/api/libros/1/imagenes
    @GetMapping
    public ResponseEntity<List<ImagenLibroDTO>> listar(@PathVariable Long libroId) {
        return ResponseEntity.ok(imagenLibroService.listar(libroId));
    }

    // POST http://localhost:8080/api/libros/1/imagenes
    @PostMapping
    public ResponseEntity<ImagenLibroDTO> agregar(
        @PathVariable Long libroId,
        @RequestBody AgregarImagenDTO request
    ) {
        ImagenLibroDTO imagen = imagenLibroService.agregar(libroId, request);
        return ResponseEntity
            .created(URI.create("/api/libros/" + libroId + "/imagenes/" + imagen.id()))
            .body(imagen);
    }

    // DELETE http://localhost:8080/api/libros/1/imagenes/1
    @DeleteMapping("/{imagenId}")
    public ResponseEntity<Void> eliminar(
        @PathVariable Long libroId,
        @PathVariable Long imagenId
    ) {
        imagenLibroService.eliminar(libroId, imagenId);
        return ResponseEntity.noContent().build();
    }
}

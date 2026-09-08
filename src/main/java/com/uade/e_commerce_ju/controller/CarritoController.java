package com.uade.e_commerce_ju.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce_ju.dto.carrito.AgregarItemDTO;
import com.uade.e_commerce_ju.dto.carrito.CarritoResponseDTO;
import com.uade.e_commerce_ju.dto.carrito.ModificarCantidadDTO;
import com.uade.e_commerce_ju.service.CarritoService;

@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping("/{usuarioId}")
    public CarritoResponseDTO obtener(@PathVariable Long usuarioId) {
        return carritoService.obtenerPorUsuario(usuarioId);
    }

    @PostMapping("/{usuarioId}/items")
    public CarritoResponseDTO agregarItem(
        @PathVariable Long usuarioId,
        @RequestBody AgregarItemDTO request
    ) {
        return carritoService.agregarItem(usuarioId, request);
    }

    @PutMapping("/{usuarioId}/items/{itemId}")
    public CarritoResponseDTO modificarCantidad(
        @PathVariable Long usuarioId,
        @PathVariable Long itemId,
        @RequestBody ModificarCantidadDTO request
    ) {
        return carritoService.modificarCantidad(usuarioId, itemId, request);
    }

    @DeleteMapping("/{usuarioId}/items/{itemId}")
    public ResponseEntity<Void> eliminarItem(
        @PathVariable Long usuarioId,
        @PathVariable Long itemId
    ) {
        carritoService.eliminarItem(usuarioId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> vaciar(@PathVariable Long usuarioId) {
        carritoService.vaciar(usuarioId);
        return ResponseEntity.noContent().build();
    }
}

package com.uade.e_commerce_ju.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.uade.e_commerce_ju.dto.carrito.AgregarItemDTO;
import com.uade.e_commerce_ju.dto.carrito.CarritoResponseDTO;
import com.uade.e_commerce_ju.exception.StockInsuficienteException;
import com.uade.e_commerce_ju.service.CarritoService;

@ExtendWith(MockitoExtension.class)
class CarritoControllerTests {

    @Mock
    private CarritoService carritoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new CarritoController(carritoService))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void obtieneElCarritoDelUsuario() throws Exception {
        when(carritoService.obtenerPorUsuario(1L)).thenReturn(
            new CarritoResponseDTO(2L, 1L, List.of(), BigDecimal.ZERO)
        );

        mockMvc.perform(get("/api/carritos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.usuarioId").value(1))
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void informaConflictoCuandoNoHayStock() throws Exception {
        when(carritoService.agregarItem(eq(1L), any(AgregarItemDTO.class)))
            .thenThrow(new StockInsuficienteException("Stock insuficiente"));

        mockMvc.perform(post("/api/carritos/1/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"libroId\":2,\"cantidad\":3}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.mensaje").value("Stock insuficiente"))
            .andExpect(jsonPath("$.path").value("/api/carritos/1/items"));
    }

    @Test
    void eliminaUnItemConRespuestaSinContenido() throws Exception {
        mockMvc.perform(delete("/api/carritos/1/items/4"))
            .andExpect(status().isNoContent());

        verify(carritoService).eliminarItem(1L, 4L);
    }
}

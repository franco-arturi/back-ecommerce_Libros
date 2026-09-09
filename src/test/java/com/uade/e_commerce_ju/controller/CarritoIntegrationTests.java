package com.uade.e_commerce_ju.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce_ju.model.Libro;
import com.uade.e_commerce_ju.model.Usuario;
import com.uade.e_commerce_ju.repository.LibroRepository;
import com.uade.e_commerce_ju.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CarritoIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Test
    void agregaYConsultaUnItemUsandoTodasLasCapas() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername("tito");
        usuario.setEmail("titoros@test.com");
        usuario.setPassword("password123");
        usuario.setNombre("tito");
        usuario.setApellido("roso");
        usuario = usuarioRepository.saveAndFlush(usuario);

        Libro libro = new Libro();
        libro.setTitulo("Clean Code");
        libro.setAutor("Robert C. Martin");
        libro.setPrecio(25000.0);
        libro.setStock(5);
        libro = libroRepository.saveAndFlush(libro);

        mockMvc.perform(post("/api/carritos/{usuarioId}/items", usuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"libroId\":" + libro.getId() + ",\"cantidad\":2}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.usuarioId").value(usuario.getId()))
            .andExpect(jsonPath("$.items[0].libroId").value(libro.getId()))
            .andExpect(jsonPath("$.items[0].cantidad").value(2))
            .andExpect(jsonPath("$.total").value(50000.0));

        mockMvc.perform(get("/api/carritos/{usuarioId}", usuario.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].titulo").value("Clean Code"));
    }
}

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

import com.uade.e_commerce_ju.model.Usuario;
import com.uade.e_commerce_ju.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioIntegrationTests {

    private static final String REGISTRO_VALIDO = """
        {
          "username": "rodrigo",
          "email": "roro@test.com",
          "password": "password123",
          "nombre": "rodrigo",
          "apellido": "gimenes"
        }
        """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void registraUnUsuario() throws Exception {
        mockMvc.perform(post("/api/usuarios/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(REGISTRO_VALIDO))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("rodrigo"))
            .andExpect(jsonPath("$.email").value("roro@test.com"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void buscaUnUsuarioPorId() throws Exception {
        Usuario usuario = crearUsuario();

        mockMvc.perform(get("/api/usuarios/" + usuario.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("roro@test.com"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void listaLosUsuarios() throws Exception {
        crearUsuario();

        mockMvc.perform(get("/api/usuarios/listarUser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].email").value("roro@test.com"))
            .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    private Usuario crearUsuario() {
        Usuario usuario = new Usuario();
        usuario.setUsername("rodrigo");
        usuario.setEmail("roro@test.com");
        usuario.setPassword("password123");
        usuario.setNombre("rodrigo");
        usuario.setApellido("gimenes");
        return usuarioRepository.saveAndFlush(usuario);
    }
}

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

/**
 * Reglas actuales de SecurityConfig: GET publico en libros/categorias, POST/PUT/DELETE de
 * categorias solo requieren estar autenticado (no hace falta ser ADMIN), y el listado de
 * usuarios (GET /api/usuarios/listarUser) es el unico endpoint restringido a rol ADMIN.
 * El admin de estos tests es el que crea DataInitializer al arrancar la app
 * (ver application.properties: app.admin.email / app.admin.password).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void catalogoDeLibrosEsPublico() throws Exception {
        mockMvc.perform(get("/api/libros")).andExpect(status().isOk());
    }

    @Test
    void catalogoDeCategoriasEsPublico() throws Exception {
        mockMvc.perform(get("/api/categorias")).andExpect(status().isOk());
    }

    @Test
    void listarUsuariosSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios/listarUser"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void listarUsuariosConTokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios/listarUser")
                .header("Authorization", "Bearer token-invalido"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listarUsuariosConUsuarioRegularDevuelve403() throws Exception {
        registrar("juanp", "juan@test.com");
        String token = login("juan@test.com", "secreta123");

        mockMvc.perform(get("/api/usuarios/listarUser")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void listarUsuariosConAdminDevuelve200() throws Exception {
        String token = login("admin@ecommerce.com", "admin123");

        mockMvc.perform(get("/api/usuarios/listarUser")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void crearCategoriaSinAutenticarDevuelve401() throws Exception {
        String body = """
            {"nombre":"Ficcion","descripcion":"Libros de ficcion"}
            """;

        mockMvc.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void crearCategoriaConUsuarioAutenticadoDevuelve201() throws Exception {
        registrar("juanp", "juan@test.com");
        String token = login("juan@test.com", "secreta123");

        String body = """
            {"nombre":"Ficcion","descripcion":"Libros de ficcion"}
            """;

        mockMvc.perform(post("/api/categorias")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Ficcion"));
    }

    private void registrar(String username, String email) throws Exception {
        String body = """
            {"username":"%s","email":"%s","password":"secreta123","nombre":"Juan","apellido":"Perez"}
            """.formatted(username, email);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());
    }

    private String login(String email, String password) throws Exception {
        String body = """
            {"email":"%s","password":"%s"}
            """.formatted(email, password);

        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        return response.replaceAll("(?s).*\"token\":\"([^\"]+)\".*", "$1");
    }
}

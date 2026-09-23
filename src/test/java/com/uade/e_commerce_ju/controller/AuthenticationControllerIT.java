package com.uade.e_commerce_ju.controller;

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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private static final String REGISTRO_BODY = """
        {"username":"juanp","email":"juan@test.com","password":"secreta123","nombre":"Juan","apellido":"Perez"}
        """;

    @Test
    void registrarUsuarioNuevoDevuelve201ConRolUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(REGISTRO_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value("juan@test.com"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void registrarConEmailDuplicadoDevuelve409() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(REGISTRO_BODY))
            .andExpect(status().isCreated());

        String duplicado = """
            {"username":"otro","email":"juan@test.com","password":"otraClave1","nombre":"Otro","apellido":"Usuario"}
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicado))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void registrarConEmailInvalidoDevuelve400() throws Exception {
        String invalido = """
            {"username":"juanp","email":"no-es-un-email","password":"secreta123","nombre":"Juan","apellido":"Perez"}
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalido))
            .andExpect(status().isBadRequest());
    }

    @Test
    void loginConCredencialesValidasDevuelveToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(REGISTRO_BODY))
            .andExpect(status().isCreated());

        String login = """
            {"email":"juan@test.com","password":"secreta123"}
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(login))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.usuario.email").value("juan@test.com"));
    }

    @Test
    void loginConPasswordIncorrectaDevuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(REGISTRO_BODY))
            .andExpect(status().isCreated());

        String login = """
            {"email":"juan@test.com","password":"incorrecta"}
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(login))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void loginConUsuarioInexistenteDevuelve401() throws Exception {
        String login = """
            {"email":"nadie@test.com","password":"secreta123"}
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(login))
            .andExpect(status().isUnauthorized());
    }
}

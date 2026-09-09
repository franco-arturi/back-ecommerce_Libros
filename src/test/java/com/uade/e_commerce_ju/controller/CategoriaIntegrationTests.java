package com.uade.e_commerce_ju.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce_ju.model.Categoria;
import com.uade.e_commerce_ju.model.Libro;
import com.uade.e_commerce_ju.model.Usuario;
import com.uade.e_commerce_ju.repository.CategoriaRepository;
import com.uade.e_commerce_ju.repository.LibroRepository;
import com.uade.e_commerce_ju.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoriaIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void completaElCrudYListaCategoriasAlfabeticamente() throws Exception {
        mockMvc.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Tecnologia\",\"descripcion\":\"Libros tecnicos\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Tecnologia"))
            .andExpect(jsonPath("$.descripcion").value("Libros tecnicos"));

        Categoria arte = crearCategoria("Arte");

        mockMvc.perform(get("/api/categorias"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombre").value("Arte"))
            .andExpect(jsonPath("$[1].nombre").value("Tecnologia"));

        mockMvc.perform(put("/api/categorias/{id}", arte.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Bellas Artes\",\"descripcion\":\"Arte actualizado\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Bellas Artes"));

        mockMvc.perform(delete("/api/categorias/{id}", arte.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categorias/{id}", arte.getId()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensaje").value("No existe la categoria con id " + arte.getId()));
    }

    @Test
    void validaNombreObligatorioYDuplicadoSinDistinguirMayusculas() throws Exception {
        crearCategoria("Programacion");

        mockMvc.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"   \",\"descripcion\":\"Invalida\"}"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"programacion\",\"descripcion\":\"Duplicada\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.mensaje").value(
                "Ya existe una categoria con el nombre programacion"
            ));
    }

    @Test
    void creaLibroConCategoriaRealYFiltraPorCategoriaId() throws Exception {
        Usuario vendedor = crearUsuario("catalogo");
        Categoria categoria = crearCategoria("Desarrollo");
        Categoria otraCategoria = crearCategoria("Literatura");

        mockMvc.perform(post("/api/libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "vendedorId": %d,
                      "titulo": "Patrones de diseno",
                      "autor": "Gamma",
                      "descripcion": "Catalogo de patrones",
                      "precio": 28000,
                      "stock": 4,
                      "categoriaId": %d
                    }
                    """.formatted(vendedor.getId(), categoria.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.categoria.id").value(categoria.getId()))
            .andExpect(jsonPath("$.categoria.nombre").value("Desarrollo"));

        mockMvc.perform(get("/api/libros").param("categoriaId", categoria.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].categoria.id").value(categoria.getId()));

        mockMvc.perform(get("/api/libros").param("categoriaId", otraCategoria.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void cambiaLaCategoriaAlActualizarUnaPublicacion() throws Exception {
        Usuario vendedor = crearUsuario("editor");
        Categoria inicial = crearCategoria("Inicial");
        Categoria nueva = crearCategoria("Nueva");
        Libro libro = crearLibro(vendedor, inicial, "Libro editable");

        mockMvc.perform(put("/api/libros/{id}", libro.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "vendedorId": %d,
                      "titulo": "Libro actualizado",
                      "autor": "Autor",
                      "descripcion": "Cambio de categoria",
                      "precio": 15000,
                      "categoriaId": %d
                    }
                    """.formatted(vendedor.getId(), nueva.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categoria.id").value(nueva.getId()))
            .andExpect(jsonPath("$.categoria.nombre").value("Nueva"));

        mockMvc.perform(get("/api/libros").param("categoriaId", inicial.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rechazaUnaCategoriaInexistenteAlCrearOFiltrarLibros() throws Exception {
        Usuario vendedor = crearUsuario("categoria_invalida");

        mockMvc.perform(post("/api/libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "vendedorId": %d,
                      "titulo": "Libro invalido",
                      "autor": "Autor",
                      "descripcion": "Categoria inexistente",
                      "precio": 1000,
                      "stock": 1,
                      "categoriaId": 999999
                    }
                    """.formatted(vendedor.getId())))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/libros").param("categoriaId", "999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void impideEliminarUnaCategoriaConLibrosAsociados() throws Exception {
        Usuario vendedor = crearUsuario("categoria_en_uso");
        Categoria categoria = crearCategoria("En uso");
        crearLibro(vendedor, categoria, "Libro relacionado");

        mockMvc.perform(delete("/api/categorias/{id}", categoria.getId()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.mensaje").value(
                "No se puede eliminar la categoria porque tiene libros asociados"
            ));

        mockMvc.perform(get("/api/categorias/{id}", categoria.getId()))
            .andExpect(status().isOk());
    }

    private Categoria crearCategoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion("Categoria para pruebas");
        return categoriaRepository.saveAndFlush(categoria);
    }

    private Usuario crearUsuario(String sufijo) {
        Usuario usuario = new Usuario();
        usuario.setUsername("usuario_" + sufijo);
        usuario.setEmail(sufijo + "@test.com");
        usuario.setPassword("password123");
        usuario.setNombre("Usuario");
        usuario.setApellido("Prueba");
        return usuarioRepository.saveAndFlush(usuario);
    }

    private Libro crearLibro(Usuario vendedor, Categoria categoria, String titulo) {
        Libro libro = new Libro();
        libro.setVendedor(vendedor);
        libro.setCategoria(categoria);
        libro.setTitulo(titulo);
        libro.setAutor("Autor");
        libro.setDescripcion("Libro para pruebas");
        libro.setPrecio(12000);
        libro.setStock(3);
        return libroRepository.saveAndFlush(libro);
    }
}

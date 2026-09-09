package com.uade.e_commerce_ju.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce_ju.dto.categoria.CategoriaCreateDTO;
import com.uade.e_commerce_ju.dto.categoria.CategoriaResponseDTO;
import com.uade.e_commerce_ju.dto.categoria.CategoriaUpdateDTO;
import com.uade.e_commerce_ju.exception.ArgumentoInvalidoException;
import com.uade.e_commerce_ju.exception.RecursoDuplicadoException;
import com.uade.e_commerce_ju.exception.RecursoEnUsoException;
import com.uade.e_commerce_ju.exception.RecursoNoEncontradoException;
import com.uade.e_commerce_ju.model.Categoria;
import com.uade.e_commerce_ju.repository.CategoriaRepository;
import com.uade.e_commerce_ju.repository.LibroRepository;

@Service
@Transactional
public class CategoriaService {

    private static final int LARGO_MAXIMO_NOMBRE = 100;
    private static final int LARGO_MAXIMO_DESCRIPCION = 500;

    private final CategoriaRepository categoriaRepository;
    private final LibroRepository libroRepository;

    public CategoriaService(
        CategoriaRepository categoriaRepository,
        LibroRepository libroRepository
    ) {
        this.categoriaRepository = categoriaRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream()
            .map(this::crearRespuesta)
            .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerPorId(Long id) {
        return crearRespuesta(buscarCategoria(id));
    }

    public CategoriaResponseDTO crear(CategoriaCreateDTO request) {
        if (request == null) {
            throw new ArgumentoInvalidoException("El cuerpo de la solicitud es obligatorio");
        }

        String nombre = validarNombre(request.nombre());
        validarNombreDuplicado(nombre, null);

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(validarDescripcion(request.descripcion()));
        return crearRespuesta(categoriaRepository.save(categoria));
    }

    public CategoriaResponseDTO actualizar(Long id, CategoriaUpdateDTO request) {
        if (request == null) {
            throw new ArgumentoInvalidoException("El cuerpo de la solicitud es obligatorio");
        }

        Categoria categoria = buscarCategoria(id);
        String nombre = validarNombre(request.nombre());
        validarNombreDuplicado(nombre, id);

        categoria.setNombre(nombre);
        categoria.setDescripcion(validarDescripcion(request.descripcion()));
        return crearRespuesta(categoriaRepository.save(categoria));
    }

    public void eliminar(Long id) {
        Categoria categoria = buscarCategoria(id);
        if (libroRepository.existsByCategoriaId(id)) {
            throw new RecursoEnUsoException(
                "No se puede eliminar la categoria porque tiene libros asociados"
            );
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria buscarCategoria(Long id) {
        if (id == null) {
            throw new RecursoNoEncontradoException("La categoria es obligatoria");
        }
        return categoriaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No existe la categoria con id " + id
            ));
    }

    private String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ArgumentoInvalidoException("El campo nombre es obligatorio");
        }
        String limpio = nombre.trim();
        if (limpio.length() > LARGO_MAXIMO_NOMBRE) {
            throw new ArgumentoInvalidoException(
                "El campo nombre no puede superar los " + LARGO_MAXIMO_NOMBRE + " caracteres"
            );
        }
        return limpio;
    }

    private String validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }
        String limpia = descripcion.trim();
        if (limpia.length() > LARGO_MAXIMO_DESCRIPCION) {
            throw new ArgumentoInvalidoException(
                "El campo descripcion no puede superar los "
                    + LARGO_MAXIMO_DESCRIPCION + " caracteres"
            );
        }
        return limpia;
    }

    private void validarNombreDuplicado(String nombre, Long idActual) {
        boolean duplicado = idActual == null
            ? categoriaRepository.existsByNombreIgnoreCase(nombre)
            : categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, idActual);
        if (duplicado) {
            throw new RecursoDuplicadoException("Ya existe una categoria con el nombre " + nombre);
        }
    }

    private CategoriaResponseDTO crearRespuesta(Categoria categoria) {
        return new CategoriaResponseDTO(
            categoria.getId(),
            categoria.getNombre(),
            categoria.getDescripcion()
        );
    }
}

package com.uade.e_commerce_ju.service;

import org.springframework.stereotype.Service;
import com.uade.e_commerce_ju.dto.libro.LibroDetalleDTO;
import com.uade.e_commerce_ju.dto.libro.LibroListadoDTO;
import com.uade.e_commerce_ju.model.Libro;
import com.uade.e_commerce_ju.repository.LibroRepository;

import java.util.List;
import java.util.Optional;

@Service
public class LibroService {

	private final LibroRepository libroRepository;

	public LibroService(LibroRepository libroRepository) {
		this.libroRepository = libroRepository;
	}

	//obtener catalogo de libros con filtros y generar el listado DTO
	public List<LibroListadoDTO> getLibros(String categoria, String titulo) {
        List<Libro> libros = libroRepository.buscarConFiltrosYOrden(categoria, titulo);
        
        return libros.stream()
            .map(libro -> new LibroListadoDTO(
                libro.getId(),
                libro.getTitulo(),
                libro.getAutor(),
                libro.getPrecio(),
                libro.getCategoria()
            )).toList();
    }

	//obtener libro por id y generar el detalle DTO
	public LibroDetalleDTO getLibroById(Long id) {
        Optional<Libro> libroOpt = libroRepository.findById(id);
        
        if (libroOpt.isEmpty()) {
            return null;
        }
        
        Libro libro = libroOpt.get();
        return new LibroDetalleDTO(
            libro.getId(),
            libro.getTitulo(),
            libro.getAutor(),
            libro.getPrecio(),
            libro.getStock(),
            libro.getDescripcion(),
            libro.getCategoria(),
            libro.getImagenes()
        );
    }
}

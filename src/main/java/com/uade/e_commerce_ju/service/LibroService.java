package com.uade.e_commerce_ju.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce_ju.dto.categoria.CategoriaResponseDTO;
import com.uade.e_commerce_ju.dto.libro.ImagenLibroDTO;
import com.uade.e_commerce_ju.dto.libro.LibroActualizarDTO;
import com.uade.e_commerce_ju.dto.libro.LibroActualizarStockDTO;
import com.uade.e_commerce_ju.dto.libro.LibroAltaDTO;
import com.uade.e_commerce_ju.dto.libro.LibroDetalleDTO;
import com.uade.e_commerce_ju.dto.libro.LibroListadoDTO;
import com.uade.e_commerce_ju.exception.ArgumentoInvalidoException;
import com.uade.e_commerce_ju.exception.OperacionNoAutorizadaException;
import com.uade.e_commerce_ju.exception.RecursoNoEncontradoException;
import com.uade.e_commerce_ju.model.Categoria;
import com.uade.e_commerce_ju.model.Libro;
import com.uade.e_commerce_ju.model.Usuario;
import com.uade.e_commerce_ju.repository.CategoriaRepository;
import com.uade.e_commerce_ju.repository.ImagenLibroRepository;
import com.uade.e_commerce_ju.repository.LibroRepository;
import com.uade.e_commerce_ju.repository.UsuarioRepository;

import java.util.List;

@Service
@Transactional
public class LibroService {

	private final LibroRepository libroRepository;
	private final UsuarioRepository usuarioRepository;
	private final ImagenLibroRepository imagenLibroRepository;
	private final CategoriaRepository categoriaRepository;

	public LibroService(
		LibroRepository libroRepository,
		UsuarioRepository usuarioRepository,
		ImagenLibroRepository imagenLibroRepository,
		CategoriaRepository categoriaRepository
	) {
		this.libroRepository = libroRepository;
		this.usuarioRepository = usuarioRepository;
		this.imagenLibroRepository = imagenLibroRepository;
		this.categoriaRepository = categoriaRepository;
	}

	//obtener catalogo de libros con filtros y generar el listado DTO
	@Transactional(readOnly = true)
    public List<LibroListadoDTO> getLibros(Long categoriaId, String titulo) {
        if (categoriaId != null && !categoriaRepository.existsById(categoriaId)) {
            throw new RecursoNoEncontradoException(
                "No existe la categoria con id " + categoriaId
            );
        }
        List<Libro> libros = libroRepository.buscarConFiltrosYOrden(categoriaId, titulo);

        return libros.stream()
            .map(libro -> new LibroListadoDTO(
                libro.getId(),
                libro.getTitulo(),
                libro.getAutor(),
                libro.getPrecio(),
                crearCategoriaRespuesta(libro.getCategoria())
            )).toList();
    }

	//obtener libro por id y generar el detalle DTO, con sus imagenes
	@Transactional(readOnly = true)
	public LibroDetalleDTO getLibroById(Long id) {
        return crearDetalle(buscarLibro(id));
    }

	// alta de una publicacion de libro. Las imagenes se cargan aparte,
	// con POST /api/libros/{libroId}/imagenes
	public LibroDetalleDTO crear(LibroAltaDTO request) {
		if (request == null) {
			throw new ArgumentoInvalidoException("El cuerpo de la solicitud es obligatorio");
		}

		Usuario vendedor = buscarVendedor(request.vendedorId());
		Categoria categoria = buscarCategoria(request.categoriaId());
		String titulo = obligatorio(request.titulo(), "titulo");
		validarPrecio(request.precio());
		validarStock(request.stock());

		Libro libro = new Libro();
		libro.setVendedor(vendedor);
		libro.setTitulo(titulo);
		libro.setAutor(request.autor());
		libro.setDescripcion(request.descripcion());
		libro.setPrecio(request.precio());
		libro.setStock(request.stock());
		libro.setCategoria(categoria);

		return crearDetalle(libroRepository.save(libro));
	}

	// modificar los datos generales de una publicacion (no el stock)
	public LibroDetalleDTO actualizar(Long id, LibroActualizarDTO request) {
		if (request == null) {
			throw new ArgumentoInvalidoException("El cuerpo de la solicitud es obligatorio");
		}

		Libro libro = buscarLibro(id);
		validarPropietario(libro, request.vendedorId());
		Categoria categoria = buscarCategoria(request.categoriaId());

		String titulo = obligatorio(request.titulo(), "titulo");
		validarPrecio(request.precio());

		libro.setTitulo(titulo);
		libro.setAutor(request.autor());
		libro.setDescripcion(request.descripcion());
		libro.setPrecio(request.precio());
		libro.setCategoria(categoria);

		return crearDetalle(libroRepository.save(libro));
	}

	// actualizar unicamente el stock de una publicacion
	public LibroDetalleDTO actualizarStock(Long id, LibroActualizarStockDTO request) {
		if (request == null) {
			throw new ArgumentoInvalidoException("El cuerpo de la solicitud es obligatorio");
		}

		Libro libro = buscarLibro(id);
		validarPropietario(libro, request.vendedorId());
		validarStock(request.stock());

		libro.setStock(request.stock());

		return crearDetalle(libroRepository.save(libro));
	}

	// eliminar una publicacion, solo permitido al vendedor que la publico
	public void eliminar(Long id, Long vendedorId) {
		Libro libro = buscarLibro(id);
		validarPropietario(libro, vendedorId);
		// las imagenes apuntan al libro con una clave foranea, asi que van primero
		imagenLibroRepository.deleteByLibroId(id);
		libroRepository.delete(libro);
	}

	private Libro buscarLibro(Long id) {
		return libroRepository.findById(id)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe el libro con id " + id));
	}

	private Usuario buscarVendedor(Long vendedorId) {
		if (vendedorId == null) {
			throw new ArgumentoInvalidoException("El vendedorId es obligatorio");
		}
		return usuarioRepository.findById(vendedorId)
			.orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + vendedorId));
	}

	private Categoria buscarCategoria(Long categoriaId) {
		if (categoriaId == null) {
			throw new ArgumentoInvalidoException("El categoriaId es obligatorio");
		}
		return categoriaRepository.findById(categoriaId)
			.orElseThrow(() -> new RecursoNoEncontradoException(
				"No existe la categoria con id " + categoriaId
			));
	}

	private void validarPropietario(Libro libro, Long vendedorId) {
		if (vendedorId == null) {
			throw new ArgumentoInvalidoException("El vendedorId es obligatorio");
		}
		if (!libro.getVendedor().getId().equals(vendedorId)) {
			throw new OperacionNoAutorizadaException(
				"Solo el usuario que publico el libro puede modificarlo o eliminarlo"
			);
		}
	}

	private String obligatorio(String valor, String campo) {
		if (valor == null || valor.isBlank()) {
			throw new ArgumentoInvalidoException("El campo " + campo + " es obligatorio");
		}
		return valor.trim();
	}

	private void validarPrecio(Double precio) {
		if (precio == null || precio <= 0) {
			throw new ArgumentoInvalidoException("El precio debe ser mayor a cero");
		}
	}

	private void validarStock(Integer stock) {
		if (stock == null || stock < 0) {
			throw new ArgumentoInvalidoException("El stock no puede ser negativo");
		}
	}

	private LibroDetalleDTO crearDetalle(Libro libro) {
		List<ImagenLibroDTO> imagenes = imagenLibroRepository
			.findByLibroIdOrderByIdAsc(libro.getId()).stream()
			.map(imagen -> new ImagenLibroDTO(imagen.getId(), imagen.getUrl()))
			.toList();

		return new LibroDetalleDTO(
            libro.getId(),
            libro.getTitulo(),
            libro.getAutor(),
            libro.getPrecio(),
            libro.getStock(),
            libro.getDescripcion(),
            crearCategoriaRespuesta(libro.getCategoria()),
            imagenes,
            libro.getVendedor().getId()
        );
	}

	private CategoriaResponseDTO crearCategoriaRespuesta(Categoria categoria) {
		return new CategoriaResponseDTO(
			categoria.getId(),
			categoria.getNombre(),
			categoria.getDescripcion()
		);
	}
}

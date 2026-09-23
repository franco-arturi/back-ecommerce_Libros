package com.uade.e_commerce_ju.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce_ju.dto.carrito.AgregarItemDTO;
import com.uade.e_commerce_ju.dto.carrito.CarritoResponseDTO;
import com.uade.e_commerce_ju.dto.carrito.CheckoutResponseDTO;
import com.uade.e_commerce_ju.dto.carrito.ItemCarritoDTO;
import com.uade.e_commerce_ju.dto.carrito.ModificarCantidadDTO;
import com.uade.e_commerce_ju.exception.CantidadInvalidaException;
import com.uade.e_commerce_ju.exception.RecursoNoEncontradoException;
import com.uade.e_commerce_ju.exception.StockInsuficienteException;
import com.uade.e_commerce_ju.model.Carrito;
import com.uade.e_commerce_ju.model.ItemCarrito;
import com.uade.e_commerce_ju.model.Libro;
import com.uade.e_commerce_ju.model.Usuario;
import com.uade.e_commerce_ju.repository.CarritoRepository;
import com.uade.e_commerce_ju.repository.ItemCarritoRepository;
import com.uade.e_commerce_ju.repository.LibroRepository;
import com.uade.e_commerce_ju.repository.UsuarioRepository;

@Service
@Transactional
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    public CarritoService(
        CarritoRepository carritoRepository,
        ItemCarritoRepository itemCarritoRepository,
        LibroRepository libroRepository,
        UsuarioRepository usuarioRepository
    ) {
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public CarritoResponseDTO obtenerPorUsuario(Long usuarioId) {
        validarUsuario(usuarioId);
        return carritoRepository.findByUsuarioId(usuarioId)
            .map(this::crearRespuesta)
            .orElseGet(() -> carritoVacio(usuarioId));
    }

    public CarritoResponseDTO agregarItem(Long usuarioId, AgregarItemDTO request) {
        validarCantidad(request == null ? null : request.cantidad());
        if (request.libroId() == null) {
            throw new CantidadInvalidaException("El libroId es obligatorio");
        }

        Usuario usuario = validarUsuario(usuarioId);
        Libro libro = buscarLibro(request.libroId());
        validarStock(libro, request.cantidad());
        Carrito carrito = obtenerOCrearCarrito(usuario);
        ItemCarrito item = itemCarritoRepository
            .findByCarritoIdAndLibroId(carrito.getId(), libro.getId())
            .orElse(null);
        int cantidadFinal = request.cantidad() + (item == null ? 0 : item.getCantidad());
        validarStock(libro, cantidadFinal);

        if (item == null) {
            item = new ItemCarrito();
            item.setLibro(libro);
            item.setCantidad(request.cantidad());
            carrito.agregarItem(item);
        } else {
            item.setCantidad(cantidadFinal);
        }

        return crearRespuesta(carritoRepository.save(carrito));
    }

    public CarritoResponseDTO modificarCantidad(
        Long usuarioId,
        Long itemId,
        ModificarCantidadDTO request
    ) {
        validarCantidad(request == null ? null : request.cantidad());
        validarUsuario(usuarioId);
        ItemCarrito item = buscarItemDelUsuario(usuarioId, itemId);
        validarStock(item.getLibro(), request.cantidad());
        item.setCantidad(request.cantidad());
        itemCarritoRepository.save(item);
        return crearRespuesta(item.getCarrito());
    }

    public void eliminarItem(Long usuarioId, Long itemId) {
        validarUsuario(usuarioId);
        ItemCarrito item = buscarItemDelUsuario(usuarioId, itemId);
        Carrito carrito = item.getCarrito();
        carrito.quitarItem(item);
        carritoRepository.save(carrito);
    }

    public void vaciar(Long usuarioId) {
        validarUsuario(usuarioId);
        carritoRepository.findByUsuarioId(usuarioId).ifPresent(carrito -> {
            carrito.vaciar();
            carritoRepository.save(carrito);
        });
    }

    private Carrito obtenerOCrearCarrito(Usuario usuario) {
        return carritoRepository.findByUsuarioId(usuario.getId()).orElseGet(() -> {
            Carrito carrito = new Carrito();
            carrito.setUsuario(usuario);
            return carritoRepository.save(carrito);
        });
    }

    private Usuario validarUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new RecursoNoEncontradoException("El usuario es obligatorio");
        }
        return usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No existe el usuario con id " + usuarioId
            ));
    }

    private Libro buscarLibro(Long libroId) {
        return libroRepository.findById(libroId)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No existe el libro con id " + libroId
            ));
    }

    private ItemCarrito buscarItemDelUsuario(Long usuarioId, Long itemId) {
        return itemCarritoRepository.findByIdAndCarritoUsuarioId(itemId, usuarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No existe el item " + itemId + " en el carrito del usuario " + usuarioId
            ));
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new CantidadInvalidaException("La cantidad debe ser mayor a cero");
        }
    }

    private void validarStock(Libro libro, int cantidadSolicitada) {
        int stockDisponible = libro.getStock() == null ? 0 : libro.getStock();
        if (stockDisponible <= 0 || cantidadSolicitada > stockDisponible) {
            throw new StockInsuficienteException(
                "Stock insuficiente para el libro " + libro.getId()
                    + ": disponible " + stockDisponible
                    + ", solicitado " + cantidadSolicitada
            );
        }
    }

    private CarritoResponseDTO carritoVacio(Long usuarioId) {
        return new CarritoResponseDTO(null, usuarioId, List.of(), BigDecimal.ZERO);
    }

    private CarritoResponseDTO crearRespuesta(Carrito carrito) {
        List<ItemCarritoDTO> items = carrito.getItems().stream()
            .map(this::crearItemDTO)
            .toList();
        BigDecimal total = items.stream()
            .map(ItemCarritoDTO::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarritoResponseDTO(
            carrito.getId(),
            carrito.getUsuario().getId(),
            items,
            total
        );
    }

    private ItemCarritoDTO crearItemDTO(ItemCarrito item) {
        BigDecimal precio = BigDecimal.valueOf(item.getLibro().getPrecio());
        BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(item.getCantidad()));
        return new ItemCarritoDTO(
            item.getId(),
            item.getLibro().getId(),
            item.getLibro().getTitulo(),
            precio,
            item.getCantidad(),
            subtotal
        );
    }
    
    // --- NUEVO MÉTODO PARA EL CHECKOUT ---
    public CheckoutResponseDTO checkout(Long usuarioId) {
        // Validamos que el usuario exista (reutilizando tu método)
        validarUsuario(usuarioId);
        
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró un carrito para el usuario"));

        // 1. Rechazar un carrito vacío
        if (carrito.getItems().isEmpty()) {
            throw new CantidadInvalidaException("No se puede hacer checkout de un carrito vacío");
        }

        // 2. Volver a validar todos los stocks antes de comprar
        for (ItemCarrito item : carrito.getItems()) {
            // Reutilizamos tu método privado que ya lanza la excepción si no hay stock
            validarStock(item.getLibro(), item.getCantidad()); 
        }

        BigDecimal totalFinal = BigDecimal.ZERO;

        // 3. Descontar stock y calcular total
        for (ItemCarrito item : carrito.getItems()) {
            Libro libro = item.getLibro();
            
            // Restamos el stock
            libro.setStock(libro.getStock() - item.getCantidad());
            libroRepository.save(libro);
            
            // Calculamos el subtotal y lo sumamos al total final usando BigDecimal
            BigDecimal precio = BigDecimal.valueOf(libro.getPrecio());
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(item.getCantidad()));
            totalFinal = totalFinal.add(subtotal);
        }

        // 4. Vaciar o finalizar el carrito tras una compra correcta
        carrito.vaciar(); // Usamos el método vaciar() que ya tiene tu modelo Carrito
        carritoRepository.save(carrito);

        // 5. Devolver el total final (no procesamos pagos reales)
        return new CheckoutResponseDTO(totalFinal, "Compra finalizada con éxito");
    }
}

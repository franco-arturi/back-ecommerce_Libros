package com.uade.e_commerce_ju.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.e_commerce_ju.dto.carrito.AgregarItemDTO;
import com.uade.e_commerce_ju.dto.carrito.CarritoResponseDTO;
import com.uade.e_commerce_ju.dto.carrito.ModificarCantidadDTO;
import com.uade.e_commerce_ju.exception.StockInsuficienteException;
import com.uade.e_commerce_ju.model.Carrito;
import com.uade.e_commerce_ju.model.ItemCarrito;
import com.uade.e_commerce_ju.model.Libro;
import com.uade.e_commerce_ju.model.Usuario;
import com.uade.e_commerce_ju.repository.CarritoRepository;
import com.uade.e_commerce_ju.repository.ItemCarritoRepository;
import com.uade.e_commerce_ju.repository.LibroRepository;
import com.uade.e_commerce_ju.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTests {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private CarritoService carritoService;

    @BeforeEach
    void setUp() {
        carritoService = new CarritoService(
            carritoRepository,
            itemCarritoRepository,
            libroRepository,
            usuarioRepository
        );
    }

    @Test
    void agregaUnLibroDisponibleAlCarrito() {
        Usuario usuario = usuario(1L);
        Libro libro = libro(2L, 10);
        Carrito carrito = carrito(3L, usuario);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(libroRepository.findById(2L)).thenReturn(Optional.of(libro));
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoIdAndLibroId(3L, 2L))
            .thenReturn(Optional.empty());
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        CarritoResponseDTO response = carritoService.agregarItem(
            1L,
            new AgregarItemDTO(2L, 2)
        );

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().libroId()).isEqualTo(2L);
        assertThat(response.items().getFirst().cantidad()).isEqualTo(2);
        assertThat(response.total()).isEqualByComparingTo("200.00");
    }

    @Test
    void agregarElMismoLibroAcumulaLaCantidad() {
        Usuario usuario = usuario(1L);
        Libro libro = libro(2L, 10);
        Carrito carrito = carrito(3L, usuario);
        ItemCarrito item = item(4L, carrito, libro, 2);
        carrito.agregarItem(item);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(libroRepository.findById(2L)).thenReturn(Optional.of(libro));
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoIdAndLibroId(3L, 2L))
            .thenReturn(Optional.of(item));
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        CarritoResponseDTO response = carritoService.agregarItem(
            1L,
            new AgregarItemDTO(2L, 3)
        );

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().cantidad()).isEqualTo(5);
        assertThat(response.total()).isEqualByComparingTo("500.00");
    }

    @Test
    void rechazaUnaCantidadMayorAlStock() {
        Usuario usuario = usuario(1L);
        Libro libro = libro(2L, 2);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(libroRepository.findById(2L)).thenReturn(Optional.of(libro));

        assertThatThrownBy(() -> carritoService.agregarItem(
            1L,
            new AgregarItemDTO(2L, 3)
        )).isInstanceOf(StockInsuficienteException.class);

        verify(carritoRepository, never()).save(any());
    }

    @Test
    void modificaLaCantidadCuandoHayStock() {
        Usuario usuario = usuario(1L);
        Libro libro = libro(2L, 8);
        Carrito carrito = carrito(3L, usuario);
        ItemCarrito item = item(4L, carrito, libro, 1);
        carrito.agregarItem(item);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(itemCarritoRepository.findByIdAndCarritoUsuarioId(4L, 1L))
            .thenReturn(Optional.of(item));

        CarritoResponseDTO response = carritoService.modificarCantidad(
            1L,
            4L,
            new ModificarCantidadDTO(6)
        );

        assertThat(response.items().getFirst().cantidad()).isEqualTo(6);
        verify(itemCarritoRepository).save(item);
    }

    @Test
    void eliminaUnItemDelCarrito() {
        Usuario usuario = usuario(1L);
        Libro libro = libro(2L, 8);
        Carrito carrito = carrito(3L, usuario);
        ItemCarrito item = item(4L, carrito, libro, 1);
        carrito.agregarItem(item);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(itemCarritoRepository.findByIdAndCarritoUsuarioId(4L, 1L))
            .thenReturn(Optional.of(item));

        carritoService.eliminarItem(1L, 4L);

        assertThat(carrito.getItems()).isEmpty();
        verify(carritoRepository).save(carrito);
    }

    @Test
    void vaciaTodosLosItemsDelCarrito() {
        Usuario usuario = usuario(1L);
        Libro libro = libro(2L, 8);
        Carrito carrito = carrito(3L, usuario);
        carrito.agregarItem(item(4L, carrito, libro, 1));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));

        carritoService.vaciar(1L);

        assertThat(carrito.getItems()).isEmpty();
        verify(carritoRepository).save(carrito);
    }

    @Test
    void devuelveUnCarritoVacioSinCrearloDuranteLaConsulta() {
        Usuario usuario = usuario(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());

        CarritoResponseDTO response = carritoService.obtenerPorUsuario(1L);

        assertThat(response.id()).isNull();
        assertThat(response.usuarioId()).isEqualTo(1L);
        assertThat(response.items()).isEmpty();
        assertThat(response.total()).isZero();
        verify(carritoRepository, never()).save(any());
    }

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    private Libro libro(Long id, int stock) {
        Libro libro = new Libro();
        libro.setId(id);
        libro.setTitulo("Libro de prueba");
        libro.setPrecio(100.0);
        libro.setStock(stock);
        return libro;
    }

    private Carrito carrito(Long id, Usuario usuario) {
        Carrito carrito = new Carrito();
        carrito.setId(id);
        carrito.setUsuario(usuario);
        return carrito;
    }

    private ItemCarrito item(
        Long id,
        Carrito carrito,
        Libro libro,
        int cantidad
    ) {
        ItemCarrito item = new ItemCarrito();
        item.setId(id);
        item.setCarrito(carrito);
        item.setLibro(libro);
        item.setCantidad(cantidad);
        return item;
    }
}

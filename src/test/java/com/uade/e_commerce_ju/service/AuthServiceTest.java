package com.uade.e_commerce_ju.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uade.e_commerce_ju.dto.auth.AuthResponseDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioLoginDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioRegistroDTO;
import com.uade.e_commerce_ju.dto.usuario.UsuarioResponseDTO;
import com.uade.e_commerce_ju.exception.ArgumentoInvalidoException;
import com.uade.e_commerce_ju.exception.CredencialesInvalidasException;
import com.uade.e_commerce_ju.exception.RecursoDuplicadoException;
import com.uade.e_commerce_ju.model.Role;
import com.uade.e_commerce_ju.model.Usuario;
import com.uade.e_commerce_ju.repository.UsuarioRepository;
import com.uade.e_commerce_ju.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void registrarCreaUsuarioConRolUserYPasswordEncriptado() {
        UsuarioRegistroDTO request = new UsuarioRegistroDTO(
            "juanp", "juan@test.com", "secreta123", "Juan", "Perez"
        );
        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(usuarioRepository.existsByUsername("juanp")).thenReturn(false);
        when(passwordEncoder.encode("secreta123")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        UsuarioResponseDTO response = authService.registrar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("juan@test.com");
        assertThat(response.rol()).isEqualTo(Role.USER.name());
        verify(passwordEncoder).encode("secreta123");
    }

    @Test
    void registrarFallaSiElEmailYaExiste() {
        UsuarioRegistroDTO request = new UsuarioRegistroDTO(
            "juanp", "juan@test.com", "secreta123", "Juan", "Perez"
        );
        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(request))
            .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    void registrarFallaSiElEmailTieneFormatoInvalido() {
        UsuarioRegistroDTO request = new UsuarioRegistroDTO(
            "juanp", "no-es-un-email", "secreta123", "Juan", "Perez"
        );

        assertThatThrownBy(() -> authService.registrar(request))
            .isInstanceOf(ArgumentoInvalidoException.class);
    }

    @Test
    void registrarFallaSiLaPasswordEsMuyCorta() {
        UsuarioRegistroDTO request = new UsuarioRegistroDTO(
            "juanp", "juan@test.com", "abc", "Juan", "Perez"
        );

        assertThatThrownBy(() -> authService.registrar(request))
            .isInstanceOf(ArgumentoInvalidoException.class);
    }

    @Test
    void loginDevuelveTokenConCredencialesValidas() {
        UsuarioLoginDTO request = new UsuarioLoginDTO("juan@test.com", "secreta123");
        Usuario usuario = Usuario.builder()
            .id(1L).username("juanp").email("juan@test.com").password("HASH")
            .nombre("Juan").apellido("Perez").role(Role.USER).build();

        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generarToken(usuario)).thenReturn("token123");
        when(jwtService.getExpiracionSegundos()).thenReturn(3600L);

        AuthResponseDTO response = authService.login(request);

        assertThat(response.token()).isEqualTo("token123");
        assertThat(response.usuario().email()).isEqualTo("juan@test.com");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void loginFallaConCredencialesInvalidas() {
        UsuarioLoginDTO request = new UsuarioLoginDTO("juan@test.com", "incorrecta");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void loginFallaSiFaltanCampos() {
        assertThatThrownBy(() -> authService.login(new UsuarioLoginDTO("", "")))
            .isInstanceOf(CredencialesInvalidasException.class);

        verifyNoInteractions(authenticationManager);
        verify(usuarioRepository, never()).findByEmail(any());
    }
}

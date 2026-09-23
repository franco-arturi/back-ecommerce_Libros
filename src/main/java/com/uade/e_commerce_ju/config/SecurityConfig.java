package com.uade.e_commerce_ju.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.uade.e_commerce_ju.model.Role;
import com.uade.e_commerce_ju.security.JwtAuthenticationFilter;
import com.uade.e_commerce_ju.security.SecurityErrorHandler;

/**
 * Configuracion central de Spring Security.
 *
 * Reglas de acceso:
 *  - Publico: registro y login (/api/auth/**), consulta del catalogo (GET de libros y categorias),
 *    consola H2 del perfil demo.
 *  - Solo ADMIN: listado completo de usuarios.
 *  - Todo lo demas requiere estar autenticado con un JWT valido.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityErrorHandler securityErrorHandler;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter,
        SecurityErrorHandler securityErrorHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityErrorHandler = securityErrorHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // API REST con JWT en el header: no hay cookies de sesion, no aplica CSRF.
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            // Stateless: el servidor no guarda sesion, cada request trae su token.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Permite que la consola H2 se muestre dentro de su propio frame.
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .exceptionHandling(errores -> errores
                .authenticationEntryPoint(securityErrorHandler)
                .accessDeniedHandler(securityErrorHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/h2-console/**", "/error", "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/libros/**", "/api/categorias/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/usuarios/listarUser").hasRole(Role.ADMIN.name())
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** BCrypt: hash con salt aleatorio, las passwords nunca se guardan en texto plano. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Valida email y password en el login. Internamente usa UsuarioDetailsService
     * para buscar el usuario y PasswordEncoder para comparar contra el hash guardado.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /** Permite que el front (React/Vite en desarrollo) llame a la API desde otro puerto. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

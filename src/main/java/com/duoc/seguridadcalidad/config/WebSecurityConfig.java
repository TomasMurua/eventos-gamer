package com.duoc.seguridadcalidad.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /** Rutas publicas del caso: inicio, registro y login. Todo lo demas requiere autenticacion. */
    private static final String[] RUTAS_PUBLICAS = {
            "/", "/home", "/recetas", "/registro", "/login", "/error",
            "/css/**", "/img/**", "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home")
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; img-src 'self' data:; style-src 'self'; "
                                        + "script-src 'self'; form-action 'self'; frame-ancestors 'none'")));

        return http.build();
    }

    /**
     * Usuarios de prueba en memoria. Las contrasenas se leen de variables de entorno
     * (con valores por defecto solo para desarrollo) y se guardan cifradas con BCrypt.
     */
    @Bean
    public UserDetailsManager userDetailsManager(
            PasswordEncoder passwordEncoder,
            @Value("${app.usuarios.password-usuario}") String passwordUsuario,
            @Value("${app.usuarios.password-admin}") String passwordAdmin) {
        return new InMemoryUserDetailsManager(
                User.builder()
                        .username("ana@eventosgamer.cl")
                        .password(passwordEncoder.encode(passwordUsuario))
                        .roles("USER")
                        .build(),
                User.builder()
                        .username("diego@eventosgamer.cl")
                        .password(passwordEncoder.encode(passwordUsuario))
                        .roles("USER")
                        .build(),
                User.builder()
                        .username("admin@eventosgamer.cl")
                        .password(passwordEncoder.encode(passwordAdmin))
                        .roles("USER", "ADMIN")
                        .build());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

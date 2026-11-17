package com.duoc.seguridadcalidad.config;

import com.duoc.seguridadcalidad.security.CustomAuthenticationProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /** Paginas publicas: inicio, busqueda de eventos, registro y login. Todo lo demas requiere autenticacion. */
    private static final String[] RUTAS_PUBLICAS = {
            "/", "/home", "/recetas", "/eventos", "/eventos/buscar", "/registro", "/login", "/error",
            "/css/**", "/img/**", "/fonts/**", "/favicon.ico"
    };

    /** Tamano maximo de una peticion (igual a spring.servlet.multipart.max-request-size). */
    static final long MAXIMO_PETICION = 8L * 1024 * 1024;

    private final CustomAuthenticationProvider authProvider;

    public WebSecurityConfig(CustomAuthenticationProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authProvider)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/eventos")
                        .permitAll())
                // Si un archivo supera el limite, Tomcat no alcanza a leer el token CSRF del formulario y la
                // peticion se rechazaria como 403. Se responde 413 para que la pagina explique el motivo real.
                .exceptionHandling(e -> e.accessDeniedHandler((request, response, ex) -> {
                    if (ex instanceof CsrfException && subidaDemasiadoGrande(request)) {
                        response.sendError(HttpStatus.PAYLOAD_TOO_LARGE.value());
                    } else {
                        response.sendError(HttpStatus.FORBIDDEN.value());
                    }
                }))
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; img-src 'self' data:; style-src 'self'; "
                                        + "script-src 'self'; form-action 'self'; frame-ancestors 'none'; "
                                        + "base-uri 'self'; object-src 'none'"))
                        .referrerPolicy(r -> r.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicyHeader(p -> p.policy("camera=(), microphone=(), geolocation=(), payment=()"))
                        .httpStrictTransportSecurity(h -> h.includeSubDomains(true).maxAgeInSeconds(31536000)));

        return http.build();
    }

    static boolean subidaDemasiadoGrande(HttpServletRequest request) {
        String tipo = request.getContentType();
        return tipo != null && tipo.startsWith("multipart/") && request.getContentLengthLong() > MAXIMO_PETICION;
    }
}

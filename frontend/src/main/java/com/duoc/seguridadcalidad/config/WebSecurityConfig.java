package com.duoc.seguridadcalidad.config;

import com.duoc.seguridadcalidad.security.CustomAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /** Paginas publicas: inicio, busqueda de eventos y login. Todo lo demas requiere autenticacion. */
    private static final String[] RUTAS_PUBLICAS = {
            "/", "/home", "/recetas", "/eventos", "/eventos/buscar", "/login", "/error",
            "/css/**", "/img/**", "/fonts/**", "/favicon.ico"
    };

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
}

package com.duoc.seguridadcalidad.backend;

import com.duoc.seguridadcalidad.backend.Dtos.EventoDetalle;
import com.duoc.seguridadcalidad.backend.Dtos.EventoResumen;
import com.duoc.seguridadcalidad.backend.Dtos.Filtros;
import com.duoc.seguridadcalidad.backend.Dtos.LoginResponse;
import com.duoc.seguridadcalidad.backend.Dtos.Portada;
import com.duoc.seguridadcalidad.backend.Dtos.Usuario;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

/** Cliente HTTP hacia la API REST del backend. */
@Component
public class BackendClient {

    private final RestClient http;

    public BackendClient(RestClient.Builder builder, @Value("${app.backend-url}") String backendUrl) {
        this.http = builder.baseUrl(backendUrl).build();
    }

    /** Devuelve la respuesta del login si las credenciales son validas, o vacio si el backend las rechaza. */
    public Optional<LoginResponse> login(String username, String password) {
        try {
            return Optional.ofNullable(http.post().uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("username", username, "password", password))
                    .retrieve()
                    .body(LoginResponse.class));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return Optional.empty();
            }
            throw e;
        }
    }

    public Portada portada() {
        return http.get().uri("/api/portada").retrieve().body(Portada.class);
    }

    public Filtros filtros() {
        return http.get().uri("/api/eventos/filtros").retrieve().body(Filtros.class);
    }

    public List<EventoResumen> buscar(Map<String, String> filtros) {
        Map<String, String> conValor = new LinkedHashMap<>();
        filtros.forEach((clave, valor) -> {
            if (valor != null && !valor.isBlank()) {
                conValor.put(clave, valor);
            }
        });
        // Los valores se pasan como variables de plantilla para que RestClient los codifique completos
        EventoResumen[] resultado = http.get()
                .uri(b -> {
                    UriBuilder builder = b.path("/api/eventos");
                    conValor.keySet().forEach(clave -> builder.queryParam(clave, "{" + clave + "}"));
                    return builder.build(conValor);
                })
                .retrieve()
                .body(EventoResumen[].class);
        return resultado == null ? List.of() : Arrays.asList(resultado);
    }

    public Optional<EventoDetalle> detalle(Long id, String token) {
        try {
            return Optional.ofNullable(http.get().uri("/api/eventos/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(EventoDetalle.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    public Usuario yo(String token) {
        return http.get().uri("/api/usuarios/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(Usuario.class);
    }

    public List<Usuario> usuarios(String token) {
        Usuario[] resultado = http.get().uri("/api/admin/usuarios")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(Usuario[].class);
        return resultado == null ? List.of() : Arrays.asList(resultado);
    }
}

package com.duoc.seguridadcalidad.backend;

import com.duoc.seguridadcalidad.backend.Dtos.Comentario;
import com.duoc.seguridadcalidad.backend.Dtos.ErrorApi;
import com.duoc.seguridadcalidad.backend.Dtos.EventoDetalle;
import com.duoc.seguridadcalidad.backend.Dtos.EventoResumen;
import com.duoc.seguridadcalidad.backend.Dtos.Filtros;
import com.duoc.seguridadcalidad.backend.Dtos.LoginResponse;
import com.duoc.seguridadcalidad.backend.Dtos.Medio;
import com.duoc.seguridadcalidad.backend.Dtos.Portada;
import com.duoc.seguridadcalidad.backend.Dtos.RegistroResponse;
import com.duoc.seguridadcalidad.backend.Dtos.Usuario;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriBuilder;

/** Cliente HTTP hacia la API REST del backend. */
@Component
public class BackendClient {

    /** Respuestas de la API que traen un mensaje para el usuario (datos invalidos, duplicados, limites). */
    private static final Set<Integer> RECHAZOS = Set.of(400, 409, 413, 429);

    /** Tipos de archivo que el frontend acepta reenviar al navegador. */
    private static final Set<String> TIPOS_MEDIO = Set.of("image/jpeg", "image/png", "image/webp", "video/mp4",
            "video/webm");

    private static final String BEARER = "Bearer ";

    private final RestClient http;

    public BackendClient(RestClient.Builder builder, @Value("${app.backend-url}") String backendUrl) {
        this.http = builder.baseUrl(backendUrl).build();
    }

    /**
     * Devuelve la respuesta del login si las credenciales son validas, o vacio si el backend las rechaza
     * (incluido el bloqueo por exceso de intentos). Se reenvia la IP del visitante para que el backend
     * cuente los intentos fallidos por IP real y no por la del frontend.
     */
    public Optional<LoginResponse> login(String username, String password, String ipCliente) {
        try {
            return Optional.ofNullable(http.post().uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> {
                        if (ipCliente != null) {
                            h.set("X-Forwarded-For", ipCliente);
                        }
                    })
                    .body(Map.of("username", username, "password", password))
                    .retrieve()
                    .body(LoginResponse.class));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.BAD_REQUEST
                    || e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
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

    public RegistroResponse registrar(Map<String, String> datos, String ipCliente) {
        return conRechazos(() -> http.post().uri("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> {
                    if (ipCliente != null) {
                        h.set("X-Forwarded-For", ipCliente);
                    }
                })
                .body(datos)
                .retrieve()
                .body(RegistroResponse.class));
    }

    public EventoResumen publicar(Map<String, Object> evento, String token) {
        return conRechazos(() -> http.post().uri("/api/eventos")
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(evento)
                .retrieve()
                .body(EventoResumen.class));
    }

    public List<EventoResumen> misEventos(String token) {
        EventoResumen[] resultado = http.get().uri("/api/eventos/mios")
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .retrieve()
                .body(EventoResumen[].class);
        return resultado == null ? List.of() : Arrays.asList(resultado);
    }

    /** Reenvia el archivo al backend con un nombre fijo: el nombre original del usuario no viaja. */
    public Medio subirMedio(Long eventoId, MultipartFile archivo, String descripcion, String token) {
        HttpHeaders cabeceraArchivo = new HttpHeaders();
        cabeceraArchivo.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        cabeceraArchivo.setContentDispositionFormData("archivo", "archivo");
        MultiValueMap<String, Object> partes = new LinkedMultiValueMap<>();
        partes.add("archivo", new HttpEntity<>(archivo.getResource(), cabeceraArchivo));
        partes.add("descripcion", descripcion == null ? "" : descripcion);
        return conRechazos(() -> http.post().uri("/api/eventos/{id}/medios", eventoId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(partes)
                .retrieve()
                .body(Medio.class));
    }

    /**
     * Copia la foto o el video desde el backend hacia la respuesta del navegador, sin cargarlo entero en memoria.
     * Reenvia el header Range para que los videos se puedan adelantar.
     */
    public void transmitirMedio(Long eventoId, Long medioId, String token, String range, HttpServletResponse destino) {
        http.get().uri("/api/eventos/{id}/medios/{medio}", eventoId, medioId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .headers(h -> {
                    if (range != null && range.startsWith("bytes=")) {
                        h.set(HttpHeaders.RANGE, range);
                    }
                })
                .exchange((request, respuesta) -> {
                    int status = respuesta.getStatusCode().value();
                    if (status == 401 || status == 403 || status == 404 || status == 416) {
                        throw HttpClientErrorException.create(respuesta.getStatusCode(), "", HttpHeaders.EMPTY,
                                new byte[0], null);
                    }
                    if (status != 200 && status != 206) {
                        throw HttpServerErrorException.create(HttpStatus.BAD_GATEWAY, "", HttpHeaders.EMPTY,
                                new byte[0], null);
                    }
                    MediaType tipo = respuesta.getHeaders().getContentType();
                    if (tipo == null || !TIPOS_MEDIO.contains(tipo.getType() + "/" + tipo.getSubtype())) {
                        throw HttpClientErrorException.create(HttpStatus.NOT_FOUND, "", HttpHeaders.EMPTY,
                                new byte[0], null);
                    }
                    destino.setStatus(status);
                    destino.setContentType(tipo.getType() + "/" + tipo.getSubtype());
                    copiarHeader(respuesta.getHeaders(), destino, HttpHeaders.CONTENT_LENGTH);
                    copiarHeader(respuesta.getHeaders(), destino, HttpHeaders.CONTENT_RANGE);
                    copiarHeader(respuesta.getHeaders(), destino, HttpHeaders.ACCEPT_RANGES);
                    destino.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline");
                    destino.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=3600");
                    StreamUtils.copy(respuesta.getBody(), destino.getOutputStream());
                    return null;
                });
    }

    public void eliminarMedio(Long eventoId, Long medioId, String token) {
        http.delete().uri("/api/eventos/{id}/medios/{medio}", eventoId, medioId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .retrieve()
                .toBodilessEntity();
    }

    public Comentario comentar(Long eventoId, String texto, String token) {
        return conRechazos(() -> http.post().uri("/api/eventos/{id}/comentarios", eventoId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("texto", texto == null ? "" : texto))
                .retrieve()
                .body(Comentario.class));
    }

    public void eliminarComentario(Long eventoId, Long comentarioId, String token) {
        http.delete().uri("/api/eventos/{id}/comentarios/{comentario}", eventoId, comentarioId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .retrieve()
                .toBodilessEntity();
    }

    public void valorar(Long eventoId, int puntaje, String token) {
        conRechazos(() -> http.put().uri("/api/eventos/{id}/valoracion", eventoId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("puntaje", puntaje))
                .retrieve()
                .toBodilessEntity());
    }

    public void compartir(Long eventoId, String mensaje, String token) {
        Map<String, String> cuerpo = new LinkedHashMap<>();
        cuerpo.put("mensaje", mensaje);
        conRechazos(() -> http.post().uri("/api/eventos/{id}/compartidos", eventoId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .toBodilessEntity());
    }

    /** Convierte los rechazos con mensaje de la API en RechazoBackendException; el resto sigue igual. */
    private static <T> T conRechazos(Supplier<T> llamada) {
        try {
            return llamada.get();
        } catch (HttpClientErrorException e) {
            if (!RECHAZOS.contains(e.getStatusCode().value())) {
                throw e;
            }
            ErrorApi error = null;
            try {
                error = e.getResponseBodyAs(ErrorApi.class);
            } catch (RuntimeException ignorada) {
                // cuerpo vacio o no JSON: se usa el mensaje generico
            }
            String mensaje = error != null && error.mensaje() != null ? error.mensaje()
                    : "No se pudo completar la operación";
            throw new RechazoBackendException(e.getStatusCode().value(), mensaje, error == null ? null : error.errores());
        }
    }

    private static void copiarHeader(HttpHeaders origen, HttpServletResponse destino, String nombre) {
        String valor = origen.getFirst(nombre);
        if (valor != null) {
            destino.setHeader(nombre, valor);
        }
    }
}

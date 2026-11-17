package com.duoc.seguridadcalidad.api;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

/** Registro, publicacion de eventos, fotos y videos, comentarios, valoraciones y compartidos. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ScriptsBaseDeDatos.class)
class ComunidadApiTest {

    /** PNG de 1x1 pixel. */
    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D, 'I', 'H', 'D', 'R',
            0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89};

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    String token(String usuario, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .with(r -> { r.setRemoteAddr("10.1.1." + Math.abs(usuario.hashCode() % 200)); return r; })
                        .content("{\"username\":\"" + usuario + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(body).get("token").asText();
    }

    String json(Object valor) throws Exception {
        return mapper.writeValueAsString(valor);
    }

    Map<String, Object> eventoPresencial(String nombre) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("nombre", nombre);
        e.put("videojuego", "Tekken 8");
        e.put("categoria", "torneo");
        e.put("modalidad", "PRESENCIAL");
        e.put("pais", "Chile");
        e.put("ciudad", "Viña del Mar");
        e.put("fechaHora", LocalDateTime.now().plusDays(30).withNano(0).toString());
        e.put("ubicacion", "Centro Cultural, Calle Valparaíso 100");
        e.put("organizador", "Club Tekken Quinta");
        e.put("resumen", "Torneo abierto de Tekken 8");
        e.put("descripcion", "Brackets de doble eliminación.");
        e.put("requisitos", "Traer control.");
        e.put("inscripcion", "Gratis en el lugar.");
        e.put("cupos", 64);
        return e;
    }

    long publicar(String token, String nombre) throws Exception {
        String body = mvc.perform(post("/api/eventos").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(eventoPresencial(nombre))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoria").value("Torneo"))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asLong();
    }

    @Test
    void registroCreaCuentaUserAunqueManden() throws Exception {
        // Se intenta colar un rol ADMIN en el JSON: el DTO no tiene ese campo y la cuenta queda como USER
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .with(r -> { r.setRemoteAddr("10.2.0.1"); return r; })
                        .content("{\"nombre\":\"Camila Soto\",\"username\":\"camila\",\"email\":\"Camila@Correo.cl\","
                                + "\"password\":\"Torneos2026x\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("camila"));
        String token = token("camila@correo.cl", "Torneos2026x");
        mvc.perform(get("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    void registroRechazaClaveDebilYDuplicados() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .with(r -> { r.setRemoteAddr("10.2.0.2"); return r; })
                        .content("{\"nombre\":\"Pedro\",\"username\":\"pedro\",\"email\":\"pedro@correo.cl\","
                                + "\"password\":\"solominusculas\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .with(r -> { r.setRemoteAddr("10.2.0.2"); return r; })
                        .content("{\"nombre\":\"Otra Ana\",\"username\":\"ANA\",\"email\":\"otra@correo.cl\","
                                + "\"password\":\"Torneos2026x\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .with(r -> { r.setRemoteAddr("10.2.0.2"); return r; })
                        .content("{\"nombre\":\"<script>\",\"username\":\"x y\",\"email\":\"no-es-correo\","
                                + "\"password\":\"Torneos2026x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.username").exists())
                .andExpect(jsonPath("$.errores.email").exists());
    }

    @Test
    void registroLimitaCuentasPorIp() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                            .with(r -> { r.setRemoteAddr("10.2.0.99"); return r; })
                            .content("{\"nombre\":\"Bot\",\"username\":\"bot" + i + "\",\"email\":\"bot" + i
                                    + "@correo.cl\",\"password\":\"Torneos2026x\"}"))
                    .andExpect(status().isCreated());
        }
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .with(r -> { r.setRemoteAddr("10.2.0.99"); return r; })
                        .content("{\"nombre\":\"Bot\",\"username\":\"bot9\",\"email\":\"bot9@correo.cl\","
                                + "\"password\":\"Torneos2026x\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void publicarRequiereTokenYValidaElEnlace() throws Exception {
        mvc.perform(post("/api/eventos").contentType(MediaType.APPLICATION_JSON)
                        .content(json(eventoPresencial("Sin token"))))
                .andExpect(status().isUnauthorized());

        String ana = token("ana", "Gamer2026!");
        long id = publicar(ana, "Copa Quinta Región");
        mvc.perform(get("/api/eventos/mios").header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));

        Map<String, Object> online = eventoPresencial("Online con enlace malicioso");
        online.put("modalidad", "ONLINE");
        online.put("enlaceAcceso", "javascript:alert(document.cookie)");
        mvc.perform(post("/api/eventos").header(HttpHeaders.AUTHORIZATION, ana)
                        .contentType(MediaType.APPLICATION_JSON).content(json(online)))
                .andExpect(status().isBadRequest());

        Map<String, Object> pasado = eventoPresencial("Evento en el pasado");
        pasado.put("fechaHora", "2020-01-01T10:00:00");
        mvc.perform(post("/api/eventos").header(HttpHeaders.AUTHORIZATION, ana)
                        .contentType(MediaType.APPLICATION_JSON).content(json(pasado)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.fechaHora").exists());
    }

    @Test
    void soloElAutorSubeFotosYSeValidaElContenidoReal() throws Exception {
        String ana = token("ana", "Gamer2026!");
        String diego = token("diego", "Gamer2026!");
        long id = publicar(ana, "Evento con fotos");

        MockMultipartFile foto = new MockMultipartFile("archivo", "afiche.png", "image/png", PNG);
        String body = mvc.perform(multipart("/api/eventos/{id}/medios", id).file(foto).param("descripcion", "Afiche")
                        .header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("FOTO"))
                .andReturn().getResponse().getContentAsString();
        long medioId = mapper.readTree(body).get("id").asLong();

        // Otro usuario no puede agregar fotos a un evento ajeno
        mvc.perform(multipart("/api/eventos/{id}/medios", id).file(foto).param("descripcion", "Intruso")
                        .header(HttpHeaders.AUTHORIZATION, diego))
                .andExpect(status().isForbidden());

        // Un HTML disfrazado de PNG se rechaza por su contenido
        MockMultipartFile falso = new MockMultipartFile("archivo", "foto.png", "image/png",
                "<html><script>alert(1)</script></html>".getBytes());
        mvc.perform(multipart("/api/eventos/{id}/medios", id).file(falso).param("descripcion", "Falso")
                        .header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/eventos/{id}/medios/{m}", id, medioId).header(HttpHeaders.AUTHORIZATION, diego))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
        // El medio no se puede pedir a traves de otro evento
        mvc.perform(get("/api/eventos/{id}/medios/{m}", 1, medioId).header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/eventos/{id}/medios/{m}", id, medioId)).andExpect(status().isUnauthorized());

        mvc.perform(delete("/api/eventos/{id}/medios/{m}", id, medioId).header(HttpHeaders.AUTHORIZATION, diego))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/eventos/{id}/medios/{m}", id, medioId).header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isNoContent());
    }

    @Test
    void comentariosValoracionesYCompartidos() throws Exception {
        String ana = token("ana", "Gamer2026!");
        String diego = token("diego", "Gamer2026!");

        String body = mvc.perform(post("/api/eventos/3/comentarios").header(HttpHeaders.AUTHORIZATION, diego)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("texto", "<img src=x onerror=alert(1)> ¡Nos vemos!"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long comentarioId = mapper.readTree(body).get("id").asLong();

        mvc.perform(delete("/api/eventos/3/comentarios/{c}", comentarioId).header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isForbidden());

        mvc.perform(put("/api/eventos/3/valoracion").header(HttpHeaders.AUTHORIZATION, ana)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"puntaje\":6}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/eventos/3/valoracion").header(HttpHeaders.AUTHORIZATION, ana)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"puntaje\":2}"))
                .andExpect(status().isNoContent());
        mvc.perform(put("/api/eventos/3/valoracion").header(HttpHeaders.AUTHORIZATION, ana)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"puntaje\":5}"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/eventos/3").header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comunidad.miValoracion").value(5))
                .andExpect(jsonPath("$.comunidad.totalValoraciones").value(1))
                .andExpect(jsonPath("$.comunidad.comentarios[0].eliminable").value(false));

        mvc.perform(post("/api/eventos/3/compartidos").header(HttpHeaders.AUTHORIZATION, ana)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("mensaje", "¡Vamos a la LAN!"))))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/portada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compartidos[0].mensaje").value("¡Vamos a la LAN!"));

        mvc.perform(delete("/api/eventos/3/comentarios/{c}", comentarioId).header(HttpHeaders.AUTHORIZATION, diego))
                .andExpect(status().isNoContent());
    }
}

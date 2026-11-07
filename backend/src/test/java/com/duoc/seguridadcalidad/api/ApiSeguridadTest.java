package com.duoc.seguridadcalidad.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ScriptsBaseDeDatos.class)
class ApiSeguridadTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    String token(String usuario, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = mapper.readTree(body);
        return json.get("token").asText();
    }

    @Test
    void loginDevuelveTokenConUsuarioOCorreo() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ana@eventosgamer.cl\",\"password\":\"Gamer2026!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("USER"));
        token("ana@eventosgamer.cl", "Gamer2026!");
        token("admin", "AdminPruebas2026!");
        token("llam", "ClaveLlamPruebas1!");
    }

    ResultActions login(String usuario, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .with(r -> { r.setRemoteAddr("10.0.0." + Math.abs(usuario.hashCode() % 200)); return r; })
                .content("{\"username\":\"" + usuario + "\",\"password\":\"" + password + "\"}"));
    }

    @Test
    void loginFallidoNoRevelaSiElUsuarioExiste() throws Exception {
        login("ana", "otra").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
        login("no-existe", "otra").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void adminSinClaveDelEntornoNoUsaLaDelScript() throws Exception {
        login("admin", "Admin2026!").andExpect(status().isUnauthorized());
    }

    @Test
    void bloqueaTrasCincoIntentosFallidos() throws Exception {
        for (int i = 0; i < 5; i++) {
            login("diego", "incorrecta-" + i).andExpect(status().isUnauthorized());
        }
        // Bloqueado aunque ahora la clave sea la correcta
        login("diego", "Gamer2026!").andExpect(status().isTooManyRequests());
    }

    @Test
    void portadaYBusquedaSonPublicas() throws Exception {
        mvc.perform(get("/api/portada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recientes", hasSize(4)))
                .andExpect(jsonPath("$.banners", hasSize(3)));
        mvc.perform(get("/api/eventos").param("modalidad", "ONLINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
        mvc.perform(get("/api/eventos").param("lugar", "santiago").param("categoria", "Torneo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Torneo Universitario de Esports"));
        mvc.perform(get("/api/eventos").param("nombre", "copa").param("desde", "2026-11-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/eventos/filtros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paises", hasSize(4)));
    }

    @Test
    void busquedaConInyeccionSqlNoDevuelveDatos() throws Exception {
        mvc.perform(get("/api/eventos").param("nombre", "' OR '1'='1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void detalleExigeToken() throws Exception {
        mvc.perform(get("/api/eventos/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/eventos/1").header("Authorization", "Bearer token.invalido.x"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/eventos/1").header("Authorization", "Bearer " + token("ana", "Gamer2026!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizador").value("Asociación Gamer Chile"))
                .andExpect(jsonPath("$.fotos", hasSize(2)));
    }

    @Test
    void apiIncluyeCabecerasDeSeguridad() throws Exception {
        mvc.perform(get("/api/portada"))
                .andExpect(header().string("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));
    }

    @Test
    void soloAdminVeUsuarios() throws Exception {
        mvc.perform(get("/api/admin/usuarios").header("Authorization", "Bearer " + token("ana", "Gamer2026!")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/usuarios").header("Authorization", "Bearer " + token("admin", "AdminPruebas2026!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("ana"));
    }

    @Test
    void perfilDelUsuarioAutenticado() throws Exception {
        mvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token("ana@eventosgamer.cl", "Gamer2026!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@eventosgamer.cl"));
    }
}

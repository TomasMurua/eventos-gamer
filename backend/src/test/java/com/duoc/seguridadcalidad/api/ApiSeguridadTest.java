package com.duoc.seguridadcalidad.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        token("diego", "Gamer2026!");
        token("admin", "Admin2026!");
        token("llam", "ClaveLlamPruebas1!");
    }

    @Test
    void loginConClaveIncorrectaResponde401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ana\",\"password\":\"otra\"}"))
                .andExpect(status().isUnauthorized());
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
    void soloAdminVeUsuarios() throws Exception {
        mvc.perform(get("/api/admin/usuarios").header("Authorization", "Bearer " + token("ana", "Gamer2026!")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/usuarios").header("Authorization", "Bearer " + token("admin", "Admin2026!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("ana"));
    }

    @Test
    void perfilDelUsuarioAutenticado() throws Exception {
        mvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token("diego", "Gamer2026!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("diego@eventosgamer.cl"));
    }
}

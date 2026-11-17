package com.duoc.seguridadcalidad.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/** Cuota de espacio por usuario para fotos y videos, con una cuota pequena para la prueba. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ScriptsBaseDeDatos.class)
@TestPropertySource(properties = "app.medios.cuota-usuario-bytes=50")
class CuotaMediosTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void noPermiteSuperarElEspacioDelUsuario() throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ana\",\"password\":\"Gamer2026!\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = "Bearer " + mapper.readTree(body).get("token").asString();

        // ana es la autora del evento 2 en los datos iniciales
        MockMultipartFile foto = new MockMultipartFile("archivo", "a.png", "image/png", ComunidadApiTest.PNG);
        mvc.perform(multipart("/api/eventos/2/medios").file(foto).param("descripcion", "Primera")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isCreated());
        mvc.perform(multipart("/api/eventos/2/medios").file(foto).param("descripcion", "Segunda")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.startsWith("Alcanzaste el espacio máximo")));
    }
}

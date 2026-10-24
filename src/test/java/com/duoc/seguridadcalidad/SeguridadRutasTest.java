package com.duoc.seguridadcalidad;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SeguridadRutasTest {

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {"/", "/home", "/recetas", "/registro", "/login", "/css/style.css"})
    void rutasPublicasNoPidenLogin(String ruta) throws Exception {
        mvc.perform(get(ruta)).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/perfil", "/eventos", "/eventos/1"})
    void rutasPrivadasRedirigenAlLogin(String ruta) throws Exception {
        mvc.perform(get(ruta))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@eventosgamer.cl", "diego@eventosgamer.cl"})
    void usuariosDePruebaPuedenIngresar(String correo) throws Exception {
        mvc.perform(formLogin("/login").user(correo).password("Gamer2026!"))
                .andExpect(authenticated().withUsername(correo))
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    void adminPuedeIngresar() throws Exception {
        mvc.perform(formLogin("/login").user("admin@eventosgamer.cl").password("Admin2026!"))
                .andExpect(authenticated().withRoles("USER", "ADMIN"));
    }

    @Test
    void adminLlamNoExisteSinVariableDeEntorno() throws Exception {
        mvc.perform(formLogin("/login").user("llam").password("cualquier-clave"))
                .andExpect(unauthenticated());
    }

    @Test
    void contrasenaIncorrectaEsRechazada() throws Exception {
        mvc.perform(formLogin("/login").user("ana@eventosgamer.cl").password("incorrecta"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void usuarioAutenticadoVeEventosYDetalle() throws Exception {
        mvc.perform(get("/eventos").with(user("ana@eventosgamer.cl")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Santiago Gaming Fest 2026")));
        mvc.perform(get("/eventos/1").with(user("ana@eventosgamer.cl")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Estacionamiento")));
    }

    @Test
    void busquedaFiltraPorCategoriaYCiudad() throws Exception {
        mvc.perform(get("/eventos").param("categoria", "Torneo").param("ciudad", "Valparaiso")
                        .with(user("ana@eventosgamer.cl")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Copa Valparaiso")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Santiago Gaming Fest"))));
    }

    @Test
    void eventoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/eventos/999").with(user("ana@eventosgamer.cl")))
                .andExpect(status().isNotFound());
    }

    @Test
    void registroPermiteIngresarConLaNuevaCuenta() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Nuevo Gamer")
                        .param("correo", "Nuevo@Correo.cl")
                        .param("password", "ClaveSegura1")
                        .param("confirmarPassword", "ClaveSegura1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registrado="));

        mvc.perform(formLogin("/login").user("nuevo@correo.cl").password("ClaveSegura1"))
                .andExpect(authenticated().withUsername("nuevo@correo.cl"));
    }

    @Test
    void registroRechazaCorreoDuplicadoYClavesDistintas() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Ana")
                        .param("correo", "ana@eventosgamer.cl")
                        .param("password", "ClaveSegura1")
                        .param("confirmarPassword", "OtraClave22"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ya existe una cuenta con ese correo")))
                .andExpect(content().string(containsString("Las contrasenas no coinciden")));
    }

    @Test
    void postSinTokenCsrfEsRechazado() throws Exception {
        mvc.perform(post("/registro")
                        .param("nombre", "Sin Token")
                        .param("correo", "sintoken@correo.cl")
                        .param("password", "ClaveSegura1")
                        .param("confirmarPassword", "ClaveSegura1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void perfilSeGuardaParaElUsuarioAutenticado() throws Exception {
        mvc.perform(post("/perfil").with(csrf()).with(user("diego@eventosgamer.cl"))
                        .param("nombreUsuario", "diego_pro")
                        .param("avatar", "avatar-3")
                        .param("juegosInteres", "Valorant", "Tekken 8"))
                .andExpect(redirectedUrl("/perfil?guardado="));

        mvc.perform(get("/perfil").with(user("diego@eventosgamer.cl")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("@diego_pro")));
    }

    @Test
    void perfilRechazaAvatarNoPermitido() throws Exception {
        mvc.perform(post("/perfil").with(csrf()).with(user("diego@eventosgamer.cl"))
                        .param("nombreUsuario", "diego_pro")
                        .param("avatar", "javascript:alert(1)"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Elige uno de los avatares disponibles")));
    }

    @Test
    void respuestasIncluyenCabecerasDeSeguridad() throws Exception {
        mvc.perform(get("/home"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")));
    }
}

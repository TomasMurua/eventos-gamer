package com.duoc.seguridadcalidad;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
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

import com.duoc.seguridadcalidad.backend.BackendClient;
import com.duoc.seguridadcalidad.backend.Dtos.Banner;
import com.duoc.seguridadcalidad.backend.Dtos.EventoDetalle;
import com.duoc.seguridadcalidad.backend.Dtos.EventoResumen;
import com.duoc.seguridadcalidad.backend.Dtos.Filtros;
import com.duoc.seguridadcalidad.backend.Dtos.Foto;
import com.duoc.seguridadcalidad.backend.Dtos.LoginResponse;
import com.duoc.seguridadcalidad.backend.Dtos.Portada;
import com.duoc.seguridadcalidad.backend.Dtos.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SeguridadRutasTest {

    static final EventoResumen FEST = new EventoResumen(1L, "Santiago Gaming Fest 2026", "Varios", "Convención",
            "PRESENCIAL", "Chile", "Santiago", LocalDateTime.of(2026, 11, 14, 10, 0), "La convención gamer",
            980, true, "/img/eventos/evento-1-a.svg");

    static final EventoDetalle DETALLE = new EventoDetalle(1L, "Santiago Gaming Fest 2026", "Varios", "Convención",
            "PRESENCIAL", "Chile", "Santiago", LocalDateTime.of(2026, 11, 14, 10, 0), "Espacio Riesco", null,
            "Asociación Gamer Chile", "Tres pabellones", "Mayores de 12 años", "Entradas en boletería", 4000, 980,
            List.of(new Foto("/img/eventos/evento-1-a.svg", "Pabellón principal")));

    @Autowired
    MockMvc mvc;

    @MockitoBean
    BackendClient backend;

    @BeforeEach
    void datosDelBackend() {
        when(backend.portada()).thenReturn(new Portada(List.of(FEST), List.of(FEST), List.of(FEST),
                List.of(new Banner("NeonByte", "Periféricos para ganar", "Periféricos"))));
        when(backend.filtros()).thenReturn(new Filtros(List.of("Varios"), List.of("Convención"), List.of("Chile"),
                List.of("Santiago")));
        when(backend.buscar(anyMap())).thenReturn(List.of(FEST));
        when(backend.login(anyString(), anyString())).thenReturn(Optional.empty());
        when(backend.login(eq("ana@eventosgamer.cl"), eq("Gamer2026!")))
                .thenReturn(Optional.of(new LoginResponse("jwt-ana", "Bearer", 1800, "ana", "Ana Rojas", "USER")));
        when(backend.login(eq("admin"), eq("Admin2026!")))
                .thenReturn(Optional.of(new LoginResponse("jwt-admin", "Bearer", 1800, "admin", "Admin", "ADMIN")));
        when(backend.detalle(eq(1L), any())).thenReturn(Optional.of(DETALLE));
        when(backend.detalle(eq(999L), any())).thenReturn(Optional.empty());
        when(backend.yo(any())).thenReturn(new Usuario("ana", "ana@eventosgamer.cl", "Ana Rojas", "USER", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/eventos", "/", "/home", "/recetas", "/eventos/buscar", "/login", "/css/style.css"})
    void rutasPublicasNoPidenLogin(String ruta) throws Exception {
        mvc.perform(get(ruta)).andExpect(status().isOk());
    }

    @Test
    void inicioMuestraEventosYAvisos() throws Exception {
        mvc.perform(get("/eventos"))
                .andExpect(content().string(containsString("Santiago Gaming Fest 2026")))
                .andExpect(content().string(containsString("NeonByte")))
                .andExpect(content().string(containsString("Iniciar sesión")));
    }

    @Test
    void busquedaPublicaMuestraResultados() throws Exception {
        mvc.perform(get("/eventos/buscar").param("nombre", "fest").param("modalidad", "PRESENCIAL"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1 evento")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/eventos/1", "/perfil", "/admin/usuarios"})
    void rutasPrivadasRedirigenAlLogin(String ruta) throws Exception {
        mvc.perform(get(ruta))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginContraElBackend() throws Exception {
        mvc.perform(formLogin("/login").user("ana@eventosgamer.cl").password("Gamer2026!"))
                .andExpect(authenticated().withUsername("ana").withRoles("USER"))
                .andExpect(redirectedUrl("/eventos"));
        mvc.perform(formLogin("/login").user("admin").password("Admin2026!"))
                .andExpect(authenticated().withRoles("ADMIN"));
    }

    @Test
    void contrasenaIncorrectaEsRechazada() throws Exception {
        mvc.perform(formLogin("/login").user("ana@eventosgamer.cl").password("incorrecta"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void usuarioAutenticadoVeElDetalle() throws Exception {
        mvc.perform(get("/eventos/1").with(user("ana")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mayores de 12 años")))
                .andExpect(content().string(containsString("evento-1-a.svg")));
    }

    @Test
    void eventoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/eventos/999").with(user("ana"))).andExpect(status().isNotFound());
    }

    @Test
    void usuarioSinRolAdminNoVeUsuarios() throws Exception {
        mvc.perform(get("/admin/usuarios").with(user("ana").roles("USER"))).andExpect(status().isForbidden());
    }

    @Test
    void logoutSinTokenCsrfEsRechazado() throws Exception {
        mvc.perform(post("/logout").with(user("ana"))).andExpect(status().isForbidden());
    }

    @Test
    void respuestasIncluyenCabecerasDeSeguridad() throws Exception {
        mvc.perform(get("/eventos"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")));
    }
}

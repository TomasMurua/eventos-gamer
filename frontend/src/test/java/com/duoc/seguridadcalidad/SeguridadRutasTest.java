package com.duoc.seguridadcalidad;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
import com.duoc.seguridadcalidad.security.SesionUsuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.client.HttpClientErrorException;
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
        when(backend.login(anyString(), anyString(), any())).thenReturn(Optional.empty());
        when(backend.login(eq("ana@eventosgamer.cl"), eq("Gamer2026!"), any()))
                .thenReturn(Optional.of(new LoginResponse("jwt-ana", "Bearer", 1800, "ana", "Ana Rojas", "USER")));
        when(backend.login(eq("admin"), eq("Admin2026!"), any()))
                .thenReturn(Optional.of(new LoginResponse("jwt-admin", "Bearer", 1800, "admin", "Admin", "ADMIN")));
        when(backend.detalle(eq(1L), any())).thenReturn(Optional.of(DETALLE));
        when(backend.detalle(eq(999L), any())).thenReturn(Optional.empty());
        when(backend.yo(any())).thenReturn(new Usuario("ana", "ana@eventosgamer.cl", "Ana Rojas", "USER", null));
    }

    static RequestPostProcessor sesion(String usuario, String rol) {
        var principal = new SesionUsuario(usuario, usuario, rol, "jwt-" + usuario);
        return authentication(new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
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
        mvc.perform(get("/eventos/1").with(sesion("ana", "USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mayores de 12 años")))
                .andExpect(content().string(containsString("evento-1-a.svg")));
    }

    @Test
    void eventoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/eventos/999").with(sesion("ana", "USER"))).andExpect(status().isNotFound());
    }

    @Test
    void cadaSesionUsaSuPropioToken() throws Exception {
        mvc.perform(get("/eventos/1").with(sesion("ana", "USER"))).andExpect(status().isOk());
        mvc.perform(get("/eventos/1").with(sesion("diego", "USER"))).andExpect(status().isOk());
        verify(backend).detalle(1L, "jwt-ana");
        verify(backend).detalle(1L, "jwt-diego");
    }

    @Test
    void loginNoGuardaLaContrasena() throws Exception {
        mvc.perform(formLogin("/login").user("ana@eventosgamer.cl").password("Gamer2026!"))
                .andExpect(authenticated().withAuthentication(a -> {
                    org.assertj.core.api.Assertions.assertThat(a.getCredentials()).isNull();
                    org.assertj.core.api.Assertions.assertThat(((SesionUsuario) a.getPrincipal()).token()).isEqualTo("jwt-ana");
                }));
    }

    @Test
    void tokenExpiradoCierraLaSesion() throws Exception {
        when(backend.yo("jwt-ana")).thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "401",
                null, null, null));
        mvc.perform(get("/perfil").with(sesion("ana", "USER")))
                .andExpect(redirectedUrl("/login?expirada"));
    }

    @Test
    void filtrosInvalidosRespondenBadRequestSinError500() throws Exception {
        mvc.perform(get("/eventos/buscar").param("nombre", "A".repeat(200)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("admite hasta 80 caracteres")));
        mvc.perform(get("/eventos/buscar").param("desde", "ZAP%n%s%n%s"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/eventos/buscar").param("modalidad", "<script>"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("<script>"))));
    }

    @Test
    void errorDelBackendNoSeConvierteEn500() throws Exception {
        when(backend.buscar(anyMap())).thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "400",
                null, null, null));
        mvc.perform(get("/eventos/buscar").param("nombre", "copa")).andExpect(status().isBadRequest());
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
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("Permissions-Policy", containsString("camera=()")));
        mvc.perform(get("/eventos").secure(true))
                .andExpect(header().string("Strict-Transport-Security", containsString("max-age=31536000")));
    }
}

package com.duoc.seguridadcalidad;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.duoc.seguridadcalidad.backend.BackendClient;
import com.duoc.seguridadcalidad.backend.Dtos.EventoResumen;
import com.duoc.seguridadcalidad.backend.Dtos.Filtros;
import com.duoc.seguridadcalidad.backend.Dtos.RegistroResponse;
import com.duoc.seguridadcalidad.backend.RechazoBackendException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Registro, publicacion, fotos y videos, comentarios, valoraciones y compartir desde las paginas. */
@SpringBootTest
@AutoConfigureMockMvc
class ComunidadWebTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    BackendClient backend;

    @BeforeEach
    void datos() {
        when(backend.filtros()).thenReturn(new Filtros(List.of(), List.of(), List.of(), List.of(),
                List.of("Convención", "Torneo")));
        when(backend.detalle(eq(1L), any())).thenReturn(Optional.of(SeguridadRutasTest.DETALLE));
        when(backend.detalle(eq(2L), any())).thenReturn(Optional.of(SeguridadRutasTest.PROPIO));
    }

    @Test
    void registroExitosoLlevaAlLogin() throws Exception {
        when(backend.registrar(anyMap(), any())).thenReturn(new RegistroResponse("camila", "Camila Soto"));
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Camila Soto").param("username", "camila").param("email", "camila@correo.cl")
                        .param("password", "Torneos2026x").param("confirmacion", "Torneos2026x"))
                .andExpect(redirectedUrl("/login?registrado"));
    }

    @Test
    void registroConErroresNoDevuelveLaContrasena() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Camila").param("username", "camila").param("email", "camila@correo.cl")
                        .param("password", "ClaveSecreta123").param("confirmacion", "OtraClave123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Las contraseñas no coinciden")))
                .andExpect(content().string(not(containsString("ClaveSecreta123"))));
        verify(backend, never()).registrar(anyMap(), any());

        when(backend.registrar(anyMap(), any())).thenThrow(new RechazoBackendException(409,
                "El nombre de usuario o el correo ya están registrados", null));
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Ana").param("username", "ana").param("email", "ana@correo.cl")
                        .param("password", "Torneos2026x").param("confirmacion", "Torneos2026x"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ya están registrados")));
    }

    @Test
    void formulariosExigenTokenCsrf() throws Exception {
        mvc.perform(post("/registro").param("nombre", "x")).andExpect(status().isForbidden());
        mvc.perform(post("/eventos/1/comentarios").with(SeguridadRutasTest.sesion("ana", "USER"))
                .param("texto", "hola")).andExpect(status().isForbidden());
        verify(backend, never()).comentar(any(), anyString(), anyString());
    }

    @Test
    void publicarEventoValidoRedirigeAlDetalle() throws Exception {
        when(backend.publicar(anyMap(), eq("jwt-ana"))).thenReturn(new EventoResumen(11L, "Copa Quinta", "Tekken 8",
                "Torneo", "PRESENCIAL", "Chile", "Viña del Mar", LocalDateTime.now().plusDays(10), "Torneo", 0, false, null));
        mvc.perform(post("/eventos/nuevo").with(csrf()).with(SeguridadRutasTest.sesion("ana", "USER"))
                        .param("nombre", "Copa Quinta").param("videojuego", "Tekken 8").param("categoria", "Torneo")
                        .param("modalidad", "PRESENCIAL").param("pais", "Chile").param("ciudad", "Viña del Mar")
                        .param("fechaHora", LocalDateTime.now().plusDays(10).withSecond(0).withNano(0).toString())
                        .param("ubicacion", "Centro Cultural").param("organizador", "Club Tekken")
                        .param("resumen", "Torneo abierto").param("descripcion", "Brackets")
                        .param("requisitos", "Traer control").param("inscripcion", "En el lugar"))
                .andExpect(redirectedUrl("/eventos/11"));
    }

    @Test
    void publicarRechazaEnlaceQueNoEsHttps() throws Exception {
        mvc.perform(post("/eventos/nuevo").with(csrf()).with(SeguridadRutasTest.sesion("ana", "USER"))
                        .param("nombre", "Online").param("videojuego", "Valorant").param("categoria", "Torneo")
                        .param("modalidad", "ONLINE").param("pais", "Chile")
                        .param("fechaHora", LocalDateTime.now().plusDays(10).withSecond(0).withNano(0).toString())
                        .param("enlaceAcceso", "javascript:alert(1)").param("organizador", "Club")
                        .param("resumen", "Torneo").param("descripcion", "Online")
                        .param("requisitos", "PC").param("inscripcion", "Web"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El enlace debe empezar con https://")));
        verify(backend, never()).publicar(anyMap(), anyString());
    }

    @Test
    void detalleEscapaComentariosYMuestraRedes() throws Exception {
        mvc.perform(get("/eventos/1").with(SeguridadRutasTest.sesion("diego", "USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;img src=x onerror=alert(1)&gt;")))
                .andExpect(content().string(not(containsString("<img src=x"))))
                .andExpect(content().string(containsString("https://wa.me/")))
                .andExpect(content().string(containsString("4.5 de 5")))
                // diego no es el autor: no ve el formulario para subir archivos
                .andExpect(content().string(not(containsString("multipart/form-data"))));
    }

    @Test
    void autorVeElFormularioDeArchivosYSubeUnaFoto() throws Exception {
        mvc.perform(get("/eventos/2").with(SeguridadRutasTest.sesion("ana", "USER")))
                .andExpect(content().string(containsString("multipart/form-data")))
                .andExpect(content().string(containsString("/eventos/2/medios/5")));

        MockMultipartFile foto = new MockMultipartFile("archivo", "../../etc/passwd.png", "image/png", new byte[] {1, 2});
        mvc.perform(multipart("/eventos/2/medios").file(foto).param("descripcion", "Afiche")
                        .with(csrf()).with(SeguridadRutasTest.sesion("ana", "USER")))
                .andExpect(redirectedUrl("/eventos/2#medios"));
        verify(backend).subirMedio(eq(2L), any(), eq("Afiche"), eq("jwt-ana"));
    }

    @Test
    void archivoDemasiadoGrandeResponde413EnVezDe403() throws Exception {
        // El token CSRF no se alcanza a leer de un multipart que supera el limite
        mvc.perform(post("/eventos/2/medios").with(SeguridadRutasTest.sesion("ana", "USER"))
                        .contentType("multipart/form-data; boundary=limite")
                        .content(new byte[9 * 1024 * 1024]))
                .andExpect(status().isPayloadTooLarge());
        // Un formulario normal sin token sigue siendo 403
        mvc.perform(post("/eventos/2/medios").with(SeguridadRutasTest.sesion("ana", "USER"))
                        .contentType("multipart/form-data; boundary=limite")
                        .content(new byte[100]))
                .andExpect(status().isForbidden());
        verify(backend, never()).subirMedio(any(), any(), anyString(), anyString());
    }

    @Test
    void comentarValorarYCompartirUsanElTokenDeLaSesion() throws Exception {
        mvc.perform(post("/eventos/1/comentarios").with(csrf()).with(SeguridadRutasTest.sesion("ana", "USER"))
                .param("texto", "¡Nos vemos!")).andExpect(redirectedUrl("/eventos/1#comentarios"));
        verify(backend).comentar(1L, "¡Nos vemos!", "jwt-ana");

        mvc.perform(post("/eventos/1/valoracion").with(csrf()).with(SeguridadRutasTest.sesion("ana", "USER"))
                .param("puntaje", "9")).andExpect(redirectedUrl("/eventos/1#valoracion"));
        verify(backend, never()).valorar(any(), eq(9), anyString());
        mvc.perform(post("/eventos/1/valoracion").with(csrf()).with(SeguridadRutasTest.sesion("ana", "USER"))
                .param("puntaje", "4")).andExpect(redirectedUrl("/eventos/1#valoracion"));
        verify(backend).valorar(1L, 4, "jwt-ana");

        mvc.perform(post("/eventos/1/compartir").with(csrf()).with(SeguridadRutasTest.sesion("ana", "USER"))
                .param("mensaje", "Vamos")).andExpect(redirectedUrl("/eventos/1#compartir"));
        verify(backend).compartir(1L, "Vamos", "jwt-ana");
    }

    @Test
    void misEventosListaLoPublicado() throws Exception {
        when(backend.misEventos("jwt-ana")).thenReturn(List.of(new EventoResumen(2L, "Copa Valparaíso",
                "Street Fighter 6", "Torneo", "PRESENCIAL", "Chile", "Valparaíso", LocalDateTime.now().plusDays(4),
                "Torneo abierto", 640, true, null)));
        mvc.perform(get("/eventos/mios").with(SeguridadRutasTest.sesion("ana", "USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Copa Valparaíso")))
                .andExpect(content().string(containsString("Publicaste 1 evento")));
        verify(backend, never()).misEventos("jwt-diego");
    }
}

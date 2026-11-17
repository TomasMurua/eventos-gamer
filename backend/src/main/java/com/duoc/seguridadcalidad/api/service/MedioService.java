package com.duoc.seguridadcalidad.api.service;

import com.duoc.seguridadcalidad.api.error.SolicitudInvalidaException;
import com.duoc.seguridadcalidad.api.model.Evento;
import com.duoc.seguridadcalidad.api.model.MedioEvento;
import com.duoc.seguridadcalidad.api.model.Usuario;
import com.duoc.seguridadcalidad.api.repository.EventoRepository;
import com.duoc.seguridadcalidad.api.repository.MedioEventoRepository;
import com.duoc.seguridadcalidad.api.repository.UsuarioRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Fotos y videos de los eventos. Solo el autor del evento (o un ADMIN) puede subirlos o borrarlos.
 * Los archivos se guardan fuera del codigo, en un directorio propio, con un nombre aleatorio que genera
 * el servidor: el nombre que manda el usuario nunca se usa para armar rutas.
 */
@Service
public class MedioService {

    static final int MAXIMO_POR_EVENTO = 12;

    private static final Logger log = LoggerFactory.getLogger("seguridad");
    private static final Pattern NOMBRE_ARCHIVO = Pattern.compile("^[0-9a-f-]{36}\\.(jpg|png|webp|mp4|webm)$");

    /** Archivo listo para enviar al cliente. */
    public record ArchivoMedio(Resource recurso, String contentType, String nombreDescarga) {
    }

    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final MedioEventoRepository medios;
    private final Path directorio;
    private final long maximoFoto;
    private final long maximoVideo;

    public MedioService(EventoRepository eventos, UsuarioRepository usuarios, MedioEventoRepository medios,
                        @Value("${app.medios.directorio}") String directorio,
                        @Value("${app.medios.max-foto-bytes:5242880}") long maximoFoto,
                        @Value("${app.medios.max-video-bytes:20971520}") long maximoVideo) throws IOException {
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.medios = medios;
        this.directorio = Files.createDirectories(Path.of(directorio)).toAbsolutePath().normalize();
        this.maximoFoto = maximoFoto;
        this.maximoVideo = maximoVideo;
    }

    @Transactional
    public MedioEvento subir(Long eventoId, String username, MultipartFile archivo, String descripcion) {
        Evento evento = eventos.findById(eventoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Usuario usuario = usuarios.findByUsername(username).orElseThrow(() -> new AccessDeniedException("sin usuario"));
        exigirPermiso(evento, usuario, "subir medio");

        if (medios.countByEventoId(eventoId) >= MAXIMO_POR_EVENTO) {
            throw new SolicitudInvalidaException("El evento ya tiene el máximo de " + MAXIMO_POR_EVENTO + " fotos y videos");
        }
        if (archivo == null || archivo.isEmpty()) {
            throw new SolicitudInvalidaException("Selecciona una foto o un video");
        }
        String texto = Textos.limpiar(descripcion);
        if (texto == null || texto.length() > 160) {
            throw new SolicitudInvalidaException("La descripción es obligatoria y admite hasta 160 caracteres");
        }

        FormatoMedio formato = FormatoMedio.detectar(cabecera(archivo)).orElseThrow(() ->
                new SolicitudInvalidaException("Solo se aceptan fotos JPG, PNG o WebP y videos MP4 o WebM"));
        long maximo = formato.tipo == MedioEvento.Tipo.FOTO ? maximoFoto : maximoVideo;
        if (archivo.getSize() > maximo) {
            throw new SolicitudInvalidaException("El archivo supera el máximo de " + (maximo / (1024 * 1024)) + " MB");
        }

        String nombre = UUID.randomUUID() + "." + formato.extension;
        Path destino = rutaSegura(nombre);
        try (InputStream entrada = archivo.getInputStream()) {
            Files.copy(entrada, destino);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo", e);
        }
        try {
            MedioEvento medio = medios.save(new MedioEvento(evento, usuario, formato.tipo, nombre, formato.contentType,
                    archivo.getSize(), texto, LocalDateTime.now()));
            log.info("Medio subido: evento={} medio={} tipo={} bytes={} usuario={}", eventoId, medio.getId(),
                    formato.tipo, archivo.getSize(), usuario.getUsername());
            return medio;
        } catch (RuntimeException e) {
            borrarArchivo(nombre);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ArchivoMedio leer(Long eventoId, Long medioId) {
        MedioEvento medio = medios.findByIdAndEventoId(medioId, eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Path ruta = rutaSegura(medio.getArchivo());
        if (!Files.isRegularFile(ruta)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        String contentType = FormatoMedio.porContentType(medio.getContentType())
                .map(f -> f.contentType).orElse("application/octet-stream");
        return new ArchivoMedio(new FileSystemResource(ruta), contentType, "medio-" + medio.getId() + extension(medio));
    }

    @Transactional
    public void eliminar(Long eventoId, Long medioId, String username) {
        MedioEvento medio = medios.findByIdAndEventoId(medioId, eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Usuario usuario = usuarios.findByUsername(username).orElseThrow(() -> new AccessDeniedException("sin usuario"));
        exigirPermiso(medio.getEvento(), usuario, "eliminar medio");
        medios.delete(medio);
        medios.flush();
        borrarArchivo(medio.getArchivo());
        log.info("Medio eliminado: evento={} medio={} usuario={}", eventoId, medioId, usuario.getUsername());
    }

    private static void exigirPermiso(Evento evento, Usuario usuario, String accion) {
        if (!evento.esAutor(usuario) && !usuario.esAdmin()) {
            log.warn("Permiso denegado para {}: evento={} usuario={}", accion, evento.getId(), usuario.getUsername());
            throw new AccessDeniedException("Solo el autor del evento puede modificar sus fotos y videos");
        }
    }

    /** Lee los primeros bytes para reconocer el formato real del archivo. */
    private static byte[] cabecera(MultipartFile archivo) {
        try (InputStream entrada = archivo.getInputStream()) {
            return entrada.readNBytes(64);
        } catch (IOException e) {
            throw new SolicitudInvalidaException("No se pudo leer el archivo");
        }
    }

    /** Solo acepta nombres generados por el servidor y verifica que la ruta final quede dentro del directorio. */
    private Path rutaSegura(String nombre) {
        if (!NOMBRE_ARCHIVO.matcher(nombre).matches()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Path ruta = directorio.resolve(nombre).normalize();
        if (!ruta.startsWith(directorio)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ruta;
    }

    private void borrarArchivo(String nombre) {
        try {
            Files.deleteIfExists(rutaSegura(nombre));
        } catch (IOException e) {
            log.warn("No se pudo borrar el archivo {}", nombre);
        }
    }

    private static String extension(MedioEvento medio) {
        String archivo = medio.getArchivo();
        return archivo.substring(archivo.lastIndexOf('.'));
    }
}

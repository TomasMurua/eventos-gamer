package com.duoc.seguridadcalidad.api.service;

import com.duoc.seguridadcalidad.api.dto.CompartidoDto;
import com.duoc.seguridadcalidad.api.dto.EventoDetalle.ComentarioDto;
import com.duoc.seguridadcalidad.api.dto.EventoDetalle.Comunidad;
import com.duoc.seguridadcalidad.api.error.ConflictoException;
import com.duoc.seguridadcalidad.api.error.DemasiadasSolicitudesException;
import com.duoc.seguridadcalidad.api.error.SolicitudInvalidaException;
import com.duoc.seguridadcalidad.api.model.Comentario;
import com.duoc.seguridadcalidad.api.model.Compartido;
import com.duoc.seguridadcalidad.api.model.Evento;
import com.duoc.seguridadcalidad.api.model.Usuario;
import com.duoc.seguridadcalidad.api.model.Valoracion;
import com.duoc.seguridadcalidad.api.repository.ComentarioRepository;
import com.duoc.seguridadcalidad.api.repository.CompartidoRepository;
import com.duoc.seguridadcalidad.api.repository.EventoRepository;
import com.duoc.seguridadcalidad.api.repository.UsuarioRepository;
import com.duoc.seguridadcalidad.api.repository.ValoracionRepository;
import com.duoc.seguridadcalidad.api.security.LimiteFrecuencia;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Comentarios, valoraciones y eventos compartidos en el muro del sitio. */
@Service
public class ComunidadService {

    private static final Logger log = LoggerFactory.getLogger("seguridad");

    /** Maximo de comentarios por usuario por minuto, para frenar el spam. */
    static final int COMENTARIOS_POR_MINUTO = 5;
    /** El detalle muestra los comentarios mas recientes hasta este numero. */
    static final int COMENTARIOS_VISIBLES = 100;

    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final ComentarioRepository comentarios;
    private final ValoracionRepository valoraciones;
    private final CompartidoRepository compartidos;
    private final LimiteFrecuencia limites;

    public ComunidadService(EventoRepository eventos, UsuarioRepository usuarios, ComentarioRepository comentarios,
                            ValoracionRepository valoraciones, CompartidoRepository compartidos,
                            LimiteFrecuencia limites) {
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.comentarios = comentarios;
        this.valoraciones = valoraciones;
        this.compartidos = compartidos;
        this.limites = limites;
    }

    @Transactional
    public ComentarioDto comentar(Long eventoId, String username, String texto) {
        String limpio = Textos.limpiar(texto);
        if (limpio == null) {
            throw new SolicitudInvalidaException("El comentario no puede estar vacío");
        }
        String clave = "comentario:" + username;
        if (!limites.intentar(clave, COMENTARIOS_POR_MINUTO, Duration.ofMinutes(1))) {
            log.warn("Limite de comentarios alcanzado: usuario={}", username);
            throw new DemasiadasSolicitudesException("Estás comentando muy seguido. Espera un minuto.");
        }
        Comentario c = comentarios.save(new Comentario(evento(eventoId), usuario(username), limpio, LocalDateTime.now()));
        return new ComentarioDto(c.getId(), username, c.getTexto(), c.getCreadoEn(), true);
    }

    /** El autor del comentario o un ADMIN (moderacion) pueden borrarlo. */
    @Transactional
    public void eliminarComentario(Long eventoId, Long comentarioId, String username) {
        Comentario c = comentarios.findByIdAndEventoId(comentarioId, eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Usuario usuario = usuario(username);
        if (!c.getUsuario().getId().equals(usuario.getId()) && !usuario.esAdmin()) {
            log.warn("Intento de borrar comentario ajeno: comentario={} usuario={}", comentarioId, username);
            throw new AccessDeniedException("Solo puedes borrar tus comentarios");
        }
        comentarios.delete(c);
    }

    /** Crea o cambia la valoracion del usuario (una por evento). */
    @Transactional
    public void valorar(Long eventoId, String username, int puntaje) {
        Evento evento = evento(eventoId);
        Usuario usuario = usuario(username);
        valoraciones.findByEventoIdAndUsuarioId(eventoId, usuario.getId()).ifPresentOrElse(
                v -> v.cambiar(puntaje, LocalDateTime.now()),
                () -> valoraciones.save(new Valoracion(evento, usuario, puntaje, LocalDateTime.now())));
    }

    /** Cada usuario puede compartir un mismo evento una sola vez en el muro (evita llenar la portada). */
    @Transactional
    public CompartidoDto compartir(Long eventoId, String username, String mensaje) {
        Evento evento = evento(eventoId);
        Usuario usuario = usuario(username);
        if (compartidos.existsByEventoIdAndUsuarioId(eventoId, usuario.getId())) {
            throw new ConflictoException("Ya compartiste este evento en el muro");
        }
        Compartido c = compartidos.save(new Compartido(evento, usuario, Textos.limpiar(mensaje), LocalDateTime.now()));
        return CompartidoDto.de(c);
    }

    @Transactional(readOnly = true)
    public List<CompartidoDto> muro() {
        return compartidos.recientes(PageRequest.of(0, 6)).stream().map(CompartidoDto::de).toList();
    }

    /** Resumen para el detalle del evento, visto por el usuario indicado. */
    @Transactional(readOnly = true)
    public Comunidad resumen(Long eventoId, Usuario quienConsulta) {
        List<Valoracion> lista = valoraciones.findByEventoId(eventoId);
        Double promedio = lista.isEmpty() ? null
                : Math.round(lista.stream().mapToInt(Valoracion::getPuntaje).average().orElse(0) * 10) / 10.0;
        Integer mia = lista.stream()
                .filter(v -> quienConsulta != null && v.getUsuario().getId().equals(quienConsulta.getId()))
                .map(Valoracion::getPuntaje).findFirst().orElse(null);
        List<ComentarioDto> textos = new ArrayList<>(comentarios.recientes(eventoId,
                        PageRequest.of(0, COMENTARIOS_VISIBLES)).stream()
                .map(c -> new ComentarioDto(c.getId(), c.getUsuario().getUsername(), c.getTexto(), c.getCreadoEn(),
                        quienConsulta != null && (quienConsulta.esAdmin()
                                || c.getUsuario().getId().equals(quienConsulta.getId()))))
                .toList());
        // Se piden los mas recientes y se muestran en orden cronologico
        Collections.reverse(textos);
        return new Comunidad(promedio, lista.size(), mia, textos, compartidos.countByEventoId(eventoId));
    }

    private Evento evento(Long id) {
        return eventos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Usuario usuario(String username) {
        return usuarios.findByUsername(username).orElseThrow(() -> new AccessDeniedException("sin usuario"));
    }
}

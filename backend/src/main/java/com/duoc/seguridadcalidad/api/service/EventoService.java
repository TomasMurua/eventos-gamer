package com.duoc.seguridadcalidad.api.service;

import com.duoc.seguridadcalidad.api.dto.BannerDto;
import com.duoc.seguridadcalidad.api.dto.EventoDetalle;
import com.duoc.seguridadcalidad.api.dto.EventoResumen;
import com.duoc.seguridadcalidad.api.dto.Filtros;
import com.duoc.seguridadcalidad.api.dto.NuevoEventoRequest;
import com.duoc.seguridadcalidad.api.dto.Portada;
import com.duoc.seguridadcalidad.api.error.DemasiadasSolicitudesException;
import com.duoc.seguridadcalidad.api.error.SolicitudInvalidaException;
import com.duoc.seguridadcalidad.api.model.Evento;
import com.duoc.seguridadcalidad.api.model.Modalidad;
import com.duoc.seguridadcalidad.api.model.Usuario;
import com.duoc.seguridadcalidad.api.repository.BannerRepository;
import com.duoc.seguridadcalidad.api.repository.EventoRepository;
import com.duoc.seguridadcalidad.api.repository.MedioEventoRepository;
import com.duoc.seguridadcalidad.api.repository.UsuarioRepository;
import com.duoc.seguridadcalidad.api.security.LimiteFrecuencia;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventoService {

    /** Categorias que se pueden elegir al publicar un evento. */
    public static final List<String> CATEGORIAS = List.of("Benéfico", "Convención", "Desarrollo", "Encuentro",
            "LAN Party", "Lanzamiento", "Retro", "Torneo");

    private static final Logger log = LoggerFactory.getLogger("seguridad");

    /** Maximo de eventos que un usuario puede publicar en 24 horas. */
    static final int EVENTOS_POR_DIA = 10;

    private final EventoRepository eventos;
    private final BannerRepository banners;
    private final UsuarioRepository usuarios;
    private final MedioEventoRepository medios;
    private final ComunidadService comunidad;
    private final LimiteFrecuencia limites;

    public EventoService(EventoRepository eventos, BannerRepository banners, UsuarioRepository usuarios,
                         MedioEventoRepository medios, ComunidadService comunidad, LimiteFrecuencia limites) {
        this.eventos = eventos;
        this.banners = banners;
        this.usuarios = usuarios;
        this.medios = medios;
        this.comunidad = comunidad;
        this.limites = limites;
    }

    public Portada portada() {
        return new Portada(
                eventos.findAllByOrderByPublicadoEnDesc(PageRequest.of(0, 4)).stream().map(EventoResumen::de).toList(),
                eventos.findAllByOrderByPopularidadDesc(PageRequest.of(0, 4)).stream().map(EventoResumen::de).toList(),
                eventos.findByDestacadoTrueOrderByFechaHoraAsc().stream().map(EventoResumen::de).toList(),
                banners.findByActivoTrue().stream().map(BannerDto::de).toList(),
                comunidad.muro());
    }

    public Filtros filtros() {
        return new Filtros(eventos.videojuegos(), eventos.categorias(), eventos.paises(), eventos.ciudades(), CATEGORIAS);
    }

    /**
     * Busca eventos con los filtros recibidos. Cada filtro vacio se ignora. La consulta se arma con la
     * API Criteria de JPA, asi que los valores viajan siempre como parametros y nunca se concatenan al SQL.
     */
    public List<EventoResumen> buscar(String nombre, String videojuego, String categoria, String lugar,
                                      LocalDate desde, LocalDate hasta, Modalidad modalidad) {
        List<Specification<Evento>> condiciones = new ArrayList<>();
        if (tieneTexto(nombre)) {
            String patron = "%" + nombre.trim().toLowerCase(Locale.ROOT) + "%";
            condiciones.add((root, q, cb) -> cb.like(cb.lower(root.get("nombre")), patron));
        }
        if (tieneTexto(videojuego)) {
            condiciones.add((root, q, cb) -> cb.equal(cb.lower(root.get("videojuego")), videojuego.trim().toLowerCase(Locale.ROOT)));
        }
        if (tieneTexto(categoria)) {
            condiciones.add((root, q, cb) -> cb.equal(cb.lower(root.get("categoria")), categoria.trim().toLowerCase(Locale.ROOT)));
        }
        if (tieneTexto(lugar)) {
            String valor = lugar.trim().toLowerCase(Locale.ROOT);
            condiciones.add((root, q, cb) -> cb.or(
                    cb.equal(cb.lower(root.get("pais")), valor),
                    cb.equal(cb.lower(root.get("ciudad")), valor)));
        }
        if (desde != null) {
            condiciones.add((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("fechaHora"), desde.atStartOfDay()));
        }
        if (hasta != null) {
            condiciones.add((root, q, cb) -> cb.lessThan(root.get("fechaHora"), hasta.plusDays(1).atStartOfDay()));
        }
        if (modalidad != null) {
            condiciones.add((root, q, cb) -> cb.equal(root.get("modalidad"), modalidad));
        }
        return eventos.findAll(Specification.allOf(condiciones), Sort.by("fechaHora"))
                .stream().map(EventoResumen::de).toList();
    }

    /** Detalle completo, con lo que puede hacer el usuario que consulta (editar si es el autor o ADMIN). */
    public Optional<EventoDetalle> detalle(Long id, String username) {
        Usuario quien = usuarios.findByUsername(username).orElse(null);
        return eventos.findById(id).map(e -> {
            boolean editable = quien != null && (e.esAutor(quien) || quien.esAdmin());
            List<EventoDetalle.Medio> lista = medios.findByEventoIdOrderBySubidoEnAsc(id).stream()
                    .map(m -> new EventoDetalle.Medio(m.getId(), m.getTipo().name(), m.getDescripcion()))
                    .toList();
            return EventoDetalle.de(e, editable, lista, comunidad.resumen(id, quien));
        });
    }

    public List<EventoResumen> publicadosPor(String username) {
        Usuario usuario = usuarios.findByUsername(username).orElseThrow(() -> new AccessDeniedException("sin usuario"));
        return eventos.findByAutorIdOrderByFechaHoraAsc(usuario.getId()).stream().map(EventoResumen::de).toList();
    }

    /** Publica un evento nuevo. El autor es siempre el usuario del token, nunca un dato del formulario. */
    @Transactional
    public EventoResumen publicar(NuevoEventoRequest r, String username) {
        Usuario autor = usuarios.findByUsername(username).orElseThrow(() -> new AccessDeniedException("sin usuario"));
        String clave = "evento:" + username;
        if (!limites.intentar(clave, EVENTOS_POR_DIA, Duration.ofDays(1))) {
            log.warn("Limite de eventos publicados alcanzado: usuario={}", username);
            throw new DemasiadasSolicitudesException("Publicaste muchos eventos hoy. Intenta mañana.");
        }
        String categoria = CATEGORIAS.stream().filter(c -> c.equalsIgnoreCase(r.categoria().trim())).findFirst()
                .orElseThrow(() -> new SolicitudInvalidaException("La categoría no es válida"));

        String ciudad = Textos.limpiar(r.ciudad());
        String ubicacion = Textos.limpiar(r.ubicacion());
        String enlace = Textos.limpiar(r.enlaceAcceso());
        if (r.modalidad() == Modalidad.PRESENCIAL) {
            if (ciudad == null || ubicacion == null) {
                throw new SolicitudInvalidaException("Un evento presencial necesita ciudad y dirección");
            }
            enlace = null;
        } else {
            if (enlace == null || !esEnlaceHttps(enlace)) {
                throw new SolicitudInvalidaException("Un evento online necesita un enlace que empiece con https://");
            }
            ubicacion = null;
        }

        Evento evento = eventos.save(new Evento(Textos.limpiar(r.nombre()), Textos.limpiar(r.videojuego()), categoria,
                r.modalidad(), Textos.limpiar(r.pais()), ciudad, r.fechaHora(), ubicacion, enlace,
                Textos.limpiar(r.organizador()), Textos.limpiar(r.resumen()), Textos.limpiar(r.descripcion()),
                Textos.limpiar(r.requisitos()), Textos.limpiar(r.inscripcion()), r.cupos(), autor, LocalDateTime.now()));
        log.info("Evento publicado: id={} autor={}", evento.getId(), autor.getUsername());
        return EventoResumen.de(evento);
    }

    /** Solo enlaces https con servidor, para que nadie pueda guardar un enlace "javascript:" en la pagina. */
    static boolean esEnlaceHttps(String enlace) {
        try {
            URI uri = new URI(enlace);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null && !uri.getHost().isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}

package com.duoc.seguridadcalidad.api.service;

import com.duoc.seguridadcalidad.api.dto.BannerDto;
import com.duoc.seguridadcalidad.api.dto.EventoDetalle;
import com.duoc.seguridadcalidad.api.dto.EventoResumen;
import com.duoc.seguridadcalidad.api.dto.Filtros;
import com.duoc.seguridadcalidad.api.dto.Portada;
import com.duoc.seguridadcalidad.api.model.Evento;
import com.duoc.seguridadcalidad.api.model.Modalidad;
import com.duoc.seguridadcalidad.api.repository.BannerRepository;
import com.duoc.seguridadcalidad.api.repository.EventoRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventoService {

    private final EventoRepository eventos;
    private final BannerRepository banners;

    public EventoService(EventoRepository eventos, BannerRepository banners) {
        this.eventos = eventos;
        this.banners = banners;
    }

    public Portada portada() {
        return new Portada(
                eventos.findAllByOrderByPublicadoEnDesc(PageRequest.of(0, 4)).stream().map(EventoResumen::de).toList(),
                eventos.findAllByOrderByPopularidadDesc(PageRequest.of(0, 4)).stream().map(EventoResumen::de).toList(),
                eventos.findByDestacadoTrueOrderByFechaHoraAsc().stream().map(EventoResumen::de).toList(),
                banners.findByActivoTrue().stream().map(BannerDto::de).toList());
    }

    public Filtros filtros() {
        return new Filtros(eventos.videojuegos(), eventos.categorias(), eventos.paises(), eventos.ciudades());
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

    public Optional<EventoDetalle> detalle(Long id) {
        return eventos.findById(id).map(EventoDetalle::de);
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}

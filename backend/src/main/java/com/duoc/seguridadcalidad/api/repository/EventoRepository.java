package com.duoc.seguridadcalidad.api.repository;

import com.duoc.seguridadcalidad.api.model.Evento;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {

    List<Evento> findAllByOrderByPublicadoEnDesc(Pageable pageable);

    List<Evento> findAllByOrderByPopularidadDesc(Pageable pageable);

    List<Evento> findByDestacadoTrueOrderByFechaHoraAsc();

    @Query("select distinct e.videojuego from Evento e order by e.videojuego")
    List<String> videojuegos();

    @Query("select distinct e.categoria from Evento e order by e.categoria")
    List<String> categorias();

    @Query("select distinct e.pais from Evento e order by e.pais")
    List<String> paises();

    @Query("select distinct e.ciudad from Evento e where e.ciudad is not null order by e.ciudad")
    List<String> ciudades();
}

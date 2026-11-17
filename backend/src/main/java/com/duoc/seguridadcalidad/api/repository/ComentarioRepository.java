package com.duoc.seguridadcalidad.api.repository;

import com.duoc.seguridadcalidad.api.model.Comentario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    /** Comentarios mas recientes con su autor en una sola consulta (sin una consulta extra por comentario). */
    @Query("select c from Comentario c join fetch c.usuario where c.evento.id = :eventoId order by c.creadoEn desc")
    List<Comentario> recientes(@Param("eventoId") Long eventoId, Pageable pagina);

    long countByEventoId(Long eventoId);

    Optional<Comentario> findByIdAndEventoId(Long id, Long eventoId);
}

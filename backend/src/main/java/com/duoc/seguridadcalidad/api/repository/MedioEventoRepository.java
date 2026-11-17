package com.duoc.seguridadcalidad.api.repository;

import com.duoc.seguridadcalidad.api.model.MedioEvento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedioEventoRepository extends JpaRepository<MedioEvento, Long> {

    List<MedioEvento> findByEventoIdOrderBySubidoEnAsc(Long eventoId);

    /** Busca el medio dentro del evento indicado, asi no se puede pedir un archivo cambiando solo el id del medio. */
    Optional<MedioEvento> findByIdAndEventoId(Long id, Long eventoId);

    long countByEventoId(Long eventoId);

    /** Bytes que ya subio el usuario entre todos sus eventos (para la cuota por usuario). */
    @Query("select coalesce(sum(m.tamano), 0) from MedioEvento m where m.usuario.id = :usuarioId")
    long bytesSubidosPor(@Param("usuarioId") Long usuarioId);
}

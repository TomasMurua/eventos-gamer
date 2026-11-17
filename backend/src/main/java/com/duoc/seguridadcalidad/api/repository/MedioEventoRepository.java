package com.duoc.seguridadcalidad.api.repository;

import com.duoc.seguridadcalidad.api.model.MedioEvento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedioEventoRepository extends JpaRepository<MedioEvento, Long> {

    List<MedioEvento> findByEventoIdOrderBySubidoEnAsc(Long eventoId);

    /** Busca el medio dentro del evento indicado, asi no se puede pedir un archivo cambiando solo el id del medio. */
    Optional<MedioEvento> findByIdAndEventoId(Long id, Long eventoId);

    long countByEventoId(Long eventoId);
}

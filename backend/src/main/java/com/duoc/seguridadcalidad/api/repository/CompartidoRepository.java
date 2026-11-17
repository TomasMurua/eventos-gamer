package com.duoc.seguridadcalidad.api.repository;

import com.duoc.seguridadcalidad.api.model.Compartido;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompartidoRepository extends JpaRepository<Compartido, Long> {

    List<Compartido> findAllByOrderByCreadoEnDesc(Pageable pageable);

    long countByEventoId(Long eventoId);
}

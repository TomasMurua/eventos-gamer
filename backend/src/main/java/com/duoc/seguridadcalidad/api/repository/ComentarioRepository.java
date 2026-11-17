package com.duoc.seguridadcalidad.api.repository;

import com.duoc.seguridadcalidad.api.model.Comentario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByEventoIdOrderByCreadoEnAsc(Long eventoId);

    Optional<Comentario> findByIdAndEventoId(Long id, Long eventoId);
}

package com.duoc.seguridadcalidad.api.repository;

import com.duoc.seguridadcalidad.api.model.Valoracion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    List<Valoracion> findByEventoId(Long eventoId);

    Optional<Valoracion> findByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);
}

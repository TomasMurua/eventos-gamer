package com.duoc.seguridadcalidad.api.repository;

import com.duoc.seguridadcalidad.api.model.Compartido;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompartidoRepository extends JpaRepository<Compartido, Long> {

    @Query("select c from Compartido c join fetch c.evento join fetch c.usuario order by c.creadoEn desc")
    List<Compartido> recientes(Pageable pagina);

    long countByEventoId(Long eventoId);

    boolean existsByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);
}

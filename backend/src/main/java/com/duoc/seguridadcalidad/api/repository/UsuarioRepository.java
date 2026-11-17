package com.duoc.seguridadcalidad.api.repository;

import com.duoc.seguridadcalidad.api.model.Usuario;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** El login acepta el nombre de usuario o el correo. */
    Optional<Usuario> findFirstByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    List<Usuario> findAllByOrderByIdAsc();

    /** SELECT ... FOR UPDATE: las subidas de un mismo usuario se procesan de a una (cuota y maximo por evento). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> bloquear(@Param("id") Long id);
}

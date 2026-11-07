package com.duoc.seguridadcalidad.api.repository;

import com.duoc.seguridadcalidad.api.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** El login acepta el nombre de usuario o el correo. */
    Optional<Usuario> findFirstByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    Optional<Usuario> findByUsername(String username);

    List<Usuario> findAllByOrderByIdAsc();
}

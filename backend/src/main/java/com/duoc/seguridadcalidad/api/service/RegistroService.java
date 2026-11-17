package com.duoc.seguridadcalidad.api.service;

import com.duoc.seguridadcalidad.api.dto.RegistroRequest;
import com.duoc.seguridadcalidad.api.error.ConflictoException;
import com.duoc.seguridadcalidad.api.error.SolicitudInvalidaException;
import com.duoc.seguridadcalidad.api.model.Usuario;
import com.duoc.seguridadcalidad.api.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Crea cuentas nuevas con rol USER y la contrasena guardada como hash BCrypt. */
@Service
public class RegistroService {

    static final String YA_REGISTRADO = "El nombre de usuario o el correo ya están registrados";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public RegistroService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrar(RegistroRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String nombre = request.nombre().trim().replaceAll("\\s+", " ");
        validarClave(request.password(), username, email);

        if (usuarios.existsByUsernameIgnoreCaseOrEmailIgnoreCase(username, email)) {
            throw new ConflictoException(YA_REGISTRADO);
        }
        try {
            return usuarios.saveAndFlush(
                    new Usuario(username, email, nombre, passwordEncoder.encode(request.password()), "USER"));
        } catch (DataIntegrityViolationException e) {
            // Dos registros simultaneos con el mismo correo: lo frena la restriccion UNIQUE de la base de datos
            throw new ConflictoException(YA_REGISTRADO);
        }
    }

    /**
     * Politica de contrasenas: entre 10 y 64 caracteres (BCrypt solo usa los primeros 72 bytes), con mayuscula,
     * minuscula y numero, y sin contener el nombre de usuario ni el correo.
     */
    static void validarClave(String clave, String username, String email) {
        if (clave.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new SolicitudInvalidaException("La contraseña es demasiado larga");
        }
        boolean mayuscula = clave.chars().anyMatch(Character::isUpperCase);
        boolean minuscula = clave.chars().anyMatch(Character::isLowerCase);
        boolean numero = clave.chars().anyMatch(Character::isDigit);
        if (!mayuscula || !minuscula || !numero) {
            throw new SolicitudInvalidaException("La contraseña debe tener mayúsculas, minúsculas y números");
        }
        String minusculas = clave.toLowerCase(Locale.ROOT);
        String local = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        if (minusculas.contains(username.toLowerCase(Locale.ROOT)) || (local.length() >= 3 && minusculas.contains(local))) {
            throw new SolicitudInvalidaException("La contraseña no puede contener tu nombre de usuario ni tu correo");
        }
    }
}

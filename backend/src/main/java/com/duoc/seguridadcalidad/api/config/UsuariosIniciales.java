package com.duoc.seguridadcalidad.api.config;

import com.duoc.seguridadcalidad.api.model.Usuario;
import com.duoc.seguridadcalidad.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ajusta las contrasenas de los usuarios de prueba con las variables de entorno (si estan definidas)
 * y crea el administrador "llam" solo cuando APP_PASSWORD_LLAM existe, para que su clave nunca
 * quede escrita en el codigo ni en los scripts de la base de datos.
 */
@Component
public class UsuariosIniciales implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuariosIniciales.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final String passwordUsuario;
    private final String passwordAdmin;
    private final String passwordLlam;

    public UsuariosIniciales(UsuarioRepository usuarios, PasswordEncoder passwordEncoder,
                             @Value("${app.usuarios.password-usuario:}") String passwordUsuario,
                             @Value("${app.usuarios.password-admin:}") String passwordAdmin,
                             @Value("${app.usuarios.password-llam:}") String passwordLlam) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.passwordUsuario = passwordUsuario;
        this.passwordAdmin = passwordAdmin;
        this.passwordLlam = passwordLlam;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        actualizar("ana", passwordUsuario);
        actualizar("diego", passwordUsuario);
        if (passwordAdmin.isBlank()) {
            log.warn("APP_PASSWORD_ADMIN no esta definida: la cuenta admin no podra iniciar sesion");
        }
        actualizar("admin", passwordAdmin);
        if (!passwordLlam.isBlank()) {
            Usuario llam = usuarios.findByUsername("llam").orElseGet(() -> usuarios.save(
                    new Usuario("llam", "llam@eventosgamer.cl", "llam", "", "ADMIN")));
            llam.setPasswordHash(passwordEncoder.encode(passwordLlam));
        }
    }

    private void actualizar(String username, String password) {
        if (password.isBlank()) {
            return;
        }
        usuarios.findByUsername(username).ifPresent(u -> u.setPasswordHash(passwordEncoder.encode(password)));
    }
}

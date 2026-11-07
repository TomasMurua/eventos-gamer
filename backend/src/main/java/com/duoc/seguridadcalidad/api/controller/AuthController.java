package com.duoc.seguridadcalidad.api.controller;

import com.duoc.seguridadcalidad.api.dto.ErrorRespuesta;
import com.duoc.seguridadcalidad.api.dto.LoginRequest;
import com.duoc.seguridadcalidad.api.dto.LoginResponse;
import com.duoc.seguridadcalidad.api.model.Usuario;
import com.duoc.seguridadcalidad.api.repository.UsuarioRepository;
import com.duoc.seguridadcalidad.api.security.IntentosLogin;
import com.duoc.seguridadcalidad.api.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger("seguridad");

    /** Mismo mensaje para usuario inexistente y clave incorrecta, asi no se puede saber que cuentas existen. */
    private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final IntentosLogin intentos;
    private final String hashFicticio;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService,
                          IntentosLogin intentos) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.intentos = intentos;
        // Se compara contra este hash cuando el usuario no existe, para que la respuesta tarde lo mismo
        this.hashFicticio = passwordEncoder.encode("clave-que-no-corresponde-a-ningun-usuario");
    }

    /** Publica: recibe usuario (o correo) y contrasena, y devuelve un token JWT. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        String nombre = request.username().trim();
        if (intentos.bloqueado(nombre, ip)) {
            log.warn("Login bloqueado por exceso de intentos: usuario={} ip={}", nombre, ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ErrorRespuesta(429, "Demasiados intentos fallidos. Espera unos minutos."));
        }

        Optional<Usuario> encontrado = usuarios.findFirstByUsernameIgnoreCaseOrEmailIgnoreCase(nombre, nombre);
        String hash = encontrado.map(Usuario::getPasswordHash).orElse(hashFicticio);
        boolean claveValida = passwordEncoder.matches(request.password(), hash);

        if (encontrado.isEmpty() || !claveValida || !encontrado.get().isActivo()) {
            intentos.registrarFallo(nombre, ip);
            log.warn("Login fallido: usuario={} ip={}", nombre, ip);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorRespuesta(401, CREDENCIALES_INVALIDAS));
        }

        Usuario usuario = encontrado.get();
        intentos.registrarExito(nombre, ip);
        log.info("Login exitoso: usuario={} ip={}", usuario.getUsername(), ip);
        String token = jwtService.generar(usuario);
        return ResponseEntity.ok(new LoginResponse(token, "Bearer", jwtService.duracionEnSegundos(),
                usuario.getUsername(), usuario.getNombre(), usuario.getRol()));
    }
}

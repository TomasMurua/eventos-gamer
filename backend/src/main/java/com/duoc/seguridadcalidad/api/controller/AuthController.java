package com.duoc.seguridadcalidad.api.controller;

import com.duoc.seguridadcalidad.api.dto.ErrorRespuesta;
import com.duoc.seguridadcalidad.api.dto.LoginRequest;
import com.duoc.seguridadcalidad.api.dto.LoginResponse;
import com.duoc.seguridadcalidad.api.dto.RegistroRequest;
import com.duoc.seguridadcalidad.api.dto.RegistroResponse;
import com.duoc.seguridadcalidad.api.model.Usuario;
import com.duoc.seguridadcalidad.api.repository.UsuarioRepository;
import com.duoc.seguridadcalidad.api.security.IntentosLogin;
import com.duoc.seguridadcalidad.api.security.JwtService;
import com.duoc.seguridadcalidad.api.security.LimiteFrecuencia;
import com.duoc.seguridadcalidad.api.service.RegistroService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private final RegistroService registro;
    private final LimiteFrecuencia limites;
    private final int maxRegistrosPorIp;
    private final Duration ventanaRegistros;
    private final String hashFicticio;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService,
                          IntentosLogin intentos, RegistroService registro, LimiteFrecuencia limites,
                          @Value("${app.registro.max-por-ip:5}") int maxRegistrosPorIp,
                          @Value("${app.registro.minutos-ventana:60}") long minutosVentana) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.intentos = intentos;
        this.registro = registro;
        this.limites = limites;
        this.maxRegistrosPorIp = maxRegistrosPorIp;
        this.ventanaRegistros = Duration.ofMinutes(minutosVentana);
        // Cuando el usuario no existe se compara contra este hash, para que la respuesta tarde lo mismo.
        // Es el hash de una clave aleatoria que se genera al arrancar: no hay ninguna clave escrita en el codigo.
        byte[] aleatorio = new byte[32];
        new SecureRandom().nextBytes(aleatorio);
        this.hashFicticio = passwordEncoder.encode(Base64.getEncoder().encodeToString(aleatorio));
    }

    /** Publica: recibe usuario (o correo) y contrasena, y devuelve un token JWT. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        Optional<Usuario> encontrado = usuarios.findFirstByUsernameIgnoreCaseOrEmailIgnoreCase(
                request.username().trim(), request.username().trim());
        // El contador usa la cuenta real (asi "ana" y "ana@eventosgamer.cl" suman al mismo contador)
        String cuenta = encontrado.map(Usuario::getUsername).orElse(IntentosLogin.normalizar(request.username()));

        if (intentos.bloqueado(cuenta, ip)) {
            log.warn("Login bloqueado por exceso de intentos: cuenta={} ip={}", paraLog(cuenta), ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ErrorRespuesta(429, "Demasiados intentos fallidos. Espera unos minutos."));
        }

        String hash = encontrado.map(Usuario::getPasswordHash).orElse(hashFicticio);
        boolean claveValida = passwordEncoder.matches(request.password(), hash);

        if (encontrado.isEmpty() || !claveValida || !encontrado.get().isActivo()) {
            intentos.registrarFallo(cuenta, ip);
            log.warn("Login fallido: cuenta={} ip={}", paraLog(cuenta), ip);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorRespuesta(401, CREDENCIALES_INVALIDAS));
        }

        Usuario usuario = encontrado.get();
        intentos.registrarExito(cuenta, ip);
        log.info("Login exitoso: cuenta={} ip={}", usuario.getUsername(), ip);
        String token = jwtService.generar(usuario);
        return ResponseEntity.ok(new LoginResponse(token, "Bearer", jwtService.duracionEnSegundos(),
                usuario.getUsername(), usuario.getNombre(), usuario.getRol()));
    }

    /**
     * Publica: crea una cuenta con rol USER. La contrasena se guarda solo como hash BCrypt.
     * Se limita la cantidad de cuentas creadas por IP (o por red /64 en IPv6) para frenar la creacion masiva.
     * Solo cuentan los registros exitosos: un error de tipeo no consume el cupo.
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequest request, HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        String clave = "registro:" + LimiteFrecuencia.grupoIp(ip);
        if (!limites.disponible(clave, maxRegistrosPorIp, ventanaRegistros)) {
            log.warn("Registro bloqueado por exceso de cuentas nuevas: ip={}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ErrorRespuesta(429, "Se crearon demasiadas cuentas desde tu red. Intenta más tarde."));
        }
        Usuario nuevo = registro.registrar(request);
        limites.registrar(clave);
        log.info("Cuenta registrada: cuenta={} ip={}", nuevo.getUsername(), ip);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegistroResponse(nuevo.getUsername(), nuevo.getNombre()));
    }

    /** Quita saltos de linea y caracteres de control (evita falsificar lineas del log) y limita el largo. */
    static String paraLog(String texto) {
        String limpio = texto.replaceAll("[\\p{Cntrl}]", "_");
        return limpio.length() > 40 ? limpio.substring(0, 40) + "..." : limpio;
    }
}

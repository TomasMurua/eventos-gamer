package com.duoc.seguridadcalidad.api.controller;

import com.duoc.seguridadcalidad.api.dto.ErrorRespuesta;
import com.duoc.seguridadcalidad.api.dto.LoginRequest;
import com.duoc.seguridadcalidad.api.dto.LoginResponse;
import com.duoc.seguridadcalidad.api.model.Usuario;
import com.duoc.seguridadcalidad.api.repository.UsuarioRepository;
import com.duoc.seguridadcalidad.api.security.JwtService;
import jakarta.validation.Valid;
import java.util.Optional;
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

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Publica: recibe usuario (o correo) y contrasena, y devuelve un token JWT. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Optional<Usuario> encontrado = usuarios.findFirstByUsernameIgnoreCaseOrEmailIgnoreCase(
                request.username(), request.username());
        if (encontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorRespuesta(401, "El usuario no existe"));
        }
        Usuario usuario = encontrado.get();
        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorRespuesta(401, "Contraseña incorrecta"));
        }
        String token = jwtService.generar(usuario);
        return ResponseEntity.ok(new LoginResponse(token, "Bearer", jwtService.duracionEnSegundos(),
                usuario.getUsername(), usuario.getNombre(), usuario.getRol()));
    }
}

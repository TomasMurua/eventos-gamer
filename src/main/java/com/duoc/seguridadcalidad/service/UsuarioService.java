package com.duoc.seguridadcalidad.service;

import com.duoc.seguridadcalidad.model.Perfil;
import com.duoc.seguridadcalidad.model.RegistroForm;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

/**
 * Registro de usuarios y perfiles. Los usuarios viven en el UserDetailsManager de Spring Security
 * (contrasenas con BCrypt); los perfiles en memoria hasta que se agregue la base de datos.
 */
@Service
public class UsuarioService {

    public static final List<String> JUEGOS = List.of(
            "League of Legends", "Valorant", "Counter-Strike 2", "Fortnite", "Minecraft",
            "Street Fighter 6", "Tekken 8", "EA Sports FC", "Rocket League", "Clasicos retro");

    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> nombres = new ConcurrentHashMap<>();
    private final Map<String, Perfil> perfiles = new ConcurrentHashMap<>();

    public UsuarioService(UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    public static String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
    }

    public boolean existe(String correo) {
        return userDetailsManager.userExists(normalizarCorreo(correo));
    }

    public void registrar(RegistroForm form) {
        String correo = normalizarCorreo(form.getCorreo());
        if (userDetailsManager.userExists(correo)) {
            throw new IllegalStateException("El correo ya esta registrado");
        }
        userDetailsManager.createUser(User.builder()
                .username(correo)
                .password(passwordEncoder.encode(form.getPassword()))
                .roles("USER")
                .build());
        nombres.put(correo, form.getNombre().trim());
    }

    public String nombre(String username) {
        return nombres.getOrDefault(username, username);
    }

    public Perfil perfil(String username) {
        return perfiles.computeIfAbsent(username, u -> {
            Perfil perfil = new Perfil();
            perfil.setNombreUsuario(sugerirNombreUsuario(u));
            return perfil;
        });
    }

    public void guardarPerfil(String username, Perfil perfil) {
        List<String> juegosValidos = perfil.getJuegosInteres().stream().filter(JUEGOS::contains).toList();
        perfil.setJuegosInteres(juegosValidos);
        perfiles.put(username, perfil);
    }

    private static String sugerirNombreUsuario(String username) {
        String base = username.contains("@") ? username.substring(0, username.indexOf('@')) : username;
        String limpio = base.replaceAll("[^A-Za-z0-9_.-]", "");
        return limpio.length() >= 3 ? limpio.substring(0, Math.min(limpio.length(), 20)) : "gamer_" + limpio;
    }
}

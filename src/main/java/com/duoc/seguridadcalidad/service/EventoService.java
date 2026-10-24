package com.duoc.seguridadcalidad.service;

import com.duoc.seguridadcalidad.model.Evento;
import com.duoc.seguridadcalidad.model.Evento.Servicios;
import com.duoc.seguridadcalidad.model.Marca;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

/**
 * Catalogo de eventos en memoria.
 */
@Service
public class EventoService {

    private final List<Evento> eventos = List.of(
            new Evento(1L, "Santiago Gaming Fest 2026", "Convencion",
                    "La convencion gamer mas grande de la capital.",
                    "Tres pabellones con zona de free play, stands de desarrolladores chilenos, cosplay y charlas sobre la industria de videojuegos.",
                    LocalDate.of(2026, 11, 14), LocalTime.of(10, 0), "Santiago",
                    "Espacio Riesco, Av. El Salto 5000, Huechuraba",
                    List.of("Asociacion Gamer Chile", "Productora Nivel 99"),
                    List.of("Estudio Pixel Andino", "Comunidad Indie Dev Chile", "Liga Esports Sur"),
                    new Servicios(true, "Metro Linea 2 estacion Vespucio Norte + bus de acercamiento", true, false),
                    true),
            new Evento(2L, "Copa Valparaiso de Fighting Games", "Torneo",
                    "Torneo abierto de juegos de pelea con premios en efectivo.",
                    "Brackets de doble eliminacion en tres juegos de pelea. Inscripcion previa obligatoria y control de identidad en la entrada.",
                    LocalDate.of(2026, 11, 21), LocalTime.of(11, 30), "Valparaiso",
                    "Parque Cultural de Valparaiso, Cerro Carcel 471",
                    List.of("Club FGC Puerto"),
                    List.of("Comentaristas FGC Chile", "Arbitros Liga Puerto"),
                    new Servicios(false, "Trolebus y micros por Av. Pedro Montt", true, false),
                    true),
            new Evento(3L, "LAN Party Conce Retro", "LAN Party",
                    "48 horas de juego en red con clasicos de los 2000.",
                    "Trae tu equipo y juega clasicos en red local durante todo el fin de semana. Hay zona de descanso y duchas.",
                    LocalDate.of(2026, 11, 28), LocalTime.of(18, 0), "Concepcion",
                    "Gimnasio Universitario, Av. Esteban Iturra s/n, Concepcion",
                    List.of("Centro de Alumnos Informatica UdeC"),
                    List.of("Coleccionistas Retro Biobio"),
                    new Servicios(true, "Biotren estacion Concepcion + micros por Chacabuco", true, true),
                    false),
            new Evento(4L, "Lanzamiento Andes Legends", "Lanzamiento",
                    "Presentacion oficial de un RPG desarrollado en Chile.",
                    "El estudio muestra el gameplay en vivo, firma de copias fisicas y ronda de preguntas con el equipo de desarrollo.",
                    LocalDate.of(2026, 12, 5), LocalTime.of(19, 0), "Santiago",
                    "Centro Cultural GAM, Av. Libertador Bernardo O'Higgins 227",
                    List.of("Estudio Pixel Andino"),
                    List.of("Directora creativa de Estudio Pixel Andino", "Compositor de la banda sonora"),
                    new Servicios(false, "Metro Linea 1 estacion Universidad Catolica", true, false),
                    true),
            new Evento(5L, "Torneo Universitario de Esports", "Torneo",
                    "Equipos de universidades de todo Chile compiten en MOBA y shooters.",
                    "Fase final presencial con transmision en vivo. Las barras de cada universidad tienen sectores asignados.",
                    LocalDate.of(2026, 12, 12), LocalTime.of(15, 0), "Santiago",
                    "Movistar Arena, Parque O'Higgins",
                    List.of("Liga Esports Sur", "Federacion Universitaria de Esports"),
                    List.of("Casters Liga Esports Sur", "Entrenadores de equipos finalistas"),
                    new Servicios(true, "Metro Linea 2 estacion Parque O'Higgins", true, false),
                    false),
            new Evento(6L, "Feria Retro Gamer La Serena", "Retro",
                    "Compra, venta e intercambio de consolas y juegos clasicos.",
                    "Mas de 40 expositores con consolas de coleccion, reparacion en vivo y torneo de juegos de 16 bits.",
                    LocalDate.of(2026, 12, 19), LocalTime.of(11, 0), "La Serena",
                    "Coliseo Monumental, Av. Estadio s/n, La Serena",
                    List.of("Coleccionistas del Norte Chico"),
                    List.of("Tecnicos de restauracion retro", "Coleccionistas Retro Biobio"),
                    new Servicios(true, "Micros por Av. Francisco de Aguirre", true, true),
                    false),
            new Evento(7L, "Game Jam Temuco", "Desarrollo",
                    "48 horas para crear un videojuego desde cero en equipo.",
                    "Abierto a programadores, artistas y musicos. Mentores de la industria acompanan a los equipos y hay premiacion final.",
                    LocalDate.of(2027, 1, 9), LocalTime.of(9, 0), "Temuco",
                    "Incubadora Universidad de La Frontera, Av. Francisco Salazar 01145",
                    List.of("Comunidad Indie Dev Chile"),
                    List.of("Mentores Comunidad Indie Dev Chile"),
                    new Servicios(true, "Micros por Av. Alemania", true, false),
                    true));

    private final List<Marca> marcas = List.of(
            new Marca("NeonByte", "Perifericos para ganar cada partida", "Perifericos"),
            new Marca("Fibra Ultra", "Internet de baja latencia para gamers", "Conectividad"),
            new Marca("Silla Pro Arena", "Comodidad para maratones de juego", "Mobiliario"));

    public List<Evento> proximos(int limite) {
        LocalDate hoy = LocalDate.now();
        return eventos.stream()
                .filter(e -> !e.fecha().isBefore(hoy))
                .sorted(Comparator.comparing(Evento::fecha))
                .limit(limite)
                .toList();
    }

    public List<Evento> destacados() {
        return eventos.stream()
                .filter(Evento::destacado)
                .sorted(Comparator.comparing(Evento::fecha))
                .toList();
    }

    public List<Marca> marcas() {
        return marcas;
    }

    public List<String> categorias() {
        return List.copyOf(new TreeSet<>(eventos.stream().map(Evento::categoria).toList()));
    }

    public List<String> ciudades() {
        return List.copyOf(new TreeSet<>(eventos.stream().map(Evento::ciudad).toList()));
    }

    /**
     * Filtra por categoria, rango de fechas y ciudad. Los filtros vacios se ignoran.
     */
    public List<Evento> buscar(String categoria, LocalDate desde, LocalDate hasta, String ciudad) {
        return eventos.stream()
                .filter(e -> vacio(categoria) || e.categoria().equalsIgnoreCase(categoria))
                .filter(e -> vacio(ciudad) || e.ciudad().equalsIgnoreCase(ciudad))
                .filter(e -> desde == null || !e.fecha().isBefore(desde))
                .filter(e -> hasta == null || !e.fecha().isAfter(hasta))
                .sorted(Comparator.comparing(Evento::fecha))
                .toList();
    }

    public Optional<Evento> buscarPorId(Long id) {
        return eventos.stream().filter(e -> e.id().equals(id)).findFirst();
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}

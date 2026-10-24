# Eventos Gamer

Aplicacion web para reunir informacion de eventos de la comunidad gamer (torneos, convenciones,
LAN parties, lanzamientos). Proyecto de la asignatura **ISY2202 Seguridad y Calidad en el Desarrollo
de Software** (Duoc UC), Experiencia 1, Semana 1: "Creando una aplicacion web segura usando frameworks
y librerias de seguridad".

## Stack

- Java 17 y Maven
- Spring Boot 3.5 (Spring Web)
- Spring Security 6 (autenticacion por formulario, contrasenas con BCrypt, CSRF, cabeceras de seguridad)
- Thymeleaf + `thymeleaf-extras-springsecurity6`
- Bean Validation para los formularios

## Rutas

| Ruta | Acceso | Descripcion |
|---|---|---|
| `/`, `/home` | Publica | Inicio: proximos eventos, eventos destacados y marcas asociadas |
| `/registro` | Publica | Registro con nombre, correo y contrasena |
| `/login` | Publica | Inicio de sesion |
| `/perfil` | Privada | Perfil: nombre de usuario, avatar, juegos de interes y notificaciones |
| `/eventos` | Privada | Busqueda de eventos por categoria, fecha y ubicacion |
| `/eventos/{id}` | Privada | Detalle: descripcion, direccion, fecha y hora, organizadores, servicios y expositores |

Cualquier otra ruta requiere autenticacion y redirige a `/login`.

## Usuarios de prueba

| Correo | Contrasena (por defecto) | Roles |
|---|---|---|
| `ana@eventosgamer.cl` | `Gamer2026!` | USER |
| `diego@eventosgamer.cl` | `Gamer2026!` | USER |
| `admin@eventosgamer.cl` | `Admin2026!` | USER, ADMIN |

Las contrasenas se leen de las variables de entorno `APP_PASSWORD_USUARIO` y `APP_PASSWORD_ADMIN`.
Los valores por defecto son solo para desarrollo local. Tambien se pueden crear cuentas nuevas desde `/registro`.

## Medidas de seguridad implementadas

- Proteccion de URLs con `SecurityFilterChain`: solo inicio, registro y login son publicas.
- Contrasenas cifradas con `BCryptPasswordEncoder`, tanto las de prueba como las registradas.
- Proteccion CSRF en todos los formularios `POST` (incluido el logout).
- Validacion del lado del servidor en registro y perfil (largos, formato de correo, lista cerrada de avatares y juegos).
- Cabeceras de seguridad: `Content-Security-Policy`, `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`.
- Cookie de sesion `HttpOnly` y `SameSite=Lax`; las paginas de error no muestran stack traces.
- Thymeleaf escapa por defecto todo lo que se imprime con `th:text`.

## Ejecutar en local

```bash
./mvnw spring-boot:run
```

Abrir http://localhost:8080/home

## Tests

```bash
./mvnw test
```

Los tests verifican rutas publicas y privadas, login de los usuarios de prueba, registro, CSRF,
validaciones y cabeceras de seguridad.

## Despliegue en la maquina virtual con Docker

```bash
docker build -t eventos-gamer .
docker run -d --name eventos-gamer --restart unless-stopped -p 80:8080 \
  -e APP_PASSWORD_USUARIO='<contrasena>' -e APP_PASSWORD_ADMIN='<contrasena>' \
  eventos-gamer
```

La aplicacion queda disponible en `http://<IP-de-la-VM>/` (tambien responde en `/home` y `/recetas`).
Recordar abrir el puerto 80 en las reglas de red de la VM.

## Integrantes

- Tomas Murua
- Raul Low

# Los juegos como son (LJCS)

Aplicacion web segura que reune los eventos de la comunidad gamer en Chile: torneos, convenciones,
LAN parties y lanzamientos.

## Integrantes

- Tomas Murua ([@TomasMurua](https://github.com/TomasMurua))
- Raul Low ([@ralow-uc](https://github.com/ralow-uc))

## Stack

- Java 17 y Maven
- Spring Boot 3.5 (Spring Web)
- Spring Security 6 (autenticacion por formulario, contrasenas con BCrypt, CSRF, cabeceras de seguridad)
- Thymeleaf + `thymeleaf-extras-springsecurity6`
- Bean Validation para los formularios
- Tipografias Big Shoulders Display y Archivo (licencia OFL) servidas desde la propia aplicacion

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

| Usuario | Contrasena (por defecto) | Roles |
|---|---|---|
| `ana@eventosgamer.cl` | `Gamer2026!` | USER |
| `diego@eventosgamer.cl` | `Gamer2026!` | USER |
| `admin@eventosgamer.cl` | `Admin2026!` | USER, ADMIN |
| `llam` | sin valor por defecto | USER, ADMIN |

Las contrasenas se leen de las variables de entorno `APP_PASSWORD_USUARIO`, `APP_PASSWORD_ADMIN` y
`APP_PASSWORD_LLAM`. Los valores por defecto son solo para desarrollo local. El administrador `llam` solo
se crea si `APP_PASSWORD_LLAM` esta definida, asi su clave nunca queda en el codigo. El login acepta el
correo o el nombre de usuario. Tambien se pueden crear cuentas nuevas desde `/registro`.

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

Los tests verifican rutas publicas y privadas, login de los usuarios de prueba (incluido `llam` cuando
su variable esta definida), registro, CSRF, validaciones y cabeceras de seguridad.

## Despliegue en la maquina virtual con Docker

```bash
docker build -t eventos-gamer .
docker run -d --name eventos-gamer --restart unless-stopped -p 80:8080 \
  --env-file /ruta/a/eventos-gamer.env \
  eventos-gamer
```

El archivo `eventos-gamer.env` (con permisos `600`, fuera del repositorio) define `APP_PASSWORD_USUARIO`,
`APP_PASSWORD_ADMIN` y `APP_PASSWORD_LLAM`.

La aplicacion queda disponible en `http://<IP-de-la-VM>/` (tambien responde en `/home` y `/recetas`).
Recordar abrir el puerto 80 en las reglas de red de la VM.

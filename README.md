# Los juegos como son (LJCS)

Sitio que reune eventos de la comunidad gamer: torneos, convenciones, LAN parties, ferias retro,
lanzamientos y transmisiones online.

Sitio publicado: https://ljcs-eventos.eastus2.cloudapp.azure.com/eventos

La direccion `http://40.75.66.16/eventos` tambien funciona y redirige a la version HTTPS.

## Integrantes

- Tomas Murua ([@TomasMurua](https://github.com/TomasMurua))
- Raul Low ([@ralow-uc](https://github.com/ralow-uc))

## Arquitectura

```
Internet ──HTTPS──> proxy (Caddy) ──> frontend (Spring Boot + Thymeleaf) ──> backend (API REST + JWT) ──> MySQL
                         └──── /api/* ────────────────────────────────────────> backend
```

| Carpeta | Contenido |
|---|---|
| `frontend/` | Paginas Thymeleaf, Spring Security con login contra la API del backend |
| `backend/` | API REST con Spring Web, Spring Security, Spring Data JPA y JWT |
| `db/` | Scripts de creacion (`01_schema.sql`), datos (`02_datos.sql`) y usuario de la aplicacion (`03_usuario_app.sh`) |
| `proxy/` | Configuracion de Caddy: HTTPS con Let's Encrypt y enrutamiento |
| `docker-compose.yml` | Los cuatro contenedores y sus redes |

Solo el proxy publica puertos (80 y 443). El frontend y el backend se comunican por una red interna de
Docker, y MySQL queda en otra red interna sin salida a Internet.

## Stack

- Java 17, Maven y Spring Boot 3.5
- Spring Security 6 en las dos capas
- Spring Data JPA con MySQL 8.4
- JJWT 0.12 para firmar y validar los tokens con HS256
- Thymeleaf con `thymeleaf-extras-springsecurity6`
- Caddy 2.10 como proxy inverso con TLS automatico
- Docker y Docker Compose

## Funcionalidades y rutas

| Ruta | Acceso | Descripcion |
|---|---|---|
| `/eventos` (tambien `/`) | Publica | Inicio: eventos recien publicados, populares y destacados, avisos de marcas y acceso al login |
| `/eventos/buscar` | Publica | Busqueda por nombre, videojuego, categoria, pais o ciudad, rango de fechas y modalidad |
| `/eventos/{id}` | Privada | Detalle: descripcion, videojuego, fecha y hora, ubicacion o enlace, organizador, requisitos, inscripcion y fotos |
| `/perfil` | Privada | Datos del usuario que inicio sesion |
| `/admin/usuarios` | Solo ADMIN | Usuarios registrados |
| `/login` | Publica | Inicio de sesion |

### API del backend

| Metodo y ruta | Acceso | Descripcion |
|---|---|---|
| `POST /api/auth/login` | Publica | Recibe `{"username","password"}` y devuelve un token JWT valido por 30 minutos |
| `GET /api/portada` | Publica | Datos de la pagina de inicio |
| `GET /api/eventos` | Publica | Busqueda con los filtros `nombre`, `videojuego`, `categoria`, `lugar`, `desde`, `hasta`, `modalidad` |
| `GET /api/eventos/filtros` | Publica | Valores disponibles para los filtros |
| `GET /api/eventos/{id}` | JWT | Detalle del evento |
| `GET /api/usuarios/me` | JWT | Usuario dueno del token |
| `GET /api/admin/usuarios` | JWT con rol ADMIN | Listado de usuarios |

Las rutas privadas reciben el token en el header `Authorization: Bearer <token>`.

## Usuarios de prueba

| Usuario | Correo | Contrasena | Rol |
|---|---|---|---|
| `ana` | `ana@eventosgamer.cl` | `Gamer2026!` | USER |
| `diego` | `diego@eventosgamer.cl` | `Gamer2026!` | USER |
| `admin` | `admin@eventosgamer.cl` | se define con `APP_PASSWORD_ADMIN` | ADMIN |
| `llam` | `llam@eventosgamer.cl` | se define con `APP_PASSWORD_LLAM` (opcional) | ADMIN |

El login acepta el nombre de usuario o el correo. La cuenta `admin` no trae una clave usable en el script de
datos: la API se la asigna al arrancar desde la variable de entorno, asi su clave nunca queda en el repositorio.

## Seguridad

La tabla agrupa las medidas segun la categoria del OWASP Top 10 2021 que cubren.

| Categoria | Medidas |
|---|---|
| A01 Control de acceso roto | Rutas publicas y privadas definidas en `SecurityFilterChain` (frontend) y por API (backend); rol ADMIN para `/admin/**`; cada sesion web guarda su propio token JWT; el backend vuelve a leer el usuario y su rol desde la base de datos en cada peticion; usuario de MySQL con permisos minimos |
| A02 Fallas criptograficas | HTTPS con certificado de Let's Encrypt y HSTS; contrasenas con BCrypt; secreto JWT de al menos 256 bits leido desde el entorno, y la API no arranca sin el; cookie de sesion `Secure`, `HttpOnly` y `SameSite=Lax` |
| A03 Inyeccion | Consultas con JPA Criteria y parametros, nunca concatenando SQL; Thymeleaf escapa todo lo que imprime; validacion de largo y formato en los parametros; Content-Security-Policy sin scripts externos |
| A04 Diseno inseguro | Separacion en capas, el backend y la base de datos no se exponen; detalle de eventos solo con token |
| A05 Configuracion incorrecta | Sin modo debug ni usuario por defecto de Spring; paginas de error sin stack traces; cabeceras CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy y Permissions-Policy; contenedores sin root, de solo lectura y sin capacidades extra; sin claves en el codigo |
| A06 Componentes vulnerables | Spring Boot 3.5.16 y dependencias en su ultima version de parche; imagenes base oficiales |
| A07 Fallas de autenticacion | Mismo mensaje para usuario inexistente o clave incorrecta; bloqueo de 15 minutos tras 5 fallos de una cuenta desde la misma IP o 20 fallos desde una IP, sin dejar fuera al dueno de la cuenta que entra desde otra red; tokens de 30 minutos con emisor verificado; cuentas inactivas no pueden ingresar |
| A08 Integridad de software y datos | Tokens firmados con HS256 y algoritmo fijo; dependencias desde Maven Central con el wrapper versionado |
| A09 Registro y monitoreo | Registro de logins exitosos y fallidos, bloqueos, tokens invalidos y accesos denegados, sin contrasenas ni tokens y con los textos del usuario saneados antes de escribirlos |
| A10 SSRF | El servidor no descarga URLs que entregue el usuario; el backend solo es alcanzable desde la red interna |

## Ejecutar en local

Requiere Docker. Copiar `.env.example` a `.env` y completar las claves. Para probar en local sirve
`DOMINIO=localhost`, con el que Caddy genera un certificado propio.

```bash
cp .env.example .env
docker compose up -d --build
```

Abrir https://localhost/eventos

## Tests

```bash
cd backend && ./mvnw test
cd frontend && ./mvnw test
```

Los tests del backend cargan los mismos scripts de `db/` en una base H2 y prueban el login, el bloqueo por
intentos fallidos, la busqueda con un intento de inyeccion SQL, los permisos por rol y las cabeceras.
Los del frontend prueban rutas publicas y privadas, el login contra el backend, que cada sesion use su propio
token, el cierre de sesion cuando el token expira, CSRF y las cabeceras de seguridad.

## Despliegue en la maquina virtual

La VM corre Ubuntu 24.04 con Docker. Las imagenes del frontend y del backend se construyen en otro equipo
para `linux/amd64` y se copian a la VM; el archivo `.env` vive solo en la VM con permisos `600`.

```bash
docker compose up -d --no-build
```

En la red de Azure solo estan abiertos el 80 y el 443. SSH acepta conexiones desde una sola IP.

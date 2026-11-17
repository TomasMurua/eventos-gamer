# Los juegos como son (LJCS)

Sitio que reune eventos de la comunidad gamer: torneos, convenciones, LAN parties, ferias retro,
lanzamientos y transmisiones online. Los usuarios registrados publican sus eventos, les agregan fotos y videos,
comentan, valoran y comparten eventos en el sitio y en redes sociales.

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
| `backend/` | API REST con Spring Web, Spring Security, Spring Data JPA y JWT. Guarda las fotos y videos en un volumen propio |
| `db/` | Scripts de creacion (`01_schema.sql`), datos (`02_datos.sql`) y usuario de la aplicacion (`03_usuario_app.sh`). `migraciones/s5.sql` actualiza una base de la semana 3 sin borrar datos |
| `proxy/` | Configuracion de Caddy: HTTPS con Let's Encrypt y enrutamiento |
| `docker-compose.yml` | Los cuatro contenedores y sus redes |

Solo el proxy publica puertos (80 y 443). El frontend y el backend se comunican por una red interna de
Docker, y MySQL queda en otra red interna sin salida a Internet.

## Stack

- Java 17, Maven y Spring Boot 4.1
- Spring Security 7 en las dos capas
- Spring Data JPA con MySQL 8.4
- JJWT 0.12 para firmar y validar los tokens con HS256
- Thymeleaf con `thymeleaf-extras-springsecurity6`
- SonarQube Community Build, JaCoCo y OWASP Dependency-Check para el analisis del codigo
- Caddy 2.10 como proxy inverso con TLS automatico
- Docker y Docker Compose

## Funcionalidades y rutas

| Ruta | Acceso | Descripcion |
|---|---|---|
| `/eventos` (tambien `/`) | Publica | Inicio: eventos recien publicados, populares y destacados, avisos de marcas y acceso al login |
| `/eventos/buscar` | Publica | Busqueda por nombre, videojuego, categoria, pais o ciudad, rango de fechas y modalidad |
| `/eventos/{id}` | Privada | Detalle: descripcion, videojuego, fecha y hora, ubicacion o enlace, organizador, requisitos, inscripcion, fotos y videos, valoracion, comentarios y opciones para compartir |
| `/eventos/nuevo` | Privada | Publicar un evento |
| `/eventos/mios` | Privada | Eventos publicados por el usuario |
| `/eventos/{id}/medios` | Privada, solo el autor del evento o ADMIN | Subir una foto (JPG, PNG o WebP, hasta 5 MB) o un video (MP4 o WebM, hasta 7 MB) |
| `/eventos/{id}/comentarios` | Privada | Comentar. Cada usuario puede borrar sus comentarios; ADMIN puede borrar cualquiera |
| `/eventos/{id}/valoracion` | Privada | Valorar de 1 a 5 (una valoracion por usuario, se puede cambiar) |
| `/eventos/{id}/compartir` | Privada | Compartir en el muro de la comunidad. El detalle tambien trae enlaces para WhatsApp, X, Facebook, LinkedIn y Telegram |
| `/perfil` | Privada | Datos del usuario que inicio sesion |
| `/admin/usuarios` | Solo ADMIN | Usuarios registrados |
| `/registro` | Publica | Crear una cuenta (nombre completo, usuario, correo y contrasena) |
| `/login` | Publica | Inicio de sesion |

### API del backend

| Metodo y ruta | Acceso | Descripcion |
|---|---|---|
| `POST /api/auth/login` | Publica | Recibe `{"username","password"}` y devuelve un token JWT valido por 30 minutos |
| `POST /api/auth/registro` | Publica | Crea una cuenta con rol USER. Maximo 5 cuentas creadas por IP (o red /64 en IPv6) cada hora |
| `GET /api/portada` | Publica | Datos de la pagina de inicio, incluido el muro de la comunidad |
| `GET /api/eventos` | Publica | Busqueda con los filtros `nombre`, `videojuego`, `categoria`, `lugar`, `desde`, `hasta`, `modalidad` |
| `GET /api/eventos/filtros` | Publica | Valores disponibles para los filtros |
| `POST /api/eventos` | JWT | Publica un evento a nombre del usuario del token |
| `GET /api/eventos/mios` | JWT | Eventos publicados por el usuario del token |
| `GET /api/eventos/{id}` | JWT | Detalle del evento con fotos, videos, valoraciones y comentarios |
| `POST /api/eventos/{id}/medios` | JWT, autor o ADMIN | Sube una foto o un video (`multipart/form-data`) |
| `GET /api/eventos/{id}/medios/{medio}` | JWT | Archivo de la foto o el video (acepta `Range`) |
| `DELETE /api/eventos/{id}/medios/{medio}` | JWT, autor o ADMIN | Borra una foto o un video |
| `POST /api/eventos/{id}/comentarios` | JWT | Agrega un comentario |
| `DELETE /api/eventos/{id}/comentarios/{comentario}` | JWT, autor del comentario o ADMIN | Borra un comentario |
| `PUT /api/eventos/{id}/valoracion` | JWT | Guarda la valoracion del usuario (1 a 5) |
| `POST /api/eventos/{id}/compartidos` | JWT | Comparte el evento en el muro de la comunidad |
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
| A01 Control de acceso roto | Rutas publicas y privadas definidas en `SecurityFilterChain` (frontend) y por API (backend); rol ADMIN para `/admin/**`; cada sesion web guarda su propio token JWT; el backend vuelve a leer el usuario y su rol desde la base de datos en cada peticion; solo el autor del evento (o ADMIN) sube o borra fotos y videos, y solo el autor de un comentario (o ADMIN) lo borra; el autor de un evento sale del token, nunca del formulario; usuario de MySQL con permisos minimos (no puede borrar eventos ni usuarios) |
| A02 Fallas criptograficas | HTTPS con certificado de Let's Encrypt y HSTS; contrasenas con BCrypt; secreto JWT de al menos 256 bits leido desde el entorno, y la API no arranca sin el; cookie de sesion `Secure`, `HttpOnly` y `SameSite=Lax` |
| A03 Inyeccion | Consultas con JPA Criteria y parametros, nunca concatenando SQL; Thymeleaf escapa todo lo que imprime, incluidos comentarios y mensajes del muro; los enlaces de eventos online deben ser `https://`, asi no se puede guardar un `javascript:`; validacion de largo y formato en frontend y backend; Content-Security-Policy sin scripts externos |
| A04 Diseno inseguro | Separacion en capas, el backend y la base de datos no se exponen; detalle de eventos solo con token; las fotos y videos se reconocen por su firma (primeros bytes) y no por la extension, se guardan fuera del codigo con un nombre aleatorio, se sirven con `X-Content-Type-Options: nosniff` y sin cache; limites contra abuso: 5 cuentas nuevas por IP cada hora, 5 comentarios por minuto, 10 eventos al dia, 12 archivos por evento, 100 MB de archivos por usuario y un compartido por evento y usuario; cuerpo maximo de 9 MB para subir archivos y 1 MB para el resto (Caddy), `/tmp` de 64 MB y 448 MB de memoria por contenedor |
| A05 Configuracion incorrecta | Sin modo debug ni usuario por defecto de Spring; paginas de error sin stack traces; cabeceras CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy y Permissions-Policy; contenedores sin root, de solo lectura y sin capacidades extra; sin claves en el codigo |
| A06 Componentes vulnerables | Spring Boot 4.1.2 y `mysql-connector-j` 26.10.0, sin CVE conocidos segun OWASP Dependency-Check, que se ejecuta con `mvn verify` y falla si aparece una vulnerabilidad con CVSS 9 o mas; imagenes base oficiales |
| A07 Fallas de autenticacion | Mismo mensaje para usuario inexistente o clave incorrecta; bloqueo de 15 minutos tras 5 fallos de una cuenta desde la misma IP o 20 fallos desde una IP, sin dejar fuera al dueno de la cuenta que entra desde otra red; tokens de 30 minutos con emisor verificado; cuentas inactivas no pueden ingresar |
| A08 Integridad de software y datos | Tokens firmados con HS256 y algoritmo fijo; dependencias desde Maven Central con el wrapper versionado |
| A09 Registro y monitoreo | Registro de logins exitosos y fallidos, bloqueos, tokens invalidos y accesos denegados, sin contrasenas ni tokens y con los textos del usuario saneados antes de escribirlos |
| A10 SSRF | El servidor no descarga URLs que entregue el usuario; el backend solo es alcanzable desde la red interna |

### Riesgos aceptados

- El registro responde 409 si el usuario o el correo ya existen, asi que se puede averiguar si un correo esta
  registrado. El limite de registros por IP frena que alguien lo pruebe en masa.
- El correo no se verifica, porque el sitio no envia correos.
- Las fotos se guardan tal como se suben, con sus metadatos EXIF, que pueden incluir la ubicacion GPS. El formulario
  de subida se lo avisa al usuario.
- La API tiene la proteccion CSRF desactivada a proposito. No usa cookies ni sesiones, y el token viaja en el header
  `Authorization`, que el navegador no agrega por su cuenta. Un test comprueba que la API nunca entrega cookies.
  El frontend usa cookie de sesion y mantiene CSRF activo en todos los formularios.

## Ejecutar en local

Requiere Docker. Copiar `.env.example` a `.env` y completar las claves. Para probar en local sirve
`DOMINIO=localhost`, con el que Caddy genera un certificado propio.

```bash
cp .env.example .env
docker compose up -d --build
```

Abrir https://localhost/eventos

## Analisis estatico

SonarQube Community Build analiza los dos modulos (proyectos `ljcs-backend` y `ljcs-frontend`, con un quality gate
que exige cero problemas de seguridad) y OWASP Dependency-Check revisa sus dependencias:

```bash
docker run -d --name sonarqube -p 127.0.0.1:9000:9000 sonarqube:26.11.0.134175-community
export SONAR_TOKEN=<token de analisis>
cd backend && ./mvnw test sonar:sonar          # cobertura con JaCoCo y analisis de SonarQube
./mvnw dependency-check:check                     # reporte en target/dependency-check-report.html
```

## Tests

```bash
cd backend && ./mvnw test
cd frontend && ./mvnw test
```

Los tests del backend cargan los mismos scripts de `db/` en una base H2 y prueban el login, el bloqueo por
intentos fallidos, la busqueda con un intento de inyeccion SQL, los permisos por rol, las cabeceras, el registro
(rol USER aunque se intente enviar otro, clave debil, duplicados, limite por IP), la publicacion de eventos, que
solo el autor suba fotos, que un HTML disfrazado de PNG se rechace, comentarios, valoraciones y compartidos.
Los del frontend prueban rutas publicas y privadas, el login contra el backend, que cada sesion use su propio
token, el cierre de sesion cuando el token expira, CSRF en todos los formularios, que los comentarios se muestren
escapados y las cabeceras de seguridad.

## Despliegue en la maquina virtual

La VM corre Ubuntu 24.04 con Docker. Las imagenes del frontend y del backend se construyen en otro equipo
para `linux/amd64` y se copian a la VM; el archivo `.env` vive solo en la VM con permisos `600`. Las fotos y videos
quedan en el volumen `medios`.

```bash
docker compose up -d --no-build
```

La version de las semanas 4 y 5 cambia el esquema. Los scripts de `db/` solo corren con el volumen de MySQL vacio,
asi que una base que ya existe se actualiza con la migracion (se puede ejecutar mas de una vez):

```bash
docker compose exec -T db sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD"' < db/migraciones/s5.sql
```

En la red de Azure solo estan abiertos el 80 y el 443. SSH acepta conexiones desde una sola IP.

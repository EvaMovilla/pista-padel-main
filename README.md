# Sistema de Reserva de Pistas de Pádel

## Integrantes

- Eva Movilla
- Virginia Castejón
- Lucía Pérez-Maura

---

# Descripción del proyecto

Aplicación web desarrollada con Spring Boot y frontend HTML/CSS/JavaScript para la gestión de reservas de pistas de pádel.

El sistema permite:

- Registro y autenticación de usuarios.
- Gestión de roles USER y ADMIN.
- Consulta de disponibilidad de pistas.
- Creación, modificación y cancelación de reservas.
- Gestión de pistas y usuarios.
- Tareas programadas.
- Validación de errores HTTP.
- Pruebas automáticas.

---

# Usuarios de prueba

## ADMIN

| Email | Contraseña |
| :--- | :--- |
| admin@test.com | pass |

---

## USER

| Email | Contraseña |
| :--- | :--- |
| cliente@test.com | pass |

---

# Base de datos H2

| Campo | Valor |
| :--- | :--- |
| URL | http://localhost:8080/h2-console |
| User | SA |
| Password | (vacío) |

---

# Roles

| Rol | Permisos |
| :--- | :--- |
| USER | Ver pistas, crear/cancelar sus propias reservas y editar su perfil |
| ADMIN | Todo lo anterior + gestionar pistas y consultar todas las reservas |

---

# Seguridad

La aplicación utiliza Spring Security con autenticación basada en sesiones HTTP.

Las contraseñas se almacenan cifradas mediante BCryptPasswordEncoder.

Los endpoints están protegidos según el rol del usuario:

- USER: acceso a reservas y perfil propio.
- ADMIN: gestión completa de pistas y reservas.

---

# Ejecución del proyecto

## Backend

Ejecutar desde terminal:

```bash
mvn spring-boot:run
```

La API quedará disponible en:

```text
http://localhost:8080
```

---

## Frontend

Abrir los archivos HTML mediante Live Server o desde:

```text
http://127.0.0.1:3000
```

---

# Endpoints

## Auth

| Método | Ruta | Cuerpo | Descripción | Respuestas |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/pistaPadel/auth/register` | `{ "nombre", "apellidos", "email", "telefono", "password" }` | Registra un nuevo usuario | `201` idUsuario <br> `400` validación <br> `409` email duplicado |
| `POST` | `/pistaPadel/auth/login` | `{ "email", "password" }` | Inicia sesión y crea cookie de sesión | `200` OK <br> `401` credenciales incorrectas |
| `GET` | `/pistaPadel/auth/me` | — | Devuelve datos del usuario autenticado | `200` usuario <br> `401` no autenticado |
| `POST` | `/pistaPadel/auth/logout` | — | Cierra la sesión activa | `200` Logout correcto |

---

## Pistas (Courts)

| Método | Ruta | Cuerpo | Descripción | Respuestas |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/pistaPadel/courts` | — | Lista todas las pistas | `200` array de pistas |
| `GET` | `/pistaPadel/courts/{courtId}` | — | Obtiene una pista por ID | `200` pista <br> `404` no existe |
| `POST` | `/pistaPadel/courts` | `{ "nombre", "ubicacion", "precioHora", "activa" }` | Crea una nueva pista (ADMIN) | `201` pista creada <br> `403` no admin <br> `409` nombre duplicado |
| `PATCH` | `/pistaPadel/courts/{courtId}` | `{ "nombre"?, "ubicacion"?, "precioHora"?, "activa"? }` | Actualiza campos de una pista (ADMIN) | `200` pista actualizada <br> `403` no admin <br> `404` no existe <br> `409` nombre duplicado |
| `DELETE` | `/pistaPadel/courts/{courtId}` | — | Borra la pista físicamente o la desactiva si tiene reservas futuras (ADMIN) | `204` No Content <br> `403` no admin <br> `404` no existe |

---

## Reservas (Reservations)

| Método | Ruta | Cuerpo | Descripción | Respuestas |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/pistaPadel/reservations` | `{ "courtId", "date", "startTime", "durationMinutes" }` | Crea una reserva para el usuario autenticado | `201` reserva <br> `400` datos inválidos <br> `409` solapamiento de horario |
| `GET` | `/pistaPadel/reservations` | — | Lista las reservas del usuario autenticado. Params opcionales: `from`, `to` | `200` array de reservas |
| `GET` | `/pistaPadel/reservations/{reservationId}` | — | Obtiene una reserva por ID | `200` reserva <br> `403` no es el propietario <br> `404` no existe |
| `PATCH` | `/pistaPadel/reservations/{reservationId}` | `{ "fechaReserva", "horaInicio", "duracionMinutos" }` | Reprograma una reserva | `200` reserva actualizada <br> `403` no autorizado <br> `404` no existe <br> `409` solapamiento |
| `DELETE` | `/pistaPadel/reservations/{reservationId}` | — | Cancela una reserva (cambia estado a CANCELADA) | `204` No Content <br> `403` no es el propietario <br> `404` no existe |
| `GET` | `/pistaPadel/admin/reservations` | — | Lista todas las reservas con filtros opcionales: `date`, `courtId`, `userId` (ADMIN) | `200` array de reservas <br> `403` no admin |

---

## Disponibilidad (Availability)

| Método | Ruta | Cuerpo | Descripción | Respuestas |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/pistaPadel/availability` | — | Devuelve disponibilidad de todas las pistas (o una) para una fecha. Params: `date` (obligatorio), `courtId` (opcional) | `200` disponibilidad <br> `400` falta date |
| `GET` | `/pistaPadel/courts/{courtId}/availability` | — | Devuelve disponibilidad de una pista concreta para una fecha. Param: `date` (obligatorio) | `200` disponibilidad <br> `404` pista no existe |

---

## Usuarios (Users)

| Método | Ruta | Cuerpo | Descripción | Respuestas |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/pistaPadel/users` | — | Lista todos los usuarios (ADMIN) | `200` array de usuarios <br> `403` no admin |
| `GET` | `/pistaPadel/users/{userId}` | — | Obtiene un usuario por ID. USER solo puede ver el suyo | `200` usuario <br> `403` no autorizado <br> `404` no existe |
| `PATCH` | `/pistaPadel/users/{userId}` | `{ "nombre"?, "apellidos"?, "telefono"?, "password"? }` | Actualiza datos de un usuario. USER solo puede editar el suyo | `200` usuario actualizado <br> `403` no autorizado <br> `404` no existe |

---

## Health

| Método | Ruta | Cuerpo | Descripción | Respuestas |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/pistaPadel/health` | — | Comprueba que la API está activa | `200` "OK" |

---

# Gestión de errores

| Código | Significado |
| :--- | :--- |
| 400 | Datos inválidos |
| 401 | Usuario no autenticado |
| 403 | Usuario sin permisos |
| 404 | Recurso inexistente |
| 409 | Conflicto de negocio |

---

# Tareas programadas

La aplicación incluye tareas programadas mediante `@Scheduled`:

- Recordatorio diario de reservas a las 02:00.
- Envío mensual de disponibilidad de pistas el día 1 de cada mes.

Cuando `mail.enabled=false`, los correos se simulan mediante logs.

---

# Tests

Ejecutar:

```bash
mvn test
```

Las pruebas automáticas cubren:

- AuthControllerTest
- UsersControllerTest
- CourtsControllerTest
- ReservaServiceTest
- PistaServiceTest
- UsuarioServiceTest
- EmailSchedulerTest

Resultado esperado:

```text
BUILD SUCCESS
```

---

# Tecnologías utilizadas

## Backend

- Java 21 (Amazon Corretto)
- Spring Boot 3.4.2
- Spring Security
- Spring Data JPA + Hibernate
- H2 Database
- Maven

---

## Frontend

- HTML5
- CSS3
- JavaScript

---

## Testing

- JUnit 5
- Mockito
- MockMvc
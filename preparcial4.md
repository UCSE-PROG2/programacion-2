# Ejercicio TP 4 — Sistema de gestión de un hotel

## Paso 0 — Repositorio

Antes de escribir una línea de código, creá un repositorio **privado** en GitHub con el nombre `hotel-api` y agregá a **maximilianolovera@gmail.com** como colaborador con rol de lectura. La entrega se realiza a través de ese repositorio.

---

## Descripción del sistema

Un hotel necesita una API REST para gestionar sus habitaciones y las reservas de sus huéspedes. La API debe permitir registrar habitaciones, crear y consultar reservas con filtros opcionales, cancelar reservas y obtener un reporte agrupado por tipo de habitación.

---

## Base de datos

Crear el esquema `hotel` en MySQL y ejecutar el siguiente script:

```sql
CREATE SCHEMA IF NOT EXISTS hotel;

CREATE TABLE hotel.habitacion (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero           INT          NOT NULL,
    tipo             VARCHAR(50)  NOT NULL,
    precio_por_noche DOUBLE       NOT NULL,
    descripcion      VARCHAR(200),
    disponible       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE hotel.reserva (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    habitacion_id   BIGINT       NOT NULL,
    nombre_huesped  VARCHAR(100) NOT NULL,
    email_huesped   VARCHAR(100) NOT NULL,
    fecha_entrada   VARCHAR(20)  NOT NULL,
    fecha_salida    VARCHAR(20)  NOT NULL,
    total_pagado    DOUBLE       NOT NULL,
    estado          VARCHAR(30)  NOT NULL DEFAULT 'CONFIRMADA',
    FOREIGN KEY (habitacion_id) REFERENCES hotel.habitacion(id)
);

INSERT INTO hotel.habitacion (numero, tipo, precio_por_noche, descripcion, disponible) VALUES
(101, 'Simple',  8500.00,  'Habitación individual con vista al jardín',   TRUE),
(102, 'Simple',  8500.00,  'Habitación individual con vista a la calle',  TRUE),
(201, 'Doble',  14000.00,  'Habitación doble con balcón',                 TRUE),
(202, 'Doble',  14000.00,  'Habitación doble superior',                   TRUE),
(301, 'Suite',  28000.00,  'Suite presidencial con jacuzzi',              TRUE),
(302, 'Suite',  28000.00,  'Suite junior con terraza',                    TRUE);

INSERT INTO hotel.reserva (habitacion_id, nombre_huesped, email_huesped, fecha_entrada, fecha_salida, total_pagado, estado) VALUES
(1, 'Ana García',        'ana@mail.com',    '2026-07-01', '2026-07-04',  25500.00,  'CONFIRMADA'),
(2, 'Luis Pérez',        'luis@mail.com',   '2026-07-05', '2026-07-07',  17000.00,  'CONFIRMADA'),
(3, 'María López',       'maria@mail.com',  '2026-07-10', '2026-07-15',  70000.00,  'CONFIRMADA'),
(4, 'Carlos Ruiz',       'carlos@mail.com', '2026-07-12', '2026-07-14',  28000.00,  'CANCELADA'),
(5, 'Sofía Martín',      'sofia@mail.com',  '2026-08-01', '2026-08-05', 112000.00,  'CONFIRMADA'),
(6, 'Diego Torres',      'diego@mail.com',  '2026-08-10', '2026-08-12',  56000.00,  'CONFIRMADA'),
(1, 'Valentina Cruz',    'vale@mail.com',   '2026-08-15', '2026-08-18',  25500.00,  'CONFIRMADA'),
(3, 'Facundo Gómez',     'facu@mail.com',   '2026-09-01', '2026-09-03',  28000.00,  'PENDIENTE'),
(5, 'Camila Sosa',       'cami@mail.com',   '2026-09-15', '2026-09-20', 140000.00,  'PENDIENTE'),
(2, 'Tomás Fernández',   'tomas@mail.com',  '2026-10-01', '2026-10-03',  17000.00,  'CONFIRMADA');
```

---

## Crear el proyecto

Generar el proyecto desde [Spring Initializr](https://start.spring.io) con las dependencias: **Spring Web**, **Spring Data JPA**, **MySQL Driver**, **Validation** y **Lombok**.

Configurar la conexión a la base de datos en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hotel
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

---

## Estructura del proyecto

Respetar la siguiente arquitectura en capas:

```
src/main/java/com/hotel/
├── model/
│   ├── Habitacion.java
│   └── Reserva.java
├── repository/
│   ├── HabitacionRepository.java
│   └── ReservaRepository.java
├── service/
│   └── HotelService.java
├── controller/
│   ├── HabitacionController.java
│   └── ReservaController.java
├── exception/
│   └── GlobalExceptionHandler.java
└── HotelApplication.java
```

---

## Entidades (`model/`)

**`Habitacion`** — representa una habitación del hotel. Campos: numero, tipo, precioPorNoche, descripcion y disponible. Debe estar mapeada a la tabla `hotel.habitacion` con las anotaciones JPA correspondientes.

**`Reserva`** — representa una reserva de un huésped. Campos: nombreHuesped, emailHuesped, fechaEntrada, fechaSalida, totalPagado y estado. Debe tener una relación `@ManyToOne` hacia `Habitacion` y estar mapeada a la tabla `hotel.reserva`.

---

## Capa de repositorio (`repository/`)

**`HabitacionRepository`** — extiende `JpaRepository<Habitacion, Long>`. Además de los métodos heredados, debe agregar:

- Un método de consulta que filtre habitaciones por tipo y/o disponibilidad. Ambos parámetros son opcionales: si alguno no se informa, no se aplica ese filtro.

**`ReservaRepository`** — extiende `JpaRepository<Reserva, Long>`. Además de los métodos heredados, debe agregar:

- Un método que devuelva todas las reservas de una habitación específica, recibiendo el ID de la habitación como parámetro.

---

## Capa de servicio (`service/`)

**`HotelService`** — coordina la lógica de negocio y delega la persistencia a ambos repositorios. Debe exponer los siguientes métodos:

- **`registrarHabitacion`** — recibe los datos de la habitación y la persiste.
- **`buscarHabitacionPorId`** — devuelve la habitación o lanza una excepción si no existe.
- **`listarHabitaciones`** — recibe dos filtros opcionales: tipo y disponible. Si ninguno está presente, devuelve todas las habitaciones.
- **`actualizarHabitacion`** — actualiza los campos de una habitación existente. Si no existe, lanza una excepción.
- **`eliminarHabitacion`** — elimina la habitación por ID. Si no existe, lanza una excepción. Si la habitación tiene reservas con estado `CONFIRMADA` o `PENDIENTE`, debe lanzar una excepción indicando que no puede eliminarse.
- **`registrarReserva`** — recibe el ID de la habitación y los datos de la reserva. Verifica que la habitación exista (lanza excepción si no) y persiste la reserva vinculada a esa habitación.
- **`buscarReservaPorId`** — devuelve la reserva o lanza una excepción si no existe.
- **`buscarReservasPorHabitacion`** — devuelve todas las reservas de una habitación dado su ID. Si la habitación no existe, lanza una excepción.
- **`buscarReservas`** — recibe tres filtros opcionales: estado, tipo de habitación y fechaDesde. Cada filtro solo se aplica si fue enviado; si ninguno está presente, devuelve todas las reservas. Implementar esta consulta usando **CriteriaBuilder** (`EntityManager`, `CriteriaQuery`, `Predicate`) para construir los predicados dinámicamente según los parámetros presentes.
- **`cancelarReserva`** — cambia el estado de una reserva existente a `CANCELADA`. Si no existe, lanza una excepción.
- **`reportePorTipo`** — devuelve, agrupado por tipo de habitación: nombre del tipo, cantidad de reservas, ingresos totales (suma de `totalPagado`) y precio promedio por noche de las habitaciones de ese tipo.

---

## Endpoints esperados

### Habitaciones

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/habitaciones` | Listar todas las habitaciones |
| GET | `/habitaciones/{id}` | Buscar habitación por ID |
| GET | `/habitaciones?tipo=...` | Filtrar por tipo (Simple, Doble, Suite) |
| GET | `/habitaciones?disponible=...` | Filtrar por disponibilidad |
| POST | `/habitaciones` | Crear habitación |
| PUT | `/habitaciones/{id}` | Actualizar habitación |
| DELETE | `/habitaciones/{id}` | Eliminar habitación |

### Reservas

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/reservas` | Listar todas las reservas |
| GET | `/reservas/{id}` | Buscar reserva por ID |
| GET | `/habitaciones/{habitacionId}/reservas` | Listar reservas de una habitación |
| GET | `/reservas?estado=...` | Filtrar por estado (CONFIRMADA, PENDIENTE, CANCELADA) |
| GET | `/reservas?tipo=...` | Filtrar por tipo de habitación |
| GET | `/reservas?fechaDesde=...` | Reservas con fecha de entrada igual o posterior |
| POST | `/reservas` | Crear reserva (el cuerpo debe incluir `habitacionId`) |
| PATCH | `/reservas/{id}/cancelar` | Cancelar una reserva |
| DELETE | `/reservas/{id}` | Eliminar reserva |

### Reporte

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/reporte/tipos` | Cantidad de reservas, ingresos totales y precio promedio por noche agrupados por tipo de habitación |

Los tres filtros de `/reservas` son combinables entre sí: si se envían varios, se aplican todos simultáneamente.

---

## Consideraciones técnicas

- Los endpoints de creación deben devolver **HTTP 201 Created** con el recurso creado en el cuerpo.
- El endpoint de cancelación (`PATCH /reservas/{id}/cancelar`) debe devolver **HTTP 200 OK** con la reserva actualizada.
- El endpoint de eliminación debe devolver **HTTP 204 No Content**.
- Si un recurso no existe, la respuesta debe ser **HTTP 404 Not Found** con un mensaje que indique qué no se encontró.
- Si se intenta eliminar una habitación con reservas activas, la respuesta debe ser **HTTP 409 Conflict** con un mensaje descriptivo.
- Implementar un manejador global de excepciones con `@RestControllerAdvice` que intercepte las excepciones del service y devuelva el código y mensaje apropiados.
- Validar que el número de habitación sea mayor a cero y que el precio por noche sea mayor a cero. Para las reservas, validar que el nombre y email del huésped no estén vacíos y que el total pagado sea mayor a cero. Si la validación falla, la respuesta debe ser **HTTP 400 Bad Request**.

---

## Tests unitarios

Escribir tests unitarios para la capa de servicio (`HotelService`) que cubran los distintos escenarios de cada método. La cobertura de líneas del servicio debe ser **igual o superior al 80%**.

- Usar **JUnit 5** (ya incluido en el starter de pruebas de Spring Boot).
- Usar **H2** como base de datos en memoria para los tests, tal como se explica en la sección 8 del README de la unidad 2. Agregar la dependencia `testImplementation` de H2 y configurar un `application.properties` en `src/test/resources/` apuntando a H2.
- Verificar tanto los caminos exitosos como los casos de error (por ejemplo, que se lance la excepción correcta cuando la habitación no existe, o cuando se intenta eliminar una habitación con reservas activas).

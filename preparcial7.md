# Ejercicio TP 4 — Sistema de gestión de una clínica veterinaria

## Paso 0 — Repositorio

Antes de escribir una línea de código, creá un repositorio **privado** en GitHub con el nombre `veterinaria-api` y agregá a **maximilianolovera@gmail.com** como colaborador con rol de lectura. La entrega se realiza a través de ese repositorio.

---

## Descripción del sistema

Una clínica veterinaria necesita una API REST para gestionar sus mascotas registradas y las consultas médicas. La API debe permitir registrar mascotas, crear y consultar turnos con filtros opcionales, cancelarlos y obtener un reporte agrupado por especie.

---

## Base de datos

Crear el esquema `veterinaria` en MySQL y ejecutar el siguiente script:

```sql
CREATE SCHEMA IF NOT EXISTS veterinaria;

CREATE TABLE veterinaria.mascota (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL,
    especie           VARCHAR(50)  NOT NULL,
    raza              VARCHAR(100) NOT NULL,
    propietario       VARCHAR(150) NOT NULL,
    email_propietario VARCHAR(100) NOT NULL,
    edad_anios        INT          NOT NULL
);

CREATE TABLE veterinaria.consulta (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    mascota_id         BIGINT       NOT NULL,
    nombre_veterinario VARCHAR(150) NOT NULL,
    fecha_consulta     VARCHAR(20)  NOT NULL,
    motivo             VARCHAR(200) NOT NULL,
    diagnostico        VARCHAR(300),
    costo_consulta     DOUBLE       NOT NULL,
    estado             VARCHAR(30)  NOT NULL DEFAULT 'PENDIENTE',
    FOREIGN KEY (mascota_id) REFERENCES veterinaria.mascota(id)
);

INSERT INTO veterinaria.mascota (nombre, especie, raza, propietario, email_propietario, edad_anios) VALUES
('Rex',   'Perro',  'Pastor Alemán',     'Carlos García',   'carlos@mail.com',  5),
('Luna',  'Gato',   'Siamés',            'Ana López',       'ana@mail.com',     3),
('Max',   'Perro',  'Golden Retriever',  'Luis Torres',     'luis@mail.com',    2),
('Mia',   'Gato',   'Persa',             'Sofía Ruiz',      'sofia@mail.com',   7),
('Kiwi',  'Ave',    'Cotorra Argentina', 'Pedro Martín',    'pedro@mail.com',   4),
('Spike', 'Perro',  'Bulldog Inglés',    'Valentina Díaz',  'vale@mail.com',    6),
('Nemo',  'Reptil', 'Tortuga Griega',    'Facundo Sosa',    'facu@mail.com',    10),
('Milo',  'Perro',  'Beagle',            'Camila Gómez',    'cami@mail.com',    1),
('Cleo',  'Gato',   'Bengalí',           'Tomás Fernández', 'tomas@mail.com',   2),
('Paco',  'Ave',    'Cacatúa',           'Romina Castro',   'romi@mail.com',    8);

INSERT INTO veterinaria.consulta (mascota_id, nombre_veterinario, fecha_consulta, motivo, diagnostico, costo_consulta, estado) VALUES
(1, 'Dra. Martínez', '2026-05-10', 'Control anual',        'Buen estado general',  8500.00,  'COMPLETADA'),
(2, 'Dr. Pérez',     '2026-05-15', 'Pérdida de apetito',   'Gastritis leve',      12000.00,  'COMPLETADA'),
(3, 'Dra. Martínez', '2026-05-20', 'Vacuna antirrábica',   'Vacuna aplicada',      6000.00,  'COMPLETADA'),
(4, 'Dr. Rodríguez', '2026-05-25', 'Revisión ocular',      'Conjuntivitis',        9500.00,  'COMPLETADA'),
(3, 'Dra. Martínez', '2026-06-02', 'Diarrea persistente',  'Gastroenteritis',     11000.00,  'COMPLETADA'),
(2, 'Dr. Pérez',     '2026-06-08', 'Control post-cirugía', 'Recuperación normal', 14000.00,  'COMPLETADA'),
(1, 'Dra. Martínez', '2026-07-01', 'Cojea pata trasera',   NULL,                   8500.00,  'PENDIENTE'),
(6, 'Dr. Pérez',     '2026-07-05', 'Control de piel',      NULL,                  10000.00,  'PENDIENTE'),
(5, 'Dra. López',    '2026-07-10', 'Control de plumaje',   NULL,                   7500.00,  'PENDIENTE'),
(8, 'Dr. Rodríguez', '2026-07-12', 'Primera consulta',     NULL,                   8000.00,  'CANCELADA');
```

---

## Crear el proyecto

Generar el proyecto desde [Spring Initializr](https://start.spring.io) con la siguiente configuración:

- **Group**: `com`
- **Artifact**: `veterinaria`
- **Package name**: `com.veterinaria`
- **Dependencies**: Spring Web, Spring Data JPA, MySQL Driver, Validation, Lombok

Configurar la conexión a la base de datos en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/veterinaria
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

---

## Estructura del proyecto

Respetar la siguiente arquitectura en capas:

```
src/main/java/com/veterinaria/
├── model/
│   ├── Mascota.java
│   └── Consulta.java
├── repository/
│   ├── MascotaRepository.java
│   └── ConsultaRepository.java
├── service/
│   └── VeterinariaService.java
├── controller/
│   ├── MascotaController.java
│   └── ConsultaController.java
├── exception/
│   └── GlobalExceptionHandler.java
└── VeterinariaApplication.java
```

---

## Entidades (`model/`)

**`Mascota`** — representa una mascota registrada en la clínica. Campos: nombre, especie, raza, propietario, emailPropietario y edadAnios. Debe estar mapeada a la tabla `veterinaria.mascota` con las anotaciones JPA correspondientes.

**`Consulta`** — representa una consulta médica. Campos: nombreVeterinario, fechaConsulta, motivo, diagnostico, costoConsulta y estado. Debe tener una relación `@ManyToOne` hacia `Mascota` y estar mapeada a la tabla `veterinaria.consulta`. Los valores válidos de estado son: `PENDIENTE`, `COMPLETADA` y `CANCELADA`.

---

## Capa de repositorio (`repository/`)

**`MascotaRepository`** — extiende `JpaRepository<Mascota, Long>`. Además de los métodos heredados, debe agregar:

- Un método de consulta que filtre mascotas por especie y/o propietario (búsqueda parcial en el nombre del propietario, sin distinción de mayúsculas). Ambos parámetros son opcionales: si alguno no se informa, no se aplica ese filtro.

**`ConsultaRepository`** — extiende `JpaRepository<Consulta, Long>`. Además de los métodos heredados, debe agregar:

- Un método que devuelva todas las consultas de una mascota específica, recibiendo el ID de la mascota como parámetro.

---

## Capa de servicio (`service/`)

**`VeterinariaService`** — coordina la lógica de negocio y delega la persistencia a ambos repositorios. Debe exponer los siguientes métodos:

- **`registrarMascota`** — recibe los datos de la mascota y la persiste.
- **`buscarMascotaPorId`** — devuelve la mascota o lanza una excepción si no existe.
- **`listarMascotas`** — recibe dos filtros opcionales: especie (coincidencia exacta) y propietario (búsqueda parcial, sin distinción de mayúsculas). Si ninguno está presente, devuelve todas las mascotas.
- **`actualizarMascota`** — actualiza los campos de una mascota existente. Si no existe, lanza una excepción.
- **`eliminarMascota`** — elimina la mascota por ID. Si no existe, lanza una excepción. Si la mascota tiene consultas con estado `PENDIENTE`, debe lanzar una excepción indicando que no puede eliminarse.
- **`registrarConsulta`** — recibe el ID de la mascota y los datos de la consulta. Verifica que la mascota exista (lanza excepción si no) y persiste la consulta vinculada a esa mascota.
- **`buscarConsultaPorId`** — devuelve la consulta o lanza una excepción si no existe.
- **`buscarConsultasPorMascota`** — devuelve todas las consultas de una mascota dado su ID. Si la mascota no existe, lanza una excepción.
- **`buscarConsultas`** — recibe tres filtros opcionales: estado, especie de la mascota y fechaDesde. Cada filtro solo se aplica si fue enviado; si ninguno está presente, devuelve todas las consultas. Implementar esta consulta usando **CriteriaBuilder** (`EntityManager`, `CriteriaQuery`, `Predicate`) para construir los predicados dinámicamente según los parámetros presentes.
- **`cancelarConsulta`** — cambia el estado de una consulta existente a `CANCELADA`. Si no existe, lanza una excepción.
- **`reportePorEspecie`** — devuelve, agrupado por especie: nombre de la especie, cantidad de consultas, ingresos totales (suma de `costoConsulta`) y costo promedio por consulta.

---

## Endpoints esperados

### Mascotas

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/mascotas` | Listar todas las mascotas |
| GET | `/mascotas/{id}` | Buscar mascota por ID |
| GET | `/mascotas?especie=...` | Filtrar por especie (exacta) |
| GET | `/mascotas?propietario=...` | Filtrar por propietario (parcial) |
| POST | `/mascotas` | Crear mascota |
| PUT | `/mascotas/{id}` | Actualizar mascota |
| DELETE | `/mascotas/{id}` | Eliminar mascota |

### Consultas

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/consultas` | Listar todas las consultas |
| GET | `/consultas/{id}` | Buscar consulta por ID |
| GET | `/mascotas/{mascotaId}/consultas` | Listar consultas de una mascota |
| GET | `/consultas?estado=...` | Filtrar por estado (PENDIENTE, COMPLETADA, CANCELADA) |
| GET | `/consultas?especie=...` | Filtrar por especie de la mascota |
| GET | `/consultas?fechaDesde=...` | Consultas con fecha de consulta igual o posterior |
| POST | `/consultas` | Crear consulta (el cuerpo debe incluir `mascotaId`) |
| PATCH | `/consultas/{id}/cancelar` | Cancelar una consulta |
| DELETE | `/consultas/{id}` | Eliminar consulta |

### Reporte

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/reporte/especies` | Cantidad de consultas, ingresos totales y costo promedio por especie |

Los tres filtros de `/consultas` son combinables entre sí: si se envían varios, se aplican todos simultáneamente.

---

## Consideraciones técnicas

- Los endpoints de creación deben devolver **HTTP 201 Created** con el recurso creado en el cuerpo.
- El endpoint de cancelación (`PATCH /consultas/{id}/cancelar`) debe devolver **HTTP 200 OK** con la consulta actualizada.
- El endpoint de eliminación debe devolver **HTTP 204 No Content**.
- Si un recurso no existe, la respuesta debe ser **HTTP 404 Not Found** con un mensaje que indique qué no se encontró.
- Si se intenta eliminar una mascota con consultas en estado `PENDIENTE`, la respuesta debe ser **HTTP 409 Conflict** con un mensaje descriptivo.
- Implementar un manejador global de excepciones con `@RestControllerAdvice` que intercepte las excepciones del service y devuelva el código y mensaje apropiados.
- Validar que el nombre y la especie de la mascota no estén vacíos y que la edad sea mayor o igual a cero. Para las consultas, validar que el nombre del veterinario y el motivo no estén vacíos y que el costo sea mayor a cero. Si la validación falla, la respuesta debe ser **HTTP 400 Bad Request**.

---

## Tests unitarios

Escribir tests unitarios para la capa de servicio (`VeterinariaService`) que cubran los distintos escenarios de cada método. La cobertura de líneas del servicio debe ser **igual o superior al 80%**.

- Usar **JUnit 5** (ya incluido en el starter de pruebas de Spring Boot).
- Usar **H2** como base de datos en memoria para los tests, tal como se explica en la sección 8 del README de la unidad 2. Agregar la dependencia `testImplementation` de H2 y configurar un `application.properties` en `src/test/resources/` apuntando a H2.
- Verificar tanto los caminos exitosos como los casos de error (por ejemplo, que se lance la excepción correcta cuando la mascota no existe, o cuando se intenta eliminar una mascota con consultas pendientes).

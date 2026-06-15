# Ejercicio TP 4 — Sistema de gestión de un cine

## Paso 0 — Repositorio

Antes de escribir una línea de código, creá un repositorio **privado** en GitHub con el nombre `cine-api` y agregá a **maximilianolovera@gmail.com** como colaborador con rol de lectura. La entrega se realiza a través de ese repositorio.

---

## Descripción del sistema

Un cine necesita una API REST para gestionar su cartelera de películas y las funciones programadas. La API debe permitir registrar películas, crear y consultar funciones con filtros opcionales, cancelarlas y obtener un reporte agrupado por género.

---

## Base de datos

Crear el esquema `cine` en MySQL y ejecutar el siguiente script:

```sql
CREATE SCHEMA IF NOT EXISTS cine;

CREATE TABLE cine.pelicula (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo        VARCHAR(150) NOT NULL,
    director      VARCHAR(100) NOT NULL,
    genero        VARCHAR(80)  NOT NULL,
    clasificacion VARCHAR(10)  NOT NULL,
    duracion      INT          NOT NULL,
    idioma        VARCHAR(50)  NOT NULL
);

CREATE TABLE cine.funcion (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    pelicula_id      BIGINT      NOT NULL,
    sala             VARCHAR(50) NOT NULL,
    fecha_funcion    VARCHAR(20) NOT NULL,
    horario          VARCHAR(10) NOT NULL,
    precio           DOUBLE      NOT NULL,
    butacas_vendidas INT         NOT NULL DEFAULT 0,
    estado           VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADA',
    FOREIGN KEY (pelicula_id) REFERENCES cine.pelicula(id)
);

INSERT INTO cine.pelicula (titulo, director, genero, clasificacion, duracion, idioma) VALUES
('Oppenheimer',                       'Christopher Nolan', 'Drama',           '+13', 180, 'Inglés'),
('Spider-Man: No Way Home',           'Jon Watts',         'Acción',          'ATP', 148, 'Inglés'),
('Everything Everywhere All at Once', 'The Daniels',       'Ciencia Ficción', '+13', 139, 'Inglés'),
('El Conde',                          'Pablo Larraín',     'Drama',           '+16', 110, 'Español'),
('Barbie',                            'Greta Gerwig',      'Comedia',         'ATP', 114, 'Inglés'),
('Avatar: El Camino del Agua',        'James Cameron',     'Acción',          'ATP', 192, 'Inglés'),
('Argentina, 1985',                   'Santiago Mitre',    'Drama',           '+13', 140, 'Español'),
('Killers of the Flower Moon',        'Martin Scorsese',   'Crimen',          '+16', 206, 'Inglés');

INSERT INTO cine.funcion (pelicula_id, sala, fecha_funcion, horario, precio, butacas_vendidas, estado) VALUES
(1, 'Sala 1', '2026-06-10', '18:00', 3500.00, 120, 'FINALIZADA'),
(1, 'Sala 1', '2026-06-10', '21:30', 3500.00,  95, 'FINALIZADA'),
(2, 'Sala 3', '2026-06-12', '16:00', 3200.00, 150, 'FINALIZADA'),
(5, 'Sala 2', '2026-06-15', '14:00', 3200.00,  80, 'FINALIZADA'),
(6, 'IMAX',   '2026-06-18', '20:00', 5500.00, 200, 'FINALIZADA'),
(3, 'Sala 1', '2026-06-20', '19:00', 3200.00,  60, 'CANCELADA'),
(1, 'Sala 2', '2026-07-20', '18:00', 3500.00,   0, 'PROGRAMADA'),
(4, 'Sala 3', '2026-07-21', '20:00', 3500.00,   0, 'PROGRAMADA'),
(7, 'Sala 1', '2026-07-22', '18:30', 3200.00,   0, 'PROGRAMADA'),
(8, 'IMAX',   '2026-07-25', '21:00', 5500.00,   0, 'PROGRAMADA');
```

---

## Crear el proyecto

Generar el proyecto desde [Spring Initializr](https://start.spring.io) con la siguiente configuración:

- **Group**: `com`
- **Artifact**: `cine`
- **Package name**: `com.cine`
- **Dependencies**: Spring Web, Spring Data JPA, MySQL Driver, Validation, Lombok

Configurar la conexión a la base de datos en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cine
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

---

## Estructura del proyecto

Respetar la siguiente arquitectura en capas:

```
src/main/java/com/cine/
├── model/
│   ├── Pelicula.java
│   └── Funcion.java
├── repository/
│   ├── PeliculaRepository.java
│   └── FuncionRepository.java
├── service/
│   └── CineService.java
├── controller/
│   ├── PeliculaController.java
│   └── FuncionController.java
├── exception/
│   └── GlobalExceptionHandler.java
└── CineApplication.java
```

---

## Entidades (`model/`)

**`Pelicula`** — representa una película en cartelera. Campos: titulo, director, genero, clasificacion, duracion (en minutos) e idioma. Debe estar mapeada a la tabla `cine.pelicula` con las anotaciones JPA correspondientes.

**`Funcion`** — representa una función programada. Campos: sala, fechaFuncion, horario, precio, butacasVendidas y estado. Debe tener una relación `@ManyToOne` hacia `Pelicula` y estar mapeada a la tabla `cine.funcion`. Los valores válidos de estado son: `PROGRAMADA`, `FINALIZADA` y `CANCELADA`.

---

## Capa de repositorio (`repository/`)

**`PeliculaRepository`** — extiende `JpaRepository<Pelicula, Long>`. Además de los métodos heredados, debe agregar:

- Un método de consulta que filtre películas por género y/o clasificación. Ambos parámetros son opcionales: si alguno no se informa, no se aplica ese filtro.

**`FuncionRepository`** — extiende `JpaRepository<Funcion, Long>`. Además de los métodos heredados, debe agregar:

- Un método que devuelva todas las funciones de una película específica, recibiendo el ID de la película como parámetro.

---

## Capa de servicio (`service/`)

**`CineService`** — coordina la lógica de negocio y delega la persistencia a ambos repositorios. Debe exponer los siguientes métodos:

- **`registrarPelicula`** — recibe los datos de la película y la persiste.
- **`buscarPeliculaPorId`** — devuelve la película o lanza una excepción si no existe.
- **`listarPeliculas`** — recibe dos filtros opcionales: genero (coincidencia exacta) y clasificacion (coincidencia exacta). Si ninguno está presente, devuelve todas las películas.
- **`actualizarPelicula`** — actualiza los campos de una película existente. Si no existe, lanza una excepción.
- **`eliminarPelicula`** — elimina la película por ID. Si no existe, lanza una excepción. Si la película tiene funciones con estado `PROGRAMADA`, debe lanzar una excepción indicando que no puede eliminarse.
- **`registrarFuncion`** — recibe el ID de la película y los datos de la función. Verifica que la película exista (lanza excepción si no) y persiste la función vinculada a esa película.
- **`buscarFuncionPorId`** — devuelve la función o lanza una excepción si no existe.
- **`buscarFuncionesPorPelicula`** — devuelve todas las funciones de una película dado su ID. Si la película no existe, lanza una excepción.
- **`buscarFunciones`** — recibe tres filtros opcionales: estado, genero de la película y fechaDesde. Cada filtro solo se aplica si fue enviado; si ninguno está presente, devuelve todas las funciones. Implementar esta consulta usando **CriteriaBuilder** (`EntityManager`, `CriteriaQuery`, `Predicate`) para construir los predicados dinámicamente según los parámetros presentes.
- **`cancelarFuncion`** — cambia el estado de una función existente a `CANCELADA`. Si no existe, lanza una excepción.
- **`reportePorGenero`** — devuelve, agrupado por género: nombre del género, cantidad de funciones, recaudación total (suma de `precio × butacasVendidas`) y precio promedio de entrada.

---

## Endpoints esperados

### Películas

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/peliculas` | Listar todas las películas |
| GET | `/peliculas/{id}` | Buscar película por ID |
| GET | `/peliculas?genero=...` | Filtrar por género |
| GET | `/peliculas?clasificacion=...` | Filtrar por clasificación |
| POST | `/peliculas` | Crear película |
| PUT | `/peliculas/{id}` | Actualizar película |
| DELETE | `/peliculas/{id}` | Eliminar película |

### Funciones

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/funciones` | Listar todas las funciones |
| GET | `/funciones/{id}` | Buscar función por ID |
| GET | `/peliculas/{peliculaId}/funciones` | Listar funciones de una película |
| GET | `/funciones?estado=...` | Filtrar por estado (PROGRAMADA, FINALIZADA, CANCELADA) |
| GET | `/funciones?genero=...` | Filtrar por género de la película |
| GET | `/funciones?fechaDesde=...` | Funciones con fecha igual o posterior |
| POST | `/funciones` | Crear función (el cuerpo debe incluir `peliculaId`) |
| PATCH | `/funciones/{id}/cancelar` | Cancelar una función |
| DELETE | `/funciones/{id}` | Eliminar función |

### Reporte

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/reporte/generos` | Cantidad de funciones, recaudación total y precio promedio por género |

Los tres filtros de `/funciones` son combinables entre sí: si se envían varios, se aplican todos simultáneamente.

---

## Consideraciones técnicas

- Los endpoints de creación deben devolver **HTTP 201 Created** con el recurso creado en el cuerpo.
- El endpoint de cancelación (`PATCH /funciones/{id}/cancelar`) debe devolver **HTTP 200 OK** con la función actualizada.
- El endpoint de eliminación debe devolver **HTTP 204 No Content**.
- Si un recurso no existe, la respuesta debe ser **HTTP 404 Not Found** con un mensaje que indique qué no se encontró.
- Si se intenta eliminar una película con funciones en estado `PROGRAMADA`, la respuesta debe ser **HTTP 409 Conflict** con un mensaje descriptivo.
- Implementar un manejador global de excepciones con `@RestControllerAdvice` que intercepte las excepciones del service y devuelva el código y mensaje apropiados.
- Validar que el título y el director de la película no estén vacíos y que la duración sea mayor a cero. Para las funciones, validar que la sala y la fecha no estén vacíos y que el precio sea mayor a cero. Si la validación falla, la respuesta debe ser **HTTP 400 Bad Request**.

---

## Tests unitarios

Escribir tests unitarios para la capa de servicio (`CineService`) que cubran los distintos escenarios de cada método. La cobertura de líneas del servicio debe ser **igual o superior al 80%**.

- Usar **JUnit 5** (ya incluido en el starter de pruebas de Spring Boot).
- Usar **H2** como base de datos en memoria para los tests, tal como se explica en la sección 8 del README de la unidad 2. Agregar la dependencia `testImplementation` de H2 y configurar un `application.properties` en `src/test/resources/` apuntando a H2.
- Verificar tanto los caminos exitosos como los casos de error (por ejemplo, que se lance la excepción correcta cuando la película no existe, o cuando se intenta eliminar una película con funciones programadas).

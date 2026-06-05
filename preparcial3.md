# Ejercicio TP 4 — Sistema de gestión de una librería

## Paso 0 — Repositorio

Antes de escribir una línea de código, creá un repositorio **privado** en GitHub con el nombre `libreria-api` y agregá a **maximilianolovera@gmail.com** como colaborador con rol de lectura. La entrega se realiza a través de ese repositorio.

---

## Descripción del sistema

Una librería necesita una API REST para gestionar su catálogo de libros. La API debe permitir registrar libros, consultarlos con filtros opcionales, actualizarlos, eliminarlos y obtener un reporte agrupado por género literario.

---

## Base de datos

Crear el esquema `libreria` en MySQL y ejecutar el siguiente script:

```sql
CREATE SCHEMA IF NOT EXISTS libreria;

CREATE TABLE libreria.libro (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo    VARCHAR(150) NOT NULL,
    autor     VARCHAR(100) NOT NULL,
    genero    VARCHAR(80)  NOT NULL,
    editorial VARCHAR(100) NOT NULL,
    precio    DOUBLE       NOT NULL,
    stock     INT          NOT NULL DEFAULT 0,
    isbn      VARCHAR(20)  NOT NULL
);

INSERT INTO libreria.libro (titulo, autor, genero, editorial, precio, stock, isbn) VALUES
('El nombre del viento',          'Patrick Rothfuss',    'Fantasía',       'DAW Books',       18500.00, 12, '978-0756404741'),
('La Vía del Rey',                'Brandon Sanderson',   'Fantasía',       'Tor Books',       21000.00,  8, '978-0765326355'),
('Dune',                          'Frank Herbert',       'Ciencia Ficción','Chilton Books',   16800.00, 15, '978-0441013593'),
('Neuromante',                    'William Gibson',      'Ciencia Ficción','Ace Books',       14200.00, 10, '978-0441569595'),
('Crimen y castigo',              'Fiódor Dostoyevski',  'Clásico',        'RBA',             12500.00,  6, '978-8415740414'),
('Anna Karénina',                 'León Tolstói',        'Clásico',        'Alba Editorial',  13900.00,  5, '978-8490657386'),
('El código Da Vinci',            'Dan Brown',           'Thriller',       'Doubleday',       11800.00, 20, '978-0385504201'),
('El silencio de los inocentes',  'Thomas Harris',       'Thriller',       'St. Martin Press',13200.00, 14, '978-0312195458'),
('Sapiens',                       'Yuval Noah Harari',   'No ficción',     'Harper Collins',  17500.00, 18, '978-0062316097'),
('El mundo de ayer',              'Stefan Zweig',        'No ficción',     'Acantilado',      15000.00,  9, '978-8416748242');
```

---

## Crear el proyecto

Generar el proyecto desde [Spring Initializr](https://start.spring.io) con las dependencias: **Spring Web**, **Spring Data JPA**, **MySQL Driver**, **Validation** y **Lombok**.

Configurar la conexión a la base de datos en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/libreria
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

---

## Estructura del proyecto

Respetar la siguiente arquitectura en capas:

```
src/main/java/com/libreria/
├── model/
│   └── Libro.java
├── repository/
│   └── LibroRepository.java
├── service/
│   └── LibreriaService.java
├── controller/
│   └── LibroController.java
├── exception/
│   └── GlobalExceptionHandler.java
└── LibreriaApplication.java
```

---

## Entidad (`model/`)

**`Libro`** — representa un libro del catálogo. Campos: título, autor, género, editorial, precio, stock e ISBN. Debe estar mapeada a la tabla `libreria.libro` con las anotaciones JPA correspondientes.

---

## Capa de repositorio (`repository/`)

**`LibroRepository`** — extiende `JpaRepository<Libro, Long>`. Además de los métodos heredados, debe agregar:

- Un método de consulta que filtre libros por precio máximo, género y stock mínimo. Los tres parámetros son opcionales: si alguno no se informa, no se aplica ese filtro.

---

## Capa de servicio (`service/`)

**`LibreriaService`** — coordina la lógica de negocio y delega la persistencia al repositorio. Debe exponer los siguientes métodos:

- **`registrarLibro`** — recibe los datos del libro y lo persiste.
- **`buscarLibroPorId`** — devuelve el libro o lanza una excepción si no existe.
- **`buscarLibros`** — recibe tres filtros opcionales: precio máximo, género y stock mínimo. Cada filtro solo se aplica si fue enviado; si ninguno está presente, devuelve todos los libros.
- **`actualizarLibro`** — actualiza los campos de un libro existente. Si no existe, lanza una excepción.
- **`eliminarLibro`** — elimina el libro por ID. Si no existe, lanza una excepción.
- **`reportePorGenero`** — devuelve, agrupado por género: nombre del género, cantidad de libros, stock total disponible y precio promedio de los libros.

---

## Endpoints esperados

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/libros` | Listar todos los libros |
| GET | `/libros/{id}` | Buscar libro por ID |
| GET | `/libros?precioMaximo=...` | Filtrar por precio máximo |
| GET | `/libros?genero=...` | Filtrar por género literario |
| GET | `/libros?stockMinimo=...` | Libros con al menos N unidades en stock |
| POST | `/libros` | Crear libro |
| PUT | `/libros/{id}` | Actualizar libro |
| DELETE | `/libros/{id}` | Eliminar libro |
| GET | `/reporte/generos` | Cantidad de libros, stock total y precio promedio por género |

Los tres filtros de `/libros` son combinables entre sí: si se envían varios, se aplican todos simultáneamente.

---

## Consideraciones técnicas

- Los endpoints de creación deben devolver **HTTP 201 Created** con el recurso creado en el cuerpo.
- El endpoint de eliminación debe devolver **HTTP 204 No Content**.
- Si un recurso no existe, la respuesta debe ser **HTTP 404 Not Found** con un mensaje que indique qué no se encontró.
- Implementar un manejador global de excepciones con `@RestControllerAdvice` que intercepte las excepciones del service y devuelva el código y mensaje apropiados.
- Validar que el título del libro y el autor no estén vacíos, que el precio sea mayor a cero y que el stock sea un número mayor o igual a cero. Si la validación falla, la respuesta debe ser **HTTP 400 Bad Request**.

---

## Tests unitarios

Escribir tests unitarios para la capa de servicio (`LibreriaService`) que cubran los distintos escenarios de cada método. La cobertura de líneas del servicio debe ser **igual o superior al 80%**.

- Usar **JUnit 5** (ya incluido en el starter de pruebas de Spring Boot).
- Usar **H2** como base de datos en memoria para los tests, tal como se explica en la sección 8 del README de la unidad 2. Agregar la dependencia `testImplementation` de H2 y configurar un `application.properties` en `src/test/resources/` apuntando a H2.
- Verificar tanto los caminos exitosos como los casos de error (por ejemplo, que se lance la excepción correcta cuando el libro no existe).

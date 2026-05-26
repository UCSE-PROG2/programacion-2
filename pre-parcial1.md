# Ejercicio TP 3 — Sistema de gestión de una biblioteca

## Paso 0 — Repositorio

Antes de escribir una línea de código, creá un repositorio **privado** en GitHub con el nombre `biblioteca-api` y agregá a **maximilianolovera@gmail.com** como colaborador con rol de lectura. La entrega se realiza a través de ese repositorio.

---

## Descripción del sistema

Una biblioteca municipal necesita una API REST para gestionar su catálogo de libros y las secciones en que están organizados. La API debe permitir registrar secciones y libros, consultar el catálogo con filtros opcionales y obtener un reporte de la colección agrupado por sección.

---

## Almacenamiento en memoria

No se utilizará base de datos ni ORM. Toda la información se almacenará en **listas en memoria** durante la ejecución del servidor. Los repositorios son responsables de asignar IDs de forma automática mediante un contador interno.

---

## Crear el proyecto

Generar el proyecto desde [Spring Initializr](https://start.spring.io) con las dependencias: **Spring Web**, **Spring Boot Starter Validation** y **Lombok**.

---

## Estructura del proyecto

Respetar la siguiente arquitectura en capas:

```
src/main/java/com/biblioteca/
├── model/
│   ├── Seccion.java
│   └── Libro.java
├── repository/
│   ├── SeccionRepository.java
│   └── LibroRepository.java
├── service/
│   └── BibliotecaService.java
├── controller/
│   ├── SeccionController.java
│   └── LibroController.java
├── exception/
│   └── GlobalExceptionHandler.java
└── BibliotecaApplication.java
```

---

## Entidades (`model/`)

**`Seccion`** — representa una sección temática de la biblioteca. Campos: nombre y descripción.

**`Libro`** — representa un libro del catálogo. Campos: título, autor, ISBN, año de publicación, ejemplares disponibles y la sección a la que pertenece. Cada libro pertenece a una única sección.

---

## Capa de repositorio (`repository/`)

**`SeccionRepository`** — mantiene una lista en memoria con todas las secciones. Debe exponer métodos para: guardar, buscar por ID, listar todas y eliminar por ID.

**`LibroRepository`** — mantiene una lista en memoria con todos los libros. Debe exponer métodos para: guardar, buscar por ID, listar todos y eliminar por ID.

Ambos repositorios asignan el ID automáticamente al guardar, sin intervención del service ni del controller.

---

## Capa de servicio (`service/`)

**`BibliotecaService`** — coordina la lógica de negocio y delega el almacenamiento a los repositorios. Debe exponer los siguientes métodos:

- **`registrarSeccion`** — recibe los datos de la sección y la persiste.
- **`registrarLibro`** — recibe los datos del libro y el ID de la sección. Si la sección no existe, lanza una excepción con mensaje descriptivo.
- **`buscarLibroPorId`** — devuelve el libro o lanza una excepción si no existe.
- **`buscarLibros`** — recibe tres filtros opcionales: autor (búsqueda parcial, sin distinción de mayúsculas), año de publicación y ejemplares mínimos disponibles. Cada filtro solo se aplica si fue enviado; si ninguno está presente, devuelve todos los libros.
- **`actualizarLibro`** — actualiza los campos de un libro existente. Si no existe, lanza una excepción.
- **`eliminarLibro`** — elimina el libro por ID. Si no existe, lanza una excepción.
- **`reportePorSeccion`** — devuelve, agrupado por sección: nombre de la sección, cantidad de libros y total de ejemplares disponibles.

---

## Endpoints esperados

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/libros` | Listar todos los libros |
| GET | `/libros/{id}` | Buscar libro por ID |
| GET | `/libros?autor=...` | Filtrar por autor (parcial) |
| GET | `/libros?anio=...` | Filtrar por año de publicación |
| GET | `/libros?ejemplaresMinimos=...` | Libros con al menos N ejemplares |
| POST | `/libros` | Crear libro |
| PUT | `/libros/{id}` | Actualizar libro |
| DELETE | `/libros/{id}` | Eliminar libro |
| GET | `/secciones` | Listar todas las secciones |
| GET | `/secciones/{id}` | Buscar sección por ID |
| POST | `/secciones` | Crear sección |
| GET | `/reporte/secciones` | Cantidad de libros y ejemplares totales por sección |

Los tres filtros de `/libros` son combinables entre sí: si se envían varios, se aplican todos simultáneamente.

---

## Consideraciones técnicas

- Los endpoints de creación deben devolver **HTTP 201 Created** con el recurso creado en el cuerpo.
- El endpoint de eliminación debe devolver **HTTP 204 No Content**.
- Si un recurso no existe, la respuesta debe ser **HTTP 404 Not Found** con un mensaje que indique qué no se encontró.
- Implementar un manejador global de excepciones con `@RestControllerAdvice` que intercepte las excepciones del service y devuelva el código y mensaje apropiados.
- Validar que el título y el autor del libro no estén vacíos, y que los ejemplares disponibles sean un número mayor o igual a cero. Si la validación falla, la respuesta debe ser **HTTP 400 Bad Request**.

---

## Criterios de evaluación

| Ítem | Descripción |
|------|-------------|
| Repositorio | Repositorio privado creado y colaborador sumado antes de la entrega |
| Estructura | El proyecto respeta la arquitectura en capas: model, repository, service, controller |
| Repositorios | Las listas en memoria gestionan los IDs automáticamente |
| Service | Valida la existencia de la sección antes de registrar un libro |
| Filtros | Los tres filtros de búsqueda son opcionales y combinables |
| Reporte | Agrupa por sección con cantidad de libros y ejemplares totales |
| Códigos HTTP | 201 al crear, 204 al eliminar, 404 cuando el recurso no existe |
| Errores | El manejador global captura las excepciones y devuelve respuestas claras |
| Validaciones | Campos obligatorios validados con anotaciones; errores devuelven 400 |

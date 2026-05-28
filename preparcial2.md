# Ejercicio TP 4 — Sistema de gestión de una agencia de viajes

## Paso 0 — Repositorio

Antes de escribir una línea de código, creá un repositorio **privado** en GitHub con el nombre `agencia-viajes-api` y agregá a **maximilianolovera@gmail.com** como colaborador con rol de lectura. La entrega se realiza a través de ese repositorio.

---

## Descripción del sistema

Una agencia de viajes necesita una API REST para gestionar su catálogo de paquetes de viaje. La API debe permitir registrar paquetes, consultarlos con filtros opcionales, actualizarlos, eliminarlos y obtener un reporte agrupado por destino.

---

## Base de datos

Crear el esquema `agencia` en MySQL y ejecutar el siguiente script:

```sql
CREATE SCHEMA IF NOT EXISTS agencia;

CREATE TABLE agencia.paquete (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL,
    destino           VARCHAR(100) NOT NULL,
    pais              VARCHAR(80)  NOT NULL,
    precio_total      DOUBLE       NOT NULL,
    duracion_dias     INT          NOT NULL,
    fecha_salida      VARCHAR(20)  NOT NULL,
    cupos_disponibles INT          NOT NULL DEFAULT 0
);

INSERT INTO agencia.paquete (nombre, destino, pais, precio_total, duracion_dias, fecha_salida, cupos_disponibles) VALUES
('Escapada Porteña',          'Buenos Aires',          'Argentina',  85000.00,  4, '2026-07-10', 20),
('Aventura Patagónica',       'Bariloche',             'Argentina', 210000.00,  7, '2026-07-15', 15),
('Cataratas del Iguazú',      'Puerto Iguazú',         'Argentina', 175000.00,  5, '2026-08-02', 12),
('Maravillas Cuzqueñas',      'Cusco',                 'Perú',      320000.00, 10, '2026-07-20',  8),
('Lima Gastronómica',         'Lima',                  'Perú',      185000.00,  6, '2026-08-10', 10),
('Machu Picchu Express',      'Cusco',                 'Perú',      290000.00,  8, '2026-09-05',  6),
('Encantes de Florianópolis', 'Florianópolis',         'Brasil',    195000.00,  7, '2026-12-15', 18),
('Carnival de Río',           'Río de Janeiro',        'Brasil',    420000.00, 12, '2027-02-20',  4),
('Santiago & Valparaíso',     'Santiago',              'Chile',     160000.00,  5, '2026-08-22', 14),
('Atacama Extremo',           'San Pedro de Atacama',  'Chile',     340000.00,  9, '2026-09-18',  7);
```

---

## Crear el proyecto

Generar el proyecto desde [Spring Initializr](https://start.spring.io) con las dependencias: **Spring Web**, **Spring Data JPA**, **MySQL Driver**, **Validation** y **Lombok**.

Configurar la conexión a la base de datos en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/agencia
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

---

## Estructura del proyecto

Respetar la siguiente arquitectura en capas:

```
src/main/java/com/agencia/
├── model/
│   └── Paquete.java
├── repository/
│   └── PaqueteRepository.java
├── service/
│   └── AgenciaService.java
├── controller/
│   └── PaqueteController.java
├── exception/
│   └── GlobalExceptionHandler.java
└── AgenciaApplication.java
```

---

## Entidad (`model/`)

**`Paquete`** — representa un paquete de viaje. Campos: nombre, destino, país, precio total, duración en días, fecha de salida y cupos disponibles. Debe estar mapeada a la tabla `agencia.paquete` con las anotaciones JPA correspondientes.

---

## Capa de repositorio (`repository/`)

**`PaqueteRepository`** — extiende `JpaRepository<Paquete, Long>`. Además de los métodos heredados, debe agregar:

- Un método de consulta que filtre paquetes por precio máximo, duración mínima en días y cupos mínimos disponibles. Los tres parámetros son opcionales: si alguno no se informa, no se aplica ese filtro.

---

## Capa de servicio (`service/`)

**`AgenciaService`** — coordina la lógica de negocio y delega la persistencia al repositorio. Debe exponer los siguientes métodos:

- **`registrarPaquete`** — recibe los datos del paquete y lo persiste.
- **`buscarPaquetePorId`** — devuelve el paquete o lanza una excepción si no existe.
- **`buscarPaquetes`** — recibe tres filtros opcionales: precio máximo, duración mínima en días y cupos mínimos disponibles. Cada filtro solo se aplica si fue enviado; si ninguno está presente, devuelve todos los paquetes.
- **`actualizarPaquete`** — actualiza los campos de un paquete existente. Si no existe, lanza una excepción.
- **`eliminarPaquete`** — elimina el paquete por ID. Si no existe, lanza una excepción.
- **`reportePorDestino`** — devuelve, agrupado por destino: nombre del destino, país, cantidad de paquetes, total de cupos disponibles y precio promedio de los paquetes.

---

## Endpoints esperados

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/paquetes` | Listar todos los paquetes |
| GET | `/paquetes/{id}` | Buscar paquete por ID |
| GET | `/paquetes?precioMaximo=...` | Filtrar por precio máximo |
| GET | `/paquetes?duracionMinima=...` | Filtrar por duración mínima en días |
| GET | `/paquetes?cuposMinimos=...` | Paquetes con al menos N cupos disponibles |
| POST | `/paquetes` | Crear paquete |
| PUT | `/paquetes/{id}` | Actualizar paquete |
| DELETE | `/paquetes/{id}` | Eliminar paquete |
| GET | `/reporte/destinos` | Cantidad de paquetes, cupos totales y precio promedio por destino |

Los tres filtros de `/paquetes` son combinables entre sí: si se envían varios, se aplican todos simultáneamente.

---

## Consideraciones técnicas

- Los endpoints de creación deben devolver **HTTP 201 Created** con el recurso creado en el cuerpo.
- El endpoint de eliminación debe devolver **HTTP 204 No Content**.
- Si un recurso no existe, la respuesta debe ser **HTTP 404 Not Found** con un mensaje que indique qué no se encontró.
- Implementar un manejador global de excepciones con `@RestControllerAdvice` que intercepte las excepciones del service y devuelva el código y mensaje apropiados.
- Validar que el nombre del paquete y el destino no estén vacíos, que el precio total sea mayor a cero y que los cupos disponibles sean un número mayor o igual a cero. Si la validación falla, la respuesta debe ser **HTTP 400 Bad Request**.

---

## Criterios de evaluación

| Ítem | Descripción |
|------|-------------|
| Repositorio | Repositorio privado creado y colaborador sumado antes de la entrega |
| Base de datos | Esquema y tabla creados; datos de prueba cargados con el script |
| Estructura | El proyecto respeta la arquitectura en capas: model, repository, service, controller |
| Entidad | `Paquete` correctamente mapeada con anotaciones JPA |
| Filtros | Los tres filtros de búsqueda son opcionales y combinables |
| Reporte | Agrupa por destino con cantidad de paquetes, cupos totales y precio promedio |
| Códigos HTTP | 201 al crear, 204 al eliminar, 404 cuando el recurso no existe |
| Errores | El manejador global captura las excepciones y devuelve respuestas claras |
| Validaciones | Campos obligatorios validados con anotaciones; errores devuelven 400 |

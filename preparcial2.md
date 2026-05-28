# Ejercicio TP 4 — Sistema de gestión de una agencia de viajes

## Paso 0 — Repositorio

Antes de escribir una línea de código, creá un repositorio **privado** en GitHub con el nombre `agencia-viajes-api` y agregá a **maximilianolovera@gmail.com** como colaborador con rol de lectura. La entrega se realiza a través de ese repositorio.

---

## Descripción del sistema

Una agencia de viajes necesita una API REST para gestionar sus destinos turísticos y los paquetes de viaje que ofrece. La API debe permitir registrar destinos y paquetes, consultar el catálogo con filtros opcionales y obtener un reporte de la oferta agrupado por destino.

---

## Almacenamiento en memoria

No se utilizará base de datos ni ORM. Toda la información se almacenará en **listas en memoria** durante la ejecución del servidor. Los repositorios son responsables de asignar IDs de forma automática mediante un contador interno.

---

## Crear el proyecto

Generar el proyecto desde [Spring Initializr](https://start.spring.io) con las dependencias: **Spring Web**, **Validation** y **Lombok**.

---

## Estructura del proyecto

Respetar la siguiente arquitectura en capas:

```
src/main/java/com/agencia/
├── model/
│   ├── Destino.java
│   └── Paquete.java
├── repository/
│   ├── DestinoRepository.java
│   └── PaqueteRepository.java
├── service/
│   └── AgenciaService.java
├── controller/
│   ├── DestinoController.java
│   └── PaqueteController.java
├── exception/
│   └── GlobalExceptionHandler.java
└── AgenciaApplication.java
```

---

## Entidades (`model/`)

**`Destino`** — representa un destino turístico. Campos: nombre, país y descripción.

**`Paquete`** — representa un paquete de viaje disponible en la agencia. Campos: nombre, precio total, duración en días, fecha de salida (texto libre, por ejemplo `"2026-07-15"`), cupos disponibles y el destino al que corresponde. Cada paquete pertenece a un único destino.

---

## Capa de repositorio (`repository/`)

**`DestinoRepository`** — mantiene una lista en memoria con todos los destinos. Debe exponer métodos para: guardar, buscar por ID, listar todos y eliminar por ID.

**`PaqueteRepository`** — mantiene una lista en memoria con todos los paquetes. Debe exponer métodos para: guardar, buscar por ID, listar todos y eliminar por ID.

Ambos repositorios asignan el ID automáticamente al guardar, sin intervención del service ni del controller.

---

## Capa de servicio (`service/`)

**`AgenciaService`** — coordina la lógica de negocio y delega el almacenamiento a los repositorios. Debe exponer los siguientes métodos:

- **`registrarDestino`** — recibe los datos del destino y lo persiste.
- **`registrarPaquete`** — recibe los datos del paquete y el ID del destino. Si el destino no existe, lanza una excepción con mensaje descriptivo.
- **`buscarPaquetePorId`** — devuelve el paquete o lanza una excepción si no existe.
- **`buscarPaquetes`** — recibe tres filtros opcionales: precio máximo, duración mínima en días y cupos mínimos disponibles. Cada filtro solo se aplica si fue enviado; si ninguno está presente, devuelve todos los paquetes.
- **`actualizarPaquete`** — actualiza los campos de un paquete existente. Si no existe, lanza una excepción.
- **`eliminarPaquete`** — elimina el paquete por ID. Si no existe, lanza una excepción.
- **`reportePorDestino`** — devuelve, agrupado por destino: nombre del destino, cantidad de paquetes, total de cupos disponibles y precio promedio de los paquetes.

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
| GET | `/destinos` | Listar todos los destinos |
| GET | `/destinos/{id}` | Buscar destino por ID |
| POST | `/destinos` | Crear destino |
| GET | `/reporte/destinos` | Cantidad de paquetes, cupos totales y precio promedio por destino |

Los tres filtros de `/paquetes` son combinables entre sí: si se envían varios, se aplican todos simultáneamente.

---

## Consideraciones técnicas

- Los endpoints de creación deben devolver **HTTP 201 Created** con el recurso creado en el cuerpo.
- El endpoint de eliminación debe devolver **HTTP 204 No Content**.
- Si un recurso no existe, la respuesta debe ser **HTTP 404 Not Found** con un mensaje que indique qué no se encontró.
- Implementar un manejador global de excepciones con `@RestControllerAdvice` que intercepte las excepciones del service y devuelva el código y mensaje apropiados.
- Validar que el nombre del paquete no esté vacío, que el precio total sea mayor a cero y que los cupos disponibles sean un número mayor o igual a cero. Si la validación falla, la respuesta debe ser **HTTP 400 Bad Request**.

---

## Criterios de evaluación

| Ítem | Descripción |
|------|-------------|
| Repositorio | Repositorio privado creado y colaborador sumado antes de la entrega |
| Estructura | El proyecto respeta la arquitectura en capas: model, repository, service, controller |
| Repositorios | Las listas en memoria gestionan los IDs automáticamente |
| Service | Valida la existencia del destino antes de registrar un paquete |
| Filtros | Los tres filtros de búsqueda son opcionales y combinables |
| Reporte | Agrupa por destino con cantidad de paquetes, cupos totales y precio promedio |
| Códigos HTTP | 201 al crear, 204 al eliminar, 404 cuando el recurso no existe |
| Errores | El manejador global captura las excepciones y devuelve respuestas claras |
| Validaciones | Campos obligatorios validados con anotaciones; errores devuelven 400 |

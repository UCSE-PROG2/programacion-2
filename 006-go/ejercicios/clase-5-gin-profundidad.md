# Ejercicios — Clase 5: Gin en profundidad

Ejercicios de evaluación para la [Clase 5](../README.md#clase-5--gin-en-profundidad). Profundizan lo que la Clase 2 usó sin explicar del todo: parámetros de path y de query, `binding`, códigos de estado por caso, y `context.Context`.

Los Ejercicios 1, 3, 4 y 5 son mini-servidores Gin independientes, sobre un slice fijo en memoria — practican Gin de forma aislada, sin depender del proyecto del TP. El Ejercicio 2 sí trabaja sobre el `service`/`handler` de `Producto` ya conectado a Mongo desde la Clase 2, para practicar la distinción entre validación de forma y de negocio en un contexto real. Ningún ejercicio de esta clase filtra ni pagina **en la base** todavía — eso es la Clase 4, que retoma exactamente estos mismos casos y los resuelve "de verdad" contra Mongo.

---

## Ejercicio 1 — Lista de tareas con filtro por query param

Un slice fijo en memoria de `Tarea{ID string, Titulo string, Completada bool}`.

**Requerimientos:**

1. `GET /tareas` — sin query params, devuelve todas las tareas.
2. `GET /tareas?completada=true` (o `false`) — filtra por el valor del query param, parseándolo con `strconv.ParseBool`. Si viene un valor que no es `true`/`false` (ej: `?completada=tal-vez`), responder `400 Bad Request` con un mensaje claro en vez de ignorarlo silenciosamente.
3. `POST /tareas` — crea una tarea nueva a partir del body JSON, con `Titulo` obligatorio (`binding:"required"`) y `Completada` opcional (por defecto `false`).
4. Probar los tres casos con `curl`: sin filtro, con filtro válido, y con filtro inválido.

**Evalúa:** lectura y validación de query params (no solo params de path), diferenciar "el filtro no vino" de "el filtro vino mal formado", `binding` con un campo opcional.

**Checklist:**
- [ ] `?completada=true` y `?completada=false` devuelven subconjuntos distintos y correctos
- [ ] `?completada=tal-vez` responde 400, no 200 con la lista completa ni un 500
- [ ] `POST /tareas` sin `titulo` responde 400 por `binding`

---

## Ejercicio 2 — Validación de negocio que `binding` no puede expresar

Sobre el `Producto` real de la Clase 2, ya conectado a `producto.MongoRepository`.

**Requerimientos:**

1. `POST /productos` ya valida con los tags conocidos (`Nombre` requerido, `Precio > 0`) vía `binding`.
2. Agregar, **dentro de `service.go`** (no en `handler.go` — ya existe esa separación desde la Clase 2, aunque compartan paquete), un rechazo con `409 Conflict` si ya existe un producto con el mismo `Nombre` (comparación sin distinguir mayúsculas/minúsculas — normalizar con `strings.EqualFold`; para esto, `Service.Crear` primero necesita traer los productos existentes con `FindAll` y comparar en Go — la forma de hacer esto directamente en la base es la Clase 4).
3. El `service` debe devolver un error distinguible (no genérico) para que el `handler` pueda mapearlo a `409` específicamente, sin confundirlo con un error interno.
4. Probar: alta exitosa, alta con datos inválidos (400), alta con nombre duplicado (409).

**Evalúa:** distinguir claramente una validación de **forma** (`binding`, se resuelve con tags en el handler) de una validación de **negocio** (requiere mirar el estado actual de los datos, y por eso vive en `service.go`, no en `handler.go` ni en `repository.go`) — la misma separación de responsabilidades ya vista en la Clase 2.

**Checklist:**
- [ ] Un nombre duplicado con distinta capitalización (`"Mouse"` vs `"mouse"`) también es rechazado
- [ ] El código de estado distingue los tres casos: 201, 400, 409 (no todo cae en 400)
- [ ] La regla de negocio vive en `service.go`, no en `handler.go`

---

## Ejercicio 3 — Paginación manual con query params

Sobre un slice fijo de al menos 10 artículos precargados (`Articulo{ID, Nombre string, Precio float64}` — un mini-servidor Gin aparte, sin tocar el `Producto` real de Mongo, para aislar la práctica de paginación).

**Requerimientos:**

1. `GET /articulos?pagina=1&tamanio=3` — devuelve una porción (slice del slice) según `pagina` y `tamanio`, usando `c.DefaultQuery` para valores por defecto razonables si no vienen.
2. Validar que `pagina` y `tamanio` sean enteros positivos (`> 0`); si no, `400 Bad Request`.
3. Si `pagina` pide una página que no existe (ej: página 100 de una lista de 10 elementos), devolver `200 OK` con una lista **vacía**, no un error.
4. La respuesta JSON debe incluir, además de los artículos de esa página, metadata: `total` (cantidad total), `pagina` y `tamanio` actuales.
5. Probar al menos: primera página, última página parcial (menos elementos que `tamanio`), y una página fuera de rango.

**Evalúa:** conversión y validación de query params numéricos, slicing correcto sin salirse de los límites del slice (`index out of range` es un error común acá), diseño de una respuesta JSON con metadata además de los datos. Es intencionalmente "a mano" y sobre un slice fijo, no contra Mongo — la Clase 4 retoma este mismo problema y lo resuelve con `Skip`/`Limit` reales contra la base.

**Checklist:**
- [ ] Ninguna combinación de `pagina`/`tamanio` provoca un `panic` por índice fuera de rango
- [ ] La metadata (`total`, `pagina`, `tamanio`) es correcta en los tres casos probados
- [ ] `pagina=0` o `tamanio=-1` responden 400

---

## Ejercicio 4 — Tres códigos de estado bien diferenciados en un PUT

`PUT /productos/:id`, sobre el `Producto` real ya conectado a Mongo desde la Clase 2.

**Requerimientos:**

1. Si el body no es JSON válido o no cumple `binding`, responder `400 Bad Request` con el detalle del error de validación.
2. Si el body es válido pero el `:id` de la URL no corresponde a ningún producto existente, responder `404 Not Found`.
3. Si todo es válido y el producto existe, actualizar `Nombre` y `Precio` y responder `200 OK` con el producto actualizado completo.
4. Escribir al menos 3 pruebas manuales con `curl` (una por cada código de estado) y documentar los comandos usados (pueden ir como comentario al final del archivo o en un bloque separado).

**Evalúa:** que el orden de las validaciones importa (parsear/validar el body **antes** de buscar el `:id`, para no hacer trabajo de más si el body ya es inválido), separar claramente "dato mal formado" de "recurso inexistente" — un error común es devolver 404 para ambos casos.

**Checklist:**
- [ ] Un `PUT` con body inválido a un `:id` inexistente responde 400 (no 404) — se valida el body primero
- [ ] Un `PUT` con body válido a un `:id` inexistente responde 404
- [ ] Un `PUT` exitoso devuelve el producto **actualizado**, no el que tenía antes de la request

---

## Ejercicio 5 — Endpoint lento con timeout via `context.Context`

Simular una operación que tarda, y cortarla si se excede un plazo — practicando `context.Context` más allá de mencionarlo.

**Requerimientos:**

1. `GET /reporte` — simula un cómputo pesado con una función `generarReporte(ctx context.Context) (string, error)` que internamente hace `time.Sleep(3 * time.Second)` (simulando trabajo).
2. En el handler, envolver el contexto del request con `context.WithTimeout(c.Request.Context(), 1*time.Second)` (con su `defer cancel()`) **antes** de llamar a `generarReporte`.
3. `generarReporte` debe recibir ese contexto y, en un caso simplificado, chequear `ctx.Err()` después del `Sleep` (o dividir el sleep en pasos más chicos, chequeando entre cada uno) para decidir si seguir o abortar.
4. Si el contexto se venció, el handler responde `504 Gateway Timeout` con un mensaje claro. Si no, responde `200 OK` con el resultado.
5. Probar ambos casos: bajando el timeout simulado a menos de 3 segundos (falla) y subiéndolo a más de 3 segundos (éxito), sin cambiar el `Sleep` fijo de `generarReporte`.

**Evalúa:** uso real (no solo mencionado) de `context.WithTimeout`, propagación del contexto del request de Gin hacia una función que no conoce `gin.Context`, un código de estado HTTP específico para timeouts (504, distinto de 500).

**Checklist:**
- [ ] `generarReporte` no importa `"github.com/gin-gonic/gin"` — solo recibe `context.Context`, como corresponde a una función que ya podría vivir en el `service` (la capa que existe desde la Clase 2)
- [ ] Con timeout de 1s, la respuesta es 504 y llega en ~1s (no espera los 3s completos)
- [ ] Con timeout de 5s, la respuesta es 200 con el resultado

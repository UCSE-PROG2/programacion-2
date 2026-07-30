# Ejercicios — Clase 3: net/http, Gin y REST en Go

Ejercicios de evaluación para la [Clase 3](../README.md#clase-3--nethttp-gin-y-rest-en-go). Suman a lo de las Clases 1-2: ahora hay rutas, parámetros, JSON, `binding` y `context.Context`.

**Todavía no hay arquitectura en capas ni repository** (eso es la Clase 4) — los handlers pueden trabajar directamente sobre un slice fijo declarado en `main` o a nivel de paquete. Tampoco hay persistencia real: reiniciar el servidor pierde los datos, y **eso es esperado** en esta clase.

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

Sobre el mismo slice fijo de productos usado en la clase (`Producto{ID, Nombre, Precio}`).

**Requerimientos:**

1. `POST /productos` valida con los tags ya conocidos (`Nombre` requerido, `Precio > 0`).
2. Además, **dentro del handler** (todavía no hay service), rechazar con `409 Conflict` si ya existe un producto con el mismo `Nombre` en el slice (comparación exacta, sin distinguir mayúsculas/minúsculas — normalizar con `strings.EqualFold`).
3. Si pasa ambas validaciones, agregar el producto al slice en memoria (protegido de la forma que corresponda para esta clase — no hace falta `sync.Mutex` todavía si el ejercicio se prueba de forma secuencial, pero dejarlo mencionado como pendiente para más adelante) y responder `201 Created`.
4. Probar: alta exitosa, alta con datos inválidos (400), alta con nombre duplicado (409).

**Evalúa:** distinguir claramente una validación de **forma** (`binding`, se resuelve con tags) de una validación de **negocio** (requiere mirar el estado actual de los datos, se resuelve con código explícito en el handler) — la misma distinción que la Clase 4 va a mover a la capa de `service`.

**Checklist:**
- [ ] Un nombre duplicado con distinta capitalización (`"Mouse"` vs `"mouse"`) también es rechazado
- [ ] El código de estado distingue los tres casos: 201, 400, 409 (no todo cae en 400)
- [ ] El slice en memoria refleja el nuevo producto después de un alta exitosa

---

## Ejercicio 3 — Paginación manual con query params

Sobre un slice fijo de al menos 10 productos precargados.

**Requerimientos:**

1. `GET /productos?pagina=1&tamanio=3` — devuelve una porción (slice del slice) según `pagina` y `tamanio`, usando `c.DefaultQuery` para valores por defecto razonables si no vienen.
2. Validar que `pagina` y `tamanio` sean enteros positivos (`> 0`); si no, `400 Bad Request`.
3. Si `pagina` pide una página que no existe (ej: página 100 de una lista de 10 elementos), devolver `200 OK` con una lista **vacía**, no un error.
4. La respuesta JSON debe incluir, además de los productos de esa página, metadata: `total` (cantidad total de productos), `pagina` y `tamanio` actuales.
5. Probar al menos: primera página, última página parcial (menos elementos que `tamanio`), y una página fuera de rango.

**Evalúa:** conversión y validación de query params numéricos, slicing correcto sin salirse de los límites del slice (`index out of range` es un error común acá), diseño de una respuesta JSON con metadata además de los datos.

**Checklist:**
- [ ] Ninguna combinación de `pagina`/`tamanio` provoca un `panic` por índice fuera de rango
- [ ] La metadata (`total`, `pagina`, `tamanio`) es correcta en los tres casos probados
- [ ] `pagina=0` o `tamanio=-1` responden 400

---

## Ejercicio 4 — Tres códigos de estado bien diferenciados en un PUT

`PUT /productos/:id` sobre el slice fijo de productos.

**Requerimientos:**

1. Si el body no es JSON válido o no cumple `binding`, responder `400 Bad Request` con el detalle del error de validación.
2. Si el body es válido pero el `:id` de la URL no corresponde a ningún producto del slice, responder `404 Not Found`.
3. Si todo es válido y el producto existe, actualizar `Nombre` y `Precio` en el slice y responder `200 OK` con el producto actualizado completo.
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
- [ ] `generarReporte` no importa `"github.com/gin-gonic/gin"` — solo recibe `context.Context`, como corresponde a una función que en la Clase 4 podría vivir en el `service`
- [ ] Con timeout de 1s, la respuesta es 504 y llega en ~1s (no espera los 3s completos)
- [ ] Con timeout de 5s, la respuesta es 200 con el resultado

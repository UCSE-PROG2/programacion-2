# Ejercicios — Clase 4: Arquitectura en capas e inyección de dependencias

Ejercicios de evaluación para la [Clase 4](../README.md#clase-4--arquitectura-en-capas-e-inyección-de-dependencias). Parten de la arquitectura ya armada en la práctica de esa clase: `handler` → `service` → `ProductoRepository` (interfaz) → `InMemoryProductoRepository`, con `router.Group("/productos")` y DI manual en `main.go`.

Estos ejercicios **no repiten** armar el CRUD de `Producto` desde cero (eso ya se hizo en la práctica corta del README) — evalúan si el patrón se entendió lo suficiente como para **extenderlo** y **generalizarlo**.

---

## Ejercicio 1 — Segunda entidad, misma arquitectura

Demostrar que la arquitectura en capas no es "magia" atada a `Producto`.

**Requerimientos:**

1. Agregar una entidad nueva, `Categoria{ID, Nombre string}`, replicando **la misma estructura completa**: `model`, interfaz `CategoriaRepository` (con los mismos 5 métodos que `ProductoRepository`, adaptados), `InMemoryCategoriaRepository`, `CategoriaService`, `CategoriaHandler`.
2. Registrar las rutas bajo su propio `router.Group("/categorias")`, independiente del de `/productos`.
3. Cablear ambos repositories, services y handlers en `main.go`, dejando el grafo de dependencias completo y legible.
4. El código de `Producto` no debe modificarse — es una prueba de que agregar un dominio nuevo no rompe ni toca el existente.

**Evalúa:** que el patrón de capas se generalizó, no se memorizó para un solo caso; disciplina de nombres y estructura de carpetas consistente entre dos dominios paralelos.

**Checklist:**
- [ ] `internal/repository`, `internal/service`, `internal/handler` tienen ahora archivos para `producto` y para `categoria`, sin mezclarse en el mismo archivo
- [ ] `GET /categorias` y `GET /productos` funcionan de forma independiente
- [ ] Ningún archivo de `Producto` fue modificado

---

## Ejercicio 2 — Extender el contrato de la interfaz

Agregar una capacidad nueva al repository sin romper lo existente.

**Requerimientos:**

1. Agregar un método `FindByNombre(ctx context.Context, nombre string) (Producto, error)` a la interfaz `ProductoRepository`.
2. Implementarlo en `InMemoryProductoRepository` (recorrer el mapa buscando coincidencia exacta; si no hay, devolver el mismo tipo de error que usa `FindByID` para "no encontrado").
3. Agregar el método correspondiente en `ProductoService` (puede llamarse igual, delegando al repository).
4. Exponer `GET /productos/buscar?nombre=...` en el handler, usando este nuevo método (no filtrar `FindAll` a mano en el handler — el punto es usar el nuevo método del repository).
5. Un test unitario para `InMemoryProductoRepository.FindByNombre` (caso encontrado y caso no encontrado).

**Evalúa:** que agregar un método a una interfaz obliga a tocar **todas** sus implementaciones (acá solo hay una, pero es la antesala directa de la Clase 5, donde va a haber dos), disciplina de propagar un cambio de contrato de forma consistente por las capas.

**Checklist:**
- [ ] `ProductoRepository` (la interfaz) tiene el método nuevo declarado
- [ ] `GET /productos/buscar?nombre=Mouse` devuelve el producto correcto; con un nombre inexistente, devuelve 404
- [ ] El handler no itera el slice/mapa de productos a mano — delega en el nuevo método

---

## Ejercicio 3 — Repository fake para testear el service sin HTTP ni memoria real

El ejercicio central para demostrar por qué el `service` depende de una interfaz y no de `InMemoryProductoRepository` directamente.

**Requerimientos:**

1. En un archivo `producto_service_test.go`, definir un `FakeProductoRepository` **propio del test**: un struct que satisface `ProductoRepository` "a mano", con campos que permiten configurar qué devuelve cada método (por ejemplo, un slice `productos` predefinido, o un `error` a devolver forzado, para simular una falla del repository sin usar Mongo ni ninguna base real).
2. Usar ese fake para testear `ProductoService.Crear`, cubriendo: alta exitosa, y rechazo por nombre duplicado (la regla de negocio vista en la práctica de la Clase 4) — sin levantar ningún servidor Gin.
3. Agregar un test donde el fake **simula una falla del repository** (por ejemplo, `FindAll` devuelve un error) y verificar que `ProductoService.Crear` propaga ese error correctamente en vez de ocultarlo o hacer `panic`.
4. Ninguno de estos tests debe importar `"github.com/gin-gonic/gin"` ni instanciar `InMemoryProductoRepository`.

**Evalúa:** comprensión real de la inyección de dependencias — un test que solo pasa usando el fake demuestra que el `service` efectivamente no conoce ni depende de ninguna implementación concreta, sino del contrato.

**Checklist:**
- [ ] `FakeProductoRepository` satisface `ProductoRepository` (se puede verificar con `var _ ProductoRepository = &FakeProductoRepository{}`)
- [ ] Los tests de `ProductoService` corren sin conexión a red, sin Gin y sin `InMemoryProductoRepository`
- [ ] Existe al menos un test donde el fake fuerza un error y se verifica que el service lo propaga

---

## Ejercicio 4 — DTO de salida en el handler

Practicar por qué el DTO vive en el handler, no en capas inferiores.

**Requerimientos:**

1. Crear `dto.ProductoRespuesta` con: `Nombre`, `Precio`, y un campo nuevo calculado `Slug` (versión del nombre en minúsculas y con espacios reemplazados por guiones, ej: `"Teclado Mecánico"` → `"teclado-mecanico"`) — sin exponer el `ID` interno del modelo.
2. Escribir una función `model.Producto` → `dto.ProductoRespuesta` (puede vivir en el paquete `dto` o en el `handler`, pero **no** en el `service` ni en el `repository`).
3. Modificar `GET /productos` y `GET /productos/:id` para responder con el DTO en vez del `model.Producto` directo.
4. `POST` y `PUT` pueden seguir recibiendo/devolviendo `model.Producto` tal cual, o aplicarles el mismo criterio — decidir y justificar brevemente en un comentario.

**Evalúa:** separación entre el modelo interno de persistencia y lo que efectivamente viaja por la API, ubicación correcta de la lógica de mapeo (una decisión de presentación, no de negocio ni de datos).

**Checklist:**
- [ ] El JSON de `GET /productos` ya no incluye el campo `id` crudo del modelo (o lo reemplaza según lo decidido), pero sí incluye `slug`
- [ ] `service` y `repository` siguen trabajando exclusivamente con `model.Producto`, sin conocer `dto.ProductoRespuesta`
- [ ] La función de mapeo tiene al menos un test que verifique el cálculo del `slug`

---

## Ejercicio 5 — Segunda implementación in-memory con comportamiento distinto

Sin llegar todavía a Mongo (eso es la Clase 5), demostrar que la interfaz permite más de una variante incluso dentro de "in-memory".

**Requerimientos:**

1. Crear `InMemoryProductoRepositoryOrdenado`, una segunda implementación de `ProductoRepository` donde `FindAll` devuelve los productos **ordenados por precio ascendente** (el resto de los métodos pueden reutilizar la misma lógica que `InMemoryProductoRepository`, por ejemplo por composición o copiando el struct base).
2. En `main.go`, permitir elegir cuál de las dos implementaciones se inyecta mediante una variable de entorno (por ejemplo, `ORDENAR_PRODUCTOS=true`), sin tocar `service` ni `handler`.
3. Un test que verifique que `InMemoryProductoRepositoryOrdenado.FindAll` efectivamente devuelve los productos ordenados por precio, incluso si se insertaron en otro orden.

**Evalúa:** que "cambiar de implementación" es literalmente una decisión de una línea en `main.go`, reforzando el mismo concepto que en la Clase 5 se va a usar para pasar a MongoDB — acá con una variante controlada y sin infraestructura externa.

**Checklist:**
- [ ] Ambas implementaciones satisfacen `ProductoRepository` sin modificar la interfaz
- [ ] Cambiar la variable de entorno cambia el comportamiento observable de `GET /productos` sin recompilar código de `service`/`handler`
- [ ] El test de orden inserta los productos deliberadamente desordenados por precio antes de verificar `FindAll`

# Ejercicios — Clase 4: Arquitectura, inyección de dependencias y testing

Ejercicios de evaluación para la [Clase 4](../README.md#clase-4--arquitectura-inyección-de-dependencias-y-testing). Los Ejercicios 1 y 2 son de `testing`/`go test` puro, aplicados a structs e interfaces ya vistos en la Clase 1 (no tocan Gin ni Mongo). Los Ejercicios 3 a 6 parten de la arquitectura ya armada desde la Clase 2: `handler` → `service` → `ProductoRepository` (interfaz) → `MongoProductoRepository`, con `router.Group("/productos")` y DI manual en `main.go` — y evalúan si el patrón se entendió lo suficiente como para **extenderlo**, **testearlo** y **generalizarlo**.

---

## Ejercicio 1 — Contador: value receiver vs. pointer receiver, a propósito

Ejercicio dirigido específicamente a demostrar (con un test, no solo con un comentario) la diferencia entre ambos receivers, vistos en la Clase 1.

**Requerimientos:**

1. Struct `Contador` con un campo `valor int`.
2. Método `(c *Contador) Incrementar()` — pointer receiver, suma 1 al campo del struct original.
3. Método `(c Contador) IncrementarCopia() Contador` — value receiver, devuelve una **nueva** `Contador` con el valor incrementado, sin tocar el original.
4. Función `IncrementarNVeces(c *Contador, n int)` que llame a `Incrementar()` `n` veces sobre el mismo puntero.
5. Un test que demuestre que, tras llamar `IncrementarCopia()` sobre una variable, el `valor` de la variable original **no cambió**.
6. Un test que demuestre que, tras llamar `IncrementarNVeces(&c, 5)`, el `valor` de `c` es exactamente 5 más que el inicial.

**Evalúa:** la diferencia observable (no solo teórica) entre mutar el original y devolver una copia modificada, paso de punteros a funciones (no solo a métodos), uso de `testing`/`go test` para verificar comportamiento en vez de solo imprimir y mirar a ojo.

**Checklist:**
- [ ] El test de `IncrementarCopia` falla si, por error, se cambia el receiver a puntero (para verificar que realmente están testeando la diferencia)
- [ ] `IncrementarNVeces` recibe `*Contador`, no `Contador`
- [ ] Ambos tests corren en verde con `go test -v ./...`

---

## Ejercicio 2 — Paquete de validaciones componibles

Un paquete reutilizable de validaciones de string, pensado para combinarse — practicando interfaces (Clase 1) y testing juntos.

**Requerimientos:**

1. Crear un módulo nuevo (`go mod init`) con un paquete `validaciones` (carpeta propia).
2. Interfaz `Validador` con un método `EsValido(valor string) bool`.
3. `ValidadorLongitud` (struct con `Minimo`, `Maximo int`) que valida el largo del string.
4. `ValidadorNoVacio` (struct sin campos, o con un campo de configuración si se prefiere) que valida que el string no sea `""` después de `strings.TrimSpace`.
5. `ValidadorSoloNumeros` que valida que todos los caracteres sean dígitos (se puede recorrer el string con `for _, r := range valor` y `unicode.IsDigit(r)`).
6. `ValidarTodos(vs []Validador, valor string) bool` — aplica todos los validadores de la lista con **AND lógico** (todos deben pasar).
7. Al menos 5 tests: casos válidos e inválidos para cada uno de los 3 validadores por separado, más al menos un test de `ValidarTodos` combinando varios donde uno falla.

**Evalúa:** diseño de una interfaz pensada para componerse (no una sola implementación "de juguete"), organización en un paquete propio con `go mod init`, cobertura de tests por caso límite (string vacío, justo en el límite de longitud).

**Checklist:**
- [ ] `go mod init` generó un `go.mod` válido para este paquete
- [ ] Los 3 validadores satisfacen `Validador` sin ninguna declaración de "implements"
- [ ] `ValidarTodos` con un solo validador que falla entre varios que pasan devuelve `false`
- [ ] `go test ./...` corre 5+ tests, todos en verde

---

## Ejercicio 3 — Extender el contrato de la interfaz

Agregar una capacidad nueva al repository sin romper lo existente.

**Requerimientos:**

1. Agregar un método `FindByNombre(ctx context.Context, nombre string) (model.Producto, error)` a la interfaz `ProductoRepository`.
2. Implementarlo en `MongoProductoRepository` con un filtro `bson.M{"nombre": nombre}`; si no hay coincidencia, devolver el mismo tipo de error que usa `FindByID` para "no encontrado".
3. Agregar el método correspondiente en `ProductoService` (puede llamarse igual, delegando al repository).
4. Exponer `GET /productos/buscar?nombre=...` en el handler, usando este nuevo método (no traer todo con `FindAll` y filtrar a mano en el handler — el punto es usar el nuevo método del repository).
5. Un test para `ProductoService.BuscarPorNombre` usando el `fakeProductoRepository` del Ejercicio 4 (caso encontrado y caso no encontrado), sin necesidad de Mongo real.

**Evalúa:** que agregar un método a una interfaz obliga a tocar **todas** sus implementaciones (acá `MongoProductoRepository`, y cualquier fake de test que ya exista), disciplina de propagar un cambio de contrato de forma consistente por las capas.

**Checklist:**
- [ ] `ProductoRepository` (la interfaz) tiene el método nuevo declarado
- [ ] `GET /productos/buscar?nombre=Mouse` devuelve el producto correcto; con un nombre inexistente, devuelve 404
- [ ] El handler no itera productos a mano — delega en el nuevo método del `service`

---

## Ejercicio 4 — Repository fake para testear el service sin HTTP ni Mongo real

El ejercicio central para demostrar por qué el `service` depende de una interfaz y no de `MongoProductoRepository` directamente.

**Requerimientos:**

1. En un archivo `producto_service_test.go`, definir un `fakeProductoRepository` **propio del test**: un struct que satisface `ProductoRepository` "a mano", con campos que permiten configurar qué devuelve cada método (por ejemplo, un slice `productos` predefinido, o un `error` a devolver forzado, para simular una falla del repository sin usar Mongo).
2. Usar ese fake para testear `ProductoService.Crear`, cubriendo: alta exitosa, y rechazo por nombre duplicado (agregar esta regla de negocio al `service` si todavía no existe — comparar contra los productos que devuelve `FindAll` en el fake) — sin levantar ningún servidor Gin ni contenedor de Mongo.
3. Agregar un test donde el fake **simula una falla del repository** (por ejemplo, `FindAll` devuelve un error) y verificar que `ProductoService.Crear` propaga ese error correctamente en vez de ocultarlo o hacer `panic`.
4. Ninguno de estos tests debe importar `"github.com/gin-gonic/gin"` ni requerir Mongo levantado.

**Evalúa:** comprensión real de la inyección de dependencias — un test que solo pasa usando el fake demuestra que el `service` efectivamente no conoce ni depende de `MongoProductoRepository`, sino del contrato `ProductoRepository`.

**Checklist:**
- [ ] `fakeProductoRepository` satisface `ProductoRepository` (se puede verificar con `var _ ProductoRepository = &fakeProductoRepository{}`)
- [ ] Los tests de `ProductoService` corren sin conexión a red, sin Gin y sin Docker levantado (`go test ./internal/service` funciona con Mongo apagado)
- [ ] Existe al menos un test donde el fake fuerza un error y se verifica que el service lo propaga

---

## Ejercicio 5 — DTO de salida en el handler

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

## Ejercicio 6 — Una segunda implementación, para desarrollo local sin Docker

Desde la Clase 2, `ProductoRepository` tiene una sola implementación real (`MongoProductoRepository`). Este ejercicio agrega una segunda, puramente en memoria, útil para desarrollar o testear sin depender de que Mongo esté levantado — y demuestra que la interfaz efectivamente permite intercambiarlas.

**Requerimientos:**

1. Crear `InMemoryProductoRepository`, una implementación de `ProductoRepository` que guarde los productos en un `map[string]model.Producto` protegido con `sync.Mutex` (Gin atiende requests concurrentes).
2. En `main.go`, permitir elegir cuál de las dos implementaciones se inyecta mediante una variable de entorno (por ejemplo, `REPO=memoria` vs. el default contra Mongo), sin tocar `service` ni `handler`.
3. Un test que verifique que `InMemoryProductoRepository` satisface el mismo contrato que se testeó contra el fake en el Ejercicio 4 (mismo comportamiento observable ante `Create`/`FindByID`/etc.).

**Evalúa:** que "cambiar de implementación" es literalmente una decisión de una línea en `main.go` — el mismo mecanismo que ya se usó para pasar de una interfaz a `MongoProductoRepository` en la Clase 2, ahora en la dirección inversa.

**Checklist:**
- [ ] Ambas implementaciones satisfacen `ProductoRepository` sin modificar la interfaz
- [ ] Cambiar la variable de entorno cambia el comportamiento observable (persiste o no entre reinicios) sin recompilar código de `service`/`handler`
- [ ] `InMemoryProductoRepository` usa `sync.Mutex` para proteger el mapa

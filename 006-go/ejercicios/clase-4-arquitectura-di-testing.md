# Ejercicios — Clase 4: Arquitectura, inyección de dependencias y testing

Ejercicios de evaluación para la [Clase 4](../README.md#clase-4--arquitectura-inyección-de-dependencias-y-testing). Los Ejercicios 1 y 2 son de `testing`/`go test` puro, aplicados a structs e interfaces ya vistos en la Clase 1 (no tocan Gin ni Mongo). Los Ejercicios 3 a 6 parten de la arquitectura ya armada desde la Clase 2: paquete `internal/producto/` con `handler.go` → `service.go` → `Repository` (interfaz) → `MongoRepository`, `router.Group("/productos")` y DI manual en `main.go` — y evalúan si el patrón se entendió lo suficiente como para **extenderlo**, **testearlo** y **generalizarlo**.

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

1. Agregar un método `FindByNombre(ctx context.Context, nombre string) (Producto, error)` a la interfaz `Repository` (paquete `producto`).
2. Implementarlo en `MongoRepository` con un filtro `bson.M{"nombre": nombre}`; si no hay coincidencia, devolver el mismo tipo de error que usa `FindByID` para "no encontrado".
3. Agregar el método correspondiente en `Service` (puede llamarse igual, delegando al repository).
4. Exponer `GET /productos/buscar?nombre=...` en el handler, usando este nuevo método (no traer todo con `FindAll` y filtrar a mano en el handler — el punto es usar el nuevo método del repository).
5. Un test para `Service.BuscarPorNombre` usando el `fakeRepository` del Ejercicio 4 (caso encontrado y caso no encontrado), sin necesidad de Mongo real.

**Evalúa:** que agregar un método a una interfaz obliga a tocar **todas** sus implementaciones (acá `MongoRepository`, y cualquier fake de test que ya exista), disciplina de propagar un cambio de contrato de forma consistente.

**Checklist:**
- [ ] `Repository` (la interfaz, en `repository.go`) tiene el método nuevo declarado
- [ ] `GET /productos/buscar?nombre=Mouse` devuelve el producto correcto; con un nombre inexistente, devuelve 404
- [ ] El handler no itera productos a mano — delega en el nuevo método del `service`

---

## Ejercicio 4 — Repository fake para testear el service sin HTTP ni Mongo real

El ejercicio central para demostrar por qué el `service` depende de una interfaz y no de `MongoRepository` directamente.

**Requerimientos:**

1. En un archivo `service_test.go` (mismo paquete `producto`), definir un `fakeRepository` **propio del test**: un struct que satisface `Repository` "a mano", con campos que permiten configurar qué devuelve cada método (por ejemplo, un slice `productos` predefinido, o un `error` a devolver forzado, para simular una falla del repository sin usar Mongo).
2. Usar ese fake para testear `Service.Crear`, cubriendo: alta exitosa, y rechazo por nombre duplicado (agregar esta regla de negocio al `service` si todavía no existe — comparar contra los productos que devuelve `FindAll` en el fake) — sin levantar ningún servidor Gin ni contenedor de Mongo.
3. Agregar un test donde el fake **simula una falla del repository** (por ejemplo, `FindAll` devuelve un error) y verificar que `Service.Crear` propaga ese error correctamente en vez de ocultarlo o hacer `panic`.
4. Ninguno de estos tests debe importar `"github.com/gin-gonic/gin"` ni requerir Mongo levantado.

**Evalúa:** comprensión real de la inyección de dependencias — un test que solo pasa usando el fake demuestra que el `service` efectivamente no conoce ni depende de `MongoRepository`, sino del contrato `Repository`.

**Checklist:**
- [ ] `fakeRepository` satisface `Repository` (se puede verificar con `var _ Repository = &fakeRepository{}`)
- [ ] Los tests de `Service` corren sin conexión a red, sin Gin y sin Docker levantado (`go test ./internal/producto` funciona con Mongo apagado)
- [ ] Existe al menos un test donde el fake fuerza un error y se verifica que el service lo propaga

---

## Ejercicio 5 — Un segundo DTO, específico para la respuesta

`dto.go` ya existe desde la Clase 2, con `ProductoDTO` sirviendo tanto de entrada (`POST`/`PUT`) como de salida (`GET`). Este ejercicio practica que un dominio puede necesitar **más de un** DTO cuando entrada y salida dejan de tener la misma forma.

**Requerimientos:**

1. En el mismo `dto.go`, agregar un segundo struct `ProductoRespuesta` con: `Nombre`, `Precio`, y un campo nuevo calculado `Slug` (versión del nombre en minúsculas y con espacios reemplazados por guiones, ej: `"Teclado Mecánico"` → `"teclado-mecanico"`) — sin exponer el `ID` interno del modelo.
2. Escribir un método `(p Producto) ToRespuesta() ProductoRespuesta`, análogo a `ToDTO()` pero calculando también el `Slug`, en el mismo `dto.go` (no en `service.go` ni en `repository.go`).
3. Modificar `GET /productos` y `GET /productos/:id` (en `handler.go`) para responder con `ProductoRespuesta` en vez de `ProductoDTO`.
4. `POST` y `PUT` siguen usando `ProductoDTO` como hasta ahora — son datos de **entrada**, no tiene sentido pedirles un `Slug` que todavía no existe.

**Evalúa:** que la separación modelo/DTO no termina en "un DTO por entidad" — cuando lo que se lee y lo que se escribe tienen forma distinta, son dos structs distintos, y la lógica de mapeo de cada uno sigue viviendo en `dto.go`, nunca en `service.go` ni en `repository.go`.

**Checklist:**
- [ ] El JSON de `GET /productos` ya no incluye el campo `id` (o lo reemplaza según lo decidido), pero sí incluye `slug`
- [ ] `service.go` y `repository.go` siguen trabajando exclusivamente con `Producto`, sin conocer `ProductoRespuesta`
- [ ] `POST /productos` sigue aceptando el mismo body que antes (vía `ProductoDTO`), sin pedir `slug`
- [ ] La función de mapeo tiene al menos un test que verifique el cálculo del `slug`

---

## Ejercicio 6 — Una segunda implementación, para desarrollo local sin Docker

Desde la Clase 2, `Repository` tiene una sola implementación real (`MongoRepository`). Este ejercicio agrega una segunda, puramente en memoria, útil para desarrollar o testear sin depender de que Mongo esté levantado — y demuestra que la interfaz efectivamente permite intercambiarlas.

**Requerimientos:**

1. En `repository.go` (o un archivo nuevo `memory_repository.go`, mismo paquete `producto`), crear `InMemoryRepository`, una implementación de `Repository` que guarde los productos en un `map[string]Producto` protegido con `sync.Mutex` (Gin atiende requests concurrentes).
2. En `main.go`, permitir elegir cuál de las dos implementaciones se inyecta mediante una variable de entorno (por ejemplo, `REPO=memoria` vs. el default contra Mongo), sin tocar `service.go` ni `handler.go`.
3. Un test que verifique que `InMemoryRepository` satisface el mismo contrato que se testeó contra el fake en el Ejercicio 4 (mismo comportamiento observable ante `Create`/`FindByID`/etc.).

**Evalúa:** que "cambiar de implementación" es literalmente una decisión de una línea en `main.go` — el mismo mecanismo que ya se usó para pasar de una interfaz a `MongoRepository` en la Clase 2, ahora en la dirección inversa.

**Checklist:**
- [ ] Ambas implementaciones satisfacen `Repository` sin modificar la interfaz
- [ ] Cambiar la variable de entorno cambia el comportamiento observable (persiste o no entre reinicios) sin recompilar código de `service.go`/`handler.go`
- [ ] `InMemoryRepository` usa `sync.Mutex` para proteger el mapa

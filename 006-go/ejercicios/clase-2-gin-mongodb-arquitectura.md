# Ejercicios — Clase 2: Gin, MongoDB y arquitectura en capas

Ejercicios de evaluación para la [Clase 2](../README.md#clase-2--arrancás-el-tp-gin-mongodb-y-arquitectura-en-capas). Parten del esqueleto ya armado en la práctica de esa clase: paquete `internal/producto/` con `handler.go` → `service.go` → `Repository` (interfaz) → `MongoRepository`, `router.Group("/productos")`, DI manual en `main.go`, y `docker-compose.yml` levantando Mongo y la API.

Estos ejercicios evalúan si el patrón se entendió lo suficiente como para **replicarlo** en un dominio nuevo y verificar que la persistencia y la orquestación con Docker funcionan de verdad — no la profundidad de Gin ni de Mongo (eso son las Clases 3 y 5).

---

## Ejercicio 1 — Segunda entidad, misma arquitectura

Demostrar que la arquitectura en capas no es "magia" atada a `Producto`.

**Requerimientos:**

1. Crear un paquete nuevo `internal/categoria/`, replicando **la misma estructura completa** que `internal/producto/`: `model.go` (`Categoria{ID, Nombre string}`), `repository.go` (interfaz `Repository` con los mismos 5 métodos, más `MongoRepository`), `service.go` (`Service`), `handler.go` (`Handler` + `RegisterRoutes`).
2. Registrar las rutas bajo su propio `router.Group("/categorias")`, independiente del de `/productos`, contra su propia colección de Mongo (`categorias`).
3. Cablear ambos repositories, services y handlers en `main.go` (`producto.New...` y `categoria.New...`), dejando el grafo de dependencias completo y legible.
4. El código de `internal/producto/` no debe modificarse — es una prueba de que agregar un dominio nuevo no rompe ni toca el existente.

**Evalúa:** que el patrón de paquete por dominio se generalizó, no se memorizó para un solo caso; disciplina de nombres de archivo consistente entre dos paquetes paralelos.

**Checklist:**
- [ ] `internal/producto/` e `internal/categoria/` son dos paquetes Go independientes, cada uno con sus propios `model.go`/`repository.go`/`service.go`/`handler.go`
- [ ] `GET /categorias` y `GET /productos` funcionan de forma independiente, contra colecciones distintas
- [ ] Ningún archivo de `internal/producto/` fue modificado
- [ ] `Repository`, `Service` y `Handler` se llaman igual en ambos paquetes (no `CategoriaRepository`/`ProductoRepository`) — el nombre del paquete ya da el contexto

---

## Ejercicio 2 — CRUD completo verificado de punta a punta

Confirmar que el CRUD no solo "compila", sino que persiste de verdad — con evidencia en la base, no solo en la respuesta HTTP.

**Requerimientos:**

1. Con `docker compose up --build` levantado, ejecutar contra `/productos` la secuencia completa: `POST` (crear), `GET` (listar, debe aparecer el creado), `GET /:id` (buscarlo), `PUT /:id` (modificarlo), `GET /:id` (confirmar el cambio), `DELETE /:id`, `GET /:id` (debe dar 404 ahora).
2. Documentar los comandos `curl` usados para cada paso (en un archivo aparte, ej. `pruebas.http` o un bloque de comentarios).
3. En paralelo, verificar cada paso relevante en `mongosh` (`docker exec -it <contenedor_mongo> mongosh`) con `db.productos.find()`: el documento debe aparecer después del `POST`, reflejar el cambio después del `PUT`, y desaparecer después del `DELETE`.
4. Repetir la misma secuencia para `/categorias` (Ejercicio 1).

**Evalúa:** que la API realmente persiste en Mongo (no en una variable en memoria por error de cableado), y hábito de verificar en la base además de en la respuesta HTTP — algo que en Gestock va a ser central para depurar bugs de persistencia.

**Checklist:**
- [ ] El documento creado por `POST` es visible en `mongosh` con el mismo `_id` que devuelve la API (convertido de/a `ObjectID`)
- [ ] Después del `DELETE`, el documento ya no aparece en `db.productos.find()`
- [ ] La secuencia completa se probó tanto para `Producto` como para `Categoria`

---

## Ejercicio 3 — Fallo de conexión a Mongo, manejado explícitamente

`main.go` hoy asume que Mongo siempre está disponible. Practicar qué pasa cuando no lo está.

**Requerimientos:**

1. Probar qué ocurre al ejecutar la API **sin** el servicio `mongo` levantado (`docker compose up api`, sin el `mongo`, o apuntando a un puerto equivocado en la URI) — observar el comportamiento actual y anotarlo.
2. Si `db.Conectar` devuelve error, `main.go` debe cortar el arranque con un mensaje claro (`log.Fatalf("no se pudo conectar a Mongo: %v", err)`) en vez de arrancar el servidor Gin igual y fallar recién en el primer request.
3. Agregar un endpoint `GET /health` que haga un `client.Ping(ctx, nil)` contra Mongo y devuelva `200 OK` si responde, `503 Service Unavailable` si no.
4. Probar `GET /health` con Mongo levantado (200) y, si es posible, simular que Mongo se cae después de que la API ya arrancó (`docker compose stop mongo`) y confirmar que `/health` pasa a devolver 503.

**Evalúa:** manejo explícito de una dependencia externa que puede no estar disponible, diferencia entre fallar rápido al arrancar (`log.Fatalf`) y quedar corriendo en un estado roto, un endpoint de salud como práctica estándar de cualquier servicio en contenedores.

**Checklist:**
- [ ] Sin Mongo disponible, `main.go` no queda "corriendo" en un estado inconsistente — corta con un mensaje claro
- [ ] `GET /health` responde `503`, no `500` ni un timeout colgado, cuando Mongo no responde
- [ ] `GET /health` vuelve a responder `200` apenas Mongo está disponible de nuevo, sin reiniciar la API

---

## Ejercicio 4 — El volumen realmente persiste

Verificar en la práctica la exigencia de persistencia del TP: los datos no deben perderse si el contenedor de Mongo se detiene, se elimina o se recrea.

**Requerimientos:**

1. Con el stack levantado, crear al menos 3 productos vía `POST /productos`.
2. Ejecutar `docker compose down` (sin `-v`) y luego `docker compose up -d` de nuevo.
3. Verificar con `GET /productos` (o directo en `mongosh`) que los 3 productos siguen ahí.
4. Repetir el experimento, pero esta vez con `docker compose down -v` (que sí elimina los volúmenes) y confirmar — a propósito — que en este caso los datos **sí** se pierden. Documentar en un comentario la diferencia entre ambos comandos y cuál es el correcto para un reinicio normal del entorno.

**Evalúa:** entender qué hace un volumen de Docker más allá de la definición teórica, y la diferencia entre "recrear el contenedor" (los datos sobreviven) y "borrar el volumen" (no sobreviven) — un error operacional común.

**Checklist:**
- [ ] Después de `docker compose down` + `up` (sin `-v`), los productos cargados siguen existiendo
- [ ] Después de `docker compose down -v`, la colección aparece vacía al volver a levantar el stack
- [ ] El comentario documental explica correctamente cuál de los dos comandos borra el volumen y cuál no

---

## Ejercicio 5 — `Dockerfile` multi-stage para la API

El `Dockerfile` de `api/` (Unidad 4) puede resolverse con una imagen final mucho más liviana que la de compilación, aprovechando que un binario de Go es autocontenido.

**Requerimientos:**

1. Escribir un `Dockerfile` multi-stage: una etapa `builder` basada en una imagen `golang` que compila el binario (`go build`), y una etapa final basada en una imagen mínima (por ejemplo `alpine` o `scratch`) que solo copia el binario ya compilado y lo ejecuta.
2. Comparar el tamaño de la imagen final (`docker images`) contra una versión de una sola etapa que use la imagen `golang` completa también para ejecutar. Documentar la diferencia en un comentario.
3. Confirmar que `docker compose up --build` sigue funcionando igual con el `Dockerfile` multi-stage.

**Evalúa:** por qué un binario de Go autocontenido (Clase 1) permite imágenes finales mucho más chicas que las de otros lenguajes que necesitan un runtime instalado, aplicación práctica de multi-stage builds (ya visto conceptualmente en la Unidad 4 de Docker).

**Checklist:**
- [ ] La imagen final no incluye el toolchain de Go (compilador), solo el binario y lo mínimo para correrlo
- [ ] La diferencia de tamaño documentada es significativa (la imagen `golang` completa suele rondar varios cientos de MB más que una `alpine` con el binario ya compilado)
- [ ] `docker compose up --build` levanta la API sin errores con el nuevo `Dockerfile`

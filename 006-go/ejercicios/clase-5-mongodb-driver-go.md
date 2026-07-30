# Ejercicios — Clase 5: MongoDB y el driver de Go

Ejercicios de evaluación para la [Clase 5](../README.md#clase-5--mongodb-y-el-driver-de-go). Parten de `MongoProductoRepository` ya armado en la práctica de esa clase (segunda implementación de `ProductoRepository`, conectada a un MongoDB real levantado con Docker).

Estos ejercicios van más allá del CRUD básico del driver — cubren operadores de query, paginación real, índices y testing de integración.

---

## Ejercicio 1 — Filtro por rango de precio

Extender la búsqueda más allá de por `ID` o `Nombre`.

**Requerimientos:**

1. Agregar a `ProductoRepository` un método `FindByPrecioRango(ctx context.Context, min, max float64) ([]Producto, error)`.
2. En `MongoProductoRepository`, implementarlo armando un filtro `bson.M` que combine `$gte` y `$lte` según corresponda. Si `min` es `0`, no debería restringir el límite inferior (armar el filtro condicionalmente, no siempre con ambos operadores).
3. En `InMemoryProductoRepository` (Clase 4), implementarlo también, recorriendo el slice/mapa con un `for` — sin usar Mongo. Ambas implementaciones deben satisfacer la interfaz extendida.
4. Exponer `GET /productos?precioMin=&precioMax=` en el handler, usando este método (ambos query params opcionales).
5. Probar contra Mongo real: cargar al menos 5 productos con precios distintos, y verificar que el filtro devuelve exactamente los esperados en 3 casos (solo mínimo, solo máximo, ambos).

**Evalúa:** operadores de comparación de Mongo (`$gte`/`$lte`) combinados condicionalmente, y que extender la interfaz otra vez obliga a actualizar **las dos** implementaciones existentes (in-memory y Mongo), reforzando el Ejercicio 2 de la Clase 4.

**Checklist:**
- [ ] `FindByPrecioRango(ctx, 0, 0)` (sin filtros reales) se comporta igual que `FindAll`
- [ ] El filtro Mongo generado no incluye `$gte: 0` cuando `min` es `0` (evitar operadores innecesarios en el `bson.M`)
- [ ] Las dos implementaciones (in-memory y Mongo) dan el mismo resultado ante el mismo set de datos y el mismo rango

---

## Ejercicio 2 — Paginación real contra Mongo

La paginación de la Clase 3 era manual, sobre un slice completo ya en memoria. Acá se pagina **en la base**, sin traer todo.

**Requerimientos:**

1. Agregar `FindAllPaginado(ctx context.Context, pagina, tamanio int) (productos []Producto, total int64, err error)` a la interfaz.
2. Implementarlo en `MongoProductoRepository` usando `options.Find().SetSkip(...).SetLimit(...)` para traer solo la página pedida, y `coll.CountDocuments(ctx, bson.M{})` para el total (dos operaciones contra Mongo, no una).
3. Exponer `GET /productos?pagina=&tamanio=` reutilizando este método (reemplaza o convive con el filtro por precio del Ejercicio 1, a elección — documentar la decisión).
4. Verificar contra Mongo real con al menos 12 productos cargados: pedir la página 2 con tamaño 5 y confirmar que trae los productos correctos (ni los de la página 1 ni los de la página 3) y que `total` es 12.

**Evalúa:** que paginar "de verdad" implica dos llamadas al driver (una para los datos, otra para el conteo), y no traer todo a memoria para después recortar un slice como en la Clase 3 — la diferencia entre paginación en la aplicación y paginación en la base.

**Checklist:**
- [ ] `FindAllPaginado` nunca trae más de `tamanio` documentos de Mongo en una sola llamada (usar `options.Find`, no filtrar un slice ya completo)
- [ ] `total` refleja la cantidad real de documentos en la colección, no la cantidad de la página actual
- [ ] Pedir una página fuera de rango devuelve una lista vacía sin error

---

## Ejercicio 3 — Índice único y manejo del duplicado como error de negocio

Reemplazar la validación manual de nombre duplicado (Clase 4) por una garantía real de la base.

**Requerimientos:**

1. Al inicializar la conexión (o en un script/función aparte), crear un índice único sobre el campo `nombre` de la colección `productos`, usando `coll.Indexes().CreateOne(ctx, ...)` con `options.Index().SetUnique(true)`.
2. Modificar `MongoProductoRepository.Create` para detectar específicamente el error de clave duplicada (usar la función del driver correspondiente para identificarlo, no comparar el mensaje de error como string) y devolver un error de negocio propio y claro (ej: `"ya existe un producto con ese nombre"`), distinguible de cualquier otro error del driver.
3. Quitar (o dejar como comentario explicando por qué ya no hace falta) la validación manual de nombre duplicado que estaba en `ProductoService.Crear` desde la Clase 4 — ahora la garantía la da la base, no el service.
4. Un test de integración contra Mongo real: crear un producto, intentar crear otro con el mismo nombre, y verificar que el segundo intento falla con el error de negocio esperado (no con un error crudo del driver ni con un 500 genérico si se prueba vía HTTP).

**Evalúa:** mover una regla de integridad de la capa de aplicación a la base cuando la base la puede garantizar de forma más confiable (dos requests concurrentes podrían ambas pasar la validación manual del `service` antes de que ninguna termine de escribir — el índice único no tiene esa condición de carrera), e identificar errores específicos del driver en vez de comparar strings.

**Checklist:**
- [ ] El índice único existe en la colección (verificable con `db.productos.getIndexes()` en `mongosh`)
- [ ] Un intento de alta duplicada nunca llega a insertarse (verificar con `db.productos.find()` que sigue habiendo un solo documento con ese nombre)
- [ ] El error que ve el handler es el mensaje de negocio propio, no algo como `"E11000 duplicate key error..."` crudo

---

## Ejercicio 4 — Migración de datos con `UpdateMany`

Una operación de escritura masiva, distinta a los 5 métodos CRUD ya conocidos.

**Requerimientos:**

1. Escribir una función (puede vivir en `repository` como un método extra, o en un mini binario separado `cmd/migrar-precios/main.go`) que reciba un porcentaje de aumento y un precio tope, y aplique el aumento **solo** a los productos con `precio` menor a ese tope.
2. Implementarla con `coll.UpdateMany(ctx, filtro, update)`, usando el operador `$mul` para multiplicar el precio por `(1 + porcentaje/100)` directamente en la base (no traer todos los documentos, modificarlos en Go, y guardarlos uno por uno).
3. La función debe devolver cuántos documentos fueron modificados (`UpdateResult.ModifiedCount`).
4. Probar contra Mongo real: cargar productos con precios variados, aplicar un aumento del 10% a los menores a determinado valor, y verificar en `mongosh` que solo esos cambiaron y con el valor correcto.

**Evalúa:** operaciones de escritura masiva del driver más allá de `UpdateOne`, uso de operadores aritméticos (`$mul`) para que el cálculo lo haga la base y no la aplicación, lectura del resultado de una operación (`ModifiedCount`) para confirmar el efecto.

**Checklist:**
- [ ] Los productos con precio mayor o igual al tope quedan exactamente iguales
- [ ] El aumento se calculó con `$mul` dentro del `update`, no trayendo y re-guardando documento por documento
- [ ] La función reporta el `ModifiedCount` real, no una estimación

---

## Ejercicio 5 — Test de integración contra Mongo real

Un test que efectivamente habla con la base (no un fake, a diferencia del Ejercicio 3 de la Clase 4).

**Requerimientos:**

1. En un archivo `mongo_producto_repository_test.go`, escribir un `TestMongoProductoRepository_CRUD` que se conecte a una base de **test** (ej: `productos_test`, distinta de la de desarrollo) usando `mongo.Connect` con `context.WithTimeout`.
2. El test debe: insertar un producto con `Create`, buscarlo con `FindByID` y verificar que los datos coinciden, actualizarlo con `Update` y verificar el cambio, y borrarlo con `Delete`.
3. Usar `t.Cleanup(...)` para garantizar que el documento de test se borra al final **incluso si el test falla a mitad de camino** (evitar que quede basura en la base entre corridas).
4. Documentar en un comentario al inicio del archivo qué hace falta tener corriendo antes de ejecutar este test (el contenedor de Mongo vía Docker) — este tipo de test no debería correr en un `go test ./...` normal sin ese requisito previo (opcional: investigar y aplicar un build tag o una variable de entorno para excluirlo por defecto).

**Evalúa:** diferencia entre un test unitario (Ejercicio 3 de la Clase 4, con fake, rápido, sin dependencias externas) y un test de integración (lento, depende de infraestructura real, pero verifica el comportamiento real del driver), uso de `t.Cleanup` para dejar el entorno de test limpio.

**Checklist:**
- [ ] El test corre contra una base separada de la de desarrollo, no contamina datos reales
- [ ] `t.Cleanup` borra el documento de test aunque una aserción anterior falle
- [ ] El archivo documenta el prerequisito de tener Mongo corriendo antes de ejecutar el test

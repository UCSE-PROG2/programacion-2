# Paginación de recetas: de Mongo al frontend

Este documento explica, paso a paso, cómo se implementó la lista paginada de
recetas en `007-desarrollo-cliente/007a-ejemplo`, siguiendo el recorrido de
los datos: **Mongo → Repository → Service → Handler → JSON → cliente HTTP →
componente React**.

Decisiones de diseño:

- **Tamaño de página fijo en 10**, hardcodeado en el backend (`TamanioPagina`).
  El cliente no puede elegirlo.
- **La paginación ocurre en Mongo** (`skip` + `limit`), no en memoria: nunca se
  traen todas las recetas para recortarlas después.
- **El contrato cambió**: `GET /recetas` ya no devuelve un array, devuelve un
  objeto con la página y el total de filas.

---

## 1. El contrato (antes y después)

Antes, `GET /recetas` devolvía un array plano:

```json
[ { "id": "...", "nombre": "..." }, ... ]
```

Ahora, `GET /recetas?pagina=N` devuelve un objeto:

```json
{
  "items": [ { "id": "...", "nombre": "...", "...": "..." } ],
  "total": 23,
  "pagina": 2,
  "tamanioPagina": 10
}
```

| Campo           | Significado                                                     |
| --------------- | --------------------------------------------------------------- |
| `items`         | Las recetas de **esta** página (máximo 10).                     |
| `total`         | Cantidad de recetas en **toda** la colección (no solo la página). |
| `pagina`        | Página devuelta (base 1).                                       |
| `tamanioPagina` | Filas por página (10). El frontend lo usa para calcular páginas. |

`total` es lo que permite al frontend saber cuántas páginas dibujar:
`totalPaginas = ceil(total / tamanioPagina)`. Por ejemplo, 23 recetas → 3
páginas (10 + 10 + 3).

El parámetro `pagina` es opcional (default `1`). Si no es un entero `>= 1`, la
API responde `400`.

Este contrato está definido en dos lugares que deben mantenerse espejados
(ver Unidad 7, Clase 3, "Tipando los contratos del backend"):

- Go: `PaginaRecetasDTO` en `api/internal/receta/dto.go`
- TypeScript: `PaginaRecetasDTO` en `web/src/api/types.ts`

---

## 2. MongoDB: la consulta (`repository.go`)

Archivo: `007a-ejemplo/api/internal/receta/repository.go`

El método `FindAll` (que traía todo) se reemplazó por `FindPage`:

```go
FindPage(ctx context.Context, pagina, tamanio int) ([]Receta, int64, error)
```

Hace **dos operaciones** contra Mongo:

### 2.1 Contar el total

```go
total, err := r.coll.CountDocuments(ctx, bson.M{})
```

`CountDocuments` con filtro vacío cuenta todos los documentos de la colección
`recetas`. Ese número es el `total` del contrato.

### 2.2 Traer solo la página pedida

```go
opts := options.Find().
    SetSort(bson.D{{Key: "_id", Value: 1}}).
    SetSkip(int64((pagina - 1) * tamanio)).
    SetLimit(int64(tamanio))

cursor, err := r.coll.Find(ctx, bson.M{}, opts)
```

Tres opciones, cada una con un motivo:

| Opción     | Valor                    | Por qué                                                                 |
| ---------- | ------------------------ | ----------------------------------------------------------------------- |
| `SetSort`  | `_id` ascendente         | Mongo **no garantiza orden** sin `sort`. Sin él, una receta podría repetirse o saltearse entre página y página. `_id` es único, indexado y estable. |
| `SetSkip`  | `(pagina - 1) * tamanio` | Documentos a saltear. Página 1 → 0, página 2 → 10, página 3 → 20.       |
| `SetLimit` | `tamanio` (10)           | Máximo de documentos a devolver.                                        |

Equivalente en `mongosh`:

```js
db.recetas.countDocuments({})
db.recetas.find({}).sort({ _id: 1 }).skip(10).limit(10)   // página 2
```

Luego el cursor se decodifica a `[]Receta` (`cursor.All`) y el método retorna
`(recetas, total, nil)`.

> **Nota sobre `skip`:** es simple y suficiente para este ejemplo, pero Mongo
> igualmente recorre los documentos salteados, así que en colecciones enormes
> se vuelve lento en páginas profundas. La alternativa (paginación por cursor,
> `_id > último_visto`) queda fuera del alcance de este ejemplo.

Además, la interfaz `Repository` (arriba del mismo archivo) cambió su firma:
`FindAll` desapareció y ahora está `FindPage`. Como el `Service` depende de la
interfaz, ese es el único contrato que se movió entre capas.

---

## 3. Service: tamaño fijo y conversión a DTO (`service.go`)

Archivo: `007a-ejemplo/api/internal/receta/service.go`

```go
const TamanioPagina = 10

func (s *Service) ListarPagina(ctx context.Context, pagina int) (PaginaRecetasDTO, error)
```

Acá vive el "10 hardcodeado". El service:

1. Llama a `s.repo.FindPage(ctx, pagina, TamanioPagina)`.
2. Convierte cada `Receta` (modelo de Mongo) a `RecetaDTO` con `r.ToDTO()` — el
   mismo criterio de siempre: el Handler nunca ve una `Receta` y el Repository
   nunca ve un DTO.
3. Arma y devuelve el `PaginaRecetasDTO` con `Items`, `Total`, `Pagina` y
   `TamanioPagina`.

Se eligió el Service (y no el Handler ni el Repository) para el tamaño porque
es una regla de negocio: ni HTTP ni persistencia deberían decidirla.

`Items` se crea con `make([]RecetaDTO, 0, len(recetas))` para que una página
vacía serialice como `[]` y no como `null` (el frontend hace `.length` y `.map`
sobre `items`).

---

## 4. Handler: leer `?pagina=` (`handler.go`)

Archivo: `007a-ejemplo/api/internal/receta/handler.go`

```go
pagina, err := strconv.Atoi(c.DefaultQuery("pagina", "1"))
if err != nil || pagina < 1 {
    c.JSON(http.StatusBadRequest, gin.H{"error": "pagina debe ser un entero >= 1"})
    return
}

resultado, err := h.service.ListarPagina(c.Request.Context(), pagina)
```

- `c.DefaultQuery("pagina", "1")` lee el query param, con `1` si no viene.
- Se valida en el borde HTTP: `abc`, `0` o `-3` → `400`. Esto también protege
  a Mongo, que rechaza un `skip` negativo.
- La ruta (`GET /recetas`) no cambió y sigue protegida por `authMiddleware`.
- Una `pagina` mayor a la última **no** es un error: devuelve `items: []` con
  el `total` correcto (el frontend lo aprovecha, ver paso 7).

---

## 5. Cliente HTTP del frontend (`api/recetas.ts`)

Archivo: `007a-ejemplo/web/src/api/recetas.ts`

```ts
export async function listarRecetas(token: string, pagina: number): Promise<PaginaRecetasDTO> {
  const respuesta = await fetch(`${API_URL}/recetas?pagina=${pagina}`, {
    headers: headersAutenticados(token),
  });
  return manejarRespuesta<PaginaRecetasDTO>(respuesta);
}
```

Cambios respecto a la versión anterior:

- Recibe `pagina` y lo manda como query param.
- El tipo de retorno pasó de `RecetaDTO[]` a `PaginaRecetasDTO` (definido en
  `types.ts`). Como TypeScript conoce la forma nueva, cualquier código que aún
  asuma un array **deja de compilar** — así se detectan los usos que faltaba
  actualizar.

`manejarRespuesta` no se modificó: sigue parseando el JSON y traduciendo los
errores `{"error": "..."}` a `ErrorAPI`.

---

## 6. Estado en `RecetasPage.tsx`

Archivo: `007a-ejemplo/web/src/pages/RecetasPage.tsx`

Se sumaron tres estados junto a los de la lista:

```tsx
const [pagina, setPagina] = useState(1);          // página pedida (base 1)
const [total, setTotal] = useState(0);            // filas totales, del backend
const [tamanioPagina, setTamanioPagina] = useState(10); // filas por página, del backend
```

`tamanioPagina` arranca en 10 solo como valor inicial; en cuanto llega la
primera respuesta se pisa con el que informa el backend, así que el frontend
**no depende de tener el 10 duplicado**.

### 6.1 Cargar la página

`cargarRecetas` ahora pide la página actual y guarda las tres cosas:

```tsx
const datos = await listarRecetas(token, pagina);
setRecetas(datos.items);
setTotal(datos.total);
setTamanioPagina(datos.tamanioPagina);
```

Y su `useCallback` pasó de depender de `[token]` a `[token, pagina]`. Esto es
lo que "conecta" la paginación con el resto: el `useEffect` que llama a
`cargarRecetas` depende de esa función, y esta cambia de identidad cuando
cambia `pagina`. Entonces:

> **`setPagina(n)` → nuevo render → `cargarRecetas` nueva → el `useEffect` se
> vuelve a ejecutar → nuevo `fetch` de la página `n`.**

No hay ningún `fetch` manual en los botones: solo cambian el estado `pagina`.

Crear, editar y eliminar siguen llamando a `cargarRecetas()`, que ahora
recarga **la página en la que se está parado**.

### 6.2 Página que queda vacía

Si se elimina la última receta de la última página (p. ej. la receta 21 de 21,
estando en la página 3), Mongo devuelve `items: []` para esa página, pero
`total` sigue siendo correcto (20). Se detecta y se retrocede:

```tsx
const paginas = Math.ceil(datos.total / datos.tamanioPagina);
if (datos.items.length === 0 && pagina > 1 && paginas >= 1) {
  setPagina(paginas);   // el cambio de "pagina" recarga solo
  return;
}
```

Sin esto, la persona vería una tabla vacía con la página 3 seleccionada.

---

## 7. Renderizado de los botones de página

Al final del JSX de `RecetasPage`, debajo de la tabla:

```tsx
const totalPaginas = Math.max(1, Math.ceil(total / tamanioPagina));
```

`totalPaginas` se **deriva** en cada render de `total` y `tamanioPagina`; no es
estado propio (evita tener dos valores que puedan desincronizarse).

El bloque `<nav className="paginacion">` se muestra solo si no hay error y
`total > 0`, y contiene:

1. **Anterior**: `setPagina(pagina - 1)`, deshabilitado en la página 1.
2. **Un botón por página**:
   ```tsx
   Array.from({ length: totalPaginas }, (_, i) => i + 1).map((n) => (
     <button key={n} onClick={() => setPagina(n)} disabled={cargando || n === pagina}
             aria-current={n === pagina ? "page" : undefined}
             className={n === pagina ? "activa" : undefined}>{n}</button>
   ))
   ```
   `Array.from({ length: 3 }, (_, i) => i + 1)` genera `[1, 2, 3]`. La página
   actual queda deshabilitada, con `aria-current="page"` (accesibilidad) y la
   clase `activa` (negrita, definida en `index.css`).
3. **Siguiente**: `setPagina(pagina + 1)`, deshabilitado en la última página.
4. **Contador** `"{total} recetas"`, usando el `total` que vino de Mongo.

Todos los botones se deshabilitan mientras `cargando` es `true`, para evitar
disparar varias cargas superpuestas por clicks rápidos.

---

## 8. Recorrido completo (ejemplo: 23 recetas, click en "2")

1. La persona hace click en el botón **2** → `setPagina(2)`.
2. React re-renderiza; `cargarRecetas` (dependencia `pagina`) cambia de
   identidad y el `useEffect` la ejecuta.
3. `listarRecetas(token, 2)` → `GET /recetas?pagina=2` con
   `Authorization: Bearer <token>`.
4. `AuthMiddleware` valida el JWT; `Handler.List` lee `pagina=2`, valida `>= 1`.
5. `Service.ListarPagina` llama a `repo.FindPage(ctx, 2, 10)`.
6. `MongoRepository.FindPage` ejecuta:
   - `countDocuments({})` → `23`
   - `find({}).sort({_id:1}).skip(10).limit(10)` → recetas 11 a 20
7. El Service convierte a DTOs y responde
   `{ items: [10 recetas], total: 23, pagina: 2, tamanioPagina: 10 }`.
8. `manejarRespuesta` parsea el JSON; `cargarRecetas` hace
   `setRecetas`, `setTotal(23)`, `setTamanioPagina(10)`.
9. React re-renderiza: la tabla muestra las 10 recetas, `totalPaginas = 3`, se
   dibujan los botones `1 2 3` con el `2` marcado y deshabilitado, y
   "23 recetas" al lado.

---

## 9. Archivos modificados

| Archivo                                            | Cambio                                                        |
| -------------------------------------------------- | ------------------------------------------------------------- |
| `api/internal/receta/dto.go`                       | Nuevo `PaginaRecetasDTO`.                                     |
| `api/internal/receta/repository.go`                | `FindAll` → `FindPage` (count + sort/skip/limit).             |
| `api/internal/receta/service.go`                   | `ListarTodas` → `ListarPagina`; constante `TamanioPagina`.    |
| `api/internal/receta/handler.go`                   | `List` lee y valida `?pagina=`.                               |
| `web/src/api/types.ts`                             | Nueva interface `PaginaRecetasDTO`.                           |
| `web/src/api/recetas.ts`                           | `listarRecetas(token, pagina)` devuelve `PaginaRecetasDTO`.   |
| `web/src/pages/RecetasPage.tsx`                    | Estado de paginación, recarga por página, barra de botones.   |
| `web/src/index.css`                                | Estilos `.paginacion` / `.activa`.                            |

(Rutas relativas a `007-desarrollo-cliente/007a-ejemplo/`.)

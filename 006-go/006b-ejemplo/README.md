# 006b-ejemplo — API de una Biblioteca (Go + Gin + MongoDB)

CRUD de `Libro` con Gin + MongoDB, misma arquitectura en capas que el TP
(`producto` → acá `libro`). Ver `api/cmd/api/main.go` para el detalle de
Docker y `internal/libro/` para el código.

## Índice

1. [Setup del entorno (VS Code + debug)](#setup-del-entorno-vs-code--debug)
2. [Dependencias del proyecto (`go get`)](#dependencias-del-proyecto-go-get)
3. [Cómo correrlo](#cómo-correrlo)
4. [Endpoints](#endpoints)
5. [Postman](#postman)

---

## Setup del entorno (VS Code + debug)

1. **Go instalado** (ver Clase 1 del readme de la unidad):

   - macOS:
     ```bash
     brew install go
     ```
   - Windows: descargar el instalador `.msi` desde [go.dev/dl](https://go.dev/dl/)
     y correrlo (agrega Go al `PATH` automáticamente), o con `winget`:
     ```powershell
     winget install GoLang.Go
     ```

   Verificar en cualquier plataforma:
   ```bash
   go version
   ```

2. **Extensión de Go para VS Code**:
   ```bash
   code --install-extension golang.go
   ```
   (o desde el panel de extensiones: buscar `Go`, del publisher *Go Team at
   Google*, `golang.go`).

3. **Herramientas de la toolchain** (`gopls` — language server, `dlv` —
   debugger, más linters). Abrir la carpeta `api/` en VS Code, `Cmd+Shift+P` →
   `Go: Install/Update Tools` → seleccionar todas → Enter. Equivalente por
   CLI, si se prefiere no usar el comando de VS Code:
   ```bash
   go install golang.org/x/tools/gopls@latest
   go install github.com/go-delve/delve/cmd/dlv@latest
   ```

4. **Debug**: la configuración ya está en `api/.vscode/launch.json`
   (`Debug API (Go)`, con `MONGO_URI=mongodb://localhost:27017`). Con Mongo
   corriendo (ver [Cómo correrlo](#cómo-correrlo), opción B) y `api/` como
   carpeta raíz abierta en VS Code:
   - poner un breakpoint (ej. en `handler.go`, línea de `h.service.Crear`)
   - `F5` o panel *Run and Debug* → `Debug API (Go)`
   - disparar el request (curl o Postman) contra `localhost:8080`

## Dependencias del proyecto (`go get`)

Secuencia real usada para armar `go.mod`/`go.sum` de este proyecto, parado en
`api/`:

```bash
go mod init biblioteca/api                              # 1. crea go.mod
go get github.com/gin-gonic/gin                          # 2. framework HTTP
go get go.mongodb.org/mongo-driver/v2/mongo               # 3. driver de MongoDB
go get github.com/golang-jwt/jwt/v5                        # 4. generar/validar JWT (internal/auth)
go get golang.org/x/crypto/bcrypt                          # 5. hashear passwords (internal/auth)
go mod tidy                                                # 6. resuelve indirectas y go.sum
```

`golang.org/x/crypto/bcrypt` se usa en `internal/auth/hash.go` (`HashPassword`/
`CheckPassword`) para nunca guardar una contraseña en texto plano, y
`github.com/golang-jwt/jwt/v5` en `internal/auth/jwt.go`
(`GenerarToken`/`ValidarToken`) para emitir y validar el token que
`internal/middleware.AuthMiddleware` exige en `PUT /usuarios/password`.

`go get` sin versión trae la última release estable de cada módulo; `go mod
tidy` agrega las dependencias transitivas que falten y escribe sus checksums
en `go.sum`. Para clonar este repo y volver a resolver todo desde cero (sin
depender de que `go.sum` ya esté commiteado):

```bash
cd api
go mod tidy
go build ./...
```

## Cómo correrlo

### Opción A — Todo con Docker

```bash
docker compose up --build     # desde 006b-ejemplo/
```

API en `http://localhost:8080`. Para bajar: `docker compose down` (`-v` para
borrar también el volumen de datos).

### Opción B — Go local + Mongo suelto (necesaria para debug)

```bash
docker run -d --name mongo-biblioteca -p 27017:27017 -v datos-biblioteca:/data/db mongo:7
cd api
go run ./cmd/api
```

### Puerto 8080 ocupado

Si al correr la API falla porque el puerto 8080 ya está tomado (por ejemplo,
quedó un `go run` anterior colgado), matar el proceso que lo tiene:

- macOS/Linux:
  ```bash
  lsof -ti:8080 | xargs kill -9
  ```
- Windows (PowerShell):
  ```powershell
  Stop-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess -Force
  ```

## Endpoints

| Método | Ruta | Body |
|---|---|---|
| `GET` | `/libros` | — |
| `GET` | `/libros/buscar?titulo=...&autor=...&disponible=...&anioDesde=...&anioHasta=...&fechaDesde=...&fechaHasta=...` | — |
| `GET` | `/libros/:id` | — |
| `POST` | `/libros` | `{"titulo","autor","isbn","anio_edicion","disponible","fecha_ingreso"}` |
| `PUT` | `/libros/:id` | ídem |
| `DELETE` | `/libros/:id` | — |
| `POST` | `/usuarios/registro` | `{"email","password"}` |
| `POST` | `/usuarios/login` | `{"email","password"}` |
| `PUT` | `/usuarios/password` (requiere `Authorization: Bearer <token>`) | `{"passwordActual","passwordNueva"}` |

```bash
curl -X POST http://localhost:8080/libros \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Cien años de soledad","autor":"Gabriel García Márquez","isbn":"978-0307474728","anio_edicion":1967,"disponible":true,"fecha_ingreso":"1970-06-05"}'
```

### Búsqueda (`GET /libros/buscar`)

Todos los parámetros son opcionales y se combinan con AND — ver Clase 4 del
readme de la unidad ("Filtros opcionales: armar el `bson.M` a partir de query
params") para el detalle de cómo `internal/libro/repository.go` arma el
filtro de Mongo a partir de `BusquedaLibros`, sin traer nunca a memoria un
documento que no matchea:

| Parámetro | Tipo de filtro | Operador Mongo |
|---|---|---|
| `titulo` | Aproximación de texto, case-insensitive (busca "contiene") | `$regex` + `$options: "i"` |
| `autor` | Igualdad exacta | — |
| `disponible` | Igualdad exacta (`true`/`false`) | — |
| `anioDesde` / `anioHasta` | Rango numérico sobre `anio_edicion` | `$gte` / `$lte` |
| `fechaDesde` / `fechaHasta` | Rango de fechas sobre `fecha_ingreso` (formato `AAAA-MM-DD`) | `$gte` / `$lte` |

```bash
# Título aproximado
curl "http://localhost:8080/libros/buscar?titulo=soledad"

# Rango de año de edición
curl "http://localhost:8080/libros/buscar?anioDesde=1960&anioHasta=1965"

# Rango de fecha de ingreso
curl "http://localhost:8080/libros/buscar?fechaDesde=2024-01-01&fechaHasta=2024-12-31"

# Combinando varios filtros a la vez
curl "http://localhost:8080/libros/buscar?autor=Gabriel%20Garc%C3%ADa%20M%C3%A1rquez&disponible=false&anioHasta=1965"
```

### Usuarios (registro, login y cambio de contraseña)

`internal/usuario/` sigue la misma organización por dominio que `internal/libro/`
(ver Clase 2), y se apoya en dos paquetes transversales nuevos: `internal/auth/`
(hashing con bcrypt + emisión/validación de JWT) y `internal/middleware/`
(`AuthMiddleware`, que valida el JWT y deja disponible el ID del usuario
autenticado en el contexto de la request) — ver Clase 3 del readme de la
unidad para el detalle teórico de cada pieza.

```bash
# Registro
curl -X POST http://localhost:8080/usuarios/registro \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@test.com","password":"clave1234"}'

# Login -> devuelve { "token": "eyJhbG..." }
curl -X POST http://localhost:8080/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@test.com","password":"clave1234"}'

# Cambio de contraseña autenticado (requiere el token del login)
curl -X PUT http://localhost:8080/usuarios/password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"passwordActual":"clave1234","passwordNueva":"otraClave5678"}'
```

## Postman

`postman/Biblioteca-API.postman_collection.json` — 25 requests en orden
(crear → listar → buscar con cada filtro opcional por separado y combinados →
actualizar → eliminar → 404 → registro/login/cambio de password de usuario),
con tests automáticos y `base_url` precargada en `http://localhost:8080`.
Import → correr con la API arriba.

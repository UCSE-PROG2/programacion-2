# 007a-ejemplo — Recetario (API en Go + cliente React/TypeScript)

App de ejemplo de la **Unidad 7**: un monorepo con su propia API en Go (`api/`, dominio **Receta** + login/registro/perfil) y un cliente en React + TypeScript (`web/`) que la consume — login, registro y una home protegida que muestra los datos del usuario logueado. Mismo stack y arquitectura que la Unidad 6 (`006-go/006b-ejemplo`: Gin + MongoDB + JWT + bcrypt), pero con su propia base de datos — no comparte cuentas ni datos con esa unidad. Ver el [README de la unidad](../README.md) para la teoría de cada clase.

## Índice

1. [Requisitos](#requisitos)
2. [Cómo correrlo](#cómo-correrlo)
3. [Variables de entorno](#variables-de-entorno)
4. [Estructura del proyecto](#estructura-del-proyecto)
5. [Qué hace la app](#qué-hace-la-app)
6. [Componentes del cliente y orden de renderizado](#componentes-del-cliente-y-orden-de-renderizado)

## Requisitos

- Node.js 20+ (`node -v`)
- Docker (para levantar la API + MongoDB), o Go 1.25+ y un MongoDB propio si se prefiere correr la API en local sin Docker

## Cómo correrlo

**1. Levantar el backend** (API + MongoDB), desde esta carpeta (`007a-ejemplo/`):

```bash
docker compose up --build
```

Confirmar que responde en `http://localhost:8080` (`curl -X POST http://localhost:8080/usuarios/registro -H "Content-Type: application/json" -d '{"email":"ana@test.com","password":"12345678"}'` debería devolver el usuario creado).

> Si el docker-compose de `006-go/006b-ejemplo` ya está corriendo, bajalo primero (`docker compose down` desde esa carpeta) — ambos usan los mismos puertos (8080 y 27017) y no pueden correr al mismo tiempo.
>
> Si ya tenés un Mongo propio corriendo en el puerto 27017, `docker compose up` va a fallar por conflicto de puerto — en ese caso corré la API en local en vez de con Docker: `cd api && MONGO_URI=mongodb://localhost:27017 go run ./cmd/api`.

**2. Instalar dependencias y correr el cliente**, desde `007a-ejemplo/web/`:

```bash
cd web
npm install
npm run dev
```

Abrir `http://localhost:5173`. Sin sesión, redirige directo a `/login`; desde ahí, "Registrate" crea una cuenta nueva contra la API y deja a la persona ya logueada en la home, con un header arriba (marca, nav a Inicio/Recetas, usuario + botón de logout) para moverse por el resto de la app.

## Variables de entorno

`web/.env` (ya commiteado con un default para desarrollo local, no tiene nada sensible):

```
VITE_API_URL=http://localhost:8080
```

Si la API corre en otro host/puerto, cambiar este valor — o crear un `.env.local` (gitignoreado) que lo pise sin tocar el default commiteado.

## Estructura del proyecto

```
007a-ejemplo/
├── docker-compose.yml     ← mongo + api
├── api/                    ← API en Go (Gin + MongoDB + JWT + bcrypt)
│   ├── cmd/api/main.go     ← punto de entrada, cablea todo
│   └── internal/
│       ├── auth/            ← hashing (bcrypt) + JWT
│       ├── db/               ← conexión a MongoDB
│       ├── middleware/       ← AuthMiddleware + CORSMiddleware
│       ├── receta/           ← dominio Receta: CRUD completo bajo /recetas (requiere sesión)
│       └── usuario/          ← dominio Usuario: registro, login, /me
└── web/                    ← cliente React + TypeScript
    └── src/
        ├── api/
        │   ├── types.ts       ← interfaces que reflejan los DTOs de api/internal/
        │   ├── usuarios.ts     ← registrar/login/obtenerPerfil sobre fetch
        │   └── recetas.ts      ← listar/crear/actualizar/eliminar sobre fetch, con token
        ├── auth/
        │   └── AuthContext.tsx ← estado global de sesión (Context API), token en localStorage
        ├── components/
        │   ├── RutaProtegida.tsx
        │   └── Layout.tsx       ← header con marca, nav (Inicio/Recetas) y usuario/logout
        ├── pages/
        │   ├── LoginPage.tsx
        │   ├── RegistroPage.tsx
        │   ├── HomePage.tsx
        │   └── RecetasPage.tsx  ← catálogo + formulario de alta/edición, CRUD completo
        ├── App.tsx              ← rutas (react-router-dom) + AuthProvider
        └── main.tsx
```

## Qué hace la app

| Ruta | Acceso | Qué muestra |
|---|---|---|
| `/login` | Público | Formulario de login. Si ya hay sesión, no hay redirect automático desde acá — pero `/` y `/recetas` sí exigen sesión |
| `/registro` | Público | Formulario de registro (`POST /usuarios/registro`); al confirmar, hace login automático con las mismas credenciales |
| `/` | Requiere sesión (`RutaProtegida` + `Layout`) | Home: `id` y `email` del usuario autenticado, pedidos con `GET /usuarios/me` + el token guardado |
| `/recetas` | Requiere sesión (`RutaProtegida` + `Layout`) | Catálogo de recetas: listar, crear, editar y eliminar contra la API real (`GET/POST/PUT/DELETE /recetas`) |

El token JWT se guarda en `localStorage` al hacer login, y la sesión persiste entre recargas (`AuthProvider` lo valida contra `GET /usuarios/me` al montar la app). "Cerrar sesión" (en el header) borra el token y redirige a `/login`.

Cada receta se audita del lado del backend: `Receta` (modelo de Mongo) guarda quién la creó, quién la modificó por última vez, y cuándo pasó cada cosa — pero `RecetaDTO` (lo que efectivamente viaja por HTTP) no tiene esos campos, así que nunca llegan al cliente ni aparecen en la tabla (mismo criterio que `UsuarioDTO` excluyendo el hash de la contraseña).

## Componentes del cliente y orden de renderizado

### Carpetas de `web/src/`

Antes de seguir archivo por archivo, esto es lo que se puede esperar encontrar adentro de cada carpeta:

- **`api/`**: toda la comunicación con el backend. Nada de JSX ni de pantalla acá — son funciones que arman requests con `fetch`, tipan lo que va y viene, y traducen los errores del backend a algo que las páginas puedan mostrar.
- **`auth/`**: el estado de sesión, compartido por toda la app. Es donde vive la lógica de "quién está logueado", el token, y las funciones para iniciar/cerrar sesión — cualquier componente que necesite saber si hay usuario logueado consulta esta carpeta, no maneja el token por su cuenta.
- **`components/`**: piezas de UI reutilizables que no son una página en sí mismas, sino que envuelven o protegen a otras. Acá va cualquier cosa que se repita en más de una pantalla (un componente que revisa si hay sesión iniciada antes de dejar entrar a una ruta, un header común), a diferencia de algo específico de una sola pantalla.
- **`pages/`**: una carpeta por pantalla completa de la app, una por cada ruta. Cada archivo arma su propio formulario, tabla o contenido, y usa lo de `api/` y `auth/` para hablar con el backend y saber quién está logueado.

### Orden de renderizado

Todo arranca en `main.tsx`: es el único archivo que toca el DOM directamente, y lo único que hace es montar el componente `App` dentro del `<div id="root">` de `index.html`. De ahí para abajo, todo es un árbol de componentes de React, uno adentro del otro:

1. **`App` (`App.tsx`)** envuelve toda la aplicación en un `BrowserRouter` — el componente de la librería `react-router-dom` que le permite a la app cambiar de "página" cambiando la URL, sin recargar el navegador.
2. Adentro va **`AuthProvider`** (`auth/AuthContext.tsx`). Se renderiza siempre, antes que cualquier página, porque toda la app depende de saber si hay sesión o no. Es lo que React llama un "Context": un componente que guarda un dato (acá, el usuario logueado y su token) y se lo deja disponible a cualquier componente de más abajo sin tener que pasarlo a mano de padre a hijo. Apenas se monta, busca un token en `localStorage` y, si existe, lo valida contra el backend (`GET /usuarios/me`) antes de dejar avanzar a ninguna ruta — mientras espera esa respuesta, la app está en estado "cargando".
3. Ya con la sesión resuelta (haya o no usuario), se renderiza **`Routes`**, que mira la URL actual y decide cuál de las cuatro páginas mostrar: `/login`, `/registro`, `/` o `/recetas`.
4. Para `/` y `/recetas` (las que necesitan sesión), antes de la página se intercalan dos componentes más, siempre en el mismo orden: primero **`RutaProtegida`**, que actúa de guardia — si la sesión todavía está "cargando" muestra un simple "Cargando...", si ya terminó de cargar y no hay usuario redirige a `/login`, y solo si hay sesión confirmada deja pasar lo que tiene adentro (en React eso que "tiene adentro" se llama `children`). Superada esa guardia se renderiza **`Layout`**, que dibuja el header fijo (marca, links de navegación, email del usuario y botón de logout) y, dentro de un `<main>`, muestra la página final como su propio `children`.
5. Esa página final es **`HomePage`** o **`RecetasPage`**, según la ruta.
6. `LoginPage` y `RegistroPage` no pasan por ninguno de los pasos 4 y 5: se renderizan solas, con su propia tarjeta centrada en la pantalla, porque todavía no hay sesión iniciada ni nada que mostrar en un header.

### Componentes

| Componente | Archivo | Qué hace |
|---|---|---|
| `AuthProvider` / `useAuth` | `auth/AuthContext.tsx` | Contexto global de sesión: expone `usuario`, `token`, `cargando`, `login`, `registrar` y `logout`. Guarda el JWT en `localStorage` y resuelve la sesión contra `GET /usuarios/me` al montar la app |
| `RutaProtegida` | `components/RutaProtegida.tsx` | Guard de ruta: mientras `cargando` es `true` muestra "Cargando...", si no hay `usuario` redirige a `/login`, y si hay sesión renderiza los `children` |
| `Layout` | `components/Layout.tsx` | Header persistente (marca, nav Inicio/Recetas con `NavLink`, email del usuario, botón "Cerrar sesión") + `<main>` que envuelve la página recibida como `children` |
| `LoginPage` | `pages/LoginPage.tsx` | Formulario controlado de login; al confirmar navega a `/` |
| `RegistroPage` | `pages/RegistroPage.tsx` | Formulario controlado de registro; al confirmar hace login automático con las mismas credenciales y navega a `/` |
| `HomePage` | `pages/HomePage.tsx` | Muestra `id` y `email` del usuario autenticado (sin pedir nada nuevo a la API — usa el `usuario` que ya resolvió `AuthProvider`) |
| `RecetasPage` | `pages/RecetasPage.tsx` | CRUD completo de recetas: un formulario (que sirve tanto para crear como para editar, según `editandoId`) y una tabla con acciones Editar/Eliminar, contra la API real |

### Contenido de cada carpeta (`web/src/`)

| Carpeta | Contiene |
|---|---|
| `api/` | Cliente HTTP a mano sobre `fetch`, sin librerías externas. `types.ts` (interfaces que reflejan los DTOs del backend), `usuarios.ts` (`registrar`, `login`, `obtenerPerfil`), `recetas.ts` (`listarRecetas`, `crearReceta`, `actualizarReceta`, `eliminarReceta`, todas con el token en el header `Authorization`) |
| `auth/` | Un solo archivo: `AuthContext.tsx`, el `AuthProvider` y el hook `useAuth` descriptos arriba |
| `components/` | Piezas compartidas entre páginas: `RutaProtegida.tsx` (guard) y `Layout.tsx` (header + marco) |
| `pages/` | Una página por ruta: `LoginPage.tsx`, `RegistroPage.tsx`, `HomePage.tsx`, `RecetasPage.tsx` |
| *(raíz de `src/`)* | `App.tsx` (rutas con `react-router-dom` + `AuthProvider`), `main.tsx` (punto de entrada, monta `<App />` en `#root`), `index.css` (estilos globales: `.pagina-centrada`/`.tarjeta` para Login/Registro, `.header`/`.contenido` para Layout, `.panel`/`.tabla-wrapper` para RecetasPage) |

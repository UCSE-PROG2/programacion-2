# Unidad 7 — Programación en el cliente (React + TypeScript)

Material de apoyo para la **Unidad 7** de **Programación 2** — Ingeniería en Computación (UCSE).

Hasta acá (Unidad 6) construimos una **API**: recibe requests HTTP, devuelve JSON, no le importa quién la consume. Esta unidad cierra ese círculo desde el otro lado — un **cliente web** real, que corre en el navegador de otra persona, que llama a esa API y le muestra algo a un usuario. El framework elegido es **React**, con **TypeScript** en vez de JavaScript plano, siguiendo el mismo criterio que en la Unidad 6 con Go: tipado estático desde el día uno, no como un agregado posterior.

`007a-ejemplo/` es un proyecto real que va creciendo clase a clase: un frontend de React + TypeScript (`web/`) contra su propia API de Go (`api/`) — mismo stack y arquitectura que la Unidad 6 (Gin + MongoDB + JWT + bcrypt), pero con su propia base de datos, no la de `006-go/006b-ejemplo`, y con el dominio **Receta** en vez de `libro`. En esta primera iteración cubre **login, registro y una home que muestra los datos del usuario autenticado**, pidiéndolos con el token JWT que ya conocés de la Clase 3 de la Unidad 6.

---

## Índice

1. [Clase 1 — De la API al navegador: React, TypeScript y el entorno](clase-1-react-typescript-entorno.md)
2. [Clase 2 — Estado, eventos y formularios controlados](clase-2-estado-eventos-formularios.md)
3. [Clase 3 — Efectos, `fetch` y contratos tipados con el backend](clase-3-fetch-efectos-contratos.md)
4. [Clase 4 — Ruteo, sesión y estado global de autenticación](clase-4-ruteo-sesion-auth.md)
5. [El ejemplo de la unidad (`007a-ejemplo`)](#el-ejemplo-de-la-unidad-007a-ejemplo)
6. [Recursos recomendados](#recursos-recomendados)

---

## El ejemplo de la unidad (`007a-ejemplo`)

Ver `007a-ejemplo/README.md` para instrucciones de instalación y ejecución. Resumen de lo que cubre esta primera iteración:

- `web/`: proyecto Vite + React + TypeScript, sin librerías de UI externas.
- Páginas: **Login**, **Registro**, **Home** y **Recetas** (protegidas, ambas detrás de sesión), con `react-router-dom`.
- Un `Layout` (header con marca, navegación y usuario/logout) envuelve las páginas con sesión — Login/Registro quedan afuera, como tarjetas centradas sin navegación.
- `AuthContext` (Context API) para el estado de sesión, con persistencia del token en `localStorage`.
- Cliente HTTP propio sobre `fetch`, con interfaces de TypeScript que reflejan uno a uno los DTOs de `007a-ejemplo/api` (`RegistroDTO`, `LoginDTO`, `UsuarioDTO`, `RecetaDTO`).
- Home pide `GET /usuarios/me` y muestra el id y el email del usuario autenticado.
- Recetas: catálogo con CRUD completo (listar, crear, editar, eliminar) contra `/recetas` — cada receta queda auditada en el backend con quién la creó/modificó y cuándo, sin exponer esos campos al cliente.
- `api/`: API propia en Go (Gin + MongoDB + JWT + bcrypt), misma arquitectura en capas de la Unidad 6, con su propia base de datos (`recetario`) — no comparte cuentas ni datos con `006-go/006b-ejemplo`.

---

## Recursos recomendados

- [react.dev](https://react.dev/) — documentación oficial de React, con tutorial interactivo
- [typescriptlang.org/docs](https://www.typescriptlang.org/docs/) — documentación oficial de TypeScript
- [vitejs.dev](https://vitejs.dev/) — documentación de Vite
- [reactrouter.com](https://reactrouter.com/) — documentación de React Router
- [MDN — Using Fetch](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch) — referencia de la Fetch API
- [MDN — CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) — referencia completa de CORS
- [Curso React](https://www.youtube.com/watch?v=yIr_1CasXkM)
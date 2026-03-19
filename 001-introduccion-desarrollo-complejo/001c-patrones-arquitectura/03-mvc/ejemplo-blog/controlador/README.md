# Controlador (`controlador/` o `controller/`)

Recibe la acción del usuario, llama al modelo y decide qué vista mostrar o qué dato devolver.

## Archivos que deberían ir en esta carpeta

- **PostController.java** — Métodos: `listarPosts()`, `verPost(id)`, `crearPost(titulo, contenido)`. Usa el modelo y actualiza o elige la vista.
- Si es web: **BlogServlet.java** o controlador Spring que mapee rutas (`/posts`, `/posts/{id}`) y devuelva vista o JSON.

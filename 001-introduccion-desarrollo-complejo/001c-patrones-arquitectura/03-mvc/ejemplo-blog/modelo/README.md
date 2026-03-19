# Modelo (`modelo/` o `model/`)

Entidades (Post, Usuario, Comentario), reglas de negocio y acceso a datos.

## Archivos que deberían ir en esta carpeta

- **Post.java** — Entidad: id, título, contenido, autor, fecha.
- **Comentario.java** — Entidad asociada a un post.
- **PostRepository.java** o **PostDAO.java** — Guardar, listar, buscar por id.
- **PostService.java** — Lógica: crear post, validar, listar publicados (si aplica).

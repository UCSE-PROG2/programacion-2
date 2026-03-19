# Vista (`vista/` o `view/`)

Muestra datos y envía acciones del usuario (clic en “publicar”, “editar”) al controlador.

## Archivos que deberían ir en esta carpeta

- Si es web: **posts.html**, **detalle-post.html**, **formulario-post.html** — Templates que reciben datos del controlador.
- Si es consola: **VistaBlog.java** — Imprime listado de posts y menú (crear, ver, salir); lee entrada por teclado y notifica al controlador (por callbacks o devolviendo opción elegida).

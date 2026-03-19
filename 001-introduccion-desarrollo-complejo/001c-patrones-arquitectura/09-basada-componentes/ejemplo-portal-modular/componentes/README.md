# Componentes concretos (`componentes/`)

Módulos que implementan la interfaz del portal; cada uno ofrece título y datos o fragmento para una sección de la página.

## Archivos que deberían ir en esta carpeta

- **ComponenteNoticias.java** — Implementa `ComponentePortal`; internamente usa un `ServicioNoticias` (inyectado) para obtener últimas noticias y devuelve título + datos o fragmento.
- **ComponenteClima.java** — Implementa `ComponentePortal`; usa `ServicioClima` y devuelve datos para mostrar clima actual.
- **ComponenteUsuario.java** — Muestra datos del usuario logueado; usa `ContextoPortal` para obtener el usuario.

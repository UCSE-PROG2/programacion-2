# Contenedor/Portal (`portal/`)

Ensambla los componentes, les pasa el contexto y genera la página (HTML, JSON o estructura para la vista).

## Archivos que deberían ir en esta carpeta

- **Portal.java** — Lista o registro de componentes (`ComponenteNoticias`, `ComponenteClima`, `ComponenteUsuario`). Método `renderizarPagina()` que recorre los componentes, les pasa el contexto y ensambla el resultado (HTML, JSON o estructura para una vista).
- **ConfiguracionPortal.java** — Define qué componentes se cargan y en qué orden (archivo de config o código).

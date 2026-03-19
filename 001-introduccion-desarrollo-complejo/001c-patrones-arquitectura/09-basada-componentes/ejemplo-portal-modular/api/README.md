# Definición de interfaces (`api/` o `interfaces/`)

Contrato que todo componente del portal debe cumplir y contexto que el portal pasa a cada uno.

## Archivos que deberían ir en esta carpeta

- **ComponentePortal.java** — Interfaz: `String getId()`, `String getTitulo()`, `Fragmento renderizar(ContextoPortal ctx)` (o `Map<String,Object> getDatos()` para que el portal renderice). Define el contrato que todo componente debe cumplir.
- **ContextoPortal.java** — Objeto que el portal pasa a cada componente (usuario actual, preferencias, servicios compartidos) para que puedan obtener datos.

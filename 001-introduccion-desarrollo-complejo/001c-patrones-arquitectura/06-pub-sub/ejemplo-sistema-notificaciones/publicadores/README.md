# Publicadores (`publicadores/`)

Componentes que emiten eventos (ej. “nueva notificación”, “alerta por stock bajo”) a un tema.

## Archivos que deberían ir en esta carpeta

- **ServicioNotificaciones.java** — Cuando ocurre un evento (ej. “pedido enviado”), llama a `eventBus.publicar("notificaciones", mensaje)`.
- **ServicioAlertas.java** — Publica en tema "alertas" (ej. stock bajo, fallo de pago).

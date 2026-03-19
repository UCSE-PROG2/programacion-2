# Consumidores (`consumidores/`)

Componentes que se suscriben a eventos y reaccionan: actualizan dashboard, escriben en log, envían alertas por email/SMS.

## Archivos que deberían ir en esta carpeta

- **DashboardConsumer.java** — Suscrito a eventos de métricas y alarmas; actualiza estructuras en memoria (o modelo) que la vista del dashboard lee para refrescar gráficos y alertas.
- **LogEventosConsumer.java** — Suscrito a todos (o a alarmas); escribe cada evento en consola o archivo.
- **NotificadorConsumer.java** — Suscrito a alarmas; simula envío de notificación (email/SMS) o escribe en log.

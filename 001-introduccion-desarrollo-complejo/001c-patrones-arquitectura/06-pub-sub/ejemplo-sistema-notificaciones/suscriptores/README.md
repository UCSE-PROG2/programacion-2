# Suscriptores (`suscriptores/`)

Componentes que se registran a un tema y reaccionan al recibir mensajes (enviar email, mostrar en UI, escribir en log).

## Archivos que deberían ir en esta carpeta

- **EmailSubscriber.java** — Se suscribe a "notificaciones" (o "emails"); al recibir mensaje, simula envío de email (log o mock).
- **LogSubscriber.java** — Suscrito a uno o varios temas; escribe cada mensaje en consola o archivo.
- **UINotificationSubscriber.java** — Suscrito a "notificaciones"; actualiza una cola o lista que la interfaz consume para mostrar notificaciones en pantalla.

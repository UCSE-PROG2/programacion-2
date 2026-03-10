# Ejemplo: Sistema de notificaciones (Pub/Sub)

Este ejemplo aplica el **patrón Publicador/Suscriptor (Pub/Sub)**: unos componentes publican mensajes en canales o temas y otros se suscriben para recibirlos, sin conocerse entre sí. Típico en sistemas distribuidos y de alta escalabilidad.

## Papel de cada parte

- **Publicadores**: emiten eventos (ej. “nueva notificación”, “alerta por stock bajo”).
- **Broker/Canal**: medio donde se publican y del que se suscriben (puede ser en memoria para el ejercicio).
- **Suscriptores**: se registran a un tema y reaccionan al recibir mensajes (enviar email, mostrar en UI, escribir en log).

## Archivos que deberían ir en esta carpeta

### Núcleo Pub/Sub (`core/` o `pubsub/`)
- `EventBus.java` o `MessageBroker.java` — Interfaz o clase que permita `suscribir(tema, handler)` y `publicar(tema, mensaje)`. Mantiene la lista de suscriptores por tema y les notifica al publicar.
- `Mensaje.java` o `Evento.java` — Objeto que se publica (tema, payload, timestamp opcional).

### Publicadores (`publicadores/`)
- `ServicioNotificaciones.java` — Cuando ocurre un evento (ej. “pedido enviado”), llama a `eventBus.publicar("notificaciones", mensaje)`.
- `ServicioAlertas.java` — Publica en tema "alertas" (ej. stock bajo, fallo de pago).

### Suscriptores (`suscriptores/`)
- `EmailSubscriber.java` — Se suscribe a "notificaciones" (o "emails"); al recibir mensaje, simula envío de email (log o mock).
- `LogSubscriber.java` — Suscrito a uno o varios temas; escribe cada mensaje en consola o archivo.
- `UINotificationSubscriber.java` — Suscrito a "notificaciones"; actualiza una cola o lista que la interfaz consume para mostrar notificaciones en pantalla.

### Punto de entrada
- `Main.java` — Crea el EventBus, registra suscriptores, dispara uno o más publicadores y comprueba que los suscriptores reciben los mensajes.

## Cómo comprobar que está bien aplicado

- Los publicadores no conocen a los suscriptores; solo publican a un tema.
- Añadir un nuevo suscriptor (ej. SMS) no requiere cambiar los publicadores.
- Los suscriptores pueden ser de distintos módulos o servicios (en un diseño distribuido, el broker sería externo: RabbitMQ, Redis, etc.).

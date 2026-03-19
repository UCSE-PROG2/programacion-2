# Núcleo de eventos (`core/` o `eventos/`)

Canal donde se publican eventos y del que se suscriben los handlers (en memoria, o Kafka/RabbitMQ en producción).

## Archivos que deberían ir en esta carpeta

- **EventBus.java** o **EventPublisher.java** — `publicar(Evento e)`, `suscribir(tipoDeEvento, handler)`.
- **Evento.java** — Clase base o interfaz; subclases: **EventoMetrica** (nombre, valor, timestamp), **EventoAlarma** (nivel, mensaje, origen).

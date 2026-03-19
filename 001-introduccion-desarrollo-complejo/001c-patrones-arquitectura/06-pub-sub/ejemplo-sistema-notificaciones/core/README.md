# Núcleo Pub/Sub (`core/` o `pubsub/`)

Medio donde se publican mensajes y del que se suscriben los handlers (puede ser en memoria para el ejercicio).

## Archivos que deberían ir en esta carpeta

- **EventBus.java** o **MessageBroker.java** — Interfaz o clase que permita `suscribir(tema, handler)` y `publicar(tema, mensaje)`. Mantiene la lista de suscriptores por tema y les notifica al publicar.
- **Mensaje.java** o **Evento.java** — Objeto que se publica (tema, payload, timestamp opcional).

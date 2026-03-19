# Ejemplo: Sistema de notificaciones (Pub/Sub)

Este ejemplo aplica el **patrón Publicador/Suscriptor (Pub/Sub)**: unos componentes publican mensajes en canales o temas y otros se suscriben para recibirlos, sin conocerse entre sí. Típico en sistemas distribuidos y de alta escalabilidad.

## Estructura

- **core/** — EventBus o broker y tipo de mensaje/evento.
- **publicadores/** — Componentes que emiten eventos a un tema.
- **suscriptores/** — Componentes que se registran a un tema y reaccionan al recibir mensajes.

## Punto de entrada

- **Main.java** — Crea el EventBus, registra suscriptores, dispara uno o más publicadores y comprueba que los suscriptores reciben los mensajes. Puede estar en la raíz del ejemplo.

## Cómo comprobar que está bien aplicado

- Los publicadores no conocen a los suscriptores; solo publican a un tema.
- Añadir un nuevo suscriptor (ej. SMS) no requiere cambiar los publicadores.
- Los suscriptores pueden ser de distintos módulos o servicios (en un diseño distribuido, el broker sería externo: RabbitMQ, Redis, etc.).

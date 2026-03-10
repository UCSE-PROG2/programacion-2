# Ejemplo: Dashboard de monitoreo (Arquitectura orientada a eventos)

Este ejemplo aplica **arquitectura orientada a eventos**: los componentes se comunican mediante **eventos**. Quien emite no conoce a los consumidores; quienes reaccionan se suscriben a tipos de eventos (cambio de estado, métrica, alarma). Muy usado en tiempo real, dashboards y procesos industriales.

## Papel de cada parte

- **Productores de eventos**: sensores o servicios que emiten “métrica actualizada”, “alarma disparada”, “estado cambiado”.
- **Bus de eventos**: canal donde se publican y del que se suscriben (en memoria, o Kafka/RabbitMQ en producción).
- **Consumidores**: actualizan dashboard, escriben en log, envían alertas por email/SMS.

## Archivos que deberían ir en esta carpeta

### Núcleo de eventos (`core/` o `eventos/`)
- `EventBus.java` o `EventPublisher.java` — `publicar(Evento e)`, `suscribir(tipoDeEvento, handler)`.
- `Evento.java` — Clase base o interfaz; subclases: `EventoMetrica` (nombre, valor, timestamp), `EventoAlarma` (nivel, mensaje, origen).

### Productores (`productores/`)
- `SimuladorMetricas.java` — Genera eventos de tipo “métrica” cada X segundos (CPU, memoria, latencia) y los publica.
- `ServicioAlertas.java` — Cuando se cumple una condición (ej. CPU > 80%), publica un `EventoAlarma`.

### Consumidores (`consumidores/`)
- `DashboardConsumer.java` — Suscrito a eventos de métricas y alarmas; actualiza estructuras en memoria (o modelo) que la vista del dashboard lee para refrescar gráficos y alertas.
- `LogEventosConsumer.java` — Suscrito a todos (o a alarmas); escribe cada evento en consola o archivo.
- `NotificadorConsumer.java` — Suscrito a alarmas; simula envío de notificación (email/SMS) o escribe en log.

### Vista del dashboard (`vista/` o `dashboard/`)
- `DashboardVista.java` — Lee los datos que actualiza `DashboardConsumer` y los muestra (consola, Swing, o HTML con actualización periódica). No publica eventos; solo visualiza.

### Punto de entrada
- `Main.java` — Crea el bus, registra consumidores, arranca el simulador de métricas y (opcional) la vista del dashboard.

## Cómo comprobar que está bien aplicado

- Los productores no conocen a los consumidores; solo publican eventos.
- Añadir un nuevo consumidor (ej. guardar en BD) no requiere cambiar productores ni otros consumidores.
- El sistema reacciona en “tiempo real” en la medida en que los eventos se publican y se procesan de forma asíncrona.

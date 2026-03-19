# Productores de eventos (`productores/`)

Sensores o servicios que emiten “métrica actualizada”, “alarma disparada”, “estado cambiado”.

## Archivos que deberían ir en esta carpeta

- **SimuladorMetricas.java** — Genera eventos de tipo “métrica” cada X segundos (CPU, memoria, latencia) y los publica.
- **ServicioAlertas.java** — Cuando se cumple una condición (ej. CPU > 80%), publica un `EventoAlarma`.

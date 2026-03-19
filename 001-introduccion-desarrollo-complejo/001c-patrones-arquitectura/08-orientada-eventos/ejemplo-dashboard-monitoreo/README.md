# Ejemplo: Dashboard de monitoreo (Arquitectura orientada a eventos)

Este ejemplo aplica **arquitectura orientada a eventos**: los componentes se comunican mediante **eventos**. Quien emite no conoce a los consumidores; quienes reaccionan se suscriben a tipos de eventos (cambio de estado, métrica, alarma). Muy usado en tiempo real, dashboards y procesos industriales.

## Estructura

- **core/** — Bus de eventos y tipos de evento (métrica, alarma).
- **productores/** — Sensores o servicios que emiten eventos.
- **consumidores/** — Actualizan dashboard, escriben en log, envían alertas.
- **vista/** — Dashboard que muestra los datos que actualizan los consumidores.

## Punto de entrada

- **Main.java** — Crea el bus, registra consumidores, arranca el simulador de métricas y (opcional) la vista del dashboard. Puede estar en la raíz del ejemplo.

## Cómo comprobar que está bien aplicado

- Los productores no conocen a los consumidores; solo publican eventos.
- Añadir un nuevo consumidor (ej. guardar en BD) no requiere cambiar productores ni otros consumidores.
- El sistema reacciona en “tiempo real” en la medida en que los eventos se publican y se procesan de forma asíncrona.

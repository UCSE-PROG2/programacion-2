# Ejemplo: Cotizaciones de bolsa (Observer)

**Observer** define un mecanismo de suscripción para notificar a varios objetos cuando cambia el estado del sujeto. Aquí: cotización de una acción (sujeto); varios observadores (pantalla, alerta, registro en log) que se actualizan cuando cambia el precio.

## Archivos que deberían ir en esta carpeta

### Sujeto (observable)
- `Cotizacion.java` — Interfaz o clase: método `registrar(ObservadorCotizacion o)`, `desregistrar(ObservadorCotizacion o)`. Método `setPrecio(double precio)` que actualiza el precio y notifica a todos los observadores registrados (llamando a `o.actualizar(this)` o `o.actualizar(precio)`). Lista privada de observadores.

### Observador
- `ObservadorCotizacion.java` — Interfaz: `actualizar(Cotizacion c)` o `actualizar(double precio, String simbolo)`.

### Observadores concretos
- `PantallaCotizacion.java` — Implementa `ObservadorCotizacion`; en `actualizar()` actualiza lo que muestra (consola o UI).
- `AlertaPrecio.java` — Si el precio supera un umbral, muestra alerta o envía notificación.
- `RegistroLog.java` — Escribe cada cambio en un log.

### Cliente
- `Main.java` — Crea una cotización (sujeto), crea y registra los observadores. Simula cambios de precio con `cotizacion.setPrecio(100.5)`; los observadores reaccionan sin que el sujeto conozca sus detalles.

## Cómo comprobar que está bien aplicado

- Añadir un nuevo tipo de observador no requiere modificar el sujeto; solo implementar la interfaz y registrarse.
- El sujeto no conoce la lógica de cada observador; solo llama a `actualizar()`.

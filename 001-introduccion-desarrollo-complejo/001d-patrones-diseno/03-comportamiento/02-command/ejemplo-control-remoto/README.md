# Ejemplo: Control remoto (Command)

**Command** convierte una solicitud en un objeto; así se pueden parametrizar, encolar o deshacer. Aquí: control remoto con acciones encender, apagar, cambiar canal (como en el ejercicio de la PPT).

## Archivos que deberían ir en esta carpeta

### Comando (interfaz)
- `Comando.java` — Interfaz: `void ejecutar()` (y opcionalmente `void deshacer()`).

### Comandos concretos
- `ComandoEncender.java` — Implementa `Comando`; tiene referencia al receptor (TV o dispositivo). `ejecutar()` llama a `receptor.encender()`.
- `ComandoApagar.java` — Llama a `receptor.apagar()`.
- `ComandoCambiarCanal.java` — Guarda el número de canal en el constructor o como atributo; `ejecutar()` llama a `receptor.cambiarCanal(canal)`.

### Receptor
- `Television.java` — Clase con métodos `encender()`, `apagar()`, `cambiarCanal(int)`. Es quien realiza la acción real.

### Invocador
- `ControlRemoto.java` — Tiene una lista o mapa de botones (ej. slot 0 = encender, 1 = apagar, 2 = cambiar canal). Método `pulsarBoton(int slot)` que obtiene el `Comando` asociado y llama a `comando.ejecutar()`. Opcional: historial de comandos para undo.

### Cliente
- `Main.java` — Crea la TV, los comandos (pasando la TV), el control remoto; asigna cada comando a un botón; simula pulsar botones. No llama directamente a `tv.encender()`; siempre a través del comando.

## Cómo comprobar que está bien aplicado

- Añadir una nueva acción (ej. subir volumen) es crear un nuevo `Comando` y asignarlo a un botón; el control remoto y la TV no cambian de interfaz.
- Los comandos son objetos que se pueden guardar en una cola o historial (para undo/rehacer).

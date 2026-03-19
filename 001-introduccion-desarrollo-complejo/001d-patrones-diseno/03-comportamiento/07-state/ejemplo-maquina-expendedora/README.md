# Ejemplo: Máquina expendedora (State)

**State** hace que un objeto cambie su comportamiento según su estado interno; parece que cambia de clase. Aquí: máquina expendedora con estados (sin moneda, con moneda, vendiendo, sin stock); cada estado define qué hacer al insertar moneda, pulsar botón o dispensar.

## Archivos que deberían ir en esta carpeta

### Contexto
- `MaquinaExpendedora.java` — Tiene referencia al estado actual (`EstadoMaquina estado`), cantidad de productos, monedas insertadas. Métodos que delegan en el estado: `insertarMoneda()`, `elegirProducto()`, `devolverMoneda()`. Setter `setEstado(EstadoMaquina e)` para que los estados cambien el estado de la máquina.

### Estado (interfaz o abstracto)
- `EstadoMaquina.java` — Interfaz o clase abstracta con métodos: `insertarMoneda()`, `elegirProducto()`, `devolverMoneda()` (y opcionalmente `dispensar()`). Cada método puede hacer algo y luego llamar a `maquina.setEstado(siguienteEstado)`.

### Estados concretos
- `SinMoneda.java` — `insertarMoneda()` pone la máquina en estado “ConMoneda”; `elegirProducto()` no hace nada (o mensaje).
- `ConMoneda.java` — `elegirProducto()` pasa a “Vendiendo” y dispara dispensar; `devolverMoneda()` vuelve a “SinMoneda”.
- `Vendiendo.java` — Dispensa y, si hay stock, vuelve a “SinMoneda”; si no hay, a “SinStock”.
- `SinStock.java` — Cualquier acción devuelve mensaje o no hace nada hasta reponer.

### Cliente
- `Main.java` — Crea la máquina (estado inicial SinMoneda); simula insertar moneda, elegir producto, etc. La máquina responde según el estado actual; el cliente no comprueba estados con if/switch.

## Cómo comprobar que está bien aplicado

- La lógica condicional (qué hacer en cada caso) está en las clases de estado, no en la máquina.
- Añadir un nuevo estado implica una nueva clase y transiciones desde/hacia otros estados; el contexto sigue delegando en `estado.metodo()`.

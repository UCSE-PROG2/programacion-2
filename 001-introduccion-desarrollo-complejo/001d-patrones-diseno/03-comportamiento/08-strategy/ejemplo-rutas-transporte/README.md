# Ejemplo: Rutas de transporte (Strategy)

**Strategy** define una familia de algoritmos, los encapsula en clases y los hace intercambiables. Aquí: cálculo de ruta entre dos puntos; estrategias distintas (en coche, en bici, a pie) con diferentes tiempos o criterios.

## Archivos que deberían ir en esta carpeta

### Estrategia (interfaz)
- `EstrategiaRuta.java` — Interfaz: `Ruta calcular(Punto origen, Punto destino)` (o `double calcularTiempo(...)`). Define el contrato que todas las estrategias cumplen.

### Estrategias concretas
- `RutaEnCoche.java` — Implementa `EstrategiaRuta`; calcula usando velocidad media del coche y tal vez restricciones de tráfico (simplificado).
- `RutaEnBici.java` — Misma interfaz; lógica distinta (velocidad menor, quizá atajos).
- `RutaAPie.java` — Misma interfaz; lógica para peatón.

### Contexto
- `CalculadorRutas.java` — Tiene un atributo `EstrategiaRuta estrategia` (inyectado por constructor o setter). Método `calcularRuta(Punto a, Punto b)` que delega en `estrategia.calcular(a, b)` y devuelve el resultado. Opcional: método `setEstrategia(EstrategiaRuta e)` para cambiar en tiempo de ejecución.

### Cliente
- `Main.java` — Crea el calculador con una estrategia (ej. `new RutaEnCoche()`); llama a `calcularRuta(...)`. Para otro medio, cambia la estrategia sin modificar el calculador ni el cliente (solo la instancia inyectada).

## Cómo comprobar que está bien aplicado

- Añadir un nuevo medio de transporte es una nueva clase que implementa `EstrategiaRuta`; el contexto no cambia.
- El cliente depende de la abstracción (interfaz), no de clases concretas.

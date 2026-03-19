# Ejemplo: Árbol con texturas compartidas (Flyweight)

**Flyweight** comparte el estado intrínseco (común) entre muchos objetos para ahorrar memoria; el estado extrínseco se pasa por parámetro. Aquí: un bosque de árboles donde cada árbol tiene posición (extrínseco) pero comparte tipo/textura (intrínseco).

## Archivos que deberían ir en esta carpeta

### Flyweight (estado intrínseco compartido)
- `TipoArbol.java` — Clase que representa el tipo (nombre, ruta de textura, color base). Es lo que se comparte; no tiene coordenadas.

### Factory de flyweights
- `FabricaTiposArbol.java` — Mantiene un mapa `String nombre -> TipoArbol`. Método `obtenerTipo(String nombre, String textura, String color)` que, si no existe, crea un `TipoArbol` y lo guarda; luego lo devuelve. Así todos los árboles del mismo tipo comparten una sola instancia de `TipoArbol`.

### Objeto contextual (usa el flyweight + estado extrínseco)
- `Arbol.java` — Tiene coordenadas `x`, `y` (extrínseco) y una referencia a `TipoArbol` (intrínseco compartido). Método `dibujar()` que usa las coordenadas propias y los datos del tipo (textura, color).

### Cliente
- `Bosque.java` o `Main.java` — Crea muchos `Arbol` con pocos tipos distintos (pino, roble, etc.); obtiene cada tipo vía `FabricaTiposArbol.obtenerTipo(...)`. En memoria hay muchas instancias de `Arbol` pero pocas de `TipoArbol`.

## Cómo comprobar que está bien aplicado

- Varios `Arbol` con el mismo tipo deben referenciar la misma instancia de `TipoArbol` (misma referencia en memoria).
- El estado que varía por árbol (posición) no está dentro de `TipoArbol`.

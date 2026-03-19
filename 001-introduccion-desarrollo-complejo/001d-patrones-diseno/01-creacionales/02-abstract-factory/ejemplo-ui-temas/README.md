# Ejemplo: UI con temas (Abstract Factory)

**Abstract Factory** produce familias de objetos relacionados sin especificar clases concretas. Aquí: tema claro y tema oscuro; cada tema define botón, cuadro de texto y etiqueta con el mismo estilo.

## Archivos que deberían ir en esta carpeta

### Productos abstractos (interfaces)
- `Boton.java` — Interfaz: `renderizar()`, `getColorFondo()` (o similar).
- `CuadroTexto.java` — Interfaz para el widget de texto.
- `Etiqueta.java` — Interfaz para la etiqueta.

### Productos concretos por familia
- Tema claro: `BotonClaro.java`, `CuadroTextoClaro.java`, `EtiquetaClara.java` — Implementan las interfaces con colores/estilos claros.
- Tema oscuro: `BotonOscuro.java`, `CuadroTextoOscuro.java`, `EtiquetaOscura.java` — Mismos interfaces, estilo oscuro.

### Fábrica abstracta y concretas
- `FabricaUI.java` — Interfaz con métodos: `crearBoton()`, `crearCuadroTexto()`, `crearEtiqueta()`.
- `FabricaTemaClaro.java` — Implementa `FabricaUI`; cada método devuelve el componente del tema claro.
- `FabricaTemaOscuro.java` — Implementa `FabricaUI`; cada método devuelve el componente del tema oscuro.

### Cliente
- `VentanaLogin.java` o `Main.java` — Recibe una `FabricaUI`; construye la ventana usando solo `crearBoton()`, `crearCuadroTexto()`, `crearEtiqueta()`, sin referencias a clases concretas (BotonClaro, etc.).

## Cómo comprobar que está bien aplicado

- El cliente no instancia nunca `BotonClaro` ni `BotonOscuro`; solo usa la fábrica.
- Añadir un nuevo tema implica una nueva implementación de `FabricaUI` y sus productos; el cliente no cambia.

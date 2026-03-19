# Ejemplo: Editor con undo (Memento)

**Memento** guarda y restaura el estado interno de un objeto sin exponer sus detalles. Aquí: editor de texto que puede hacer “guardar punto de restauración” y “deshacer” (restaurar estado anterior).

## Archivos que deberían ir en esta carpeta

### Originador
- `EditorTexto.java` — Tiene el estado: texto actual (String o lista de líneas). Método `escribir(String s)` que modifica el texto. Método `guardarEstado()` que devuelve un `MementoEditor` con una copia del estado (el memento se crea aquí y se rellena con el estado interno). Método `restaurarEstado(MementoEditor m)` que recibe un memento (creado antes por este editor) y restaura el texto desde él (solo el originador puede leer/restaurar desde el memento).

### Memento
- `MementoEditor.java` — Clase que almacena una copia del estado (ej. String texto, o fecha). No expone setters públicos que permitan modificar el estado desde fuera; puede tener getter que solo use el originador (o estar en el mismo paquete). El editor es quien escribe y lee el memento.

### Cuidador (caretaker)
- `HistorialUndo.java` — Guarda una pila (o lista) de `MementoEditor`. Método `guardar(EditorTexto editor)` que llama a `editor.guardarEstado()` y apila el memento. Método `deshacer(EditorTexto editor)` que desapila un memento y llama a `editor.restaurarEstado(memento)`.

### Cliente
- `Main.java` — Crea editor e historial; tras cada cambio importante llama a `historial.guardar(editor)`; cuando el usuario pide “deshacer”, llama a `historial.deshacer(editor)`. No accede al contenido interno del memento.

## Cómo comprobar que está bien aplicado

- El estado guardado en el memento no es modificable desde fuera del originador.
- Añadir más datos al estado del editor implica actualizar solo el memento y el originador; el cuidador sigue manejando mementos de forma opaca.

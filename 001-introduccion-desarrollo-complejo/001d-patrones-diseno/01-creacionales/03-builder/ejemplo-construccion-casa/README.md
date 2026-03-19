# Ejemplo: Construcción de casa (Builder)

**Builder** construye objetos complejos paso a paso; el mismo código de construcción puede producir distintas representaciones. Aquí: una casa con propiedades como habitaciones, metros cuadrados y cocheras (como en el ejercicio de la PPT).

## Archivos que deberían ir en esta carpeta

### Producto
- `Casa.java` — Clase con atributos: `int habitaciones`, `double metrosCuadrados`, `int cocheras` (y opcionalmente otros). Getters; los setters pueden ser privados o omitirse si se usa solo el builder.

### Builder
- `CasaBuilder.java` — Clase con métodos encadenables: `conHabitaciones(int)`, `conMetrosCuadrados(double)`, `conCocheras(int)`, que guardan los valores y devuelven `this`. Método `build()` que instancia `Casa` y le asigna los valores configurados, y lo devuelve.

### Uso (Director opcional)
- `Main.java` — Ejemplo: `Casa casa = new CasaBuilder().conHabitaciones(3).conMetrosCuadrados(120).conCocheras(2).build();` Sin necesidad de constructor con muchos parámetros ni setters públicos.

## Cómo comprobar que está bien aplicado

- La construcción es fluida y legible; se pueden omitir parámetros (ej. cocheras por defecto 0) manejando valores por defecto en el builder o en `Casa`.
- Añadir un nuevo atributo a `Casa` implica añadir un método `conX(int)` en el builder y asignarlo en `build()`; el cliente sigue usando la misma API.

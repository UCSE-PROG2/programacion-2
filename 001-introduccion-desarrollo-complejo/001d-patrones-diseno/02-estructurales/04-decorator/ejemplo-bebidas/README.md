# Ejemplo: Bebidas con agregados (Decorator)

**Decorator** añade responsabilidades a un objeto envolviéndolo en otro que implementa la misma interfaz. Aquí: bebida base (café, té) y decoradores (leche, azúcar, crema) que suman precio y descripción.

## Archivos que deberían ir en esta carpeta

### Componente
- `Bebida.java` — Interfaz: `double getPrecio()`, `String getDescripcion()`.

### Concretos (bebidas base)
- `Cafe.java` — Implementa `Bebida`; precio y descripción fijos (“Café”).
- `Te.java` — Implementa `Bebida`; precio y descripción fijos (“Té”).

### Decorador base
- `AgregadoBebida.java` — Clase abstracta que implementa `Bebida` y tiene un atributo `Bebida bebida`. Constructor recibe la bebida a decorar. Los métodos por defecto delegan en `bebida`; las subclases los sobrescriben para sumar precio y ampliar descripción.

### Decoradores concretos
- `ConLeche.java` — Extiende `AgregadoBebida`; `getPrecio()` devuelve `bebida.getPrecio() + 0.5`; `getDescripcion()` devuelve `bebida.getDescripcion() + ", con leche"`.
- `ConAzucar.java`, `ConCrema.java` — Análogos.

### Cliente
- `Main.java` — Ejemplo: `Bebida b = new ConLeche(new ConAzucar(new Cafe()));` luego `b.getPrecio()` y `b.getDescripcion()`. No hay subclases “CafeConLeche”, “CafeConAzucarYLeche”, etc.

## Cómo comprobar que está bien aplicado

- Se pueden combinar decoradores en cualquier orden; el cliente trabaja siempre con la interfaz `Bebida`.
- Añadir un nuevo agregado es una nueva clase que extiende `AgregadoBebida`; no se tocan las bebidas base.

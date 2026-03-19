# Ejemplo: Colección de personas (Iterator)

**Iterator** recorre los elementos de una colección sin exponer su representación interna (lista, árbol, etc.). Aquí: una colección de personas (array, lista o estructura propia) y un iterador que permite recorrerla con `hasNext()` y `next()`.

## Archivos que deberían ir en esta carpeta

### Iterador (interfaz)
- `IteradorPersona.java` — Interfaz: `boolean hasNext()`, `Persona next()` (o `Object next()` si se usa genérico). Opcional: `void remove()`.

### Colección (interfaz o clase que devuelve iterador)
- `ColeccionPersonas.java` — Interfaz o clase abstracta con método `IteradorPersona crearIterador()` (o `iterator()`).

### Implementación concreta de la colección
- `ListaPersonas.java` — Implementa `ColeccionPersonas`; internamente usa un array o ArrayList. `crearIterador()` devuelve una instancia de `IteradorListaPersonas` que recorre esa estructura.

### Iterador concreto
- `IteradorListaPersonas.java` — Implementa `IteradorPersona`; tiene referencia a la lista y un índice (o cursor). `hasNext()` comprueba si hay más elementos; `next()` avanza y devuelve el siguiente `Persona`.

### Elemento
- `Persona.java` — Clase simple (nombre, edad, etc.).

### Cliente
- `Main.java` — Obtiene un iterador de la colección y recorre con `while (it.hasNext()) { Persona p = it.next(); ... }`. No accede al array o lista interna; solo al iterador.

## Cómo comprobar que está bien aplicado

- El cliente no conoce si la colección es array, lista o árbol; solo usa el iterador.
- Añadir otro tipo de colección (ej. árbol) implica una nueva clase de colección y su iterador; la forma de recorrer en el cliente sigue siendo la misma.

# Ejemplo: Archivos y carpetas (Composite)

**Composite** compone objetos en estructuras de árbol y trata tanto los nodos hoja como los compuestos de forma uniforme. Aquí: elementos del sistema de archivos (archivo = hoja, carpeta = compuesto que contiene archivos y/o carpetas).

## Archivos que deberían ir en esta carpeta

### Componente (interfaz común)
- `ElementoSistemaArchivos.java` — Interfaz o clase abstracta: `getNombre()`, `getTamaño()`, `listar()` (o `mostrar(String indent)`). Define el contrato para hojas y compuestos.

### Hoja
- `Archivo.java` — Implementa `ElementoSistemaArchivos`; tiene nombre y tamaño; `getTamaño()` devuelve el tamaño; `listar()` devuelve solo este archivo (o una lista de un elemento).

### Compuesto
- `Carpeta.java` — Implementa `ElementoSistemaArchivos`; tiene una colección de `ElementoSistemaArchivos` (hijos). Métodos `agregar(ElementoSistemaArchivos)`, `quitar(ElementoSistemaArchivos)`. `getTamaño()` suma los tamaños de los hijos; `listar()` recorre recursivamente y devuelve todos los elementos (o imprime con indentación).

### Cliente
- `Main.java` — Construye un árbol: carpeta raíz con carpetas y archivos dentro; llama a `getTamaño()` o `listar()` sobre la raíz sin distinguir si es archivo o carpeta.

## Cómo comprobar que está bien aplicado

- El cliente usa solo la interfaz `ElementoSistemaArchivos`; no hace `instanceof` para tratar carpetas distinto de archivos (salvo si la interfaz lo exige).
- Añadir un nuevo tipo de elemento (ej. enlace simbólico) implica una nueva clase que implemente la misma interfaz.

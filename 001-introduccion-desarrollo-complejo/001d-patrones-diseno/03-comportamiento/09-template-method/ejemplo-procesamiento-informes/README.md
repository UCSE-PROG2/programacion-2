# Ejemplo: Procesamiento de informes (Template Method)

**Template Method** define el esqueleto de un algoritmo en una clase base y deja que las subclases redefinan algunos pasos sin cambiar la estructura. Aquí: generación de informes (leer datos, formatear, escribir salida); los pasos concretos (formato PDF, texto, HTML) varían.

## Archivos que deberían ir en esta carpeta

### Clase abstracta (template)
- `GeneradorInforme.java` — Clase abstracta con método `final generarInforme()` que: (1) llama a `leerDatos()`, (2) llama a `formatear(datos)`, (3) llama a `escribirSalida(formato)`. Los métodos (1)(2)(3) son abstractos o tienen implementación por defecto. Así el flujo está fijado; solo cambian los pasos.

### Subclases concretas
- `InformePDF.java` — Extiende `GeneradorInforme`; implementa `leerDatos()` (ej. desde BD), `formatear()` para estructura PDF, `escribirSalida()` que escribe archivo PDF.
- `InformeTexto.java` — Mismo esqueleto; `formatear()` y `escribirSalida()` para texto plano.
- `InformeHTML.java` — Mismo esqueleto; formato HTML.

### Cliente
- `Main.java` — Usa `GeneradorInforme gen = new InformePDF(); gen.generarInforme();` (o con la subclase que corresponda). No conoce el orden de los pasos; eso está en la clase base.

## Cómo comprobar que está bien aplicado

- El algoritmo (orden de pasos) está en un solo lugar (la clase base); las subclases solo rellenan los huecos.
- Añadir un nuevo tipo de informe es una nueva subclase que implementa los métodos abstractos; no se duplica el flujo en cada una.

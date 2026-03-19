# Ejemplo: Documentos clonables (Prototype)

**Prototype** permite copiar objetos existentes sin que el código dependa de sus clases concretas. Aquí: documentos (informe, contrato) que se pueden clonar para crear borradores o copias con pequeños cambios.

## Archivos que deberían ir en esta carpeta

### Prototipo
- `Documento.java` — Interfaz que extiende `Cloneable` (o define `Documento clonar()`) y declara el método de clonación. Si es clase abstracta, puede tener atributos comunes (titulo, contenido, fecha) y un método `clonar()` que devuelve una copia.

### Concretos
- `Informe.java` — Implementa `Documento`; atributos propios si aplica (secciones, autor). Implementa `clonar()` creando un nuevo `Informe` y copiando campos.
- `Contrato.java` — Implementa `Documento`; implementa `clonar()` de forma análoga.

### Cliente
- `GestorDocumentos.java` o `Main.java` — Tiene un documento “plantilla”; cuando necesita una copia (borrador, versión nueva), llama a `documento.clonar()` y trabaja con la copia sin usar `new Informe(...)` con todos los campos.

## Cómo comprobar que está bien aplicado

- El cliente no conoce los constructores ni la estructura interna de `Informe`/`Contrato` para hacer la copia; solo usa `clonar()`.
- Si la copia es superficial, documentarlo; si hay referencias a objetos mutables, considerar copia profunda en el propio prototipo.

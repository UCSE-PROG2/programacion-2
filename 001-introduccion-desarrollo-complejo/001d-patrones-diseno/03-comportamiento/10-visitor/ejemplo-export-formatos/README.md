# Ejemplo: Exportar a distintos formatos (Visitor)

**Visitor** separa algoritmos de los objetos sobre los que operan; se añaden nuevas operaciones sin modificar las clases de los elementos. Aquí: estructura de nodos (documento con secciones y párrafos); visitantes que exportan a PDF, JSON o HTML.

## Archivos que deberían ir en esta carpeta

### Elementos (nodos que aceptan visitante)
- `NodoDocumento.java` — Interfaz: método `aceptar(VisitanteNodo v)`. Todas las clases de nodo lo implementan llamando a `v.visitar(this)` (sobrecarga por tipo de nodo).
- `Documento.java` — Implementa `NodoDocumento`; tiene lista de hijos (secciones). `aceptar(v)` llama a `v.visitar(this)` y luego recorre hijos aceptando el mismo visitante.
- `Seccion.java` — Nodo con título y lista de párrafos; implementa `aceptar` y delega en el visitante.
- `Parrafo.java` — Hoja; implementa `aceptar` llamando a `v.visitar(this)`.

### Visitante (interfaz)
- `VisitanteNodo.java` — Interfaz con métodos `visitar(Documento d)`, `visitar(Seccion s)`, `visitar(Parrafo p)`. Cada visitante concreto implementa qué hacer en cada tipo.

### Visitantes concretos
- `VisitanteExportarPDF.java` — Implementa `VisitanteNodo`; en cada `visitar(...)` genera la parte correspondiente del PDF (acumula en un buffer o escritor).
- `VisitanteExportarJSON.java` — Igual con estructura JSON.
- `VisitanteExportarHTML.java` — Igual con etiquetas HTML.

### Cliente
- `Main.java` — Construye un documento (raíz con secciones y párrafos). Crea un visitante (ej. `VisitanteExportarPDF`), llama a `documento.aceptar(visitante)` y obtiene el resultado (archivo o string). Para otro formato, usa otro visitante sin tocar las clases de nodo.

## Cómo comprobar que está bien aplicado

- Añadir una nueva operación (ej. exportar a XML) es una nueva clase que implementa `VisitanteNodo`; las clases Documento, Seccion, Parrafo no cambian.
- Las clases de nodo solo tienen el método `aceptar(VisitanteNodo)`; la lógica de exportación está en los visitantes.

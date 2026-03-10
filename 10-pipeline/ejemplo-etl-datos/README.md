# Ejemplo: ETL de datos (Pipeline / Tubería)

Este ejemplo aplica el **patrón pipeline**: el procesamiento se divide en **etapas secuenciales**; cada etapa recibe datos, hace una tarea específica y pasa el resultado a la siguiente. Muy usado en ETL (Extracción, Transformación, Carga) y procesamiento de grandes volúmenes de datos.

## Papel de cada etapa

- **Extracción**: lee datos de una fuente (archivo CSV, API, BD) y los entrega al siguiente eslabón.
- **Transformación**: filtra, mapea, agrega o limpia los datos.
- **Carga**: escribe el resultado en destino (archivo, BD, otro servicio).

Cada etapa tiene una entrada y una salida bien definidas; no salta etapas.

## Archivos que deberían ir en esta carpeta

### Contrato de etapas (`pipeline/`)
- `Etapa.java` — Interfaz: `List<Registro> ejecutar(List<Registro> entrada)` (o `Stream<Registro>` si se usa streaming). Un “Registro” puede ser un mapa clave-valor o un DTO.
- `Pipeline.java` — Clase que recibe una lista de `Etapa` y tiene `ejecutar(entrada)`: pasa la entrada a la primera etapa, el resultado a la segunda, y así sucesivamente; devuelve el resultado de la última.

### Etapas concretas (`etapas/`)
- `EtapaExtraccion.java` — Implementa `Etapa`; lee de un archivo CSV (o BD) y devuelve una lista de `Registro` (por línea o por fila).
- `EtapaFiltrado.java` — Implementa `Etapa`; filtra registros (ej. solo filas con cierto campo no nulo o valor > umbral).
- `EtapaTransformacion.java` — Implementa `Etapa`; mapea campos (renombrar, formatear fechas, calcular un nuevo campo) y devuelve la lista transformada.
- `EtapaCarga.java` — Implementa `Etapa`; escribe los registros en un archivo de salida, en BD o en otro formato (JSON).

### Modelo de datos
- `Registro.java` — Representación de una fila o documento (Map<String,Object> o clase con getters/setters) usada entre etapas.

### Punto de entrada
- `Main.java` — Crea las etapas en orden (extracción → filtrado → transformación → carga), las une en un `Pipeline` y ejecuta con una entrada inicial (ej. ruta del CSV). Opcional: leer configuración (rutas, umbrales) desde archivo.

## Cómo comprobar que está bien aplicado

- Cada etapa es independiente y testeable con una lista de entrada fija.
- Añadir una nueva etapa (ej. “validación”) consiste en implementar `Etapa` e insertarla en el orden correcto en el pipeline; no modificar las demás.
- El flujo de datos es lineal: entrada → etapa1 → etapa2 → … → salida, sin ramas complejas dentro del mismo pipeline (para el ejemplo, un solo pipeline lineal es suficiente).

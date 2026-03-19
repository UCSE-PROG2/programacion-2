# Ejemplo: ETL de datos (Pipeline / Tubería)

Este ejemplo aplica el **patrón pipeline**: el procesamiento se divide en **etapas secuenciales**; cada etapa recibe datos, hace una tarea específica y pasa el resultado a la siguiente. Muy usado en ETL (Extracción, Transformación, Carga) y procesamiento de grandes volúmenes de datos.

## Estructura

- **pipeline/** — Interfaz de etapa y clase Pipeline que encadena etapas.
- **etapas/** — Implementaciones concretas: extracción, filtrado, transformación, carga.

## Modelo de datos

- **Registro.java** — Representación de una fila o documento (Map<String,Object> o clase con getters/setters) usada entre etapas. Puede estar en la raíz del ejemplo o en un paquete `modelo/`.

## Punto de entrada

- **Main.java** — Crea las etapas en orden (extracción → filtrado → transformación → carga), las une en un `Pipeline` y ejecuta con una entrada inicial (ej. ruta del CSV). Opcional: leer configuración (rutas, umbrales) desde archivo. Puede estar en la raíz del ejemplo.

## Cómo comprobar que está bien aplicado

- Cada etapa es independiente y testeable con una lista de entrada fija.
- Añadir una nueva etapa (ej. “validación”) consiste en implementar `Etapa` e insertarla en el orden correcto en el pipeline; no modificar las demás.
- El flujo de datos es lineal: entrada → etapa1 → etapa2 → … → salida, sin ramas complejas dentro del mismo pipeline.

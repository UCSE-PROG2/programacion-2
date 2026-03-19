# Etapas concretas (`etapas/`)

Cada etapa implementa la interfaz `Etapa` y realiza un paso del ETL: extracción, filtrado, transformación o carga.

## Archivos que deberían ir en esta carpeta

- **EtapaExtraccion.java** — Implementa `Etapa`; lee de un archivo CSV (o BD) y devuelve una lista de `Registro` (por línea o por fila).
- **EtapaFiltrado.java** — Implementa `Etapa`; filtra registros (ej. solo filas con cierto campo no nulo o valor > umbral).
- **EtapaTransformacion.java** — Implementa `Etapa`; mapea campos (renombrar, formatear fechas, calcular un nuevo campo) y devuelve la lista transformada.
- **EtapaCarga.java** — Implementa `Etapa`; escribe los registros en un archivo de salida, en BD o en otro formato (JSON).

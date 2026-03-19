# Contrato de etapas (`pipeline/`)

Interfaz que cumplen todas las etapas y clase que las encadena en secuencia.

## Archivos que deberían ir en esta carpeta

- **Etapa.java** — Interfaz: `List<Registro> ejecutar(List<Registro> entrada)` (o `Stream<Registro>` si se usa streaming). Un “Registro” puede ser un mapa clave-valor o un DTO.
- **Pipeline.java** — Clase que recibe una lista de `Etapa` y tiene `ejecutar(entrada)`: pasa la entrada a la primera etapa, el resultado a la segunda, y así sucesivamente; devuelve el resultado de la última.

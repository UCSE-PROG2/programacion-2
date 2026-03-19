# Presentador (`presentador/` o `presenter/`)

Recibe eventos de la vista, llama al modelo y actualiza la vista con el resultado.

## Archivos que deberían ir en esta carpeta

- **CalculadoraPresentador.java** — Recibe la vista (inyección o constructor). En los handlers de eventos (sumar, igual, etc.) obtiene operandos de la vista, llama a `Calculadora` y luego `vista.mostrarResultado(resultado)`. Maneja errores (división por cero) y actualiza la vista con mensaje de error si hace falta.

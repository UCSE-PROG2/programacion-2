# Vista (`vista/` o `view/`)

Botones, campo de resultado, eventos (clic en “+”, “=”, etc.); no contiene lógica de negocio.

## Archivos que deberían ir en esta carpeta

- **VistaCalculadora.java** — Interfaz o clase que define: `mostrarResultado(String valor)`, `obtenerOperando1()`, `obtenerOperando2()`, y métodos para registrar listeners (p. ej. `onSumar(Runnable)`, `onIgual(Runnable)`).
- **VistaCalculadoraSwing.java** — Implementación con Swing: botones, campos de texto, y llamadas al presentador cuando el usuario pulsa una tecla de operación o “=”.

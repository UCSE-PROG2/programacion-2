# Ejemplo: Calculadora de escritorio (MVP - Modelo-Vista-Presentador)

Este ejemplo aplica **MVP** a una calculadora de escritorio. El **Presentador** actúa como intermediario: recibe input de la **Vista**, usa el **Modelo** para calcular y actualiza la vista con el resultado. La vista no conoce el modelo.

## Papel de cada componente

- **Modelo**: lógica de cálculo (operaciones, estado de la calculadora si aplica).
- **Vista**: botones, campo de resultado, eventos (clic en “+”, “=”, etc.); no contiene lógica de negocio.
- **Presentador**: recibe eventos de la vista, llama al modelo, y actualiza la vista con el resultado.

## Archivos que deberían ir en esta carpeta

### Modelo (`modelo/` o `model/`)
- `Calculadora.java` — Métodos: `sumar(a, b)`, `restar(a, b)`, `multiplicar(a, b)`, `dividir(a, b)`; opcional: `getResultado()`, `limpiar()`. Sin referencias a UI.

### Vista (`vista/` o `view/`)
- `VistaCalculadora.java` — Interfaz o clase que define: `mostrarResultado(String valor)`, `obtenerOperando1()`, `obtenerOperando2()`, y métodos para registrar listeners (p. ej. `onSumar(Runnable)`, `onIgual(Runnable)`).
- `VistaCalculadoraSwing.java` — Implementación con Swing: botones, campos de texto, y llamadas al presentador cuando el usuario pulsa una tecla de operación o “=”.

### Presentador (`presentador/` o `presenter/`)
- `CalculadoraPresentador.java` — Recibe la vista (inyección o constructor). En los handlers de eventos (sumar, igual, etc.) obtiene operandos de la vista, llama a `Calculadora` y luego `vista.mostrarResultado(resultado)`. Maneja errores (división por cero) y actualiza la vista con mensaje de error si hace falta.

### Punto de entrada
- `Main.java` — Crea modelo, vista y presentador; registra la vista en el presentador (o el presentador en la vista) y muestra la ventana.

## Diferencia con MVC

En MVP la vista no habla con el modelo; todo pasa por el presentador. La vista es “tonta” (solo muestra y dispara eventos), lo que facilita tests unitarios del presentador con una vista mock.

## Archivos que deberían incluirse

- Las interfaces de vista (para poder mockear en tests).
- Un test `CalculadoraPresentadorTest.java` que simule clicks y compruebe que se llama a `mostrarResultado` con el valor correcto.

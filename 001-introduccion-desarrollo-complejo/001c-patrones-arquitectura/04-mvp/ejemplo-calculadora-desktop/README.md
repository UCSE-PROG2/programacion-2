# Ejemplo: Calculadora de escritorio (MVP - Modelo-Vista-Presentador)

Este ejemplo aplica **MVP** a una calculadora de escritorio. El **Presentador** actúa como intermediario: recibe input de la **Vista**, usa el **Modelo** para calcular y actualiza la vista con el resultado. La vista no conoce el modelo.

## Estructura

- **modelo/** — Lógica de cálculo, sin referencias a UI.
- **vista/** — Botones, campo de resultado, eventos; no contiene lógica de negocio.
- **presentador/** — Recibe eventos de la vista, llama al modelo y actualiza la vista.

## Diferencia con MVC

En MVP la vista no habla con el modelo; todo pasa por el presentador. La vista es “tonta” (solo muestra y dispara eventos), lo que facilita tests unitarios del presentador con una vista mock.

## Punto de entrada

- **Main.java** — Crea modelo, vista y presentador; registra la vista en el presentador (o el presentador en la vista) y muestra la ventana. Puede estar en la raíz del ejemplo.

## Archivos adicionales recomendados

- Las interfaces de vista (para poder mockear en tests).
- Un test **CalculadoraPresentadorTest.java** que simule clicks y compruebe que se llama a `mostrarResultado` con el valor correcto.

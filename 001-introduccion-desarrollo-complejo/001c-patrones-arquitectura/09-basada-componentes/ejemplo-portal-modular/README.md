# Ejemplo: Portal modular (Arquitectura basada en componentes)

Este ejemplo aplica **arquitectura basada en componentes**: la aplicación se construye a partir de **componentes** reutilizables y autónomos, definidos por sus **interfaces** (qué ofrecen y qué necesitan). Cada componente es una unidad independiente que se ensambla en un portal (inicio, noticias, clima, usuario).

## Estructura

- **api/** — Interfaz del componente y contexto que el portal pasa a cada uno.
- **componentes/** — Implementaciones concretas (noticias, clima, usuario).
- **servicios/** — Servicios compartidos que los componentes consumen (inyectados por el portal).
- **portal/** — Contenedor que ensambla componentes y renderiza la página.

## Punto de entrada

- **Main.java** — Crea servicios, instancia componentes, los registra en el portal y ejecuta `renderizarPagina()` o arranca un servidor que sirve la página del portal. Puede estar en la raíz del ejemplo.

## Cómo comprobar que está bien aplicado

- Cada componente está en su propio paquete o módulo; no depende de otros componentes, solo de la interfaz `ComponentePortal` y del contexto/servicios inyectados.
- Añadir un componente nuevo (ej. “últimos comentarios”) implica implementar la interfaz y registrarlo en el portal; no modificar los demás componentes.
- Las dependencias entre componentes y servicios están explícitas (constructor o setter) para facilitar tests con mocks.

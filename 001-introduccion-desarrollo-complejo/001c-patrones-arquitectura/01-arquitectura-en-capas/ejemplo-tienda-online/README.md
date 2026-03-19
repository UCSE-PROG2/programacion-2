# Ejemplo: Tienda online (Arquitectura en Capas)

Este ejemplo aplica el **patrón de arquitectura en capas** a una aplicación de comercio electrónico. La aplicación se divide en capas independientes; cada capa se comunica solo con la capa adyacente inmediata.

## Estructura de capas

En esta carpeta encontrarás las subcarpetas que corresponden a cada capa. Dentro de cada una hay un `README.md` con los archivos que deberías implementar:

- **presentacion/** — Interfaz con el usuario (pantallas, formularios).
- **negocio/** — Reglas del dominio (carrito, pedidos, descuentos).
- **datos/** — Persistencia (productos, usuarios, pedidos).

## Regla de dependencias

Las dependencias deben ir **solo hacia abajo**: presentación → negocio → datos. La capa de datos no debe conocer la de presentación ni la de negocio; la de negocio no debe conocer la de presentación.

## Cómo comprobar que está bien aplicado

- No hay imports de `presentacion` dentro de `datos` ni de `negocio` hacia `presentacion`.
- La capa de negocio no contiene SQL ni detalles de UI; solo lógica de dominio.
- Cada capa puede probarse de forma aislada (p. ej. servicios con datos en memoria).

## Punto de entrada

- `Main.java` o clase que arranque la aplicación y conecte las capas (inyección manual o contenedor ligero). Puede ubicarse en la raíz del ejemplo o en un paquete raíz del proyecto.

# Ejemplo: Tienda online (Arquitectura en Capas)

Este ejemplo aplica el **patrón de arquitectura en capas** a una aplicación de comercio electrónico. La aplicación se divide en capas independientes; cada capa se comunica solo con la capa adyacente inmediata.

## Estructura de capas

- **Capa de presentación**: interfaz con el usuario (pantallas, formularios).
- **Capa de lógica de negocio**: reglas del dominio (carrito, pedidos, descuentos).
- **Capa de acceso a datos**: persistencia (productos, usuarios, pedidos).

## Archivos que deberían ir en esta carpeta

### Capa de presentación (`presentacion/` o `ui/`)
- `VistaProductos.java` — Listado de productos y búsqueda.
- `VistaCarrito.java` — Carrito y checkout.
- `VistaPedidos.java` — Historial de pedidos del usuario.
- Opcional: controladores o presenters que reciban input y llamen a la capa de negocio.

### Capa de lógica de negocio (`negocio/` o `service/`)
- `ServicioCarrito.java` — Agregar/quitar ítems, calcular totales.
- `ServicioPedidos.java` — Crear pedido, validar stock, aplicar reglas.
- `ServicioProductos.java` — Búsqueda y filtros según reglas de negocio.
- `ModeloPedido.java`, `ModeloItemCarrito.java` — Entidades o DTOs de negocio.

### Capa de acceso a datos (`datos/` o `persistencia/`)
- `RepositorioProductos.java` — CRUD o consultas de productos (ej. JDBC o interfaz a un ORM).
- `RepositorioUsuarios.java` — Acceso a usuarios.
- `RepositorioPedidos.java` — Guardar y listar pedidos.
- Si usas entidades JPA: `Producto.java`, `Usuario.java`, `Pedido.java` en un paquete `entidades/` o `model/`.

### Punto de entrada
- `Main.java` o clase que arranque la aplicación y conecte las capas (inyección manual o contenedor ligero).

## Regla de dependencias

Las dependencias deben ir **solo hacia abajo**: presentación → negocio → datos. La capa de datos no debe conocer la de presentación ni la de negocio; la de negocio no debe conocer la de presentación.

## Cómo comprobar que está bien aplicado

- No hay imports de `presentacion` dentro de `datos` ni de `negocio` hacia `presentacion`.
- La capa de negocio no contiene SQL ni detalles de UI; solo lógica de dominio.
- Cada capa puede probarse de forma aislada (p. ej. servicios con datos en memoria).

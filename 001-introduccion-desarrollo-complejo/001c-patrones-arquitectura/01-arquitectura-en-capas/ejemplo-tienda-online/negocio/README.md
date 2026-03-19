# Capa de lógica de negocio (`negocio/` o `service/`)

Aquí van las reglas del dominio: carrito, pedidos, descuentos, validaciones y DTOs o modelos de negocio.

## Archivos que deberían ir en esta carpeta

- **ServicioCarrito.java** — Agregar/quitar ítems, calcular totales.
- **ServicioPedidos.java** — Crear pedido, validar stock, aplicar reglas.
- **ServicioProductos.java** — Búsqueda y filtros según reglas de negocio.
- **ModeloPedido.java**, **ModeloItemCarrito.java** — Entidades o DTOs de negocio.

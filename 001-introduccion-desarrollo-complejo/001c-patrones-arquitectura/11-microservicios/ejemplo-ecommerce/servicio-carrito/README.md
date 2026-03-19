# Servicio de Carrito (`servicio-carrito/`)

Carrito por usuario; añadir y quitar ítems. Puede llamar al servicio de catálogo para validar producto.

## Archivos que deberían ir en esta carpeta

- **CarritoApplication.java**
- **CarritoController.java** — `GET /carrito/{usuarioId}`, `POST /carrito/items`, `DELETE /carrito/items/{id}`
- **CarritoService.java** — Añadir/quitar ítems; puede llamar al servicio de catálogo para validar producto.
- **ItemCarrito.java**, **CarritoRepository.java**

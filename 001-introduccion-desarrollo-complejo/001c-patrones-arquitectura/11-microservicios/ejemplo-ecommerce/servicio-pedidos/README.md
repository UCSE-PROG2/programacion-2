# Servicio de Pedidos/Envíos (`servicio-pedidos/`)

Crear pedido desde carrito, orquestar con servicio de pagos y carrito, y gestionar envíos.

## Archivos que deberían ir en esta carpeta

- **PedidosApplication.java**
- **PedidoController.java** — `POST /pedidos` (crear desde carrito), `GET /pedidos/{id}`, `GET /pedidos/{id}/envio`
- **PedidoService.java** — Orquestar: reservar carrito, crear pago (llamada a servicio-pagos), crear pedido y evento “pedido creado” para envíos.
- **PedidoRepository.java**

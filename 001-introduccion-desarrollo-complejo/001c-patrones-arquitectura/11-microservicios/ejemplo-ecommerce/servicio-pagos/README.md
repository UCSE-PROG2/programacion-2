# Servicio de Pagos (`servicio-pagos/`)

Crear pago para un pedido y consultar estado; simula pasarela (éxito/rechazo) y persiste estado.

## Archivos que deberían ir en esta carpeta

- **PagosApplication.java**
- **PagoController.java** — `POST /pagos` (crear pago para un pedido), `GET /pagos/{id}/estado`
- **PagoService.java** — Simular pasarela de pago (éxito/rechazo) y persistir estado.
- **PagoRepository.java**

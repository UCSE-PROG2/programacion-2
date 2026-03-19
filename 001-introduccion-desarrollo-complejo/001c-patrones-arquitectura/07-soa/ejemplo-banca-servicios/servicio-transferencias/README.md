# Servicio de Transferencias (`servicio-transferencias/`)

Expone la operación de transferir entre cuentas.

## Archivos que deberían ir en esta carpeta

- **TransferenciasResource.java** — `POST /transferencias` (origen, destino, monto).
- **TransferenciasService.java** — Validar y ejecutar transferencia; puede llamar a servicio de cuentas para debitar/acreditar (o compartir BD).
- **TransferenciaRequestDTO.java**, **TransferenciaResponseDTO.java**.

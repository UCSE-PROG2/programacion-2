# Servicio de Cuentas (`servicio-cuentas/`)

Expone consulta de saldo y movimientos por cuenta.

## Archivos que deberían ir en esta carpeta

- **CuentasResource.java** — `GET /cuentas/{id}/saldo`, `GET /cuentas/{id}/movimientos`.
- **CuentasService.java** — Consulta saldo y movimientos (contra BD o otro servicio).
- **SaldoDTO.java**, **MovimientoDTO.java**.

# Ejemplo: Banca (SOA - Arquitectura Orientada a Servicios)

Este ejemplo aplica **SOA**: los componentes se exponen como **servicios** independientes con interfaces bien definidas y se comunican por estándares abiertos (REST, SOAP). Cada servicio encapsula una capacidad (autenticación, transferencias, consulta de saldo).

## Papel de cada parte

- **Servicios**: autónomos, con contrato (API) claro; exponen operaciones (transferir, consultar saldo, validar usuario).
- **Consumidores**: clientes (web, móvil, otro servicio) que llaman a los servicios vía HTTP/SOAP.
- **Contratos**: documentación o WSDL/OpenAPI que describa entradas, salidas y errores.

## Archivos que deberían ir en esta carpeta

### Servicio de Autenticación (`servicio-autenticacion/`)
- `AutenticacionResource.java` o `AutenticacionEndpoint.java` — Rutas: `POST /auth/login` (usuario/contraseña), `POST /auth/logout`, `GET /auth/validar?token=...`.
- `AutenticacionService.java` — Lógica: validar credenciales, generar y validar token (JWT o sesión).
- `UsuarioDTO.java`, `TokenDTO.java` — Objetos para request/response.

### Servicio de Cuentas (`servicio-cuentas/`)
- `CuentasResource.java` — `GET /cuentas/{id}/saldo`, `GET /cuentas/{id}/movimientos`.
- `CuentasService.java` — Consulta saldo y movimientos (contra BD o otro servicio).
- `SaldoDTO.java`, `MovimientoDTO.java`.

### Servicio de Transferencias (`servicio-transferencias/`)
- `TransferenciasResource.java` — `POST /transferencias` (origen, destino, monto).
- `TransferenciasService.java` — Validar y ejecutar transferencia; puede llamar a servicio de cuentas para debitar/acreditar (o compartir BD).
- `TransferenciaRequestDTO.java`, `TransferenciaResponseDTO.java`.

### Contratos / API
- `openapi.yaml` o carpeta `contracts/` — Definición de endpoints, modelos y códigos de error para cada servicio (para que los consumidores sepan cómo llamarlos).

### Cliente orquestador (opcional)
- `ClienteBanca.java` o módulo web que llame a los tres servicios (login → consultar saldo → transferir) mostrando el flujo de consumo SOA.

## Cómo comprobar que está bien aplicado

- Cada servicio puede desplegarse y probarse de forma independiente (p. ej. solo autenticación).
- La comunicación es por HTTP + JSON (o XML en SOAP); no hay llamadas directas entre procesos en memoria.
- Un mismo servicio puede ser consumido por web, móvil o otro servicio sin duplicar lógica.

# Ejemplo: Banca (SOA - Arquitectura Orientada a Servicios)

Este ejemplo aplica **SOA**: los componentes se exponen como **servicios** independientes con interfaces bien definidas y se comunican por estándares abiertos (REST, SOAP). Cada servicio encapsula una capacidad (autenticación, transferencias, consulta de saldo).

## Estructura

- **servicio-autenticacion/** — Login, logout, validar token.
- **servicio-cuentas/** — Consulta saldo y movimientos.
- **servicio-transferencias/** — Ejecutar transferencias.
- **contracts/** — Definición de API (OpenAPI, WSDL) para cada servicio.

## Cliente orquestador (opcional)

- **ClienteBanca.java** o módulo web que llame a los tres servicios (login → consultar saldo → transferir) mostrando el flujo de consumo SOA. Puede estar en la raíz del ejemplo o en una carpeta `cliente/`.

## Cómo comprobar que está bien aplicado

- Cada servicio puede desplegarse y probarse de forma independiente (p. ej. solo autenticación).
- La comunicación es por HTTP + JSON (o XML en SOAP); no hay llamadas directas entre procesos en memoria.
- Un mismo servicio puede ser consumido por web, móvil o otro servicio sin duplicar lógica.

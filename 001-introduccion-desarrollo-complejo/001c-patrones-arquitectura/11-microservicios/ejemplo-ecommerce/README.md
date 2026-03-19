# Ejemplo: E-commerce (Microservicios)

Este ejemplo aplica el **patrón de microservicios**: la aplicación se divide en **servicios pequeños e independientes**, cada uno con una responsabilidad única, comunicándose por **API** (REST, eventos, etc.). Ejemplo típico: e-commerce con servicios de autenticación, catálogo, carrito, pagos y envíos.

## Estructura

Cada servicio vive en su propia carpeta (y en un proyecto real podría ser un repositorio o módulo separado):

- **servicio-auth/** — Autenticación y registro.
- **servicio-catalogo/** — Productos y categorías.
- **servicio-carrito/** — Carrito por usuario.
- **servicio-pagos/** — Crear pago y consultar estado.
- **servicio-pedidos/** — Crear pedido, orquestar carrito y pago, envíos.

## Comunicación entre servicios

Cada servicio que necesite a otro debe usar un **cliente HTTP** (o cliente gRPC) con la URL base del otro servicio (configurable por entorno). Ejemplo: `PedidoService` llama a `servicio-pagos` para crear el pago y a `servicio-carrito` para vaciar el carrito.

Opcional: carpeta **contracts/** o **api/** con OpenAPI de cada servicio para que los consumidores sepan cómo llamarlos.

## Orquestación / Frontend (opcional)

- **gateway/** — Documento o código de un API Gateway (o BFF) que agregue llamadas a auth, catálogo, carrito y pedidos para una app web o móvil.
- O un README en la raíz que explique el orden de arranque (auth, catálogo, carrito, pagos, pedidos) y las URLs de cada servicio para pruebas.

## Cómo comprobar que está bien aplicado

- Cada servicio puede compilar y ejecutarse por separado (con mocks o stubs de los otros si hace falta).
- No hay dependencias en tiempo de compilación entre servicios (no se importa el código de “pedidos” desde “pagos”); la integración es por red.
- Un mismo servicio puede ser consumido por varios clientes (web, móvil, otro microservicio) reutilizando la misma API.

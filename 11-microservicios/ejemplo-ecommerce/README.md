# Ejemplo: E-commerce (Microservicios)

Este ejemplo aplica el **patrón de microservicios**: la aplicación se divide en **servicios pequeños e independientes**, cada uno con una responsabilidad única, comunicándose por **API** (REST, eventos, etc.). Ejemplo típico: e-commerce con servicios de autenticación, catálogo, carrito, pagos y envíos.

## Papel de cada microservicio

- Cada servicio tiene su propio despliegue, datos (o BD propia) y dominio acotado.
- La comunicación entre servicios es por red (HTTP/gRPC o mensajería), no por llamadas en memoria.

## Archivos que deberían ir en esta carpeta

Conviene organizar por **carpeta por servicio** (cada una podría ser un proyecto o módulo separado):

### Servicio de Autenticación (`servicio-auth/`)
- `AuthApplication.java` — Punto de entrada (Spring Boot u otro).
- `AuthController.java` — `POST /login`, `POST /register`, `GET /usuarios/me` (con token).
- `AuthService.java`, `UsuarioRepository.java` — Lógica y persistencia de usuarios/tokens.
- `pom.xml` o `build.gradle` — Dependencias solo de este servicio.

### Servicio de Catálogo (`servicio-catalogo/`)
- `CatalogoApplication.java`
- `ProductoController.java` — `GET /productos`, `GET /productos/{id}`, `GET /productos?categoria=...`
- `ProductoService.java`, `ProductoRepository.java` — Productos y categorías.
- `Producto.java` — Entidad o DTO.

### Servicio de Carrito (`servicio-carrito/`)
- `CarritoApplication.java`
- `CarritoController.java` — `GET /carrito/{usuarioId}`, `POST /carrito/items`, `DELETE /carrito/items/{id}`
- `CarritoService.java` — Añadir/quitar ítems; puede llamar al servicio de catálogo para validar producto.
- `ItemCarrito.java`, `CarritoRepository.java`

### Servicio de Pagos (`servicio-pagos/`)
- `PagosApplication.java`
- `PagoController.java` — `POST /pagos` (crear pago para un pedido), `GET /pagos/{id}/estado`
- `PagoService.java` — Simular pasarela de pago (éxito/rechazo) y persistir estado.
- `PagoRepository.java`

### Servicio de Pedidos/Envíos (`servicio-pedidos/`)
- `PedidosApplication.java`
- `PedidoController.java` — `POST /pedidos` (crear desde carrito), `GET /pedidos/{id}`, `GET /pedidos/{id}/envio`
- `PedidoService.java` — Orquestar: reservar carrito, crear pago (llamada a servicio-pagos), crear pedido y evento “pedido creado” para envíos.
- `PedidoRepository.java`

### Comunicación entre servicios
- Cada servicio que necesite a otro debe usar un **cliente HTTP** (o cliente gRPC) con la URL base del otro servicio (configurable por entorno). Ejemplo: `PedidoService` llama a `servicio-pagos` para crear el pago y a `servicio-carrito` para vaciar el carrito.
- Opcional: carpeta `contracts/` o `api/` con OpenAPI de cada servicio para que los consumidores sepan cómo llamarlos.

### Orquestación / Frontend (opcional)
- `gateway/` o documento que describa un API Gateway (o BFF) que agregue llamadas a auth, catálogo, carrito y pedidos para una app web o móvil.
- O un `README.md` en la raíz que explique el orden de arranque (auth, catálogo, carrito, pagos, pedidos) y las URLs de cada servicio para pruebas.

## Cómo comprobar que está bien aplicado

- Cada servicio puede compilar y ejecutarse por separado (con mocks o stubs de los otros si hace falta).
- No hay dependencias en tiempo de compilación entre servicios (no se importa el código de “pedidos” desde “pagos”); la integración es por red.
- Un mismo servicio puede ser consumido por varios clientes (web, móvil, otro microservicio) reutilizando la misma API.

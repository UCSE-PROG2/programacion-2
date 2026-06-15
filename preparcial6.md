# Ejercicio TP 4 — Sistema de gestión de una tienda online

## Paso 0 — Repositorio

Antes de escribir una línea de código, creá un repositorio **privado** en GitHub con el nombre `tienda-api` y agregá a **maximilianolovera@gmail.com** como colaborador con rol de lectura. La entrega se realiza a través de ese repositorio.

---

## Descripción del sistema

Una tienda online necesita una API REST para gestionar su catálogo de productos y los pedidos de sus clientes. La API debe permitir registrar productos, crear y consultar pedidos con filtros opcionales, cancelarlos y obtener un reporte agrupado por categoría.

---

## Base de datos

Crear el esquema `tienda` en MySQL y ejecutar el siguiente script:

```sql
CREATE SCHEMA IF NOT EXISTS tienda;

CREATE TABLE tienda.producto (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    categoria   VARCHAR(80)  NOT NULL,
    precio      DOUBLE       NOT NULL,
    stock       INT          NOT NULL DEFAULT 0,
    descripcion VARCHAR(300),
    disponible  BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE tienda.pedido (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id    BIGINT       NOT NULL,
    nombre_cliente VARCHAR(150) NOT NULL,
    email_cliente  VARCHAR(100) NOT NULL,
    fecha_pedido   VARCHAR(20)  NOT NULL,
    cantidad       INT          NOT NULL,
    total_pagado   DOUBLE       NOT NULL,
    estado         VARCHAR(30)  NOT NULL DEFAULT 'PENDIENTE',
    FOREIGN KEY (producto_id) REFERENCES tienda.producto(id)
);

INSERT INTO tienda.producto (nombre, categoria, precio, stock, descripcion, disponible) VALUES
('Auriculares Sony WH-1000XM5',     'Electrónica',       89999.00, 15, 'Cancelación activa de ruido',        TRUE),
('Teclado Mecánico Logitech G Pro',  'Electrónica',       54999.00, 20, 'Switch GX Blue, RGB',                TRUE),
('Silla Gamer DXRacer Serie F',      'Muebles',          129999.00,  8, 'Con soporte lumbar ajustable',       TRUE),
('Monitor LG 27" 4K UHD',           'Electrónica',      174999.00,  5, 'Panel IPS, 60Hz',                    TRUE),
('Zapatillas Nike Air Max 270',      'Indumentaria',      49999.00, 30, 'Talle 42, color negro',              TRUE),
('Mochila Samsonite Guardit 2.0',    'Accesorios',        34999.00, 25, 'Para laptop hasta 15.6"',            TRUE),
('Smartphone Samsung Galaxy A55',    'Electrónica',      249999.00, 10, '8GB RAM, 256GB almacenamiento',      TRUE),
('Cafetera Nespresso Vertuo Pop',    'Electrodomésticos', 69999.00, 12, 'Compatible con cápsulas Vertuo',     TRUE),
('Libro: Clean Code',                'Libros',            18999.00, 40, 'Robert C. Martin',                   TRUE),
('Mesa de Centro Minimalista',       'Muebles',           45999.00,  6, 'Madera y acero, 120x60cm',           FALSE);

INSERT INTO tienda.pedido (producto_id, nombre_cliente, email_cliente, fecha_pedido, cantidad, total_pagado, estado) VALUES
(1, 'Ana García',      'ana@mail.com',    '2026-05-10', 1,  89999.00, 'ENVIADO'),
(3, 'Luis Pérez',      'luis@mail.com',   '2026-05-15', 1, 129999.00, 'ENVIADO'),
(7, 'María López',     'maria@mail.com',  '2026-05-22', 2, 499998.00, 'ENVIADO'),
(2, 'Carlos Ruiz',     'carlos@mail.com', '2026-06-01', 1,  54999.00, 'ENVIADO'),
(9, 'Sofía Martín',    'sofia@mail.com',  '2026-06-05', 3,  56997.00, 'CANCELADO'),
(5, 'Diego Torres',    'diego@mail.com',  '2026-06-10', 2,  99998.00, 'ENVIADO'),
(4, 'Valentina Cruz',  'vale@mail.com',   '2026-07-01', 1, 174999.00, 'CONFIRMADO'),
(8, 'Facundo Gómez',   'facu@mail.com',   '2026-07-05', 1,  69999.00, 'CONFIRMADO'),
(1, 'Camila Sosa',     'cami@mail.com',   '2026-07-10', 1,  89999.00, 'PENDIENTE'),
(6, 'Tomás Fernández', 'tomas@mail.com',  '2026-07-12', 2,  69998.00, 'PENDIENTE');
```

---

## Crear el proyecto

Generar el proyecto desde [Spring Initializr](https://start.spring.io) con la siguiente configuración:

- **Group**: `com`
- **Artifact**: `tienda`
- **Package name**: `com.tienda`
- **Dependencies**: Spring Web, Spring Data JPA, MySQL Driver, Validation, Lombok

Configurar la conexión a la base de datos en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tienda
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

---

## Estructura del proyecto

Respetar la siguiente arquitectura en capas:

```
src/main/java/com/tienda/
├── model/
│   ├── Producto.java
│   └── Pedido.java
├── repository/
│   ├── ProductoRepository.java
│   └── PedidoRepository.java
├── service/
│   └── TiendaService.java
├── controller/
│   ├── ProductoController.java
│   └── PedidoController.java
├── exception/
│   └── GlobalExceptionHandler.java
└── TiendaApplication.java
```

---

## Entidades (`model/`)

**`Producto`** — representa un producto del catálogo. Campos: nombre, categoria, precio, stock, descripcion y disponible. Debe estar mapeada a la tabla `tienda.producto` con las anotaciones JPA correspondientes.

**`Pedido`** — representa un pedido de un cliente. Campos: nombreCliente, emailCliente, fechaPedido, cantidad, totalPagado y estado. Debe tener una relación `@ManyToOne` hacia `Producto` y estar mapeada a la tabla `tienda.pedido`. Los valores válidos de estado son: `PENDIENTE`, `CONFIRMADO`, `ENVIADO` y `CANCELADO`.

---

## Capa de repositorio (`repository/`)

**`ProductoRepository`** — extiende `JpaRepository<Producto, Long>`. Además de los métodos heredados, debe agregar:

- Un método de consulta que filtre productos por categoria y/o disponibilidad. Ambos parámetros son opcionales: si alguno no se informa, no se aplica ese filtro.

**`PedidoRepository`** — extiende `JpaRepository<Pedido, Long>`. Además de los métodos heredados, debe agregar:

- Un método que devuelva todos los pedidos de un producto específico, recibiendo el ID del producto como parámetro.

---

## Capa de servicio (`service/`)

**`TiendaService`** — coordina la lógica de negocio y delega la persistencia a ambos repositorios. Debe exponer los siguientes métodos:

- **`registrarProducto`** — recibe los datos del producto y lo persiste.
- **`buscarProductoPorId`** — devuelve el producto o lanza una excepción si no existe.
- **`listarProductos`** — recibe dos filtros opcionales: categoria (coincidencia exacta) y disponible (booleano). Si ninguno está presente, devuelve todos los productos.
- **`actualizarProducto`** — actualiza los campos de un producto existente. Si no existe, lanza una excepción.
- **`eliminarProducto`** — elimina el producto por ID. Si no existe, lanza una excepción. Si el producto tiene pedidos con estado `PENDIENTE` o `CONFIRMADO`, debe lanzar una excepción indicando que no puede eliminarse.
- **`registrarPedido`** — recibe el ID del producto y los datos del pedido. Verifica que el producto exista (lanza excepción si no) y persiste el pedido vinculado a ese producto.
- **`buscarPedidoPorId`** — devuelve el pedido o lanza una excepción si no existe.
- **`buscarPedidosPorProducto`** — devuelve todos los pedidos de un producto dado su ID. Si el producto no existe, lanza una excepción.
- **`buscarPedidos`** — recibe tres filtros opcionales: estado, categoria del producto y fechaDesde. Cada filtro solo se aplica si fue enviado; si ninguno está presente, devuelve todos los pedidos. Implementar esta consulta usando **CriteriaBuilder** (`EntityManager`, `CriteriaQuery`, `Predicate`) para construir los predicados dinámicamente según los parámetros presentes.
- **`cancelarPedido`** — cambia el estado de un pedido existente a `CANCELADO`. Si no existe, lanza una excepción.
- **`reportePorCategoria`** — devuelve, agrupado por categoría: nombre de la categoría, cantidad de pedidos, ingresos totales (suma de `totalPagado`) y ticket promedio.

---

## Endpoints esperados

### Productos

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/productos` | Listar todos los productos |
| GET | `/productos/{id}` | Buscar producto por ID |
| GET | `/productos?categoria=...` | Filtrar por categoría |
| GET | `/productos?disponible=...` | Filtrar por disponibilidad |
| POST | `/productos` | Crear producto |
| PUT | `/productos/{id}` | Actualizar producto |
| DELETE | `/productos/{id}` | Eliminar producto |

### Pedidos

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/pedidos` | Listar todos los pedidos |
| GET | `/pedidos/{id}` | Buscar pedido por ID |
| GET | `/productos/{productoId}/pedidos` | Listar pedidos de un producto |
| GET | `/pedidos?estado=...` | Filtrar por estado (PENDIENTE, CONFIRMADO, ENVIADO, CANCELADO) |
| GET | `/pedidos?categoria=...` | Filtrar por categoría del producto |
| GET | `/pedidos?fechaDesde=...` | Pedidos con fecha igual o posterior |
| POST | `/pedidos` | Crear pedido (el cuerpo debe incluir `productoId`) |
| PATCH | `/pedidos/{id}/cancelar` | Cancelar un pedido |
| DELETE | `/pedidos/{id}` | Eliminar pedido |

### Reporte

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/reporte/categorias` | Cantidad de pedidos, ingresos totales y ticket promedio por categoría |

Los tres filtros de `/pedidos` son combinables entre sí: si se envían varios, se aplican todos simultáneamente.

---

## Consideraciones técnicas

- Los endpoints de creación deben devolver **HTTP 201 Created** con el recurso creado en el cuerpo.
- El endpoint de cancelación (`PATCH /pedidos/{id}/cancelar`) debe devolver **HTTP 200 OK** con el pedido actualizado.
- El endpoint de eliminación debe devolver **HTTP 204 No Content**.
- Si un recurso no existe, la respuesta debe ser **HTTP 404 Not Found** con un mensaje que indique qué no se encontró.
- Si se intenta eliminar un producto con pedidos en estado `PENDIENTE` o `CONFIRMADO`, la respuesta debe ser **HTTP 409 Conflict** con un mensaje descriptivo.
- Implementar un manejador global de excepciones con `@RestControllerAdvice` que intercepte las excepciones del service y devuelva el código y mensaje apropiados.
- Validar que el nombre y la categoría del producto no estén vacíos y que el precio sea mayor a cero. Para los pedidos, validar que el nombre y el email del cliente no estén vacíos, que la cantidad sea mayor a cero y que el total pagado sea mayor a cero. Si la validación falla, la respuesta debe ser **HTTP 400 Bad Request**.

---

## Tests unitarios

Escribir tests unitarios para la capa de servicio (`TiendaService`) que cubran los distintos escenarios de cada método. La cobertura de líneas del servicio debe ser **igual o superior al 80%**.

- Usar **JUnit 5** (ya incluido en el starter de pruebas de Spring Boot).
- Usar **H2** como base de datos en memoria para los tests, tal como se explica en la sección 8 del README de la unidad 2. Agregar la dependencia `testImplementation` de H2 y configurar un `application.properties` en `src/test/resources/` apuntando a H2.
- Verificar tanto los caminos exitosos como los casos de error (por ejemplo, que se lance la excepción correcta cuando el producto no existe, o cuando se intenta eliminar un producto con pedidos activos).

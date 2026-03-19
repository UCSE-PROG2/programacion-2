# Capa de acceso a datos (`datos/` o `persistencia/`)

Aquí va la persistencia: repositorios o DAOs que accedan a base de datos (JDBC, ORM, etc.).

## Archivos que deberían ir en esta carpeta

- **RepositorioProductos.java** — CRUD o consultas de productos (ej. JDBC o interfaz a un ORM).
- **RepositorioUsuarios.java** — Acceso a usuarios.
- **RepositorioPedidos.java** — Guardar y listar pedidos.
- Si usas entidades JPA: **Producto.java**, **Usuario.java**, **Pedido.java** en un paquete `entidades/` o `model/` (pueden estar en esta misma capa o en un subpaquete).

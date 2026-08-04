# Trabajo Práctico Final — Programación 2 (2026)

## Gestock — Sistema de Gestión de Stock

---

## Reglas de organización y entrega

### Equipos

- El trabajo se realiza en equipos de **hasta 3 integrantes**.
- Cada equipo debe definir un **nombre de equipo** (`team_name`), en minúsculas y sin espacios (por ejemplo: `byte-runners`, `stockops`, `deposito-9`).

### Repositorio

- Crear un repositorio **privado** en GitHub con el nombre `prog2-2026-tp-<team_name>-gestock`, reemplazando `<team_name>` por el nombre elegido.
- Todo el proyecto debe poder levantarse con un único `docker-compose` ubicado en la raíz del repositorio, que orqueste los tres servicios: la base MongoDB, la API en Go y el frontend en React. En base a esto, la raíz del repositorio debe contener:
  - `docker-compose.yml`
  - `api/` con el proyecto Go (incluyendo su propio Dockerfile).
  - `frontend/` con el proyecto React (incluyendo su propio Dockerfile).
- El `README.md` del repositorio debe incluir el nombre del equipo y el listado de integrantes, con el formato **APELLIDO, Nombres** para cada uno.
- La entrega del trabajo práctico se realiza a través de este repositorio.

---

## Enunciado del proyecto

### Contexto

**Delta Insumos S.A.** es una empresa ficticia dedicada a la distribución de insumos industriales, con varios depósitos ubicados en distintas provincias. Actualmente el control de stock se lleva de forma manual y descentralizada, lo que genera errores frecuentes: quiebres de stock no detectados a tiempo, descoordinación entre depósitos y falta de visibilidad sobre el estado general del inventario.

La empresa encargó el desarrollo de **Gestock**, un sistema que centralice la gestión de stock, compras y movimientos entre depósitos, con distintos niveles de acceso según el rol de cada usuario.

El trabajo práctico se divide en dos partes: una **API en Go** que expone la lógica de negocio y persiste la información en una base **MongoDB** corriendo en un contenedor Docker, y una **aplicación web en React** que consume dicha API.

Este documento describe **qué** debe hacer el sistema (requerimientos funcionales y no funcionales). Las decisiones de diseño técnico, arquitectura y organización del código quedan a criterio de cada equipo.

---

### Roles de usuario

El sistema debe distinguir, como mínimo, los siguientes roles:

| Rol | Descripción | Permisos principales |
|---|---|---|
| **Administrador** | Control total del sistema | Gestiona usuarios y roles, depósitos, proveedores y categorías. Configura el stock mínimo de cada producto. Accede a todos los reportes. |
| **Gerente de Depósito** | Responsable de uno o más depósitos | Gestiona productos y proveedores. Crea y aprueba órdenes de compra y transferencias entre depósitos. Accede a los reportes de los depósitos a su cargo. |
| **Operario de Depósito** | Trabajo diario dentro de un depósito | Registra ingresos y egresos de stock. Solicita transferencias hacia otros depósitos. Consulta el stock del depósito al que está asignado. |
| **Auditor** | Control interno, solo lectura | Consulta reportes, historial de movimientos y órdenes de compra de todos los depósitos, sin posibilidad de modificar datos. |

Un usuario puede estar asignado a uno o varios depósitos. Un operario o gerente solo debe poder operar sobre los depósitos a los que fue asignado; el administrador y el auditor tienen visibilidad sobre todos los depósitos.

---

## Parte 1 — API en Go

### Objetivo

Desarrollar una API REST en Go que implemente toda la lógica de negocio de Gestock y persista la información en una base MongoDB, ejecutada dentro de un contenedor Docker.

### Requerimientos funcionales

**Autenticación y usuarios**
- Los usuarios inician sesión con credenciales propias (usuario/email y contraseña).
- Solo el rol Administrador puede crear, editar, desactivar y asignar roles a otros usuarios.
- Un usuario desactivado no debe poder autenticarse ni operar en el sistema.
- Cada usuario tiene un único rol activo y puede estar asociado a uno o más depósitos.

**Depósitos**
- ABM de depósitos (nombre, ubicación —localidad y provincia—, responsable).
- Cada depósito mantiene su propio stock por producto, independiente del de otros depósitos.

**Productos y categorías**
- ABM de productos, con al menos: nombre, descripción, categoría, unidad de medida, costo unitario y stock mínimo.
- La unidad de medida de un producto se define a partir de un conjunto cerrado de valores (por ejemplo: unidad, kilogramo, litro, metro), no como texto libre.
- ABM de categorías para clasificar productos.
- Los productos pueden tener, opcionalmente, una fecha de vencimiento (para insumos perecederos).
- El stock mínimo de un producto puede configurarse de forma general o particular por depósito. Si existe una configuración particular para un depósito, esta reemplaza (override) al valor general del producto para ese depósito puntual.

**Proveedores**
- ABM de proveedores, con al menos: razón social, CUIT, contacto y productos que suministra.
- El CUIT informado debe validarse mediante el algoritmo de verificación de su dígito verificador (investigar su funcionamiento), rechazando aquellos que no sean válidos.

**Movimientos de stock**
- Todo cambio de stock (ingreso, egreso, ajuste, transferencia) debe quedar registrado como un movimiento, con producto, depósito, cantidad, tipo, usuario responsable, fecha/hora y motivo.
- El stock de un producto en un depósito **no puede quedar negativo**.
- El historial de movimientos es **inmutable**: no se editan ni eliminan movimientos ya registrados. Cualquier corrección se realiza mediante un nuevo movimiento de ajuste.

**Transferencias entre depósitos**
- Un operario o gerente puede solicitar una transferencia de stock desde su depósito hacia otro.
- La transferencia debe pasar por un flujo de estados: **solicitada → aprobada/rechazada → completada**.
- El stock se descuenta del depósito de origen recién al aprobarse, y se acredita en el depósito destino al completarse la transferencia.
- La aprobación de una transferencia recibida es responsabilidad del gerente (o administrador) del depósito destino.

**Órdenes de compra**
- Un gerente o administrador puede generar una orden de compra a un proveedor para reponer stock de uno o más productos.
- Un producto puede tener asociado más de un proveedor, cada uno con su propio costo y tiempo de entrega estimado; al generar una orden de compra debe elegirse a cuál de ellos se le realiza el pedido.
- Una orden de compra atraviesa el siguiente flujo de estados: **borrador → confirmada → (parcial) → completada**.
  - En estado **borrador**, la orden es una propuesta editable (productos, cantidades, proveedor) que todavía no fue enviada al proveedor y no genera ningún efecto sobre el stock. Puede modificarse libremente o descartarse.
  - Al **confirmarse**, la orden queda cerrada en sus productos y cantidades y se considera enviada al proveedor. Tampoco impacta el stock todavía.
  - Cada recepción de mercadería sobre una orden confirmada genera automáticamente los movimientos de ingreso correspondientes en el depósito destino. Si lo recibido no cubre la totalidad de lo pedido, la orden pasa a estado **parcial**; al completarse la totalidad, pasa a **completada**.
  - Una orden **completada** no admite nuevas recepciones ni modificaciones.
- Cuando el stock de un producto cae por debajo del mínimo configurado para un depósito, el sistema debe generar automáticamente una orden de compra en estado borrador, sugiriendo uno de los proveedores asociados al producto (por ejemplo, el de menor costo) y la cantidad a reponer. El Gerente o Administrador puede revisar esta sugerencia, modificarla, confirmarla o descartarla.

**Alertas de stock**
- El sistema debe identificar automáticamente los productos cuyo stock, en un depósito determinado, se encuentra por debajo del mínimo configurado.
- Debe identificar también los productos próximos a vencer (dentro de una ventana de días configurable), para los productos que tengan fecha de vencimiento cargada.

**Novedades a depósitos**
- El Gerente de Depósito puede enviar novedades (mensajes) a los operarios de su depósito.
- El Auditor puede enviar novedades a los operarios de un depósito puntual, de varios depósitos que seleccione, o de todos los depósitos.
- Cada novedad enviada debe almacenar, como mínimo, el texto y la fecha de envío.
- Los operarios de un depósito pueden consultar las novedades dirigidas a su depósito y conocer la cantidad de novedades no leídas.
- Desde el detalle de una novedad, el operario puede marcarla como leída o responderla.
- No es necesario que el gerente o el auditor puedan contestarle al operario (no se requiere un hilo de conversación bidireccional).

**Reportes**
- La API debe exponer información agregada para alimentar los dashboards del frontend, entre otra:
  - Stock actual y valorización del inventario (costo unitario × stock), agrupado por depósito y por categoría.
  - Movimientos de stock agrupados por fecha, depósito y tipo de movimiento.
  - Productos con alerta de stock mínimo y de próximo vencimiento.
  - Estado de órdenes de compra y transferencias pendientes, agrupadas por depósito.
- Los listados deben soportar filtros (por rango de fechas, depósito, categoría, proveedor, según corresponda) y paginación.

### Requerimientos no funcionales

- **Seguridad**: las contraseñas nunca deben almacenarse en texto plano. La autenticación de la aplicación debe realizarse mediante tokens JWT, y cada operación debe validarse contra los permisos del rol del usuario autenticado (incluyendo la restricción por depósito asignado).
- **Persistencia**: la información se almacena en MongoDB, ejecutado en un contenedor Docker. El contenedor de MongoDB debe utilizar un volumen para persistir los datos en el host, de forma que la información no se pierda si el contenedor se detiene, se elimina o se recrea.
- **Trazabilidad**: las operaciones sensibles (movimientos de stock, aprobaciones, altas/bajas de usuarios) deben quedar registradas de forma auditable.
- **Auditoría de documentos**: todo documento almacenado debe registrar, como mínimo, el ID del usuario que lo creó, el ID del usuario que realizó la última modificación, la fecha/hora de creación y la fecha/hora de la última actualización.
- **Validación de datos**: todos los datos deben validarse antes de ser almacenados; la API debe responder con errores claros ante datos inválidos o inconsistentes (por ejemplo, egresos que superen el stock disponible).
- **Escalabilidad de datos**: los endpoints de listado deben responder de forma razonable ante volúmenes moderados de datos (paginación y filtros, no traer todo en una sola respuesta).
- **Disponibilidad del entorno**: tanto la API como la base de datos deben poder levantarse mediante contenedores Docker, de forma reproducible en cualquier equipo.

---

## Parte 2 — Frontend en React

### Objetivo

Desarrollar una aplicación web en React que consuma la API de Gestock, ofreciendo una experiencia distinta según el rol del usuario autenticado.

### Requerimientos funcionales

**Acceso**
- Pantalla de inicio de sesión.
- Una vez autenticado, el usuario debe ver únicamente las secciones y acciones permitidas para su rol.

**Dashboard principal**
- Pantalla de inicio con estilo **dashboard**, que condense la información más relevante para el rol del usuario, agrupada por criterios como depósito, categoría o fecha. La información debe presentarse condensada (tarjetas, listados agrupados, indicadores), no como una tabla plana de todos los registros.
- El contenido del dashboard varía según el rol del usuario autenticado:
  - **Administrador**: visión global de todos los depósitos. Stock y valorización total agrupados por depósito y por categoría, alertas de stock mínimo y de vencimientos de toda la empresa, últimos movimientos de todos los depósitos, y el total de órdenes de compra y transferencias pendientes de aprobación en cualquier depósito.
  - **Gerente de Depósito**: la misma información que el Administrador, pero acotada únicamente a los depósitos a su cargo, incluyendo las órdenes de compra y transferencias que requieren su aprobación.
  - **Operario de Depósito**: vista acotada a su depósito asignado, orientada a la operación diaria: alertas de stock mínimo y de vencimientos, últimos movimientos registrados, el estado de las transferencias que solicitó y la cantidad de novedades no leídas de su depósito. No visualiza la valorización económica del inventario.
  - **Auditor**: la misma visión global que el Administrador, en modo solo lectura, sin botones ni acciones disponibles sobre lo que se muestra.

**Gestión de datos maestros** (según permisos del rol)
- ABM de usuarios y asignación de roles/depósitos (Administrador).
- ABM de depósitos, categorías y proveedores.
- ABM de productos, incluyendo configuración de stock mínimo.

**Operación de stock**
- Registro de movimientos de stock (ingresos, egresos, ajustes) desde el depósito asignado al usuario.
- Solicitud, aprobación/rechazo y seguimiento de transferencias entre depósitos.
- Creación, confirmación y seguimiento de órdenes de compra a lo largo de sus estados (borrador, confirmada, parcial, completada), incluyendo el registro de recepciones de mercadería.
- Revisión de las órdenes de compra sugeridas automáticamente por reposición de stock, pudiendo modificarlas, confirmarlas o descartarlas.

**Novedades**
- El Gerente de Depósito cuenta con una pantalla para redactar y enviar novedades a los operarios de su depósito.
- El Auditor cuenta con una pantalla para redactar y enviar novedades, seleccionando un depósito puntual, varios depósitos o todos los depósitos.
- El operario, al ingresar al sistema, ve la cantidad de novedades no leídas de su depósito.
- Pantalla de listado de novedades del depósito del operario, con acceso al detalle de cada una, donde puede marcarla como leída o responderla.

**Reportes**
- Pantallas de reportes con filtros (por fecha, depósito, categoría, proveedor) que reflejen la información agregada expuesta por la API, agrupando visualmente los resultados (por ejemplo, por fecha o por depósito).

### Requerimientos no funcionales

- **Diseño responsivo**: la aplicación debe utilizar Bootstrap para lograr un diseño legible y prolijo, adaptable tanto a pantallas de escritorio como a pantallas más pequeñas.
- **Manejo de sesión**: la sesión del usuario debe mantenerse de forma segura mientras la aplicación esté en uso, y finalizar al cerrar sesión o expirar el acceso.
- **Feedback al usuario**: las acciones (altas, edición, aprobaciones, errores de validación) deben comunicarse claramente en la interfaz.
- **Consistencia visual**: la interfaz debe mantener un criterio de diseño consistente entre las distintas pantallas.
- **Manejo de estados de carga y error**: la aplicación debe informar cuando una operación está en curso y cuando ocurre un error de comunicación con la API.

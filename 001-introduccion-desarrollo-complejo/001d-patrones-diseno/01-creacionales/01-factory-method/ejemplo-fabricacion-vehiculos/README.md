# Ejemplo: Fabricación de vehículos (Factory Method)

El **Factory Method** define una interfaz para crear objetos en una superclase; las subclases deciden qué clase concreta instanciar. Aquí: una fábrica de vehículos que puede producir coches o camiones según la subclase.

## Archivos que deberían ir en esta carpeta

### Producto y productos concretos
- `Vehiculo.java` — Interfaz o clase base: `arrancar()`, `detener()` (o métodos que identifiquen un vehículo).
- `Coche.java` — Implementa `Vehiculo`.
- `Camion.java` — Implementa `Vehiculo`.

### Creador (factory) y creadores concretos
- `FabricaVehiculos.java` — Clase abstracta (o interfaz) con método `crearVehiculo()` que devuelve `Vehiculo`. Puede incluir método que use el producto: `entregarVehiculo()` que llama a `crearVehiculo()` y lo devuelve.
- `FabricaCoches.java` — Extiende `FabricaVehiculos`; `crearVehiculo()` devuelve `new Coche()`.
- `FabricaCamiones.java` — Extiende `FabricaVehiculos`; `crearVehiculo()` devuelve `new Camion()`.

### Cliente
- `Main.java` — Recibe una `FabricaVehiculos` (por parámetro o config); llama a `crearVehiculo()` (o `entregarVehiculo()`) y usa el vehículo sin conocer la clase concreta.

## Cómo comprobar que está bien aplicado

- El cliente no usa `new Coche()` ni `new Camion()`; solo la fábrica crea instancias.
- Añadir un nuevo tipo de vehículo implica crear la clase del producto y una nueva subclase de `FabricaVehiculos`, sin modificar el cliente.

# Ejercicio: Sistema de Tienda en Línea con Patrones de Diseño

## Enunciado

Crea un sistema para una tienda en línea que permita a los usuarios realizar compras de productos de diferentes categorías.

El sistema debe cumplir con los siguientes requerimientos, cada uno implementado mediante un patrón de diseño específico:

1. **Gestión de productos por categoría** → Patrón Factory Method
2. **Integración con proveedores de pago** → Patrón Bridge
3. **Cálculo de costo de envío** → Patrón Strategy
4. **Gestión del carrito de compras** → Patrón Singleton
5. **Construcción de órdenes de compra** → Patrón Builder

Con que los datos se guarden en memoria es suficiente.

---

## Patrones a Implementar

### 1. Factory Method — Categorías de Productos

Permite crear productos de distintas categorías sin acoplar el código cliente a las clases concretas.

**Categorías disponibles:** Electrónica, Ropa, Vehículos.

**Guía de implementación:**

- Crear una interfaz `Product` con los métodos comunes a todos los productos.
- Crear una clase abstracta `ProductFactory` con un método abstracto `createProduct()`.
- Crear una subclase de `ProductFactory` por cada categoría (`ElectronicaFactory`, `RopaFactory`, `VehiculosFactory`), cada una con su implementación particular.
- Crear una clase `ProductSelector` que use las fábricas para crear el producto según la categoría elegida por el usuario, mediante un método `selectProduct(category)`.

---

### 2. Bridge — Proveedores de Pago

Desacopla la abstracción del pago de su implementación concreta, permitiendo que ambas varíen de forma independiente.

**Medios de pago disponibles:** PayPal, MercadoPago.

**Guía de implementación:**

- Crear una interfaz `PaymentProvider` que defina el método `processPayment(amount)`.
- Implementar `PaypalProvider` y `MercadoPagoProvider` como implementaciones concretas.
- Crear una clase abstracta `PaymentProcessor` que contenga una referencia a `PaymentProvider` (el puente).
- Crear subclases de `PaymentProcessor` si se necesitan variaciones de procesamiento (por ejemplo, pago en cuotas vs. pago de contado).

---

### 3. Strategy — Cálculo de Costo de Envío

Permite definir una familia de algoritmos de cálculo de envío, encapsularlos e intercambiarlos en tiempo de ejecución.

**Medios de envío disponibles:** Avión, Barco, Camión.

**Guía de implementación:**

- Crear una interfaz `ShippingStrategy` con el método `calculateCost(weight, distance)`.
- Implementar `AirShipping`, `SeaShipping` y `TruckShipping` con su lógica de cálculo propia.
- Crear una clase `ShippingContext` que reciba una `ShippingStrategy` y la utilice para calcular el costo.
- Durante el proceso de compra, el usuario selecciona la estrategia de envío, que se inyecta en el contexto.

---

### 4. Singleton — Carrito de Compras

Garantiza que exista una única instancia del carrito de compras durante la sesión del usuario, evitando duplicación de estado.

**Guía de implementación:**

- Crear una clase `ShoppingCart` con un constructor privado.
- Agregar un atributo estático privado `instance` del mismo tipo.
- Exponer un método estático `getInstance()` que cree la instancia si no existe y la retorne.
- La clase debe permitir agregar productos (`addProduct`), eliminar productos (`removeProduct`) y obtener el total (`getTotal`).
- Verificar en el `main` que dos referencias obtenidas via `getInstance()` apuntan al mismo objeto.

> **Nota:** En Java, prestar atención a la sincronización si el sistema fuera multihilo (`synchronized` o inicialización estática).

---

### 5. Builder — Construcción de Órdenes de Compra

Permite construir objetos complejos paso a paso, separando la construcción de su representación final. Es útil cuando un objeto tiene muchos atributos opcionales.

**Una orden de compra** puede tener: cliente, lista de productos, dirección de envío, medio de envío, medio de pago, descuentos y notas adicionales. No todos los campos son obligatorios.

**Guía de implementación:**

- Crear una clase `Order` con todos los atributos posibles (algunos opcionales).
- Crear una clase interna o separada `Order.Builder` con los mismos atributos.
- Cada método del builder (`withCustomer(...)`, `withShippingAddress(...)`, `withPaymentMethod(...)`, etc.) debe retornar `this` para permitir encadenamiento fluido (fluent interface).
- El método `build()` valida que los campos obligatorios estén presentes y retorna la instancia de `Order`.
- Ejemplo de uso:

```java
Order order = new Order.Builder()
    .withCustomer("Ana García")
    .withProduct(product1)
    .withProduct(product2)
    .withShippingAddress("Av. Siempreviva 742")
    .withShippingStrategy(new TruckShipping())
    .withPaymentMethod(new PaypalProvider())
    .withDiscount(10)
    .build();
```

---

## Cómo el diseño facilita cambios futuros

### Agregar una nueva categoría de producto

Con **Factory Method**, basta con crear una nueva subclase de `ProductFactory` (por ejemplo, `AlimentosFactory`) e implementar `createProduct()`. El resto del sistema no se modifica: `ProductSelector` puede recibir la nueva categoría sin cambios en las clases existentes.

### Modificar el costo de envío de un tipo de envío particular

Con **Strategy**, cada algoritmo de envío está encapsulado en su propia clase. Modificar el cálculo de `TruckShipping` no afecta a `AirShipping` ni a `SeaShipping`, y tampoco requiere tocar `ShippingContext` ni el código de compra.

### Agregar un nuevo medio de pago

Con **Bridge**, solo se necesita crear una nueva clase que implemente `PaymentProvider` (por ejemplo, `StripeProvider`). La jerarquía de `PaymentProcessor` no se modifica. Esto también aplica si se quiere agregar una nueva variante de procesamiento sin crear nuevos proveedores.

### Extender los atributos de una orden

Con **Builder**, agregar un nuevo campo a `Order` (por ejemplo, un código de cupón) implica agregar el atributo y un método `withCoupon(...)` al builder. El código existente que construye órdenes sin cupón no se rompe.

---

## Actividades

Organizar el código usando el patrón de desarrollo en capas con packages de Java:

```
src/
├── presentacion/       # Clase principal, interacción con el usuario
│   └── Main.java
├── logica/             # Reglas de negocio y patrones de diseño
│   ├── factory/        # Factory Method (creación de productos)
│   ├── payment/        # Bridge (proveedores de pago)
│   ├── shipping/       # Strategy (cálculo de envío)
│   ├── cart/           # Singleton (carrito de compras)
│   └── order/          # Builder (construcción de órdenes)
└── datos/              # Clases de dominio y almacenamiento en memoria
    ├── Product.java
    ├── Order.java
    └── ProductRepository.java
```

La clase `main` debe demostrar un flujo completo: selección de productos, elección de envío, elección de medio de pago, construcción de la orden. Verificar que el carrito es el mismo objeto en distintas partes del código (Singleton).

---

## Recursos sugeridos

- [Refactoring Guru — Factory Method](https://refactoring.guru/es/design-patterns/factory-method)
- [Refactoring Guru — Bridge](https://refactoring.guru/es/design-patterns/bridge)
- [Refactoring Guru — Strategy](https://refactoring.guru/es/design-patterns/strategy)
- [Refactoring Guru — Singleton](https://refactoring.guru/es/design-patterns/singleton)
- [Refactoring Guru — Builder](https://refactoring.guru/es/design-patterns/builder)

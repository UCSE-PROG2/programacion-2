package logica.factory;

import datos.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase abstracta que define el patrón Factory Method para la creación de productos.
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  PATRÓN FACTORY METHOD                                              │
 * │                                                                     │
 * │  Problema que resuelve: el código cliente necesita crear productos, │
 * │  pero no debería conocer ni depender de las clases concretas        │
 * │  (ProductoElectronica, ProductoRopa, ProductoVehiculo).             │
 * │                                                                     │
 * │  Solución: definir un método abstracto createProduct() en esta      │
 * │  clase base. Cada subclase (fábrica concreta) lo implementa         │
 * │  creando el producto del tipo que le corresponde.                   │
 * │                                                                     │
 * │  Estructura:                                                        │
 * │    ProductFactory (abstracta) ← este archivo                       │
 * │      ├── ElectronicaFactory   → crea ProductoElectronica           │
 * │      ├── RopaFactory          → crea ProductoRopa                  │
 * │      └── VehiculosFactory     → crea ProductoVehiculo              │
 * └─────────────────────────────────────────────────────────────────────┘
 *
 * El método {@link #createProduct(String, String, double)} es el "Factory Method"
 * propiamente dicho: es abstracto y debe ser sobrescrito por cada subclase.
 *
 * El método {@link #crearYRegistrar(String, String, double)} es un Template Method:
 * define la secuencia de pasos (crear + loguear), delegando el paso de
 * creación al Factory Method de cada subclase.
 */
public abstract class ProductFactory {

    /**
     * Logger de SLF4J para registrar eventos de creación de productos.
     *
     * Se usa getLogger(ProductFactory.class) para que Logback identifique el origen
     * del log. En el output aparecerá el nombre simple "ProductFactory".
     *
     * Las subclases heredan este logger o pueden definir el propio con su clase.
     */
    private static final Logger logger = LoggerFactory.getLogger(ProductFactory.class);

    /**
     * Factory Method: método abstracto que cada fábrica concreta debe implementar.
     *
     * Cada subclase decide qué clase concreta de Product instanciar:
     *   - ElectronicaFactory → new ProductoElectronica(...)
     *   - RopaFactory        → new ProductoRopa(...)
     *   - VehiculosFactory   → new ProductoVehiculo(...)
     *
     * @param nombre      Nombre del producto a crear.
     * @param descripcion Descripción del producto.
     * @param precio      Precio del producto.
     * @return Una instancia de Product del tipo concreto correspondiente.
     */
    public abstract Product createProduct(String nombre, String descripcion, double precio);

    /**
     * Template Method: método concreto que define el flujo de creación.
     *
     * Este método no sabe qué tipo de producto se va a crear (eso lo decide
     * createProduct, que es llamado internamente). Lo que sí hace es aplicar
     * lógica común a todas las fábricas: crear el producto y registrar el evento
     * usando SLF4J con nivel DEBUG (detalle del flujo interno del patrón).
     *
     * @param nombre      Nombre del producto.
     * @param descripcion Descripción del producto.
     * @param precio      Precio del producto.
     * @return El producto creado y ya registrado.
     */
    public Product crearYRegistrar(String nombre, String descripcion, double precio) {
        logger.debug("Invocando Factory Method en: {}", this.getClass().getSimpleName());

        // Llama al Factory Method — la subclase decide qué clase concreta instanciar
        Product product = createProduct(nombre, descripcion, precio);

        // Log INFO: evento de negocio relevante — producto creado exitosamente
        logger.info("Producto creado — categoría: {}, nombre: {}, precio: ${}",
                product.getCategoria(), product.getNombre(), String.format("%.2f", product.getPrecio()));

        return product;
    }
}

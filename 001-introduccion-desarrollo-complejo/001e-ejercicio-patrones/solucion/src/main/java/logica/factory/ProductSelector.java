package logica.factory;

import datos.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Selector de productos que utiliza las fábricas concretas del patrón Factory Method.
 *
 * Esta clase actúa como punto de entrada único para la creación de productos.
 * Recibe la categoría elegida por el usuario, selecciona la fábrica correcta
 * y delega la creación del producto a esa fábrica.
 *
 * El código cliente (Main.java) sólo interactúa con ProductSelector; nunca
 * instancia directamente ElectronicaFactory, RopaFactory ni VehiculosFactory,
 * ni mucho menos ProductoElectronica, ProductoRopa o ProductoVehiculo.
 *
 * Para agregar una nueva categoría (ej: "Alimentos"), sólo hay que:
 *   1. Crear AlimentosFactory extends ProductFactory.
 *   2. Crear ProductoAlimento implements Product.
 *   3. Agregar el case "alimentos" en este método.
 * Ninguna otra clase del sistema necesita modificarse. ← Open/Closed Principle
 */
public class ProductSelector {

    /**
     * Logger para registrar qué fábrica se seleccionó y posibles errores de categoría.
     * Nivel DEBUG para el detalle de selección de fábrica.
     */
    private static final Logger logger = LoggerFactory.getLogger(ProductSelector.class);

    /**
     * Selecciona la fábrica apropiada según la categoría indicada y crea el producto.
     *
     * El flujo completo es:
     *   1. Se recibe la categoría como String (flexible, no necesita un enum).
     *   2. Se instancia la fábrica concreta correspondiente.
     *   3. Se delega la creación al método {@link ProductFactory#crearYRegistrar}.
     *   4. Se retorna el Product creado (sin exponer la clase concreta).
     *
     * @param categoria   Categoría del producto: "electronica", "ropa" o "vehiculos".
     *                    La comparación es case-insensitive y acepta variantes con tilde.
     * @param nombre      Nombre del producto a crear.
     * @param descripcion Descripción del producto.
     * @param precio      Precio del producto.
     * @return El producto creado, tipado como la interfaz Product.
     * @throws IllegalArgumentException si la categoría no es reconocida.
     */
    public Product selectProduct(String categoria, String nombre, String descripcion, double precio) {

        logger.debug("Seleccionando fábrica para categoría: '{}'", categoria);

        // Se elige la fábrica según la categoría — este es el único switch que
        // conoce qué fábrica existe para cada categoría.
        ProductFactory factory = switch (categoria.toLowerCase().trim()) {
            case "electronica", "electrónica" -> new ElectronicaFactory();
            case "ropa"                        -> new RopaFactory();
            case "vehiculos", "vehículos"      -> new VehiculosFactory();
            default -> {
                logger.error("Categoría no reconocida: '{}'. Válidas: electronica, ropa, vehiculos", categoria);
                throw new IllegalArgumentException(
                        "Categoría no reconocida: '" + categoria + "'. " +
                        "Categorías válidas: electronica, ropa, vehiculos."
                );
            }
        };

        logger.debug("Fábrica seleccionada: {}", factory.getClass().getSimpleName());

        // La fábrica seleccionada crea el producto concreto y lo registra via su propio logger
        return factory.crearYRegistrar(nombre, descripcion, precio);
    }
}

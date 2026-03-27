package logica.cart;

import datos.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Carrito de compras implementado con el patrón Singleton.
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  PATRÓN SINGLETON                                                   │
 * │                                                                     │
 * │  Problema que resuelve: el carrito debe ser único durante toda la  │
 * │  sesión del usuario. Si hubiera múltiples instancias del carrito,  │
 * │  distintas partes del sistema verían distintos estados (algunos    │
 * │  productos en una instancia, otros en otra), generando inconsistencia│
 * │                                                                     │
 * │  Solución: garantizar que sólo exista UNA instancia de ShoppingCart│
 * │  y que todas las partes del sistema accedan a ella vía getInstance()│
 * │                                                                     │
 * │  Mecanismo:                                                         │
 * │    1. Constructor PRIVADO → nadie puede hacer new ShoppingCart()   │
 * │    2. Campo estático 'instance' → guarda la única instancia        │
 * │    3. Método estático getInstance() → crea la instancia si no      │
 * │       existe, o devuelve la existente si ya fue creada             │
 * │    4. synchronized → protege la creación en entornos multihilo     │
 * └─────────────────────────────────────────────────────────────────────┘
 */
public class ShoppingCart {

    /**
     * Logger estático del carrito. Al ser estático, existe antes de que haya
     * una instancia de ShoppingCart, lo cual es necesario para poder loguear
     * el momento exacto en que se crea la instancia Singleton.
     */
    private static final Logger logger = LoggerFactory.getLogger(ShoppingCart.class);

    /**
     * La única instancia de ShoppingCart. Es estática para que pertenezca
     * a la clase (no a ningún objeto) y persista durante toda la ejecución.
     */
    private static ShoppingCart instance;

    /** Lista de productos agregados al carrito en esta sesión. */
    private final List<Product> productos;

    /**
     * Constructor PRIVADO — el núcleo del patrón Singleton.
     * Al ser privado, ninguna clase externa puede hacer new ShoppingCart().
     */
    private ShoppingCart() {
        this.productos = new ArrayList<>();
        logger.debug("Instancia de ShoppingCart inicializada en memoria.");
    }

    /**
     * Punto de acceso global al carrito de compras.
     *
     * La palabra clave {@code synchronized} garantiza que, en un sistema
     * multihilo, dos hilos no puedan crear la instancia al mismo tiempo.
     *
     * Flujo de la primera llamada:
     *   1. instance == null → se entra al if
     *   2. Se crea la única instancia con el constructor privado
     *   3. Se asigna a instance y se loguea el evento
     *
     * En todas las llamadas siguientes:
     *   1. instance != null → se omite el if
     *   2. Se retorna la instancia ya existente (sin crear nada nuevo)
     *
     * @return La única instancia de ShoppingCart existente en el sistema.
     */
    public static synchronized ShoppingCart getInstance() {
        if (instance == null) {
            instance = new ShoppingCart();
            logger.info("Singleton: primera llamada a getInstance() — carrito creado (hashCode: {})",
                    System.identityHashCode(instance));
        } else {
            logger.debug("Singleton: getInstance() retorna instancia existente (hashCode: {})",
                    System.identityHashCode(instance));
        }
        return instance;
    }

    /**
     * Agrega un producto al carrito y loguea el estado actualizado.
     *
     * @param product El producto a agregar (creado por una Factory).
     */
    public void addProduct(Product product) {
        productos.add(product);
        logger.info("Producto agregado al carrito: '{}' — subtotal acumulado: ${}",
                product.getNombre(), String.format("%.2f", getTotal()));
    }

    /**
     * Elimina un producto del carrito si está presente.
     *
     * @param product El producto a eliminar.
     */
    public void removeProduct(Product product) {
        boolean removed = productos.remove(product);
        if (removed) {
            logger.info("Producto eliminado del carrito: '{}'", product.getNombre());
        } else {
            logger.warn("Se intentó eliminar '{}' pero no está en el carrito.", product.getNombre());
        }
    }

    /**
     * Retorna una copia inmutable de la lista de productos del carrito.
     *
     * @return Lista no modificable de productos en el carrito.
     */
    public List<Product> getProducts() {
        return Collections.unmodifiableList(productos);
    }

    /**
     * Calcula el total sumando los precios de todos los productos del carrito.
     *
     * @return Suma de los precios de todos los productos.
     */
    public double getTotal() {
        return productos.stream().mapToDouble(Product::getPrecio).sum();
    }

    /**
     * Vacía el carrito eliminando todos sus productos.
     */
    public void clear() {
        int cantidad = productos.size();
        productos.clear();
        logger.info("Carrito vaciado — se eliminaron {} productos.", cantidad);
    }

    /**
     * Retorna la cantidad de productos actualmente en el carrito.
     *
     * @return Número de productos en el carrito.
     */
    public int size() {
        return productos.size();
    }

    /**
     * Loguea el contenido actual del carrito en nivel INFO.
     * Cada producto se imprime en su propia línea de log para mayor legibilidad.
     */
    public void mostrarContenido() {
        logger.info("=== Contenido del carrito ({} productos) ===", productos.size());
        if (productos.isEmpty()) {
            logger.info("  (carrito vacío)");
        } else {
            for (Product p : productos) {
                logger.info("  • {} [{}] — ${}", p.getNombre(), p.getCategoria(),
                        String.format("%.2f", p.getPrecio()));
            }
            logger.info("  SUBTOTAL: ${}", String.format("%.2f", getTotal()));
        }
    }
}

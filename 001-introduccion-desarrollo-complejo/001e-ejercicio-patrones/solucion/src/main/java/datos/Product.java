package datos;

/**
 * Interfaz que representa un producto de la tienda en línea.
 *
 * Esta interfaz es el contrato común que deben cumplir todas las clases de
 * productos concretos (electrónica, ropa, vehículos). Al trabajar con esta
 * interfaz en lugar de clases concretas, el código cliente queda desacoplado
 * de la categoría específica del producto.
 *
 * Esta interfaz es parte central del patrón Factory Method: la fábrica abstracta
 * promete devolver un Product, y cada fábrica concreta decide qué subclase crear.
 */
public interface Product {

    /**
     * Retorna el nombre del producto (por ejemplo, "Laptop Lenovo ThinkPad").
     */
    String getNombre();

    /**
     * Retorna una descripción breve del producto con sus características principales.
     */
    String getDescripcion();

    /**
     * Retorna el precio del producto en pesos argentinos.
     */
    double getPrecio();

    /**
     * Retorna la categoría a la que pertenece el producto (Electrónica, Ropa, Vehículos).
     * Cada clase concreta implementa este método retornando su categoría fija.
     */
    String getCategoria();
}

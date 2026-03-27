package logica.factory;

import datos.Product;

/**
 * Fábrica concreta para productos de la categoría Ropa.
 *
 * Implementa el patrón Factory Method sobrescribiendo {@code createProduct()}
 * para instanciar específicamente un {@link ProductoRopa}.
 *
 * Al agregar esta clase al sistema (y su entrada en {@link ProductSelector}),
 * se habilita una nueva categoría sin modificar ninguna otra clase existente.
 * Este es el principio abierto/cerrado (Open/Closed Principle) en acción.
 */
public class RopaFactory extends ProductFactory {

    /**
     * Implementación del Factory Method para la categoría Ropa.
     *
     * @param nombre      Nombre del artículo de ropa.
     * @param descripcion Descripción del artículo (talle, material, color).
     * @param precio      Precio del artículo.
     * @return Un nuevo ProductoRopa encapsulado detrás de la interfaz Product.
     */
    @Override
    public Product createProduct(String nombre, String descripcion, double precio) {
        return new ProductoRopa(nombre, descripcion, precio);
    }
}

package logica.factory;

import datos.Product;

/**
 * Fábrica concreta para productos de la categoría Electrónica.
 *
 * Implementa el patrón Factory Method sobrescribiendo {@code createProduct()}
 * con la lógica específica de crear un {@link ProductoElectronica}.
 *
 * Ventaja clave: si mañana se decide cambiar la clase interna de producto
 * electrónico (por ejemplo, añadir validación de número de serie), sólo
 * se modifica esta fábrica y el ProductoElectronica, sin tocar ningún otro
 * lugar del sistema.
 */
public class ElectronicaFactory extends ProductFactory {

    /**
     * Implementación del Factory Method para la categoría Electrónica.
     *
     * Instancia y retorna un {@link ProductoElectronica} con los datos recibidos.
     * El código cliente nunca invoca este método directamente; usa
     * {@link ProductFactory#crearYRegistrar} o {@link ProductSelector}.
     *
     * @param nombre      Nombre del artículo electrónico.
     * @param descripcion Descripción técnica del artículo.
     * @param precio      Precio del artículo.
     * @return Un nuevo ProductoElectronica encapsulado detrás de la interfaz Product.
     */
    @Override
    public Product createProduct(String nombre, String descripcion, double precio) {
        return new ProductoElectronica(nombre, descripcion, precio);
    }
}

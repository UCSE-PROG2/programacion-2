package logica.factory;

import datos.Product;

/**
 * Fábrica concreta para productos de la categoría Vehículos.
 *
 * Implementa el patrón Factory Method sobrescribiendo {@code createProduct()}
 * para instanciar específicamente un {@link ProductoVehiculo}.
 *
 * Nótese que las tres fábricas (Electrónica, Ropa, Vehículos) son
 * estructuralmente idénticas en este ejemplo simple. En sistemas más complejos,
 * cada fábrica podría tener lógica adicional: validar que el precio sea mayor
 * a un mínimo, aplicar impuestos específicos de la categoría, etc.
 */
public class VehiculosFactory extends ProductFactory {

    /**
     * Implementación del Factory Method para la categoría Vehículos.
     *
     * @param nombre      Nombre del vehículo (ej: "Moto Honda CB 250").
     * @param descripcion Descripción del vehículo (año, cilindrada, color).
     * @param precio      Precio del vehículo.
     * @return Un nuevo ProductoVehiculo encapsulado detrás de la interfaz Product.
     */
    @Override
    public Product createProduct(String nombre, String descripcion, double precio) {
        return new ProductoVehiculo(nombre, descripcion, precio);
    }
}

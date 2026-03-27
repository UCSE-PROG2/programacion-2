package logica.factory;

import datos.Product;

/**
 * Producto concreto de la categoría "Electrónica".
 *
 * Esta clase implementa la interfaz {@link Product} para representar
 * artículos electrónicos: notebooks, celulares, tablets, auriculares, etc.
 *
 * En el patrón Factory Method, esta es una "Clase Concreta de Producto".
 * No es instanciada directamente por el código cliente; en cambio, la
 * {@link ElectronicaFactory} se encarga de crearla a través del método
 * {@code createProduct()}, manteniendo al cliente desacoplado de esta clase.
 */
public class ProductoElectronica implements Product {

    private final String nombre;
    private final String descripcion;
    private final double precio;

    /**
     * Constructor que recibe todos los datos del producto electrónico.
     * Es invocado exclusivamente por {@link ElectronicaFactory#createProduct}.
     *
     * @param nombre      Nombre del producto (ej: "Notebook HP Pavilion").
     * @param descripcion Descripción técnica del producto.
     * @param precio      Precio en pesos argentinos.
     */
    public ProductoElectronica(String nombre, String descripcion, double precio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public double getPrecio() {
        return precio;
    }

    /**
     * Retorna la categoría fija de este producto.
     * Al estar hardcodeada aquí, cualquier ProductoElectronica siempre
     * pertenece a la misma categoría, lo cual es correcto por diseño.
     */
    @Override
    public String getCategoria() {
        return "Electrónica";
    }

    /**
     * Representación legible del producto con su categoría, nombre y precio.
     * Útil para mostrar en el resumen del carrito y la orden de compra.
     */
    @Override
    public String toString() {
        return String.format("[Electrónica] %s — $%.2f | %s", nombre, precio, descripcion);
    }
}

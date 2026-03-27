package logica.factory;

import datos.Product;

/**
 * Producto concreto de la categoría "Ropa".
 *
 * Representa artículos de indumentaria: remeras, pantalones, zapatillas, etc.
 *
 * En el patrón Factory Method, es una "Clase Concreta de Producto".
 * Sólo la {@link RopaFactory} la instancia, nunca el código cliente directamente.
 * Esto permite que el cliente trabaje siempre con la interfaz {@link datos.Product},
 * sin conocer qué clase concreta tiene entre manos.
 */
public class ProductoRopa implements Product {

    private final String nombre;
    private final String descripcion;
    private final double precio;

    /**
     * Constructor invocado exclusivamente por {@link RopaFactory#createProduct}.
     *
     * @param nombre      Nombre del artículo (ej: "Zapatillas Nike Air Max").
     * @param descripcion Descripción del artículo (talle, material, color).
     * @param precio      Precio en pesos argentinos.
     */
    public ProductoRopa(String nombre, String descripcion, double precio) {
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

    @Override
    public String getCategoria() {
        return "Ropa";
    }

    @Override
    public String toString() {
        return String.format("[Ropa] %s — $%.2f | %s", nombre, precio, descripcion);
    }
}

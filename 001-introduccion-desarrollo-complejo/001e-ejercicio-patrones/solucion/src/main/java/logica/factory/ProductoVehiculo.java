package logica.factory;

import datos.Product;

/**
 * Producto concreto de la categoría "Vehículos".
 *
 * Representa medios de transporte: autos, motos, bicicletas, scooters, etc.
 *
 * En el patrón Factory Method, es una "Clase Concreta de Producto".
 * Sólo la {@link VehiculosFactory} la instancia a través del método
 * {@code createProduct()}, manteniendo el acoplamiento bajo en el sistema.
 */
public class ProductoVehiculo implements Product {

    private final String nombre;
    private final String descripcion;
    private final double precio;

    /**
     * Constructor invocado exclusivamente por {@link VehiculosFactory#createProduct}.
     *
     * @param nombre      Nombre del vehículo (ej: "Moto Honda CB 250").
     * @param descripcion Descripción del vehículo (año, cilindrada, color).
     * @param precio      Precio en pesos argentinos.
     */
    public ProductoVehiculo(String nombre, String descripcion, double precio) {
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
        return "Vehículos";
    }

    @Override
    public String toString() {
        return String.format("[Vehículos] %s — $%.2f | %s", nombre, precio, descripcion);
    }
}

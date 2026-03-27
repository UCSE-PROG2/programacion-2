package datos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repositorio en memoria para almacenar y consultar productos del sistema.
 *
 * Actúa como capa de acceso a datos (DAO simplificado). En un sistema real,
 * esta clase se conectaría a una base de datos; aquí usamos una lista en memoria
 * para mantener el foco en los patrones de diseño.
 *
 * Los productos son creados mediante el patrón Factory Method (en logica/factory)
 * y luego almacenados aquí para su posterior consulta.
 */
public class ProductRepository {

    /** Lista interna donde se almacenan todos los productos del catálogo. */
    private final List<Product> productos = new ArrayList<>();

    /**
     * Agrega un producto al repositorio.
     * Se llama después de crear un producto con una fábrica concreta.
     *
     * @param product El producto creado por una fábrica Factory Method.
     */
    public void agregar(Product product) {
        productos.add(product);
    }

    /**
     * Retorna una copia defensiva de todos los productos almacenados.
     * Se usa una copia para que el cliente no pueda modificar la lista interna.
     *
     * @return Lista con todos los productos del catálogo.
     */
    public List<Product> obtenerTodos() {
        return new ArrayList<>(productos);
    }

    /**
     * Filtra y retorna los productos que pertenecen a una categoría específica.
     * La comparación es case-insensitive (no distingue mayúsculas/minúsculas).
     *
     * @param categoria La categoría a filtrar (ej. "Electrónica", "Ropa", "Vehículos").
     * @return Lista de productos de esa categoría.
     */
    public List<Product> obtenerPorCategoria(String categoria) {
        return productos.stream()
                .filter(p -> p.getCategoria().equalsIgnoreCase(categoria))
                .collect(Collectors.toList());
    }

    /**
     * Busca un producto por su nombre exacto (case-insensitive).
     * Retorna un Optional para obligar al cliente a manejar el caso de que
     * el producto no exista, evitando NullPointerExceptions.
     *
     * @param nombre El nombre del producto a buscar.
     * @return Un Optional con el producto si existe, o empty si no se encontró.
     */
    public Optional<Product> buscarPorNombre(String nombre) {
        return productos.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }
}

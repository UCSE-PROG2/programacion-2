package datos;

import logica.payment.PaymentProcessor;
import logica.shipping.ShippingStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase de dominio que representa una orden de compra.
 *
 * Esta clase implementa el patrón Builder a través de su clase interna estática
 * {@link Builder}. El patrón Builder se usa porque una orden tiene muchos atributos,
 * algunos obligatorios y otros opcionales, lo que haría que los constructores
 * tradicionales sean difíciles de usar (problema conocido como "telescoping constructor").
 *
 * Una instancia de Order es INMUTABLE una vez construida: todos sus atributos son
 * final y sólo tienen getters (no setters). Esto se logra porque el Builder
 * es el único que puede invocar el constructor privado de Order.
 *
 * Jerarquía de dependencias:
 *   Order (datos) → PaymentProcessor (logica/payment) → PaymentProvider (logica/payment)
 *   Order (datos) → ShippingStrategy (logica/shipping)
 *
 * Esta dependencia de datos → logica existe porque Order necesita referencias a las
 * estrategias concretas para delegar comportamientos (procesamiento de pago, cálculo de envío).
 */
public class Order {

    // -----------------------------------------------------------------------
    // Atributos de la orden — todos son final: la orden no puede cambiar
    // una vez construida. Esto la hace más segura y predecible.
    // -----------------------------------------------------------------------

    /** Nombre del cliente que realizó la compra. Campo obligatorio. */
    private final String cliente;

    /** Lista de productos incluidos en la orden. Debe tener al menos uno. */
    private final List<Product> productos;

    /** Dirección de envío del pedido. Campo opcional. */
    private final String direccionEnvio;

    /**
     * Estrategia de envío seleccionada (Avión, Barco, Camión).
     * Es la interfaz del patrón Strategy. Campo opcional.
     */
    private final ShippingStrategy estrategiaEnvio;

    /**
     * Procesador de pago seleccionado (Contado/Cuotas + PayPal/MercadoPago).
     * Es la abstracción del patrón Bridge. Campo opcional.
     */
    private final PaymentProcessor metodoPago;

    /** Porcentaje de descuento a aplicar sobre el subtotal. 0 significa sin descuento. */
    private final double descuento;

    /** Notas adicionales del cliente (instrucciones de entrega, etc.). Campo opcional. */
    private final String notasAdicionales;

    /**
     * Constructor privado: sólo el Builder puede instanciar una Order.
     * Recibe un Builder ya configurado y extrae todos sus valores.
     *
     * @param builder El builder con todos los atributos ya establecidos.
     */
    private Order(Builder builder) {
        this.cliente = builder.cliente;
        this.productos = Collections.unmodifiableList(new ArrayList<>(builder.productos));
        this.direccionEnvio = builder.direccionEnvio;
        this.estrategiaEnvio = builder.estrategiaEnvio;
        this.metodoPago = builder.metodoPago;
        this.descuento = builder.descuento;
        this.notasAdicionales = builder.notasAdicionales;
    }

    // -----------------------------------------------------------------------
    // Getters — no hay setters: la orden es inmutable
    // -----------------------------------------------------------------------

    public String getCliente() { return cliente; }
    public List<Product> getProductos() { return productos; }
    public String getDireccionEnvio() { return direccionEnvio; }
    public ShippingStrategy getEstrategiaEnvio() { return estrategiaEnvio; }
    public PaymentProcessor getMetodoPago() { return metodoPago; }
    public double getDescuento() { return descuento; }
    public String getNotasAdicionales() { return notasAdicionales; }

    /**
     * Calcula el total de la orden aplicando el descuento sobre el subtotal.
     * Ejemplo: si el subtotal es $1000 y el descuento es 10%, el total es $900.
     *
     * @return El monto total a pagar después de aplicar el descuento.
     */
    public double calcularTotal() {
        double subtotal = productos.stream().mapToDouble(Product::getPrecio).sum();
        return subtotal * (1.0 - descuento / 100.0);
    }

    /**
     * Ejecuta el pago de la orden usando el PaymentProcessor configurado (Bridge).
     * Si no hay método de pago configurado, lanza una excepción.
     */
    public void procesarPago() {
        if (metodoPago == null) {
            throw new IllegalStateException("No se configuró un método de pago para esta orden.");
        }
        metodoPago.pagar(calcularTotal());
    }

    /**
     * Representación textual completa de la orden, útil para mostrar el resumen
     * al usuario o para depuración.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════╗\n");
        sb.append("║        ORDEN DE COMPRA               ║\n");
        sb.append("╚══════════════════════════════════════╝\n");
        sb.append("  Cliente       : ").append(cliente).append("\n");
        sb.append("  Productos     :\n");
        for (Product p : productos) {
            sb.append("    • ").append(p).append("\n");
        }
        if (direccionEnvio != null) {
            sb.append("  Dirección     : ").append(direccionEnvio).append("\n");
        }
        if (estrategiaEnvio != null) {
            sb.append("  Tipo de envío : ").append(estrategiaEnvio.getNombreEnvio()).append("\n");
        }
        if (metodoPago != null) {
            sb.append("  Forma de pago : ").append(metodoPago.getDescripcion()).append("\n");
        }
        if (descuento > 0) {
            sb.append("  Descuento     : ").append(descuento).append("%\n");
        }
        if (notasAdicionales != null) {
            sb.append("  Notas         : ").append(notasAdicionales).append("\n");
        }
        sb.append(String.format("  TOTAL A PAGAR : $%.2f%n", calcularTotal()));
        return sb.toString();
    }

    // =======================================================================
    //  PATRÓN BUILDER — Clase interna estática
    // =======================================================================

    /**
     * Builder para construir instancias de {@link Order} paso a paso.
     *
     * El patrón Builder resuelve el problema de constructores con muchos parámetros
     * opcionales. En lugar de tener constructores con 7 parámetros (algunos nulos),
     * se encadenan métodos legibles:
     *
     * <pre>
     * Order orden = new Order.Builder()
     *     .withCustomer("Ana García")
     *     .withProduct(laptop)
     *     .withShippingAddress("Av. Siempreviva 742")
     *     .withShippingStrategy(new TruckShipping())
     *     .withPaymentMethod(new PagoContadoProcessor(new PaypalProvider()))
     *     .withDiscount(10)
     *     .build();
     * </pre>
     *
     * Cada método retorna {@code this} (el mismo Builder), lo que permite el
     * encadenamiento fluido (fluent interface). El método {@link #build()} es el
     * único que retorna un {@link Order} y valida que los campos obligatorios estén presentes.
     */
    public static class Builder {

        // Los mismos atributos que Order — se van llenando con cada llamada fluida
        private String cliente;
        private final List<Product> productos = new ArrayList<>();
        private String direccionEnvio;
        private ShippingStrategy estrategiaEnvio;
        private PaymentProcessor metodoPago;
        private double descuento = 0;
        private String notasAdicionales;

        /**
         * Establece el nombre del cliente. Campo obligatorio.
         * @param cliente Nombre completo del cliente.
         * @return Este mismo Builder (para encadenamiento fluido).
         */
        public Builder withCustomer(String cliente) {
            this.cliente = cliente;
            return this;
        }

        /**
         * Agrega un producto a la orden. Se puede llamar múltiples veces para
         * agregar varios productos. Debe llamarse al menos una vez.
         *
         * @param product Producto a incluir en la orden.
         * @return Este mismo Builder.
         */
        public Builder withProduct(Product product) {
            this.productos.add(product);
            return this;
        }

        /**
         * Establece la dirección de envío del pedido. Campo opcional.
         * @param direccion Dirección completa (calle, número, ciudad).
         * @return Este mismo Builder.
         */
        public Builder withShippingAddress(String direccion) {
            this.direccionEnvio = direccion;
            return this;
        }

        /**
         * Establece la estrategia de envío (patrón Strategy).
         * La estrategia define el algoritmo de cálculo de costo de envío.
         * Campo opcional.
         *
         * @param strategy Una implementación de ShippingStrategy (AirShipping, SeaShipping, TruckShipping).
         * @return Este mismo Builder.
         */
        public Builder withShippingStrategy(ShippingStrategy strategy) {
            this.estrategiaEnvio = strategy;
            return this;
        }

        /**
         * Establece el método de pago (patrón Bridge).
         * El PaymentProcessor combina "cómo se cobra" (contado/cuotas) con
         * "quién procesa" (PayPal/MercadoPago). Campo opcional.
         *
         * @param processor Una subclase de PaymentProcessor con un PaymentProvider configurado.
         * @return Este mismo Builder.
         */
        public Builder withPaymentMethod(PaymentProcessor processor) {
            this.metodoPago = processor;
            return this;
        }

        /**
         * Establece un porcentaje de descuento sobre el subtotal. Campo opcional.
         * Si no se llama, el descuento es 0 (sin descuento).
         *
         * @param descuento Porcentaje entre 0 y 100 (ej: 10 significa 10% de descuento).
         * @return Este mismo Builder.
         */
        public Builder withDiscount(double descuento) {
            this.descuento = descuento;
            return this;
        }

        /**
         * Agrega notas adicionales a la orden (instrucciones especiales de entrega, etc.).
         * Campo opcional.
         *
         * @param notas Texto libre con notas del cliente.
         * @return Este mismo Builder.
         */
        public Builder withNotes(String notas) {
            this.notasAdicionales = notas;
            return this;
        }

        /**
         * Valida los campos obligatorios y construye la instancia final de {@link Order}.
         *
         * Este es el único punto donde se crea el objeto Order. Antes de crearlo,
         * verifica que los campos mínimos requeridos estén presentes:
         *   - El cliente no puede ser nulo ni vacío.
         *   - La orden debe tener al menos un producto.
         *
         * @return Una nueva instancia inmutable de Order.
         * @throws IllegalStateException si falta algún campo obligatorio.
         */
        public Order build() {
            if (cliente == null || cliente.isBlank()) {
                throw new IllegalStateException("El cliente es obligatorio para construir una orden.");
            }
            if (productos.isEmpty()) {
                throw new IllegalStateException("La orden debe tener al menos un producto.");
            }
            return new Order(this);
        }
    }
}

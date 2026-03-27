package logica.payment;

/**
 * Clase abstracta que define la "Abstracción" del patrón Bridge para el procesamiento de pagos.
 *
 * En el patrón Bridge, la Abstracción define la interfaz de alto nivel que el cliente
 * usa, y delega el trabajo de bajo nivel a la Implementación (PaymentProvider).
 *
 * Esta clase mantiene una referencia al PaymentProvider (el "puente"):
 *
 *   PaymentProcessor (abstracción)     PaymentProvider (implementación)
 *   ─────────────────────────────      ──────────────────────────────────
 *   PagoContadoProcessor               PaypalProvider
 *   PagoCuotasProcessor                MercadoPagoProvider
 *
 * La relación entre ambas jerarquías es de COMPOSICIÓN, no de herencia.
 * Esto permite combinar cualquier forma de cobro con cualquier proveedor:
 *   - Contado con PayPal
 *   - Contado con MercadoPago
 *   - Cuotas con PayPal
 *   - Cuotas con MercadoPago
 * Sin necesidad de crear una subclase por cada combinación posible (que serían 4 ahora,
 * pero N×M en sistemas con más variantes).
 */
public abstract class PaymentProcessor {

    /**
     * La implementación concreta del proveedor de pago.
     * Es el "puente" entre la abstracción y su implementación.
     * Se inyecta por constructor y puede ser cualquier PaymentProvider.
     */
    protected final PaymentProvider provider;

    /**
     * Constructor que recibe el proveedor a usar.
     * La inyección por constructor garantiza que el PaymentProcessor
     * siempre tenga un provider válido al momento de ser utilizado.
     *
     * @param provider La implementación concreta del proveedor de pago
     *                 (PaypalProvider, MercadoPagoProvider, etc.)
     */
    protected PaymentProcessor(PaymentProvider provider) {
        this.provider = provider;
    }

    /**
     * Método abstracto que cada subclase implementa con su lógica de cobro específica.
     *
     * PagoContadoProcessor: cobra el monto completo directamente.
     * PagoCuotasProcessor: divide el monto en cuotas y cobra el total al proveedor.
     *
     * @param monto El monto total de la orden a cobrar.
     */
    public abstract void pagar(double monto);

    /**
     * Retorna una descripción textual que combina el tipo de pago y el proveedor.
     * Puede ser sobrescrito por subclases para dar más detalle.
     *
     * @return Descripción como "Pago de contado con PayPal".
     */
    public String getDescripcion() {
        return "Pago con " + provider.getNombreProveedor();
    }
}

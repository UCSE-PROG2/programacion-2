package logica.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subclase concreta de la Abstracción del patrón Bridge para pago de contado.
 *
 * Implementa la lógica de un cobro único: toma el monto total y lo delega
 * directamente al proveedor de pago ({@link PaymentProvider}) sin divisiones.
 *
 * Esta clase define el "cómo se estructura el cobro" (una sola vez),
 * mientras que el proveedor define "quién lo procesa" (PayPal, MercadoPago).
 *
 * Ejemplo de uso:
 * <pre>
 *   PaymentProcessor procesador = new PagoContadoProcessor(new PaypalProvider());
 *   procesador.pagar(15000.0);
 *   // → Paga $15.000 de contado a través de PayPal
 * </pre>
 */
public class PagoContadoProcessor extends PaymentProcessor {

    /**
     * Logger para registrar el inicio y flujo del procesamiento de pago de contado.
     * El nombre del logger en los logs será "PagoContadoProcessor", permitiendo
     * que los estudiantes identifiquen en qué capa/clase ocurrió cada evento.
     */
    private static final Logger logger = LoggerFactory.getLogger(PagoContadoProcessor.class);

    /**
     * Constructor que recibe el proveedor de pago a usar.
     * Delega al constructor de PaymentProcessor para establecer el puente.
     *
     * @param provider Proveedor concreto (PaypalProvider o MercadoPagoProvider).
     */
    public PagoContadoProcessor(PaymentProvider provider) {
        super(provider);
    }

    /**
     * Procesa el pago del monto completo en un único cobro.
     *
     * El log INFO marca el inicio del procesamiento (evento de negocio relevante).
     * Luego delega al proveedor a través del puente — ahí se generan los logs
     * propios del proveedor (PaypalProvider o MercadoPagoProvider).
     *
     * @param monto Monto total de la orden a cobrar.
     */
    @Override
    public void pagar(double monto) {
        logger.info("Iniciando pago de contado — proveedor: {}, monto: ${}",
                provider.getNombreProveedor(), String.format("%.2f", monto));

        // Delega al proveedor — el "puente" en acción
        // A partir de aquí, los logs los genera el PaymentProvider concreto
        provider.processPayment(monto);
    }

    @Override
    public String getDescripcion() {
        return "Pago de contado con " + provider.getNombreProveedor();
    }
}

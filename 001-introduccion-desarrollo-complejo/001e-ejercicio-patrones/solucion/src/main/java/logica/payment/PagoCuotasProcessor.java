package logica.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subclase concreta de la Abstracción del patrón Bridge para pago en cuotas.
 *
 * Implementa la lógica de un cobro dividido: toma el monto total, lo divide
 * en la cantidad de cuotas especificada, muestra el valor por cuota, y delega
 * el cobro total al proveedor ({@link PaymentProvider}).
 *
 * Esto demuestra el poder del Bridge: podemos crear una nueva variante de
 * procesamiento (cuotas) sin tocar PaypalProvider ni MercadoPagoProvider.
 * Y podemos agregar nuevos proveedores sin tocar PagoContadoProcessor ni esta clase.
 *
 * Ejemplo de uso:
 * <pre>
 *   PaymentProcessor procesador = new PagoCuotasProcessor(new MercadoPagoProvider(), 6);
 *   procesador.pagar(12000.0);
 *   // → Paga en 6 cuotas de $2.000 a través de MercadoPago
 * </pre>
 */
public class PagoCuotasProcessor extends PaymentProcessor {

    /**
     * Logger para registrar el detalle de la división en cuotas y el procesamiento.
     */
    private static final Logger logger = LoggerFactory.getLogger(PagoCuotasProcessor.class);

    /** Número de cuotas en que se divide el pago. */
    private final int cantidadCuotas;

    /**
     * Constructor que recibe el proveedor y la cantidad de cuotas.
     *
     * @param provider       Proveedor concreto (PaypalProvider o MercadoPagoProvider).
     * @param cantidadCuotas Número de cuotas (debe ser mayor a 0).
     * @throws IllegalArgumentException si cantidadCuotas es menor o igual a 0.
     */
    public PagoCuotasProcessor(PaymentProvider provider, int cantidadCuotas) {
        super(provider);
        if (cantidadCuotas <= 0) {
            throw new IllegalArgumentException("La cantidad de cuotas debe ser mayor a 0.");
        }
        this.cantidadCuotas = cantidadCuotas;
    }

    /**
     * Procesa el pago dividido en cuotas.
     *
     * Calcula y loguea el valor de cada cuota (log INFO para que sea visible
     * por defecto). Luego delega al proveedor el cobro del monto total.
     * Los logs del proveedor se generan a continuación en PaypalProvider o MercadoPagoProvider.
     *
     * @param monto Monto total de la orden a cobrar.
     */
    @Override
    public void pagar(double monto) {
        double montoPorCuota = monto / cantidadCuotas;

        logger.info("Iniciando pago en cuotas — proveedor: {}, {} cuotas de ${} (total: ${})",
                provider.getNombreProveedor(),
                cantidadCuotas,
                String.format("%.2f", montoPorCuota),
                String.format("%.2f", monto));

        // Delega al proveedor — el "puente" en acción
        provider.processPayment(monto);
    }

    @Override
    public String getDescripcion() {
        return "Pago en " + cantidadCuotas + " cuotas con " + provider.getNombreProveedor();
    }
}

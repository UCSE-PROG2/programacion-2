package logica.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación concreta del patrón Bridge para el proveedor de pagos PayPal.
 *
 * Esta clase representa la integración con la pasarela de pago de PayPal.
 * En un sistema real, aquí iría el código que llama a la API REST de PayPal
 * con las credenciales del comercio y los datos de la transacción.
 *
 * En el contexto del Bridge, esta clase es una "Implementación Concreta":
 * puede ser combinada con cualquier subclase de {@link PaymentProcessor}
 * (PagoContadoProcessor o PagoCuotasProcessor) sin modificar ninguna de ellas.
 */
public class PaypalProvider implements PaymentProvider {

    /**
     * Logger para registrar cada paso del procesamiento de pago con PayPal.
     * Nivel DEBUG para los detalles de conexión, INFO para la confirmación final.
     */
    private static final Logger logger = LoggerFactory.getLogger(PaypalProvider.class);

    /**
     * Simula el procesamiento de un pago a través de la pasarela PayPal.
     *
     * Los logs en DEBUG muestran el flujo técnico interno (conexión, envío de datos),
     * mientras que el log INFO marca el evento de negocio relevante: pago aprobado.
     * En producción, los logs DEBUG se desactivarían en el entorno productivo
     * para no llenar los logs con información redundante.
     *
     * @param amount Monto total a cobrar.
     */
    @Override
    public void processPayment(double amount) {
        logger.debug("Iniciando conexión con la pasarela de PayPal...");
        logger.debug("Enviando solicitud de cobro por ${} a PayPal", String.format("%.2f", amount));

        // Simula la respuesta de la API de PayPal con un ID de transacción "único"
        long transactionId = System.currentTimeMillis() % 100000;
        logger.info("Pago aprobado por PayPal — monto: ${}, transacción ID: PP-{}",
                String.format("%.2f", amount), transactionId);
    }

    @Override
    public String getNombreProveedor() {
        return "PayPal";
    }
}

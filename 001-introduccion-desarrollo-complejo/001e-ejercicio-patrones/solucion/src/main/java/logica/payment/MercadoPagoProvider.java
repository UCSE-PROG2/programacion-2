package logica.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación concreta del patrón Bridge para el proveedor de pagos MercadoPago.
 *
 * Representa la integración con la plataforma de pagos MercadoPago.
 * En un sistema real, aquí iría el código que usa el SDK de MercadoPago para
 * crear una preferencia de pago, generar un QR o procesar una tarjeta.
 *
 * Al igual que {@link PaypalProvider}, esta clase implementa {@link PaymentProvider}
 * y puede ser usada con cualquier {@link PaymentProcessor} sin cambios adicionales.
 * Este intercambio en tiempo de ejecución es la esencia del patrón Bridge.
 */
public class MercadoPagoProvider implements PaymentProvider {

    /**
     * Logger para registrar el flujo de pago con MercadoPago.
     * Nivel DEBUG para detalles técnicos, INFO para confirmación del evento de negocio.
     */
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoProvider.class);

    /**
     * Simula el procesamiento de un pago a través de MercadoPago.
     *
     * En producción, este método usaría el SDK de MercadoPago para crear
     * un pago, manejar webhooks de confirmación y registrar la transacción.
     * Los logs DEBUG permitirían diagnosticar fallas en la integración.
     *
     * @param amount Monto total a cobrar.
     */
    @Override
    public void processPayment(double amount) {
        logger.debug("Iniciando conexión con la API de MercadoPago...");
        logger.debug("Generando QR de pago para monto: ${}", String.format("%.2f", amount));

        long operationId = System.currentTimeMillis() % 100000;
        logger.info("Pago acreditado por MercadoPago — monto: ${}, operación ID: MP-{}",
                String.format("%.2f", amount), operationId);
    }

    @Override
    public String getNombreProveedor() {
        return "MercadoPago";
    }
}

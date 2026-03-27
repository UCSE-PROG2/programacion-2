package logica.order;

import datos.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Servicio de lógica de negocio para la gestión de órdenes de compra.
 *
 * Esta clase actúa como capa de servicio entre la capa de presentación (Main)
 * y la capa de datos (Order). Se encarga de:
 *   - Recibir y almacenar órdenes en memoria.
 *   - Ejecutar el pago de una orden usando el Bridge configurado.
 *   - Proveer acceso a las órdenes almacenadas.
 *
 * En este ejercicio, las órdenes se construyen con el patrón Builder (en Order.Builder)
 * y luego se registran aquí. El OrderService no construye órdenes: sólo las gestiona.
 */
public class OrderService {

    /**
     * Logger para registrar el ciclo de vida completo de cada orden:
     * recepción, procesamiento de pago y confirmación.
     */
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    /** Almacenamiento en memoria de todas las órdenes confirmadas. */
    private final List<Order> ordenes = new ArrayList<>();

    /**
     * Registra una orden confirmada en el sistema y ejecuta su pago.
     *
     * El flujo completo con sus logs es:
     *   1. [INFO]  Recepción de la orden para el cliente.
     *   2. [DEBUG] Detalle de productos y monto total.
     *   3. [INFO]  Delegación del pago al PaymentProcessor (Bridge).
     *              → A partir de aquí los logs los generan PagoContadoProcessor,
     *                PagoCuotasProcessor, PaypalProvider o MercadoPagoProvider.
     *   4. [INFO]  Confirmación de que la orden fue almacenada.
     *
     * @param order La orden construida con Order.Builder y lista para procesar.
     */
    public void confirmarOrden(Order order) {
        logger.info("Recibiendo orden de '{}' — {} producto(s), total: ${}",
                order.getCliente(),
                order.getProductos().size(),
                String.format("%.2f", order.calcularTotal()));

        logger.debug("Detalle de productos en la orden:");
        order.getProductos().forEach(p ->
                logger.debug("  → {} | ${}", p.getNombre(), String.format("%.2f", p.getPrecio()))
        );

        if (order.getDescuento() > 0) {
            logger.debug("Descuento aplicado: {}%", order.getDescuento());
        }

        logger.info("Procesando pago de la orden — método: {}", order.getMetodoPago().getDescripcion());
        order.procesarPago();

        ordenes.add(order);
        logger.info("Orden confirmada y almacenada. Total de órdenes en sistema: {}", ordenes.size());
    }

    /**
     * Retorna una copia inmutable de todas las órdenes registradas.
     *
     * @return Lista no modificable de todas las órdenes del sistema.
     */
    public List<Order> obtenerTodasLasOrdenes() {
        return Collections.unmodifiableList(ordenes);
    }

    /**
     * Retorna la cantidad total de órdenes registradas en el sistema.
     *
     * @return Número de órdenes confirmadas.
     */
    public int cantidadOrdenes() {
        return ordenes.size();
    }
}

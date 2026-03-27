package logica.shipping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contexto del patrón Strategy para el cálculo de costo de envío.
 *
 * En el patrón Strategy, el Contexto es la clase que el cliente usa directamente.
 * El contexto mantiene una referencia a una {@link ShippingStrategy} y delega
 * el cálculo a ella, sin conocer cuál estrategia específica está usando.
 *
 * Responsabilidades del Contexto:
 *   1. Mantener la referencia a la estrategia activa.
 *   2. Permitir cambiar la estrategia en tiempo de ejecución (setStrategy).
 *   3. Delegar el cálculo a la estrategia actual (calcularCosto).
 *
 * Flujo típico en este sistema:
 *   1. El usuario selecciona el tipo de envío (Avión, Barco o Camión).
 *   2. Se instancia la estrategia correspondiente.
 *   3. Se la inyecta en ShippingContext (por constructor o por setStrategy).
 *   4. Se llama a calcularCosto() para obtener el precio del envío.
 */
public class ShippingContext {

    /**
     * Logger para registrar cambios de estrategia y resultados de cálculo.
     * Nivel DEBUG para los cálculos intermedios, INFO para el resultado final.
     */
    private static final Logger logger = LoggerFactory.getLogger(ShippingContext.class);

    /** La estrategia de envío actualmente seleccionada. */
    private ShippingStrategy strategy;

    /**
     * Constructor que recibe la estrategia inicial.
     * La estrategia puede cambiarse después con {@link #setStrategy(ShippingStrategy)}.
     *
     * @param strategy La estrategia de envío a usar inicialmente.
     */
    public ShippingContext(ShippingStrategy strategy) {
        this.strategy = strategy;
        logger.debug("ShippingContext creado con estrategia inicial: {}", strategy.getNombreEnvio());
    }

    /**
     * Permite cambiar la estrategia de envío en tiempo de ejecución.
     *
     * Esta capacidad de intercambio es la característica central del patrón Strategy.
     * Por ejemplo, si el usuario cambia de "Avión" a "Camión" antes de confirmar,
     * basta con llamar setStrategy(new TruckShipping()) y el contexto usa la nueva.
     * El log INFO registra el cambio para que sea visible en el flujo de la aplicación.
     *
     * @param strategy La nueva estrategia de envío a aplicar.
     */
    public void setStrategy(ShippingStrategy strategy) {
        logger.info("Estrategia de envío cambiada: {} → {}",
                this.strategy.getNombreEnvio(), strategy.getNombreEnvio());
        this.strategy = strategy;
    }

    /**
     * Delega el cálculo del costo de envío a la estrategia activa.
     *
     * Este método no sabe ni le importa si la estrategia es AirShipping,
     * SeaShipping o TruckShipping. Solo sabe que la estrategia tiene el método
     * calculateCost() y lo invoca con los parámetros recibidos.
     *
     * @param peso      Peso del paquete en kg.
     * @param distancia Distancia de envío en km.
     * @return Costo del envío calculado por la estrategia activa.
     */
    public double calcularCosto(double peso, double distancia) {
        double costo = strategy.calculateCost(peso, distancia);
        logger.info("Costo de envío calculado — estrategia: {}, peso: {} kg, distancia: {} km → ${}",
                strategy.getNombreEnvio(),
                String.format("%.1f", peso),
                String.format("%.0f", distancia),
                String.format("%.2f", costo));
        return costo;
    }

    /**
     * Retorna la estrategia actualmente configurada.
     *
     * @return La estrategia de envío activa.
     */
    public ShippingStrategy getStrategy() {
        return strategy;
    }

    /**
     * Retorna una descripción legible del tipo de envío seleccionado.
     *
     * @return Texto con el nombre de la estrategia activa (ej: "Envío por Camión").
     */
    public String getDescripcion() {
        return "Envío por " + strategy.getNombreEnvio();
    }
}

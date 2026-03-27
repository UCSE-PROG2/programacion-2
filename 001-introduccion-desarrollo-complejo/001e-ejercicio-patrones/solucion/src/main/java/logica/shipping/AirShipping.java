package logica.shipping;

/**
 * Estrategia concreta de envío por Avión — implementa el patrón Strategy.
 *
 * El envío aéreo es el más rápido y costoso. Tiene una tarifa alta por
 * kilogramo (por el límite de peso en aeronaves) y también cobra por distancia.
 *
 * Fórmula:
 *   costo = (peso × TARIFA_POR_KG) + (distancia × TARIFA_POR_KM)
 *
 * Al encapsular esta fórmula en su propia clase, modificarla en el futuro
 * (por ejemplo, agregar un recargo por combustible) sólo requiere editar
 * esta clase, sin tocar {@link ShippingContext} ni el resto del sistema.
 */
public class AirShipping implements ShippingStrategy {

    /** Costo por kilogramo transportado. El avión cobra más por peso limitado. */
    private static final double TARIFA_POR_KG = 5.0;

    /** Costo por kilómetro de distancia. Las rutas aéreas son más directas. */
    private static final double TARIFA_POR_KM = 0.05;

    /**
     * Calcula el costo del envío aéreo según peso y distancia.
     *
     * Ejemplo: 10 kg a 500 km = (10 × 5.0) + (500 × 0.05) = 50 + 25 = $75.00
     *
     * @param peso      Peso del paquete en kg.
     * @param distancia Distancia en km.
     * @return Costo del envío en pesos.
     */
    @Override
    public double calculateCost(double peso, double distancia) {
        return (peso * TARIFA_POR_KG) + (distancia * TARIFA_POR_KM);
    }

    @Override
    public String getNombreEnvio() {
        return "Avión";
    }
}

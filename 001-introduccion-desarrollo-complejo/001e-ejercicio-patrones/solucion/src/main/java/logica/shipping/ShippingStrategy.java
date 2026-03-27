package logica.shipping;

/**
 * Interfaz que define el patrón Strategy para el cálculo de costo de envío.
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  PATRÓN STRATEGY                                                    │
 * │                                                                     │
 * │  Problema que resuelve: el cálculo del costo de envío varía según  │
 * │  el medio de transporte. Si pusiéramos toda la lógica en un único  │
 * │  método con ifs/switches, sería difícil agregar nuevos medios      │
 * │  sin romper el código existente.                                    │
 * │                                                                     │
 * │  Solución: encapsular cada algoritmo de cálculo en su propia clase │
 * │  que implementa esta interfaz. El contexto ({@link ShippingContext})│
 * │  usa la interfaz y puede intercambiar la estrategia en tiempo de   │
 * │  ejecución sin conocer los detalles de cada algoritmo.             │
 * │                                                                     │
 * │  Implementaciones:                                                  │
 * │    AirShipping   → más caro, ideal para peso bajo y larga distancia│
 * │    SeaShipping   → más barato, ideal para peso alto y larga dist.  │
 * │    TruckShipping → precio intermedio, ideal para distancias medias │
 * └─────────────────────────────────────────────────────────────────────┘
 */
public interface ShippingStrategy {

    /**
     * Calcula el costo de envío basándose en el peso del paquete y la distancia.
     * Cada implementación aplica su propia fórmula de cálculo.
     *
     * @param peso     Peso del paquete en kilogramos.
     * @param distancia Distancia de envío en kilómetros.
     * @return Costo del envío en pesos argentinos.
     */
    double calculateCost(double peso, double distancia);

    /**
     * Retorna el nombre del medio de envío para mostrar en logs y resúmenes.
     *
     * @return Nombre legible (ej: "Avión", "Barco", "Camión").
     */
    String getNombreEnvio();
}

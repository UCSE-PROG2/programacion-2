package logica.shipping;

/**
 * Estrategia concreta de envío por Barco — implementa el patrón Strategy.
 *
 * El envío marítimo es el más económico para grandes volúmenes y largas distancias,
 * aunque el más lento. Tiene la tarifa por kilogramo más baja de las tres opciones.
 *
 * Fórmula:
 *   costo = (peso × TARIFA_POR_KG) + (distancia × TARIFA_POR_KM)
 *
 * La fórmula es la misma que en las otras estrategias, pero las constantes
 * son diferentes. Esto es un punto clave del patrón Strategy: la estructura
 * del algoritmo puede ser la misma, pero cada estrategia tiene sus propios
 * parámetros y puede tener lógica completamente distinta si fuera necesario.
 */
public class SeaShipping implements ShippingStrategy {

    /** Costo por kilogramo. El barco es el más económico para cargas pesadas. */
    private static final double TARIFA_POR_KG = 1.5;

    /** Costo por kilómetro. Las rutas marítimas son más largas pero más baratas. */
    private static final double TARIFA_POR_KM = 0.01;

    /**
     * Calcula el costo del envío marítimo según peso y distancia.
     *
     * Ejemplo: 10 kg a 500 km = (10 × 1.5) + (500 × 0.01) = 15 + 5 = $20.00
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
        return "Barco";
    }
}

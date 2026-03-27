package logica.shipping;

/**
 * Estrategia concreta de envío por Camión — implementa el patrón Strategy.
 *
 * El envío terrestre por camión es la opción intermedia: más rápido que el barco
 * y más barato que el avión. Es ideal para distancias medias y cargas moderadas.
 *
 * Fórmula:
 *   costo = (peso × TARIFA_POR_KG) + (distancia × TARIFA_POR_KM)
 *
 * Esta estrategia, al igual que las demás, puede ser intercambiada en el
 * {@link ShippingContext} en tiempo de ejecución llamando a {@code setStrategy()}.
 * Esto permite que el usuario cambie el tipo de envío después de haberlo
 * seleccionado inicialmente, sin ningún cambio en el código cliente.
 */
public class TruckShipping implements ShippingStrategy {

    /** Costo por kilogramo. Intermedio entre avión y barco. */
    private static final double TARIFA_POR_KG = 2.5;

    /** Costo por kilómetro. El camión es eficiente en rutas terrestres. */
    private static final double TARIFA_POR_KM = 0.03;

    /**
     * Calcula el costo del envío por camión según peso y distancia.
     *
     * Ejemplo: 10 kg a 500 km = (10 × 2.5) + (500 × 0.03) = 25 + 15 = $40.00
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
        return "Camión";
    }
}

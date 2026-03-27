package logica.payment;

/**
 * Interfaz que define la "Implementación" del patrón Bridge para pagos.
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  PATRÓN BRIDGE — Lado de Implementación                            │
 * │                                                                     │
 * │  El Bridge separa una abstracción de su implementación, de modo    │
 * │  que ambas puedan variar de forma independiente.                   │
 * │                                                                     │
 * │  En este sistema:                                                   │
 * │    ABSTRACCIÓN: PaymentProcessor (cómo se estructura el cobro)     │
 * │      ├── PagoContadoProcessor (cobro en un pago)                   │
 * │      └── PagoCuotasProcessor  (cobro en cuotas)                    │
 * │                                                                     │
 * │    IMPLEMENTACIÓN: PaymentProvider (quién procesa el pago)         │
 * │      ├── PaypalProvider     (conecta con la API de PayPal)         │
 * │      └── MercadoPagoProvider (conecta con la API de MercadoPago)  │
 * │                                                                     │
 * │  El "puente" es la referencia que PaymentProcessor tiene hacia     │
 * │  PaymentProvider. Esto permite combinar libremente:                │
 * │    Contado + PayPal, Contado + MercadoPago,                        │
 * │    Cuotas  + PayPal, Cuotas  + MercadoPago                         │
 * │  Sin crear una subclase por cada combinación posible.              │
 * └─────────────────────────────────────────────────────────────────────┘
 *
 * Al agregar un nuevo proveedor (ej: Stripe), solo se crea StripeProvider
 * implementando esta interfaz. No se modifica PaymentProcessor ni sus subclases.
 */
public interface PaymentProvider {

    /**
     * Procesa el pago del monto indicado a través del proveedor concreto.
     * Cada implementación se conecta con su pasarela de pago específica.
     *
     * @param amount El monto total a cobrar en pesos argentinos.
     */
    void processPayment(double amount);

    /**
     * Retorna el nombre del proveedor para mostrar en logs y resúmenes de orden.
     *
     * @return Nombre legible del proveedor (ej: "PayPal", "MercadoPago").
     */
    String getNombreProveedor();
}

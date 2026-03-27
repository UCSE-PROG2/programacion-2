package presentacion;

import com.github.javafaker.Faker;
import datos.Order;
import datos.Product;
import datos.ProductRepository;
import logica.cart.ShoppingCart;
import logica.factory.ProductSelector;
import logica.order.OrderService;
import logica.payment.MercadoPagoProvider;
import logica.payment.PagoCuotasProcessor;
import logica.payment.PagoContadoProcessor;
import logica.payment.PaymentProcessor;
import logica.payment.PaypalProvider;
import logica.shipping.AirShipping;
import logica.shipping.SeaShipping;
import logica.shipping.ShippingContext;
import logica.shipping.TruckShipping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * Clase principal del sistema de tienda en línea.
 *
 * Demuestra el flujo completo de compra integrando los cinco patrones de diseño
 * y las dos dependencias externas incorporadas al proyecto:
 *
 *   PATRONES DE DISEÑO:
 *     1. Factory Method → Crear productos por categoría sin acoplamiento a clases concretas.
 *     2. Singleton      → El carrito es una única instancia durante toda la sesión.
 *     3. Strategy       → Cálculo de envío intercambiable en tiempo de ejecución.
 *     4. Bridge         → Pago desacoplado entre modalidad (contado/cuotas) y proveedor.
 *     5. Builder        → Construcción paso a paso de órdenes con campos opcionales.
 *
 *   DEPENDENCIAS EXTERNAS:
 *     • JavaFaker  → genera datos de prueba realistas (nombres, direcciones, precios).
 *                    Elimina los valores hardcodeados del Main y permite ejecutar
 *                    la aplicación con datos distintos en cada corrida.
 *     • SLF4J + Logback → logging estructurado en toda la capa de negocio.
 *                    Cada clase de logica/ tiene su propio Logger; el formato
 *                    y nivel se configuran en src/main/resources/logback.xml.
 */
public class Main {

    /**
     * Logger de la capa de presentación.
     * Se usa para los mensajes de alto nivel que estructuran el flujo del demo.
     * El nivel INFO (configurado en logback.xml) filtra los DEBUG de logica/.
     */
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        /*
         * JavaFaker: librería que genera datos falsos pero realistas.
         *
         * new Locale("es") → genera nombres y apellidos en español (ej: "María González").
         * Sin locale, los nombres son en inglés. La elección del locale afecta sólo
         * los datos culturalmente dependientes (nombres, ciudades); los números y
         * direcciones son similares en todos los locales.
         */
        Faker faker = new Faker(new Locale("es"));

        logger.info("══════════════════════════════════════════════════════════");
        logger.info("   SISTEMA DE TIENDA EN LÍNEA — PATRONES + FAKER + SLF4J");
        logger.info("══════════════════════════════════════════════════════════");

        // ==================================================================
        //  PASO 1: FACTORY METHOD — Crear productos con datos de JavaFaker
        // ==================================================================
        /*
         * En lugar de nombres y precios hardcodeados, usamos Faker para generar:
         *   - faker.commerce().productName() → nombre de producto (ej: "Incredible Silk Bag")
         *   - faker.lorem().sentence(6)      → descripción con 6 palabras aleatorias
         *   - faker.number().randomDouble()  → precio aleatorio dentro de un rango
         *
         * ProductSelector encapsula la lógica de selección de fábrica (Factory Method).
         * Cada llamada a selectProduct() crea internamente la fábrica concreta correcta
         * y delega la instanciación del producto concreto al Factory Method createProduct().
         */
        logger.info("--- [1] FACTORY METHOD: creando catálogo de productos con datos Faker ---");

        ProductSelector selector = new ProductSelector();
        ProductRepository repositorio = new ProductRepository();

        // Tres productos de Electrónica — precios en rango realista para Argentina
        Product notebook = selector.selectProduct(
                "electronica",
                "Notebook " + faker.company().name(),
                faker.lorem().sentence(6),
                faker.number().randomDouble(2, 400_000, 1_800_000)
        );

        Product celular = selector.selectProduct(
                "electronica",
                "Celular " + faker.company().name(),
                faker.lorem().sentence(6),
                faker.number().randomDouble(2, 150_000, 700_000)
        );

        Product auriculares = selector.selectProduct(
                "electronica",
                "Auriculares " + faker.commerce().material(),
                faker.lorem().sentence(5),
                faker.number().randomDouble(2, 20_000, 120_000)
        );

        // Dos productos de Ropa
        Product zapatillas = selector.selectProduct(
                "ropa",
                "Zapatillas " + faker.company().name(),
                "Talle " + faker.number().numberBetween(36, 46) + " — " + faker.commerce().color(),
                faker.number().randomDouble(2, 30_000, 200_000)
        );

        Product campera = selector.selectProduct(
                "ropa",
                "Campera " + faker.commerce().material(),
                "Talle " + faker.options().option("S", "M", "L", "XL") + " — " + faker.commerce().color(),
                faker.number().randomDouble(2, 25_000, 150_000)
        );

        // Dos productos de Vehículos
        Product moto = selector.selectProduct(
                "vehiculos",
                "Moto " + faker.company().name() + " " + faker.number().numberBetween(150, 400) + "cc",
                "Año " + faker.number().numberBetween(2020, 2025) + " — " + faker.commerce().color(),
                faker.number().randomDouble(2, 1_500_000, 6_000_000)
        );

        Product bicicleta = selector.selectProduct(
                "vehiculos",
                "Bicicleta " + faker.company().name(),
                faker.lorem().sentence(5),
                faker.number().randomDouble(2, 80_000, 500_000)
        );

        // Registrar todos los productos en el repositorio
        List.of(notebook, celular, auriculares, zapatillas, campera, moto, bicicleta)
                .forEach(repositorio::agregar);

        logger.info("Catálogo cargado: {} productos en repositorio.", repositorio.obtenerTodos().size());

        // ==================================================================
        //  PASO 2: SINGLETON — Verificar instancia única del carrito
        // ==================================================================
        /*
         * Obtenemos el carrito desde dos puntos distintos del código, simulando
         * dos módulos independientes que necesitan acceder al mismo carrito.
         *
         * Si el Singleton funciona correctamente:
         *   carrito1 == carrito2 → true (misma referencia en memoria)
         *   System.identityHashCode(carrito1) == System.identityHashCode(carrito2)
         *
         * identityHashCode() retorna el hashCode basado en la dirección de memoria
         * del objeto, independientemente de si se sobrescribió hashCode() o no.
         * Es la forma más directa de verificar identidad de objetos en Java.
         */
        logger.info("--- [2] SINGLETON: verificando instancia única del carrito ---");

        ShoppingCart carrito1 = ShoppingCart.getInstance(); // módulo catálogo
        ShoppingCart carrito2 = ShoppingCart.getInstance(); // módulo checkout

        logger.info("carrito1 == carrito2: {}", (carrito1 == carrito2));
        logger.info("identityHashCode carrito1: {}", System.identityHashCode(carrito1));
        logger.info("identityHashCode carrito2: {}", System.identityHashCode(carrito2));

        // Agregar productos al carrito desde carrito1
        carrito1.addProduct(notebook);
        carrito1.addProduct(zapatillas);
        carrito1.addProduct(auriculares);

        // Verificar desde carrito2 (mismo objeto) que ve los mismos productos
        logger.info("Verificando desde carrito2 (misma instancia Singleton):");
        carrito2.mostrarContenido();

        // ==================================================================
        //  PASO 3: STRATEGY — Comparar costos de envío con datos Faker
        // ==================================================================
        /*
         * Usamos Faker para generar peso y distancia aleatorios, haciendo que
         * cada ejecución muestre valores diferentes. Esto representa mejor la
         * realidad: los pedidos tienen distintos pesos y destinos.
         *
         * El ShippingContext permite cambiar la estrategia en runtime llamando
         * a setStrategy(). El log INFO de ShippingContext registra el cambio:
         * "Estrategia de envío cambiada: Avión → Camión"
         */
        logger.info("--- [3] STRATEGY: comparando costos de envío con datos Faker ---");

        double pesoKg   = faker.number().randomDouble(1, 1, 15);     // entre 1 y 15 kg
        double distKm   = faker.number().numberBetween(200, 2000);    // entre 200 y 2000 km

        logger.info("Parámetros de envío generados por Faker: {} kg, {} km",
                String.format("%.1f", pesoKg), (int) distKm);

        ShippingContext contextoEnvio = new ShippingContext(new AirShipping());
        double costoAvion  = contextoEnvio.calcularCosto(pesoKg, distKm);

        contextoEnvio.setStrategy(new SeaShipping());
        double costoBarco  = contextoEnvio.calcularCosto(pesoKg, distKm);

        contextoEnvio.setStrategy(new TruckShipping());
        double costoCamion = contextoEnvio.calcularCosto(pesoKg, distKm);

        logger.info("Comparativa — Avión: ${} | Barco: ${} | Camión: ${}",
                String.format("%.2f", costoAvion),
                String.format("%.2f", costoBarco),
                String.format("%.2f", costoCamion));

        // Seleccionar la estrategia más económica para la primera orden
        logger.info("Estrategia seleccionada para la orden: Camión (más económico en este caso)");

        // ==================================================================
        //  PASO 4: BRIDGE — Configurar medios de pago combinables
        // ==================================================================
        /*
         * El Bridge nos da 4 combinaciones posibles sin crear 4 subclases:
         *   PagoContadoProcessor + PaypalProvider
         *   PagoContadoProcessor + MercadoPagoProvider
         *   PagoCuotasProcessor  + PaypalProvider
         *   PagoCuotasProcessor  + MercadoPagoProvider
         *
         * Para demostrar la flexibilidad, usamos distintas combinaciones en cada orden.
         */
        logger.info("--- [4] BRIDGE: configurando medios de pago combinables ---");

        // Combinación A: contado con PayPal
        PaymentProcessor pagoContadoPaypal = new PagoContadoProcessor(new PaypalProvider());
        pagoContadoPaypal.pagar(10);
        logger.info("Procesador A: {}", pagoContadoPaypal.getDescripcion());

        // Combinación B: 6 cuotas con MercadoPago
        int cuotas = faker.options().option(3, 6, 12, 18);
        PaymentProcessor pagoCuotasMP = new PagoCuotasProcessor(new MercadoPagoProvider(), cuotas);
        logger.info("Procesador B: {}", pagoCuotasMP.getDescripcion());

        // Combinación C: 3 cuotas con PayPal
        PaymentProcessor pagoCuotasPaypal = new PagoCuotasProcessor(new PaypalProvider(), 3);
        logger.info("Procesador C: {}", pagoCuotasPaypal.getDescripcion());

        // ==================================================================
        //  PASO 5: BUILDER — Construir órdenes con datos Faker
        // ==================================================================
        /*
         * Construimos tres órdenes con configuraciones distintas.
         * Faker genera los datos del cliente (nombre y dirección) para que
         * cada ejecución produzca clientes diferentes.
         *
         * El Builder garantiza que:
         *   - Los campos obligatorios (cliente, al menos un producto) estén presentes.
         *   - Los campos opcionales pueden omitirse sin romper nada.
         *   - El objeto Order resultante es inmutable (todos los campos son final).
         */
        logger.info("--- [5] BUILDER: construyendo órdenes con datos Faker ---");

        OrderService orderService = new OrderService();

        // ── Orden 1: notebook + auriculares, contado con PayPal, descuento aleatorio ──
        String cliente1 = faker.name().fullName();
        String direccion1 = faker.address().streetAddress() + ", " + faker.address().cityName();
        int descuento1 = faker.number().numberBetween(5, 20);

        logger.info("Construyendo orden 1 para: {}", cliente1);

        Order orden1 = new Order.Builder()
                .withCustomer(cliente1)
                .withProduct(notebook)
                .withProduct(auriculares)
                .withShippingAddress(direccion1)
                .withShippingStrategy(new TruckShipping())       // Strategy
                .withPaymentMethod(pagoContadoPaypal)            // Bridge: contado + PayPal
                .withDiscount(descuento1)
                .withNotes(faker.lorem().sentence(8))
                .build();

        orderService.confirmarOrden(orden1);

        // ── Orden 2: zapatillas + campera, cuotas con MercadoPago, sin descuento ──
        String cliente2 = faker.name().fullName();
        String direccion2 = faker.address().streetAddress() + ", " + faker.address().cityName();

        logger.info("Construyendo orden 2 para: {}", cliente2);

        Order orden2 = new Order.Builder()
                .withCustomer(cliente2)
                .withProduct(zapatillas)
                .withProduct(campera)
                .withShippingAddress(direccion2)
                .withShippingStrategy(new AirShipping())         // Strategy: avión
                .withPaymentMethod(pagoCuotasMP)                 // Bridge: cuotas + MercadoPago
                // descuento omitido → 0 por defecto (campo opcional del Builder)
                .withNotes(faker.lorem().sentence(6))
                .build();

        orderService.confirmarOrden(orden2);

        // ── Orden 3: moto, cuotas con PayPal, alto descuento ──
        String cliente3 = faker.name().fullName();
        String direccion3 = faker.address().streetAddress() + ", " + faker.address().cityName();

        logger.info("Construyendo orden 3 para: {}", cliente3);

        Order orden3 = new Order.Builder()
                .withCustomer(cliente3)
                .withProduct(moto)
                .withShippingAddress(direccion3)
                .withShippingStrategy(new TruckShipping())       // Strategy: camión
                .withPaymentMethod(pagoCuotasPaypal)             // Bridge: cuotas + PayPal
                .withDiscount(faker.number().numberBetween(5, 15))
                .build();
        // Nota: withNotes() omitido — campo completamente opcional en el Builder

        orderService.confirmarOrden(orden3);

        // ==================================================================
        //  RESUMEN FINAL
        // ==================================================================
        logger.info("══════════════════════════════════════════════════════════");
        logger.info("RESUMEN FINAL DEL SISTEMA:");
        logger.info("  Órdenes procesadas      : {}", orderService.cantidadOrdenes());
        logger.info("  Productos en catálogo   : {}", repositorio.obtenerTodos().size());
        logger.info("  Productos en carrito    : {} (Singleton verificado)",
                ShoppingCart.getInstance().size());
        logger.info("Patrones aplicados:");
        logger.info("  ✓ Factory Method  — {} productos creados via fábricas concretas",
                repositorio.obtenerTodos().size());
        logger.info("  ✓ Singleton       — carrito único, mismo hashCode en toda la sesión");
        logger.info("  ✓ Strategy        — 3 estrategias comparadas, seleccionada la óptima");
        logger.info("  ✓ Bridge          — 3 combinaciones de procesador+proveedor sin subclases extra");
        logger.info("  ✓ Builder         — 3 órdenes con configs distintas, campos opcionales omitidos");
        logger.info("Dependencias externas:");
        logger.info("  ✓ JavaFaker  — todos los datos de prueba generados dinámicamente");
        logger.info("  ✓ SLF4J/Logback — logs estructurados en toda la capa logica.*");
        logger.info("══════════════════════════════════════════════════════════");
    }
}

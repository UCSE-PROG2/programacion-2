package funcional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Sección 11 del material: Ejercicios resueltos
public class EjerciciosResueltos {

    record Producto(String nombre, String categoria, double precio) {}

    static void demo() {
        header("11. EJERCICIOS RESUELTOS");

        ejercicio1();
        ejercicio2();
        ejercicio3();
        ejercicio4();
        ejercicio5();
    }

    // ─── Ejercicio 1: números pares al cuadrado, suma ───────────────────────
    static void ejercicio1() {
        System.out.println("\n-- Ejercicio 1: pares al cuadrado y suma --");
        System.out.println("Lista: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]");

        int resultado = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).stream()
                .filter(n -> n % 2 == 0)          // 2, 4, 6, 8, 10
                .mapToInt(n -> n * n)              // 4, 16, 36, 64, 100
                .sum();                            // 220

        System.out.println("Resultado: " + resultado + "  (esperado: 220)");
    }

    // ─── Ejercicio 2: strings filtrados, en mayúsculas, ordenados y unidos ──
    static void ejercicio2() {
        System.out.println("\n-- Ejercicio 2: strings > 4 letras, uppercase, join --");

        String resultado = List.of("hola", "mundo", "java", "funcional", "stream", "api").stream()
                .filter(s -> s.length() > 4)                     // mundo, funcional, stream
                .map(String::toUpperCase)                        // MUNDO, FUNCIONAL, STREAM
                .sorted()                                        // FUNCIONAL, MUNDO, STREAM
                .collect(Collectors.joining(" | "));             // "FUNCIONAL | MUNDO | STREAM"

        System.out.println("Resultado : " + resultado);
        System.out.println("Esperado  : FUNCIONAL | MUNDO | STREAM");
    }

    // ─── Ejercicio 3: agrupación y estadísticas por categoría ────────────────
    static void ejercicio3() {
        System.out.println("\n-- Ejercicio 3: agrupación por categoría --");

        List<Producto> productos = List.of(
            new Producto("Laptop",      "Electrónica", 1200.0),
            new Producto("Mouse",       "Electrónica",   25.0),
            new Producto("Silla",       "Mobiliario",   350.0),
            new Producto("Escritorio",  "Mobiliario",   800.0),
            new Producto("Auriculares", "Electrónica",  150.0)
        );

        // 1. Agrupar por categoría
        Map<String, List<Producto>> porCategoria = productos.stream()
                .collect(Collectors.groupingBy(Producto::categoria));
        System.out.println("Categorías: " + porCategoria.keySet());

        // 2. Precio promedio de Electrónica
        OptionalDouble promedioElectronica = porCategoria.get("Electrónica").stream()
                .mapToDouble(Producto::precio)
                .average();
        System.out.println("Promedio Electrónica: $" + promedioElectronica.orElse(0));

        // 3. Producto más caro de Mobiliario
        Optional<Producto> masCaro = porCategoria.get("Mobiliario").stream()
                .max(Comparator.comparing(Producto::precio));
        masCaro.ifPresent(p -> System.out.println("Más caro en Mobiliario: " + p.nombre() + " $" + p.precio()));

        // 4. Map<nombre, precio> solo de Electrónica
        Map<String, Double> nombreAPrecio = porCategoria.get("Electrónica").stream()
                .collect(Collectors.toMap(Producto::nombre, Producto::precio));
        System.out.println("Electrónica nombre→precio: " + nombreAPrecio);
    }

    // ─── Ejercicio 4: Predicate + Function + pipeline completo ──────────────
    static void ejercicio4() {
        System.out.println("\n-- Ejercicio 4: Predicate y Function compuestos --");

        List<String> entrada = List.of("  java  ", "go", "  python  ", "  c  ", "kotlin");

        Function<String, String> limpiar      = String::trim;
        Function<String, String> mayusculas   = String::toUpperCase;
        Function<String, String> procesar     = limpiar.andThen(mayusculas);
        Predicate<String> masDe3Chars         = s -> s.length() > 3;

        List<String> resultado = entrada.stream()
                .map(procesar)                              // trim + toUpperCase
                .filter(masDe3Chars)                       // solo los de más de 3 chars
                .sorted(Comparator.comparingInt(String::length).reversed()) // desc por longitud
                .collect(Collectors.toList());

        System.out.println("Resultado : " + resultado);
        System.out.println("Esperado  : [PYTHON, KOTLIN, JAVA]");
    }

    // ─── Ejercicio 5: Optional encadenado sin if/else ────────────────────────
    static void ejercicio5() {
        System.out.println("\n-- Ejercicio 5: Optional encadenado --");

        // Simula findByEmail del repositorio
        String email1 = "ana@ucse.edu";
        String email2 = "desconocido@ucse.edu";

        String resultado1 = findByEmail(email1)
                .map(nombre -> nombre.toUpperCase())
                .orElse("USUARIO DESCONOCIDO");

        String resultado2 = findByEmail(email2)
                .map(String::toUpperCase)
                .orElse("USUARIO DESCONOCIDO");

        System.out.println("findByEmail(ana)          → " + resultado1);
        System.out.println("findByEmail(desconocido)  → " + resultado2);
    }

    // Simula un repositorio de usuarios
    static Optional<String> findByEmail(String email) {
        Map<String, String> db = Map.of(
            "ana@ucse.edu",   "Ana García",
            "bruno@ucse.edu", "Bruno López"
        );
        return Optional.ofNullable(db.get(email));
    }

    static void header(String titulo) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║  " + titulo);
        System.out.println("╚══════════════════════════════════════╝");
    }
}

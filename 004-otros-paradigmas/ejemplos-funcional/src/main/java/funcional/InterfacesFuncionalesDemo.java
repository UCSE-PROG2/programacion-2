package funcional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

// Sección 5 del material: Interfaces funcionales del JDK
public class InterfacesFuncionalesDemo {

    static void demo() {
        header("5. INTERFACES FUNCIONALES DEL JDK");

        demoPredicado();
        demoFunction();
        demoConsumer();
        demoSupplier();
    }

    // ─── Predicate<T>: T → boolean ─────────────────────────────────────────
    static void demoPredicado() {
        System.out.println("\n--- Predicate<T>: filtrar / validar ---");

        Predicate<String> esVacio      = String::isEmpty;
        Predicate<Integer> esMayorDe10 = n -> n > 10;
        Predicate<String> esLargo      = s -> s.length() > 5;

        System.out.println("esVacio.test(\"\")     → " + esVacio.test(""));
        System.out.println("esMayorDe10.test(15) → " + esMayorDe10.test(15));
        System.out.println("esLargo.test(\"Java\") → " + esLargo.test("Java"));

        // Composición: and, or, negate
        Predicate<String> noEsVacio     = esVacio.negate();
        Predicate<String> esLargoYlleno = esLargo.and(noEsVacio);

        System.out.println("esLargoYlleno.test(\"Funcional\") → " + esLargoYlleno.test("Funcional"));
        System.out.println("esLargoYlleno.test(\"\")           → " + esLargoYlleno.test(""));

        // Uso típico: filtrar una lista
        List<String> palabras = List.of("Java", "Funcional", "Go", "Python", "C");
        System.out.print("Palabras largas: ");
        palabras.stream().filter(esLargo).forEach(p -> System.out.print(p + " "));
        System.out.println();
    }

    // ─── Function<T, R>: T → R ─────────────────────────────────────────────
    static void demoFunction() {
        System.out.println("\n--- Function<T, R>: transformar ---");

        Function<String, Integer> longitud   = String::length;
        Function<String, String>  mayusculas = String::toUpperCase;
        Function<Integer, String> intAString = n -> "Número: " + n;

        System.out.println("longitud.apply(\"Hola\")   → " + longitud.apply("Hola"));
        System.out.println("mayusculas.apply(\"java\") → " + mayusculas.apply("java"));
        System.out.println("intAString.apply(42)      → " + intAString.apply(42));

        // Composición con andThen: primero esta, luego la siguiente
        Function<String, String> procesarNombre = mayusculas.andThen(String::trim);
        System.out.println("mayusculas.andThen(trim).apply(\"  hola  \") → "
                + procesarNombre.apply("  hola  "));

        // Composición con compose: primero la siguiente, luego esta
        Function<String, Integer> contarLetras = longitud.compose(String::trim);
        System.out.println("longitud.compose(trim).apply(\"  hola  \") → "
                + contarLetras.apply("  hola  ") + " (sin espacios)");
    }

    // ─── Consumer<T>: T → void ─────────────────────────────────────────────
    static void demoConsumer() {
        System.out.println("\n--- Consumer<T>: efectos secundarios ---");

        Consumer<String> imprimir       = System.out::println;
        Consumer<String> imprimirConTag = s -> System.out.println("[LOG] " + s);

        imprimir.accept("Hola");
        imprimirConTag.accept("Evento");

        // andThen: ejecuta primero uno, luego el otro
        Consumer<String> doble = imprimir.andThen(imprimirConTag);
        System.out.println("-- doble.accept(\"Test\"):");
        doble.accept("Test");

        // Uso típico con forEach
        System.out.println("-- forEach con Consumer:");
        List.of("Ana", "Bruno", "Carlos").forEach(imprimir);
    }

    // ─── Supplier<T>: () → T ───────────────────────────────────────────────
    static void demoSupplier() {
        System.out.println("\n--- Supplier<T>: generar valores ---");

        Supplier<String>    saludo = () -> "¡Hola, mundo!";
        Supplier<LocalDate> hoy    = LocalDate::now;
        Supplier<List<String>> nuevaLista = ArrayList::new;

        System.out.println("saludo.get() → " + saludo.get());
        System.out.println("hoy.get()    → " + hoy.get());
        System.out.println("lista vacía  → " + nuevaLista.get());

        // Uso típico: orElseGet en Optional (solo se evalúa si el Optional está vacío)
        Optional<String> nombre = Optional.empty();
        String resultado = nombre.orElseGet(() -> "nombre-por-defecto");
        System.out.println("orElseGet    → " + resultado);
    }

    static void header(String titulo) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║  " + titulo);
        System.out.println("╚══════════════════════════════════════╝");
    }
}

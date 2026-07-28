package funcional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// Sección 6 del material: Method References
public class MethodReferencesDemo {

    static void demo() {
        header("6. METHOD REFERENCES");

        System.out.println("Sintaxis: Clase::método  ≡  x -> Clase.método(x)\n");

        // --- Tipo 1: método estático ---
        System.out.println("-- Método estático: Integer::parseInt --");
        Function<String, Integer> parsear = Integer::parseInt;
        System.out.println("parsear.apply(\"42\") → " + parsear.apply("42"));

        List<String> numerosStr = List.of("1", "2", "3", "4", "5");
        List<Integer> numeros = numerosStr.stream()
                .map(Integer::parseInt)   // equivale a: s -> Integer.parseInt(s)
                .collect(Collectors.toList());
        System.out.println("Convertir lista: " + numeros);

        // --- Tipo 2: método de instancia (objeto fijo) ---
        System.out.println("\n-- Método de instancia (objeto fijo): tag::concat --");
        String tag = "[INFO] ";
        Function<String, String> agregarTag = tag::concat;
        System.out.println(agregarTag.apply("inicio del proceso"));
        System.out.println(agregarTag.apply("fin del proceso"));

        // --- Tipo 3: método de instancia (tipo arbitrario) ---
        System.out.println("\n-- Método de instancia (tipo arbitrario): String::toUpperCase --");
        List<String> nombres = List.of("ana", "bruno", "carlos");

        // Con lambda explícita
        List<String> v1 = nombres.stream()
                .map(s -> s.toUpperCase())
                .collect(Collectors.toList());

        // Con method reference (más limpia, equivalente)
        List<String> v2 = nombres.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        System.out.println("Lambda       : " + v1);
        System.out.println("Method ref   : " + v2);

        // --- Tipo 4: constructor reference ---
        System.out.println("\n-- Constructor reference: ArrayList::new --");
        Supplier<ArrayList<String>> nuevaLista = ArrayList::new;
        ArrayList<String> lista = nuevaLista.get();
        lista.add("uno");
        lista.add("dos");
        System.out.println("Lista creada: " + lista);

        // --- Comparator con method reference ---
        System.out.println("\n-- Comparator con method reference: String::compareToIgnoreCase --");
        List<String> lista2 = Arrays.asList("Carlos", "ana", "Bruno");
        lista2.sort(String::compareToIgnoreCase);
        System.out.println("Ordenado (case-insensitive): " + lista2);
    }

    static void header(String titulo) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║  " + titulo);
        System.out.println("╚══════════════════════════════════════╝");
    }
}

package funcional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Sección 3 del material: Expresiones Lambda
public class LambdasDemo {

    static void demo() {
        header("3. EXPRESIONES LAMBDA");

        // Lambda asignada a una variable
        Runnable saludar = () -> System.out.println("¡Hola, mundo!");
        saludar.run();

        // Lambda para ordenar en orden ascendente
        List<Integer> numeros = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6);
        numeros.sort((a, b) -> a - b);
        System.out.println("Ordenados asc: " + numeros);

        // --- Lambda vs clase anónima ---
        System.out.println("\n-- Lambda vs clase anónima --");
        List<String> lista = Arrays.asList("Carlos", "Ana", "Bruno");

        // Antes de Java 8: clase anónima
        Collections.sort(lista, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });
        System.out.println("Clase anónima : " + lista);

        // Con lambda
        lista.sort((s1, s2) -> s1.compareTo(s2));
        System.out.println("Lambda        : " + lista);

        // Aún más conciso: method reference
        lista.sort(String::compareTo);
        System.out.println("Method ref    : " + lista);

        // --- forEach con lambda multilínea ---
        System.out.println("\n-- forEach con lambda --");
        List<String> nombres = List.of("Ana", "Bruno", "Carlos");
        nombres.forEach(nombre -> {
            String saludo = "Hola, " + nombre + "!";
            System.out.println(saludo);
        });

        // --- Captura de variables (deben ser efectivamente finales) ---
        System.out.println("\n-- Captura de variables --");
        String prefijo = "Sr.";  // efectivamente final: no se reasigna
        nombres.forEach(nombre -> System.out.println(prefijo + " " + nombre));
    }

    static void header(String titulo) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║  " + titulo);
        System.out.println("╚══════════════════════════════════════╝");
    }
}

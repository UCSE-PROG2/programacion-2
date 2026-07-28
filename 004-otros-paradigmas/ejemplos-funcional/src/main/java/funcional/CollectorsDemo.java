package funcional;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Sección 9 del material: Collectors
public class CollectorsDemo {

    record Persona(String nombre, String ciudad, int edad) {}

    static void demo() {
        header("9. COLLECTORS");

        List<Persona> personas = List.of(
            new Persona("Ana",    "Buenos Aires", 25),
            new Persona("Bruno",  "Córdoba",      30),
            new Persona("Alicia", "Buenos Aires", 28),
            new Persona("Carlos", "Córdoba",      35)
        );

        demoGroupingBy(personas);
        demoSummarizingInt(personas);
        demoToMap(personas);
        demoPartitioningBy(personas);
    }

    // ─── groupingBy ─────────────────────────────────────────────────────────
    static void demoGroupingBy(List<Persona> personas) {
        System.out.println("\n-- groupingBy --");

        // Agrupar por ciudad → Map<String, List<Persona>>
        Map<String, List<Persona>> porCiudad = personas.stream()
                .collect(Collectors.groupingBy(Persona::ciudad));
        porCiudad.forEach((ciudad, lista) ->
                System.out.println("  " + ciudad + ": " + lista.stream()
                        .map(Persona::nombre).collect(Collectors.toList())));

        // Agrupar y contar → Map<String, Long>
        Map<String, Long> cantidadPorCiudad = personas.stream()
                .collect(Collectors.groupingBy(Persona::ciudad, Collectors.counting()));
        System.out.println("Cantidad por ciudad: " + cantidadPorCiudad);

        // Agrupar y sumar edades → Map<String, Integer>
        Map<String, Integer> edadTotalPorCiudad = personas.stream()
                .collect(Collectors.groupingBy(
                        Persona::ciudad,
                        Collectors.summingInt(Persona::edad)
                ));
        System.out.println("Suma edades por ciudad: " + edadTotalPorCiudad);
    }

    // ─── summarizingInt ─────────────────────────────────────────────────────
    static void demoSummarizingInt(List<Persona> personas) {
        System.out.println("\n-- summarizingInt --");

        IntSummaryStatistics stats = personas.stream()
                .collect(Collectors.summarizingInt(Persona::edad));

        System.out.println("Mínimo  : " + stats.getMin());
        System.out.println("Máximo  : " + stats.getMax());
        System.out.println("Promedio: " + stats.getAverage());
        System.out.println("Suma    : " + stats.getSum());
        System.out.println("Cantidad: " + stats.getCount());
    }

    // ─── toMap ──────────────────────────────────────────────────────────────
    static void demoToMap(List<Persona> personas) {
        System.out.println("\n-- toMap --");

        Map<String, Integer> nombreAEdad = personas.stream()
                .collect(Collectors.toMap(
                        Persona::nombre,  // clave
                        Persona::edad     // valor
                ));
        System.out.println("Nombre → Edad: " + nombreAEdad);
    }

    // ─── partitioningBy ─────────────────────────────────────────────────────
    static void demoPartitioningBy(List<Persona> personas) {
        System.out.println("\n-- partitioningBy --");

        Map<Boolean, List<Persona>> particion = personas.stream()
                .collect(Collectors.partitioningBy(p -> p.edad() >= 30));

        List<String> mayoresDe30 = particion.get(true).stream()
                .map(Persona::nombre).collect(Collectors.toList());
        List<String> menoresDe30 = particion.get(false).stream()
                .map(Persona::nombre).collect(Collectors.toList());

        System.out.println("≥ 30 años: " + mayoresDe30);
        System.out.println("< 30 años: " + menoresDe30);
    }

    static void header(String titulo) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║  " + titulo);
        System.out.println("╚══════════════════════════════════════╝");
    }
}

package funcional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// Sección 8 del material: Stream API
public class StreamApiDemo {

    static void demo() {
        header("8. STREAM API");

        demoCrearStreams();
        demoFilter();
        demoMap();
        demoFlatMap();
        demoSorted();
        demoDistinctLimitSkip();
        demoPeek();
        demoTerminales();
    }

    // ─── Formas de crear un Stream ──────────────────────────────────────────
    static void demoCrearStreams() {
        System.out.println("\n-- Crear streams --");

        Stream<String> desdeColeccion = List.of("a", "b", "c").stream();
        Stream<String> desdeValores   = Stream.of("a", "b", "c");
        Stream<String> desdeArray     = Arrays.stream(new String[]{"a", "b", "c"});
        IntStream rangoExclusivo      = IntStream.range(1, 6);      // 1..5
        IntStream rangoInclusivo      = IntStream.rangeClosed(1, 5); // 1..5

        System.out.println("desde colección: " + desdeColeccion.collect(Collectors.toList()));
        System.out.println("desde valores  : " + desdeValores.collect(Collectors.toList()));
        System.out.println("desde array    : " + desdeArray.collect(Collectors.toList()));
        System.out.println("range(1,6)     : " + rangoExclusivo.boxed().collect(Collectors.toList()));
        System.out.println("rangeClosed(1,5): " + rangoInclusivo.boxed().collect(Collectors.toList()));

        // Stream infinito acotado con limit
        List<Integer> primeros5Pares = Stream.iterate(0, n -> n + 2)
                .limit(5)
                .collect(Collectors.toList());
        System.out.println("primeros 5 pares: " + primeros5Pares);
    }

    // ─── filter ─────────────────────────────────────────────────────────────
    static void demoFilter() {
        System.out.println("\n-- filter --");
        List<Integer> numeros = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        List<Integer> pares = numeros.stream()
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList());
        System.out.println("pares: " + pares);

        List<Integer> mayoresDe5 = numeros.stream()
                .filter(n -> n > 5)
                .collect(Collectors.toList());
        System.out.println("mayores de 5: " + mayoresDe5);
    }

    // ─── map ────────────────────────────────────────────────────────────────
    static void demoMap() {
        System.out.println("\n-- map --");
        List<String> nombres = List.of("ana", "bruno", "carlos");

        // String → String
        List<String> enMayusculas = nombres.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        System.out.println("toUpperCase   : " + enMayusculas);

        // String → Integer (cambio de tipo)
        List<Integer> longitudes = nombres.stream()
                .map(String::length)
                .collect(Collectors.toList());
        System.out.println("longitudes    : " + longitudes);
    }

    // ─── flatMap ────────────────────────────────────────────────────────────
    static void demoFlatMap() {
        System.out.println("\n-- flatMap --");

        // Aplanar listas de listas
        List<List<Integer>> listas = List.of(
                List.of(1, 2, 3),
                List.of(4, 5),
                List.of(6)
        );

        List<Integer> todos = listas.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        System.out.println("aplanado: " + todos);
    }

    // ─── sorted ─────────────────────────────────────────────────────────────
    static void demoSorted() {
        System.out.println("\n-- sorted --");
        List<String> nombres = List.of("Carlos", "Ana", "Bruno");

        System.out.println("natural          : " + nombres.stream().sorted().collect(Collectors.toList()));
        System.out.println("reverso          : " + nombres.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
        System.out.println("por longitud     : " + nombres.stream()
                .sorted(Comparator.comparing(String::length))
                .collect(Collectors.toList()));
    }

    // ─── distinct, limit, skip ──────────────────────────────────────────────
    static void demoDistinctLimitSkip() {
        System.out.println("\n-- distinct / limit / skip --");

        List<Integer> sinDuplicados = List.of(1, 2, 2, 3, 3, 3, 4).stream()
                .distinct()
                .collect(Collectors.toList());
        System.out.println("distinct : " + sinDuplicados);

        List<Integer> primeros3 = List.of(10, 20, 30, 40, 50).stream()
                .limit(3)
                .collect(Collectors.toList());
        System.out.println("limit(3) : " + primeros3);

        List<Integer> sinPrimeros2 = List.of(10, 20, 30, 40, 50).stream()
                .skip(2)
                .collect(Collectors.toList());
        System.out.println("skip(2)  : " + sinPrimeros2);

        // Combinación: stream infinito de impares, filtrar > 10, tomar 5
        List<Integer> imparesGrandesAcotados = Stream.iterate(1, n -> n + 2)
                .filter(n -> n > 10)
                .limit(5)
                .collect(Collectors.toList());
        System.out.println("impares > 10, limit(5): " + imparesGrandesAcotados);
    }

    // ─── peek (solo para debug) ──────────────────────────────────────────────
    static void demoPeek() {
        System.out.println("\n-- peek (debug) --");
        List.of("ana", "bruno", "carlos").stream()
                .peek(n -> System.out.println("  antes de map: " + n))
                .map(String::toUpperCase)
                .peek(n -> System.out.println("  después de map: " + n))
                .collect(Collectors.toList());
    }

    // ─── Operaciones terminales ──────────────────────────────────────────────
    static void demoTerminales() {
        System.out.println("\n-- Operaciones terminales --");
        List<Integer> numeros = List.of(1, 2, 3, 4, 5);
        List<String> nombres  = List.of("Ana", "Bruno", "Alicia", "Carlos");

        // count
        long cantidad = nombres.stream().filter(n -> n.startsWith("A")).count();
        System.out.println("count (empieza con A): " + cantidad);

        // findFirst
        Optional<String> primero = nombres.stream().filter(n -> n.length() > 4).findFirst();
        System.out.println("findFirst (len>4): " + primero);

        // anyMatch / allMatch / noneMatch
        System.out.println("anyMatch(n>4)   : " + numeros.stream().anyMatch(n -> n > 4));
        System.out.println("allMatch(n>0)   : " + numeros.stream().allMatch(n -> n > 0));
        System.out.println("noneMatch(n<0)  : " + numeros.stream().noneMatch(n -> n < 0));

        // reduce
        int suma     = numeros.stream().reduce(0, Integer::sum);
        int producto = numeros.stream().reduce(1, (a, b) -> a * b);
        System.out.println("reduce sum(0..5)  : " + suma);
        System.out.println("reduce product    : " + producto);

        Optional<Integer> max = List.of(3, 1, 4, 1, 5, 9).stream().reduce(Integer::max);
        System.out.println("reduce max        : " + max);

        // min / max directos
        Optional<String> masCorto = nombres.stream().min(Comparator.comparing(String::length));
        System.out.println("nombre más corto  : " + masCorto);

        // joining
        String unido = Stream.of("Hola", "mundo", "Java")
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("joining           : " + unido);

        // mapToDouble + sum
        double totalSalarios = Stream.of(1000.0, 2000.0, 3000.0)
                .mapToDouble(Double::doubleValue)
                .sum();
        System.out.println("mapToDouble + sum : " + totalSalarios);
    }

    static void header(String titulo) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║  " + titulo);
        System.out.println("╚══════════════════════════════════════╝");
    }
}

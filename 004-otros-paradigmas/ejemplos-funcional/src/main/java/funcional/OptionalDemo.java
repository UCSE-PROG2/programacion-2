package funcional;

import java.util.Optional;

// Sección 7 del material: Optional — evitar null
public class OptionalDemo {

    static void demo() {
        header("7. OPTIONAL — EVITAR NULL");

        demoCreacion();
        demoOperaciones();
        demoPatronService();
    }

    // ─── Creación ───────────────────────────────────────────────────────────
    static void demoCreacion() {
        System.out.println("\n-- Creación --");

        Optional<String> con      = Optional.of("Hola");
        Optional<String> vacio    = Optional.empty();
        Optional<String> nullable = Optional.ofNullable(null);  // → empty
        Optional<String> presente = Optional.ofNullable("Java"); // → presente

        System.out.println("Optional.of(\"Hola\")         → " + con);
        System.out.println("Optional.empty()             → " + vacio);
        System.out.println("Optional.ofNullable(null)    → " + nullable);
        System.out.println("Optional.ofNullable(\"Java\") → " + presente);
    }

    // ─── Operaciones ────────────────────────────────────────────────────────
    static void demoOperaciones() {
        System.out.println("\n-- Operaciones --");

        Optional<String> nombre = Optional.of("  Java  ");

        // Verificar presencia
        System.out.println("isPresent() → " + nombre.isPresent());
        System.out.println("isEmpty()   → " + nombre.isEmpty());

        // Obtener el valor (solo si se sabe que está)
        System.out.println("get()       → '" + nombre.get() + "'");

        // Alternativas seguras cuando puede estar vacío
        System.out.println("\n-- orElse / orElseGet / orElseThrow --");
        String v1 = Optional.<String>empty().orElse("Default");
        String v2 = Optional.<String>empty().orElseGet(() -> "generado-dinámicamente");
        System.out.println("orElse(\"Default\")   → " + v1);
        System.out.println("orElseGet(Supplier) → " + v2);

        // map: transformar si está presente
        System.out.println("\n-- map y filter --");
        Optional<Integer> longitud = nombre.map(String::trim).map(String::length);
        System.out.println("nombre.map(trim).map(length) → " + longitud);

        // filter: mantener si cumple condición
        Optional<String> largo = nombre.filter(s -> s.trim().length() > 10);
        System.out.println("nombre.filter(len>10)        → " + largo);

        // ifPresent: acción solo si hay valor
        System.out.println("\n-- ifPresent / ifPresentOrElse --");
        nombre.ifPresent(n -> System.out.println("Nombre: " + n.trim()));

        Optional.<String>empty().ifPresentOrElse(
            n -> System.out.println("Hola, " + n),
            () -> System.out.println("Sin nombre — ejecutó el else")
        );
    }

    // ─── Patrón típico en una API REST (simulado) ───────────────────────────
    static void demoPatronService() {
        System.out.println("\n-- Patrón service/controller (simulado) --");

        // Simulamos un repositorio que devuelve Optional
        Optional<String> encontrado    = buscarUsuario(1L);
        Optional<String> noEncontrado  = buscarUsuario(99L);

        // map + orElse: el Controller convierte a respuesta HTTP
        String respuesta1 = encontrado.map(u -> "200 OK: " + u)
                                      .orElse("404 Not Found");
        String respuesta2 = noEncontrado.map(u -> "200 OK: " + u)
                                        .orElse("404 Not Found");

        System.out.println("buscarUsuario(1)  → " + respuesta1);
        System.out.println("buscarUsuario(99) → " + respuesta2);
    }

    // Simula un findById del repositorio
    static Optional<String> buscarUsuario(Long id) {
        if (id == 1L) return Optional.of("Ana García");
        return Optional.empty();
    }

    static void header(String titulo) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║  " + titulo);
        System.out.println("╚══════════════════════════════════════╝");
    }
}

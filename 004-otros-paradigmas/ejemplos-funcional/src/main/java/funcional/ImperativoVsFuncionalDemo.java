package funcional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// Sección 10 del material: Comparación imperativa vs funcional
public class ImperativoVsFuncionalDemo {

    record Pedido(String cliente, double total, String estado) {}

    static void demo() {
        header("10. IMPERATIVO VS FUNCIONAL");

        List<Pedido> pedidos = List.of(
            new Pedido("Ana",    1500.0, "COMPLETADO"),
            new Pedido("Bruno",   800.0, "PENDIENTE"),
            new Pedido("Carlos", 3200.0, "COMPLETADO"),
            new Pedido("Diana",   200.0, "CANCELADO")
        );

        System.out.println("Pedidos: " + pedidos.stream()
                .map(p -> p.cliente() + "($" + p.total() + "/" + p.estado() + ")")
                .collect(Collectors.joining(", ")));

        // --- Objetivo 1: sumar total de pedidos COMPLETADO ---
        System.out.println("\n-- Objetivo 1: suma de pedidos COMPLETADO --");

        // Estilo imperativo
        double totalImperativo = 0;
        for (Pedido p : pedidos) {
            if ("COMPLETADO".equals(p.estado())) {
                totalImperativo += p.total();
            }
        }
        System.out.println("Imperativo: $" + totalImperativo);

        // Estilo funcional
        double totalFuncional = pedidos.stream()
                .filter(p -> "COMPLETADO".equals(p.estado()))
                .mapToDouble(Pedido::total)
                .sum();
        System.out.println("Funcional : $" + totalFuncional);

        // --- Objetivo 2: clientes de pedidos COMPLETADO ordenados ---
        System.out.println("\n-- Objetivo 2: clientes COMPLETADO, ordenados --");

        // Estilo imperativo
        List<String> clientesImp = new ArrayList<>();
        for (Pedido p : pedidos) {
            if ("COMPLETADO".equals(p.estado())) {
                clientesImp.add(p.cliente());
            }
        }
        Collections.sort(clientesImp);
        System.out.println("Imperativo: " + clientesImp);

        // Estilo funcional
        List<String> clientesFun = pedidos.stream()
                .filter(p -> "COMPLETADO".equals(p.estado()))
                .map(Pedido::cliente)
                .sorted()
                .collect(Collectors.toList());
        System.out.println("Funcional : " + clientesFun);

        // --- Cheat sheet en acción ---
        System.out.println("\n-- Cheat sheet en acción --");

        // Pedidos con total > 1000
        List<String> clientesVip = pedidos.stream()
                .filter(p -> p.total() > 1000)
                .map(Pedido::cliente)
                .collect(Collectors.toList());
        System.out.println("Clientes VIP (>$1000)  : " + clientesVip);

        // ¿Algún pedido cancelado?
        boolean hayCancelado = pedidos.stream()
                .anyMatch(p -> "CANCELADO".equals(p.estado()));
        System.out.println("¿Hay cancelados?        : " + hayCancelado);

        // ¿Todos tienen estado definido (no nulo)?
        boolean todosConEstado = pedidos.stream()
                .allMatch(p -> p.estado() != null);
        System.out.println("¿Todos tienen estado?   : " + todosConEstado);
    }

    static void header(String titulo) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║  " + titulo);
        System.out.println("╚══════════════════════════════════════╝");
    }
}

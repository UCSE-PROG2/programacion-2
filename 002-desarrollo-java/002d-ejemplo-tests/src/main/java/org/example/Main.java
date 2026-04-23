package org.example;

import org.example.data.HibernateUtil;
import org.example.service.AcademiaService;

import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// CAPA DE PRESENTACIÓN — Punto de entrada
//
// Demuestra todos los conceptos de la Unidad 2, secciones 6 y 7:
//
//   Sección 6 — Relaciones entre entidades:
//     - 1 a 1:  Alumno ──── Perfil
//     - 1 a N:  Carrera ──── Materia
//     - N a N:  Alumno ──── Materia (tabla intermedia: inscripciones)
//
//   Sección 7 — CriteriaBuilder:
//     7.1  Igualdad          → alumnos activos
//     7.2  Comparación/BETWEEN → materias por rango de año
//     7.3  LIKE              → materias por nombre parcial
//     7.4  IS NULL           → alumnos sin perfil
//     7.5  IN                → materias en años específicos
//     7.6  OR                → alumnos por nombre o apellido
//     7.7  Filtros dinámicos → búsqueda de materias con parámetros opcionales
//     7.8  JOIN              → materias por carrera / alumnos por materia (N:N)
//     7.9  GROUP BY + HAVING → estadísticas de materias por carrera
//     7.10 Orden + paginación → alumnos ordenados por apellido
//
// Flujo de llamadas:
//   Main → AcademiaService → Repository → Hibernate → MySQL
// ─────────────────────────────────────────────────────────────────────────────
public class Main {

    public static void main(String[] args) {

        AcademiaService service = new AcademiaService();

        // ── SETUP: Datos iniciales ─────────────────────────────────────────────

        System.out.println("=".repeat(60));
        System.out.println("SETUP — Carga de datos iniciales");
        System.out.println("=".repeat(60));

        // Sección 6.2 — Relación 1 a N: Carrera → Materia
        // Se crean dos carreras. Cada materia tendrá FK carrera_id apuntando
        // a una de estas dos filas.
        System.out.println("\n--- Carreras ---");
        service.registrarCarrera("Ingeniería en Sistemas");   // id=1
        service.registrarCarrera("Licenciatura en Sistemas"); // id=2

        // Las materias de año 1 y 2 son comunes en ambas carreras.
        // La FK carrera_id en la tabla materias establece la relación 1-N.
        System.out.println("\n--- Materias (relación 1 a N con Carrera) ---");
        service.registrarMateria("Programación 1",        1, true,  1);
        service.registrarMateria("Programación 2",        2, true,  1);
        service.registrarMateria("Base de Datos",         2, true,  1);
        service.registrarMateria("Redes de Computadoras", 3, false, 1);
        service.registrarMateria("Algoritmos y Estructuras", 1, true, 2);
        service.registrarMateria("Sistemas Operativos",   2, true,  2);
        service.registrarMateria("Ingeniería de Software",3, false, 2);

        // Sección 6.1 — Relación 1 a 1: Alumno → Perfil
        // Algunos alumnos tienen perfil (email/teléfono), otros no.
        // Cuando se proveen datos, el Service crea un Perfil y lo asigna.
        // CascadeType.ALL en Alumno.perfil hace que Hibernate persista el Perfil
        // automáticamente al persistir el Alumno.
        System.out.println("\n--- Alumnos (relación 1 a 1 con Perfil) ---");
        service.registrarAlumno("Ana",    "García",    "30111222", "ana@mail.com",    "3512222222");
        service.registrarAlumno("Carlos", "López",     "31222333", "carlos@mail.com", "3513333333");
        service.registrarAlumno("Laura",  "Martínez",  "32333444", "laura@mail.com",  null);
        service.registrarAlumno("Diego",  "Fernández", "33444555", null,              null); // sin perfil

        // Sección 6.3 — Relación N a N: Alumno ↔ Materia
        // Hibernate gestiona la tabla intermedia "inscripciones" automáticamente.
        // Cada llamada a inscribirAlumno() inserta una fila en esa tabla.
        System.out.println("\n--- Inscripciones (relación N a N: Alumno ↔ Materia) ---");
        service.inscribirAlumno(1, 1); // Ana → Prog 1
        service.inscribirAlumno(1, 2); // Ana → Prog 2
        service.inscribirAlumno(1, 3); // Ana → Base de Datos
        service.inscribirAlumno(2, 1); // Carlos → Prog 1
        service.inscribirAlumno(2, 3); // Carlos → Base de Datos
        service.inscribirAlumno(3, 1); // Laura → Prog 1
        service.inscribirAlumno(3, 5); // Laura → Algoritmos

        // Desactivar un alumno para que los filtros de 7.1 sean significativos
        service.desactivarAlumno(4); // Diego queda inactivo


        // ── SECCIÓN 7 — CriteriaBuilder ───────────────────────────────────────

        System.out.println("\n" + "=".repeat(60));
        System.out.println("SECCIÓN 7 — Consultas con CriteriaBuilder");
        System.out.println("=".repeat(60));

        // 7.1 — Igualdad: cb.equal(campo, valor)
        // WHERE activo = true → excluye a Diego (inactivo)
        System.out.println("\n--- 7.1 Igualdad: alumnos activos ---");
        service.listarAlumnosActivos().forEach(System.out::println);

        // 7.2 — BETWEEN: cb.between(campo, min, max)
        // WHERE anio BETWEEN 1 AND 2 → solo materias de 1° y 2° año
        System.out.println("\n--- 7.2 BETWEEN: materias de 1° y 2° año ---");
        service.buscarMateriasPorRangoAnio(1, 2).forEach(System.out::println);

        // 7.3 — LIKE: cb.like(cb.lower(campo), "%valor%")
        // Case-insensitive; busca "prog" en el nombre de la materia
        System.out.println("\n--- 7.3 LIKE: materias cuyo nombre contiene 'prog' ---");
        service.buscarMateriasPorNombre("prog").forEach(System.out::println);

        // 7.4 — IS NULL: cb.isNull(campo)
        // WHERE perfil_id IS NULL → alumnos sin datos de contacto
        System.out.println("\n--- 7.4 IS NULL: alumnos sin perfil de contacto ---");
        service.listarAlumnosSinPerfil().forEach(System.out::println);

        // 7.5 — IN: root.get("campo").in(lista)
        // WHERE anio IN (1, 3) → materias de 1° o 3° año
        System.out.println("\n--- 7.5 IN: materias de año 1 o 3 ---");
        service.buscarMateriasPorAnios(List.of(1, 3)).forEach(System.out::println);

        // 7.6 — OR: cb.or(predicado1, predicado2)
        // WHERE LOWER(nombre) LIKE '%ar%' OR LOWER(apellido) LIKE '%ar%'
        System.out.println("\n--- 7.6 OR: alumnos con 'ar' en nombre o apellido ---");
        service.buscarAlumnosPorNombreOrApellido("ar").forEach(System.out::println);

        // 7.7 — Filtros dinámicos: predicados opcionales acumulados en List<Predicate>
        // Solo se agrega un predicado si el parámetro no es null.
        System.out.println("\n--- 7.7 Dinámico: obligatoria=true, sin otros filtros ---");
        service.buscarMaterias(null, null, null, true).forEach(System.out::println);

        System.out.println("\n--- 7.7 Dinámico: nombre contiene 'sistema', año ≤ 3 ---");
        service.buscarMaterias("sistema", null, 3, null).forEach(System.out::println);

        // 7.8 — JOIN con entidad relacionada (ManyToOne)
        // JOIN materias ↔ carreras, filtra por Carrera.nombre
        System.out.println("\n--- 7.8 JOIN (1-N): materias de 'Ingeniería en Sistemas' ---");
        service.buscarMateriasPorCarrera("ingeniería").forEach(System.out::println);

        // 7.8 — JOIN en relación N a N (a través de inscripciones)
        // Hibernate genera el JOIN con la tabla intermedia automáticamente.
        System.out.println("\n--- 7.8 JOIN (N-N): alumnos inscriptos en 'Programación 1' ---");
        service.listarAlumnosPorMateria("Programación 1").forEach(System.out::println);

        // 7.9 — GROUP BY: multiselect + groupBy
        // SELECT carrera.nombre, COUNT(*) FROM materias GROUP BY carrera.nombre
        System.out.println("\n--- 7.9 GROUP BY: cantidad de materias por carrera ---");
        service.mostrarMateriasPorCarrera();

        // 7.9 — GROUP BY + HAVING: filtra grupos después de agrupar
        // HAVING COUNT(*) > 2 → solo carreras con más de 2 materias
        System.out.println("\n--- 7.9 HAVING: carreras con más de 2 materias ---");
        service.mostrarCarrerasConMasDe2Materias();

        // 7.10 — Ordenamiento y paginación: orderBy + setFirstResult + setMaxResults
        // ORDER BY apellido ASC, nombre ASC LIMIT 2 OFFSET 0 (primera página)
        System.out.println("\n--- 7.10 Paginación: página 1, tamaño 2, ordenado por apellido ---");
        service.listarAlumnosOrdenadosPaginados(0, 2).forEach(System.out::println);

        System.out.println("\n--- 7.10 Paginación: página 2, tamaño 2 ---");
        service.listarAlumnosOrdenadosPaginados(1, 2).forEach(System.out::println);

        HibernateUtil.shutdown();
    }
}

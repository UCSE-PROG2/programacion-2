package org.example.service;

import org.example.data.*;

import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// CAPA DE LÓGICA — AcademiaService
//
// Contiene las reglas de negocio: validaciones, decisiones sobre qué hacer
// y bajo qué condiciones, antes de delegar al Repository.
//
// La capa de presentación (Main) solo conoce este Service, nunca al Repository.
// ─────────────────────────────────────────────────────────────────────────────
public class AcademiaService {

    private final Repository repository = Repository.getInstance();

    // ── Carrera ───────────────────────────────────────────────────────────────

    public void registrarCarrera(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la carrera no puede estar vacío");
        }
        repository.saveCarrera(new Carrera(nombre));
        System.out.println("Carrera registrada: " + nombre);
    }

    public List<Carrera> listarCarreras() {
        return repository.findAllCarreras();
    }

    // ── Materia ───────────────────────────────────────────────────────────────

    public void registrarMateria(String nombre, int anio, boolean obligatoria, Integer carreraId) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la materia no puede estar vacío");
        }
        if (anio < 1 || anio > 5) {
            throw new IllegalArgumentException("El año debe estar entre 1 y 5");
        }
        Carrera carrera = repository.findCarreraById(carreraId);
        if (carrera == null) {
            throw new IllegalArgumentException("No existe carrera con id=" + carreraId);
        }
        repository.saveMateria(new Materia(nombre, anio, obligatoria, carrera));
        System.out.println("Materia registrada: " + nombre + " (año " + anio + ", carrera: " + carrera.getNombre() + ")");
    }

    public List<Materia> listarMaterias() {
        return repository.findAllMaterias();
    }

    public List<Materia> buscarMateriasPorNombre(String nombre) {
        return repository.findMateriasByNombre(nombre);
    }

    public List<Materia> buscarMateriasPorCarrera(String nombreCarrera) {
        return repository.findMateriasByNombreCarrera(nombreCarrera);
    }

    public List<Materia> buscarMaterias(String nombre, Integer anioMin, Integer anioMax, Boolean obligatoria) {
        return repository.buscarMaterias(nombre, anioMin, anioMax, obligatoria);
    }

    public List<Materia> buscarMateriasPorAnios(List<Integer> anios) {
        return repository.findMateriasByAnios(anios);
    }

    public List<Materia> buscarMateriasPorRangoAnio(int min, int max) {
        return repository.findMateriasByRangoAnio(min, max);
    }

    // ── Alumno ────────────────────────────────────────────────────────────────

    public void registrarAlumno(String nombre, String apellido, String dni, String email, String telefono) {
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("Nombre y apellido son obligatorios");
        }
        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("El DNI es obligatorio");
        }
        Alumno alumno = new Alumno(nombre, apellido, dni, true);

        // Solo crear el Perfil si se proveyó al menos email o teléfono.
        // Alumno.perfil puede ser null (sin perfil de contacto).
        if ((email != null && !email.isBlank()) || (telefono != null && !telefono.isBlank())) {
            alumno.setPerfil(new Perfil(email, telefono));
        }

        repository.saveAlumno(alumno);
        System.out.println("Alumno registrado: " + nombre + " " + apellido);
    }

    public void desactivarAlumno(Integer id) {
        Alumno alumno = repository.findAlumnoById(id);
        if (alumno == null) {
            System.out.println("No se encontró alumno con id=" + id);
            return;
        }
        alumno.setActivo(false);
        repository.updateAlumno(alumno);
        System.out.println("Alumno desactivado: " + alumno.getNombre() + " " + alumno.getApellido());
    }

    public List<Alumno> listarAlumnos() {
        return repository.findAllAlumnos();
    }

    public List<Alumno> listarAlumnosActivos() {
        return repository.findAlumnosActivos();
    }

    public List<Alumno> buscarAlumnosPorNombreOrApellido(String texto) {
        return repository.findAlumnosByNombreOrApellido(texto);
    }

    public List<Alumno> listarAlumnosSinPerfil() {
        return repository.findAlumnosSinPerfil();
    }

    public List<Alumno> listarAlumnosPorMateria(String nombreMateria) {
        return repository.findAlumnosByNombreMateria(nombreMateria);
    }

    public List<Alumno> listarAlumnosOrdenadosPaginados(int pagina, int tamanio) {
        return repository.findAlumnosOrdenadosPaginados(pagina, tamanio);
    }

    // ── Inscripción (N a N) ───────────────────────────────────────────────────

    public void inscribirAlumno(Integer alumnoId, Integer materiaId) {
        Alumno alumno   = repository.findAlumnoById(alumnoId);
        Materia materia = repository.findMateriaById(materiaId);
        if (alumno == null) {
            System.out.println("No existe alumno con id=" + alumnoId);
            return;
        }
        if (materia == null) {
            System.out.println("No existe materia con id=" + materiaId);
            return;
        }
        repository.inscribirAlumno(alumnoId, materiaId);
        System.out.println("Inscripto: " + alumno.getNombre() + " " + alumno.getApellido()
                + " → " + materia.getNombre());
    }

    // ── Estadísticas (GROUP BY) ───────────────────────────────────────────────

    public void mostrarMateriasPorCarrera() {
        List<Object[]> resultados = repository.countMateriasPorCarrera();
        System.out.println("Carrera                          | Cantidad");
        System.out.println("---------------------------------|---------");
        for (Object[] fila : resultados) {
            System.out.printf("%-32s | %d%n", fila[0], fila[1]);
        }
    }

    public void mostrarCarrerasConMasDe2Materias() {
        List<Object[]> resultados = repository.carrerasConMasDe2Materias();
        if (resultados.isEmpty()) {
            System.out.println("Ninguna carrera tiene más de 2 materias.");
            return;
        }
        System.out.println("Carrera (HAVING COUNT > 2)       | Cantidad");
        System.out.println("---------------------------------|---------");
        for (Object[] fila : resultados) {
            System.out.printf("%-32s | %d%n", fila[0], fila[1]);
        }
    }
}

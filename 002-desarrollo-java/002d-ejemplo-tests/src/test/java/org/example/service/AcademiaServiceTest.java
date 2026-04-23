package org.example.service;

import org.example.data.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// ─────────────────────────────────────────────────────────────────────────────
// TESTS DE CAPA DE LÓGICA — AcademiaService
//
// Cubren dos tipos de comportamiento:
//
//   1. Validaciones: el Service debe rechazar entradas inválidas antes de
//      delegar al Repository. Se verifica lanzando IllegalArgumentException.
//      Estos tests no necesitan base de datos.
//
//   2. Lógica de negocio: decisiones que toma el Service antes o después de
//      llamar al Repository (crear Perfil solo si hay datos de contacto,
//      crear alumno siempre activo, etc.). Se verifica el estado en H2.
//
// Flujo de dependencias de los tests:
//   AcademiaService → Repository.getInstance() → HibernateUtil → H2 (en test)
// ─────────────────────────────────────────────────────────────────────────────
class AcademiaServiceTest {

    private AcademiaService service;
    private Repository      repository;

    @BeforeEach
    void setUp() {
        service    = new AcademiaService();
        repository = Repository.getInstance();
        limpiarTablas();
    }

    @AfterEach
    void tearDown() {
        limpiarTablas();
    }

    private void limpiarTablas() {
        try (var session = HibernateUtil.getSession()) {
            session.beginTransaction();
            session.createNativeMutationQuery("DELETE FROM inscripciones").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM alumnos").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM perfiles").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM materias").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM carreras").executeUpdate();
            session.getTransaction().commit();
        }
    }


    // ═══════════════════════════════════════════════════════════════════════
    // registrarCarrera — validaciones
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void registrarCarrera_nombreNulo_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarCarrera(null));
    }

    @Test
    void registrarCarrera_nombreVacio_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarCarrera(""));
    }

    @Test
    void registrarCarrera_nombreSoloEspacios_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarCarrera("   "));
    }

    @Test
    void registrarCarrera_valida_persisteEnBD() {
        service.registrarCarrera("Ingeniería en Sistemas");

        List<Carrera> carreras = repository.findAllCarreras();
        assertEquals(1, carreras.size());
        assertEquals("Ingeniería en Sistemas", carreras.get(0).getNombre());
    }


    // ═══════════════════════════════════════════════════════════════════════
    // registrarMateria — validaciones
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void registrarMateria_anioMenorA1_lanzaIllegalArgument() {
        service.registrarCarrera("Test");
        Integer carreraId = repository.findAllCarreras().get(0).getId();

        assertThrows(IllegalArgumentException.class,
            () -> service.registrarMateria("Materia", 0, true, carreraId));
    }

    @Test
    void registrarMateria_anioMayorA5_lanzaIllegalArgument() {
        service.registrarCarrera("Test");
        Integer carreraId = repository.findAllCarreras().get(0).getId();

        assertThrows(IllegalArgumentException.class,
            () -> service.registrarMateria("Materia", 6, true, carreraId));
    }

    @Test
    void registrarMateria_carreraIdInexistente_lanzaIllegalArgument() {
        // El Service busca la Carrera antes de persistir; si no existe, lanza excepción
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarMateria("Programación 1", 1, true, 9999));
    }

    @Test
    void registrarMateria_nombreNulo_lanzaIllegalArgument() {
        service.registrarCarrera("Test");
        Integer carreraId = repository.findAllCarreras().get(0).getId();

        assertThrows(IllegalArgumentException.class,
            () -> service.registrarMateria(null, 1, true, carreraId));
    }

    @Test
    void registrarMateria_nombreVacio_lanzaIllegalArgument() {
        service.registrarCarrera("Test");
        Integer carreraId = repository.findAllCarreras().get(0).getId();

        assertThrows(IllegalArgumentException.class,
            () -> service.registrarMateria("", 1, true, carreraId));
    }

    @Test
    void registrarMateria_valida_persisteConCarreraCorrecta() {
        service.registrarCarrera("Ingeniería en Sistemas");
        Integer carreraId = repository.findAllCarreras().get(0).getId();

        service.registrarMateria("Programación 1", 1, true, carreraId);

        List<Materia> materias = repository.findAllMaterias();
        assertEquals(1, materias.size());
        assertEquals("Programación 1", materias.get(0).getNombre());
        assertEquals(1, materias.get(0).getAnio());
        assertEquals("Ingeniería en Sistemas", materias.get(0).getCarrera().getNombre());
    }

    @Test
    void registrarMateria_anio1Y5_sonoValidos() {
        service.registrarCarrera("Test");
        Integer id = repository.findAllCarreras().get(0).getId();

        assertDoesNotThrow(() -> service.registrarMateria("Año 1", 1, true, id));
        assertDoesNotThrow(() -> service.registrarMateria("Año 5", 5, true, id));
    }


    // ═══════════════════════════════════════════════════════════════════════
    // registrarAlumno — validaciones
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void registrarAlumno_nombreNulo_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarAlumno(null, "García", "30000001", null, null));
    }

    @Test
    void registrarAlumno_nombreVacio_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarAlumno("", "García", "30000001", null, null));
    }

    @Test
    void registrarAlumno_apellidoNulo_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarAlumno("Ana", null, "30000001", null, null));
    }

    @Test
    void registrarAlumno_apellidoVacio_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarAlumno("Ana", "", "30000001", null, null));
    }

    @Test
    void registrarAlumno_dniNulo_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarAlumno("Ana", "García", null, null, null));
    }

    @Test
    void registrarAlumno_dniVacio_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarAlumno("Ana", "García", "", null, null));
    }


    // ═══════════════════════════════════════════════════════════════════════
    // registrarAlumno — lógica de negocio (creación de Perfil)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void registrarAlumno_conEmailYTelefono_creaPerfil() {
        // Regla de negocio: el Service crea un Perfil cuando hay al menos un dato de contacto
        service.registrarAlumno("Ana", "García", "30000010", "ana@mail.com", "3512345678");

        Alumno alumno = repository.findAllAlumnos().get(0);
        assertNotNull(alumno.getPerfil());
        assertEquals("ana@mail.com", alumno.getPerfil().getEmail());
        assertEquals("3512345678", alumno.getPerfil().getTelefono());
    }

    @Test
    void registrarAlumno_soloConEmail_crearPerfilConEmailYTelefonoNull() {
        service.registrarAlumno("Carlos", "López", "30000011", "carlos@mail.com", null);

        Alumno alumno = repository.findAllAlumnos().get(0);
        assertNotNull(alumno.getPerfil());
        assertEquals("carlos@mail.com", alumno.getPerfil().getEmail());
        assertNull(alumno.getPerfil().getTelefono());
    }

    @Test
    void registrarAlumno_soloConTelefono_creaPerfilConEmailNull() {
        service.registrarAlumno("Laura", "Martínez", "30000012", null, "3514000000");

        Alumno alumno = repository.findAllAlumnos().get(0);
        assertNotNull(alumno.getPerfil());
        assertNull(alumno.getPerfil().getEmail());
        assertEquals("3514000000", alumno.getPerfil().getTelefono());
    }

    @Test
    void registrarAlumno_sinEmailNiTelefono_noCreaPerfil() {
        // Sin datos de contacto: perfil_id = NULL en la tabla alumnos
        service.registrarAlumno("Diego", "Fernández", "30000013", null, null);

        Alumno alumno = repository.findAllAlumnos().get(0);
        assertNull(alumno.getPerfil());
    }

    @Test
    void registrarAlumno_siempreSeCreaCActivoTrue() {
        // Regla de negocio: todo alumno nuevo es activo al momento del registro
        service.registrarAlumno("Nuevo", "Alumno", "30000014", null, null);

        Alumno alumno = repository.findAllAlumnos().get(0);
        assertTrue(alumno.isActivo());
    }


    // ═══════════════════════════════════════════════════════════════════════
    // desactivarAlumno — lógica de negocio
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void desactivarAlumno_existente_setActivoFalse() {
        service.registrarAlumno("Pablo", "Torres", "30000020", null, null);
        Integer id = repository.findAllAlumnos().get(0).getId();

        service.desactivarAlumno(id);

        Alumno alumno = repository.findAlumnoById(id);
        assertFalse(alumno.isActivo());
    }

    @Test
    void desactivarAlumno_yaInactivo_continúaInactivo() {
        service.registrarAlumno("Ya", "Inactivo", "30000021", null, null);
        Integer id = repository.findAllAlumnos().get(0).getId();
        service.desactivarAlumno(id); // primera vez

        // Segunda llamada: no debe lanzar excepción ni cambiar estado
        assertDoesNotThrow(() -> service.desactivarAlumno(id));
        assertFalse(repository.findAlumnoById(id).isActivo());
    }

    @Test
    void desactivarAlumno_idInexistente_noLanzaExcepcion() {
        // El Service solo imprime un mensaje; no lanza excepción para IDs inexistentes
        assertDoesNotThrow(() -> service.desactivarAlumno(9999));
    }


    // ═══════════════════════════════════════════════════════════════════════
    // inscribirAlumno — lógica de negocio y casos borde
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void inscribirAlumno_alumnoInexistente_noLanzaExcepcion() {
        service.registrarCarrera("Test");
        Integer carreraId = repository.findAllCarreras().get(0).getId();
        service.registrarMateria("Prog 1", 1, true, carreraId);
        Integer materiaId = repository.findAllMaterias().get(0).getId();

        // El Service verifica la existencia y solo imprime; no lanza excepción
        assertDoesNotThrow(() -> service.inscribirAlumno(9999, materiaId));
    }

    @Test
    void inscribirAlumno_materiaInexistente_noLanzaExcepcion() {
        service.registrarAlumno("X", "X", "30000030", null, null);
        Integer alumnoId = repository.findAllAlumnos().get(0).getId();

        assertDoesNotThrow(() -> service.inscribirAlumno(alumnoId, 9999));
    }

    @Test
    void inscribirAlumno_valido_alumnoApareceEnConsultaPorMateria() {
        service.registrarCarrera("Ingeniería en Sistemas");
        Integer carreraId = repository.findAllCarreras().get(0).getId();
        service.registrarMateria("Programación 1", 1, true, carreraId);
        service.registrarAlumno("Ana", "García", "30000031", "ana@mail.com", null);

        Integer alumnoId  = repository.findAllAlumnos().get(0).getId();
        Integer materiaId = repository.findAllMaterias().get(0).getId();

        service.inscribirAlumno(alumnoId, materiaId);

        List<Alumno> inscriptos = service.listarAlumnosPorMateria("Programación 1");
        assertEquals(1, inscriptos.size());
        assertEquals("Ana", inscriptos.get(0).getNombre());
    }

    @Test
    void inscribirAlumno_variosAlumnos_unaMismaMateria_retornaTodos() {
        service.registrarCarrera("Test");
        Integer carreraId = repository.findAllCarreras().get(0).getId();
        service.registrarMateria("Bases de Datos", 2, true, carreraId);
        service.registrarAlumno("Ana",    "García", "30000032", null, null);
        service.registrarAlumno("Carlos", "López",  "30000033", null, null);
        service.registrarAlumno("Laura",  "Pérez",  "30000034", null, null);

        Integer materiaId = repository.findAllMaterias().get(0).getId();
        List<Alumno> alumnos = repository.findAllAlumnos();
        for (Alumno a : alumnos) {
            service.inscribirAlumno(a.getId(), materiaId);
        }

        List<Alumno> inscriptos = service.listarAlumnosPorMateria("Bases de Datos");
        assertEquals(3, inscriptos.size());
    }


    // ═══════════════════════════════════════════════════════════════════════
    // Consultas integradas (Service → Repository → CriteriaBuilder → H2)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void listarAlumnosActivos_soloRetornaActivos() {
        service.registrarAlumno("Activo1",         "Test", "30000040", null, null);
        service.registrarAlumno("Activo2",         "Test", "30000041", null, null);
        service.registrarAlumno("ParaDesactivar",  "Test", "30000042", null, null);

        Integer idInactivo = repository.findAllAlumnos().stream()
            .filter(a -> a.getNombre().equals("ParaDesactivar"))
            .findFirst().orElseThrow().getId();
        service.desactivarAlumno(idInactivo);

        List<Alumno> activos = service.listarAlumnosActivos();
        assertEquals(2, activos.size());
        assertTrue(activos.stream().allMatch(Alumno::isActivo));
    }

    @Test
    void buscarMaterias_filtroObligatoria_aislaCorrectamente() {
        service.registrarCarrera("Test");
        Integer carreraId = repository.findAllCarreras().get(0).getId();
        service.registrarMateria("Obligatoria", 1, true,  carreraId);
        service.registrarMateria("Optativa 1",  2, false, carreraId);
        service.registrarMateria("Optativa 2",  3, false, carreraId);

        List<Materia> obligatorias = service.buscarMaterias(null, null, null, true);
        List<Materia> optativas    = service.buscarMaterias(null, null, null, false);

        assertEquals(1, obligatorias.size());
        assertEquals(2, optativas.size());
    }

    @Test
    void buscarMateriasPorCarrera_joinDesdeService_retornaCorrectamente() {
        service.registrarCarrera("Ingeniería en Sistemas");
        service.registrarCarrera("Contaduría Pública");

        Integer ingId = repository.findAllCarreras().stream()
            .filter(c -> c.getNombre().equals("Ingeniería en Sistemas"))
            .findFirst().orElseThrow().getId();
        Integer conId = repository.findAllCarreras().stream()
            .filter(c -> c.getNombre().equals("Contaduría Pública"))
            .findFirst().orElseThrow().getId();

        service.registrarMateria("Programación 1", 1, true, ingId);
        service.registrarMateria("Programación 2", 2, true, ingId);
        service.registrarMateria("Contabilidad I", 1, true, conId);

        List<Materia> resultado = service.buscarMateriasPorCarrera("ingeniería");

        assertEquals(2, resultado.size());
    }

    @Test
    void listarAlumnosSinPerfil_retornaSoloLosSinContacto() {
        service.registrarAlumno("Con Perfil",  "Test", "30000050", "c@mail.com", null);
        service.registrarAlumno("Sin Perfil1", "Test", "30000051", null,         null);
        service.registrarAlumno("Sin Perfil2", "Test", "30000052", null,         null);

        List<Alumno> sinPerfil = service.listarAlumnosSinPerfil();

        assertEquals(2, sinPerfil.size());
        assertTrue(sinPerfil.stream().allMatch(a -> a.getPerfil() == null));
    }
}

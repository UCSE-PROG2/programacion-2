package org.example.data;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// ─────────────────────────────────────────────────────────────────────────────
// TESTS DE CAPA DE DATOS — Repository
//
// Verifican que cada método del Repository ejecuta la consulta correcta
// y retorna los resultados esperados sobre H2 (en memoria).
//
// Cada test:
//   1. Inserta datos propios mediante helpers (sin depender de otros tests).
//   2. Invoca el método del Repository bajo prueba.
//   3. Verifica el resultado con assertions.
//
// @BeforeEach y @AfterEach limpian las tablas para garantizar aislamiento:
// un test nunca ve datos insertados por otro test.
// ─────────────────────────────────────────────────────────────────────────────
class RepositoryTest {

    private Repository repository;

    @BeforeEach
    void setUp() {
        // Repository es Singleton; obtener la instancia activa.
        // El primer acceso dispara la inicialización de HibernateUtil,
        // que carga el hibernate.cfg.xml de src/test/resources (H2).
        repository = Repository.getInstance();
        limpiarTablas();
    }

    @AfterEach
    void tearDown() {
        limpiarTablas();
    }

    // Elimina todas las filas respetando el orden de claves foráneas:
    //   inscripciones → alumnos → perfiles → materias → carreras
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

    // ── Helpers para reducir repetición en los tests ──────────────────────────

    private Carrera guardarCarrera(String nombre) {
        Carrera c = new Carrera(nombre);
        repository.saveCarrera(c);
        return c;
    }

    private Materia guardarMateria(String nombre, int anio, boolean obligatoria, Carrera carrera) {
        Materia m = new Materia(nombre, anio, obligatoria, carrera);
        repository.saveMateria(m);
        return m;
    }

    private Alumno guardarAlumno(String nombre, String apellido, String dni, boolean activo) {
        Alumno a = new Alumno(nombre, apellido, dni, activo);
        repository.saveAlumno(a);
        return a;
    }

    private Alumno guardarAlumnoConPerfil(String nombre, String apellido, String dni, String email, String tel) {
        Alumno a = new Alumno(nombre, apellido, dni, true);
        a.setPerfil(new Perfil(email, tel));
        repository.saveAlumno(a);
        return a;
    }


    // ═══════════════════════════════════════════════════════════════════════
    // CRUD básico
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void saveCarrera_findCarreraById_retornaCarreraCorrecta() {
        Carrera carrera = guardarCarrera("Ingeniería en Sistemas");

        Carrera encontrada = repository.findCarreraById(carrera.getId());

        assertNotNull(encontrada);
        assertEquals("Ingeniería en Sistemas", encontrada.getNombre());
    }

    @Test
    void findAllCarreras_retornaTodasLasInsertadas() {
        guardarCarrera("Ingeniería en Sistemas");
        guardarCarrera("Contaduría Pública");
        guardarCarrera("Administración de Empresas");

        List<Carrera> carreras = repository.findAllCarreras();

        assertEquals(3, carreras.size());
    }

    @Test
    void findCarreraById_idInexistente_retornaNull() {
        Carrera resultado = repository.findCarreraById(9999);
        assertNull(resultado);
    }

    @Test
    void saveMateria_findMateriaById_retornaMateriaConCarreraAsociada() {
        // La relación @ManyToOne se verifica: la Materia debe traer su Carrera
        Carrera carrera = guardarCarrera("Ingeniería en Sistemas");
        Materia materia = guardarMateria("Programación 1", 1, true, carrera);

        Materia encontrada = repository.findMateriaById(materia.getId());

        assertNotNull(encontrada);
        assertEquals("Programación 1", encontrada.getNombre());
        assertEquals(1, encontrada.getAnio());
        assertTrue(encontrada.isObligatoria());
        // @ManyToOne con FetchType.EAGER: la Carrera se carga junto con la Materia
        assertNotNull(encontrada.getCarrera());
        assertEquals("Ingeniería en Sistemas", encontrada.getCarrera().getNombre());
    }

    @Test
    void saveAlumno_conPerfil_cascade_guardaPerfilAutomaticamente() {
        // CascadeType.ALL en Alumno.perfil: persist(alumno) también persiste el Perfil.
        // No es necesario guardar el Perfil por separado (sección 6.1).
        Alumno alumno = new Alumno("Ana", "García", "30000001", true);
        alumno.setPerfil(new Perfil("ana@mail.com", "3512345678"));
        repository.saveAlumno(alumno);

        Alumno encontrado = repository.findAlumnoById(alumno.getId());

        assertNotNull(encontrado.getPerfil());
        assertEquals("ana@mail.com", encontrado.getPerfil().getEmail());
        assertEquals("3512345678", encontrado.getPerfil().getTelefono());
    }

    @Test
    void saveAlumno_sinPerfil_perfilIdEsNull() {
        // La FK perfil_id puede ser NULL: un alumno sin datos de contacto
        Alumno alumno = new Alumno("Sin", "Perfil", "30000002", true);
        repository.saveAlumno(alumno);

        Alumno encontrado = repository.findAlumnoById(alumno.getId());

        assertNotNull(encontrado);
        assertNull(encontrado.getPerfil());
    }

    @Test
    void updateAlumno_modificaCampoActivo() {
        Alumno alumno = guardarAlumno("Laura", "Martínez", "30000003", true);

        alumno.setActivo(false);
        repository.updateAlumno(alumno);

        Alumno actualizado = repository.findAlumnoById(alumno.getId());
        assertFalse(actualizado.isActivo());
    }

    @Test
    void inscribirAlumno_creaFilaEnTablaInscripciones() {
        // Relación N a N (sección 6.3): Hibernate inserta la fila en la tabla
        // intermedia "inscripciones" al agregar la materia a la colección del alumno.
        Carrera carrera = guardarCarrera("Test");
        Materia materia = guardarMateria("Programación 1", 1, true, carrera);
        Alumno  alumno  = guardarAlumno("Carlos", "López", "30000004", true);

        repository.inscribirAlumno(alumno.getId(), materia.getId());

        // Verificamos la relación desde el lado del alumno
        List<Alumno> inscriptos = repository.findAlumnosByNombreMateria("Programación 1");
        assertEquals(1, inscriptos.size());
        assertEquals("Carlos", inscriptos.get(0).getNombre());
    }

    @Test
    void inscribirAlumno_mismoParDosVeces_noGeneraDuplicado() {
        // La PK compuesta (alumno_id, materia_id) en inscripciones evita duplicados.
        // El método del Repository verifica contains() antes de agregar.
        Carrera carrera = guardarCarrera("Test");
        Materia materia = guardarMateria("Bases de Datos", 2, true, carrera);
        Alumno  alumno  = guardarAlumno("Pedro", "Ruiz", "30000005", true);

        repository.inscribirAlumno(alumno.getId(), materia.getId());
        repository.inscribirAlumno(alumno.getId(), materia.getId()); // segunda vez: no-op

        List<Alumno> inscriptos = repository.findAlumnosByNombreMateria("Bases de Datos");
        assertEquals(1, inscriptos.size());
    }


    // ═══════════════════════════════════════════════════════════════════════
    // 7.1 — Igualdad: cb.equal(campo, valor)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void findAlumnosActivos_retornaSoloConActivoTrue() {
        guardarAlumno("Activo1",   "Test", "30001001", true);
        guardarAlumno("Activo2",   "Test", "30001002", true);
        guardarAlumno("Inactivo1", "Test", "30001003", false);
        guardarAlumno("Inactivo2", "Test", "30001004", false);

        List<Alumno> activos = repository.findAlumnosActivos();

        assertEquals(2, activos.size());
        assertTrue(activos.stream().allMatch(Alumno::isActivo));
    }

    @Test
    void findAlumnosActivos_sinActivos_retornaListaVacia() {
        guardarAlumno("Inactivo", "Test", "30001005", false);

        List<Alumno> activos = repository.findAlumnosActivos();

        assertTrue(activos.isEmpty());
    }


    // ═══════════════════════════════════════════════════════════════════════
    // 7.2 — Comparaciones numéricas: cb.between(campo, min, max)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void findMateriasByRangoAnio_between_incluyeExtremos() {
        Carrera c = guardarCarrera("Test");
        guardarMateria("Año 1", 1, true, c);
        guardarMateria("Año 2", 2, true, c);
        guardarMateria("Año 3", 3, true, c);
        guardarMateria("Año 4", 4, true, c);

        // BETWEEN 2 AND 3: incluye los extremos (años 2 y 3)
        List<Materia> resultado = repository.findMateriasByRangoAnio(2, 3);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(m -> m.getAnio() >= 2 && m.getAnio() <= 3));
    }

    @Test
    void findMateriasByRangoAnio_fueraDeLRango_retornaVacio() {
        Carrera c = guardarCarrera("Test");
        guardarMateria("Año 1", 1, true, c);
        guardarMateria("Año 2", 2, true, c);

        List<Materia> resultado = repository.findMateriasByRangoAnio(4, 5);

        assertTrue(resultado.isEmpty());
    }


    // ═══════════════════════════════════════════════════════════════════════
    // 7.3 — LIKE: cb.like(cb.lower(campo), "%valor%")
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void findMateriasByNombre_like_retornaCoincidenciasParciales() {
        Carrera c = guardarCarrera("Test");
        guardarMateria("Programación 1",        1, true, c);
        guardarMateria("Programación 2",        2, true, c);
        guardarMateria("Bases de Datos",        2, true, c);
        guardarMateria("Redes de Computadoras", 3, true, c);

        List<Materia> resultado = repository.findMateriasByNombre("programación");

        assertEquals(2, resultado.size());
    }

    @Test
    void findMateriasByNombre_like_esCaseInsensitive() {
        Carrera c = guardarCarrera("Test");
        guardarMateria("Programación 1", 1, true, c);

        // Buscar en mayúsculas debe encontrar igual (cb.lower en ambos lados)
        List<Materia> resultado = repository.findMateriasByNombre("PROGRAMACIÓN");

        assertEquals(1, resultado.size());
    }

    @Test
    void findMateriasByNombre_sinCoincidencias_retornaListaVacia() {
        Carrera c = guardarCarrera("Test");
        guardarMateria("Programación 1", 1, true, c);

        List<Materia> resultado = repository.findMateriasByNombre("zzznada");

        assertTrue(resultado.isEmpty());
    }


    // ═══════════════════════════════════════════════════════════════════════
    // 7.4 — IS NULL: cb.isNull(campo)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void findAlumnosSinPerfil_isNull_retornaSoloLosQueTienenPerfilNull() {
        guardarAlumnoConPerfil("Con",  "Perfil", "30002001", "con@mail.com", "123");
        guardarAlumno("Sin",   "Perfil",  "30002002", true);
        guardarAlumno("Sin2",  "Perfil",  "30002003", true);

        List<Alumno> sinPerfil = repository.findAlumnosSinPerfil();

        assertEquals(2, sinPerfil.size());
        assertTrue(sinPerfil.stream().allMatch(a -> a.getPerfil() == null));
    }

    @Test
    void findAlumnosSinPerfil_todosConPerfil_retornaVacio() {
        guardarAlumnoConPerfil("A", "A", "30002004", "a@a.com", "1");
        guardarAlumnoConPerfil("B", "B", "30002005", "b@b.com", "2");

        List<Alumno> sinPerfil = repository.findAlumnosSinPerfil();

        assertTrue(sinPerfil.isEmpty());
    }


    // ═══════════════════════════════════════════════════════════════════════
    // 7.5 — IN: root.get("campo").in(lista)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void findMateriasByAnios_in_retornaSoloLosAniosIndicados() {
        Carrera c = guardarCarrera("Test");
        guardarMateria("M1", 1, true, c);
        guardarMateria("M2", 2, true, c);
        guardarMateria("M3", 3, true, c);
        guardarMateria("M5", 5, true, c);

        // IN (1, 3): debe excluir los años 2 y 5
        List<Materia> resultado = repository.findMateriasByAnios(List.of(1, 3));

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(m -> m.getAnio() == 1 || m.getAnio() == 3));
    }

    @Test
    void findMateriasByAnios_listaVacia_retornaVacio() {
        Carrera c = guardarCarrera("Test");
        guardarMateria("M1", 1, true, c);

        // H2/Hibernate maneja IN() vacío retornando cero resultados
        List<Materia> resultado = repository.findMateriasByAnios(List.of());

        assertTrue(resultado.isEmpty());
    }


    // ═══════════════════════════════════════════════════════════════════════
    // 7.6 — OR: cb.or(predicado1, predicado2)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void findAlumnosByNombreOrApellido_or_encuentraPorNombre() {
        guardarAlumno("Carlos",  "López",     "30003001", true);
        guardarAlumno("Ana",     "García",    "30003002", true);
        guardarAlumno("Diego",   "Fernández", "30003003", true);

        // "car" solo coincide en el nombre de Carlos
        List<Alumno> resultado = repository.findAlumnosByNombreOrApellido("car");

        assertEquals(1, resultado.size());
        assertEquals("Carlos", resultado.get(0).getNombre());
    }

    @Test
    void findAlumnosByNombreOrApellido_or_encuentraPorApellido() {
        guardarAlumno("Ana",   "García", "30003004", true);
        guardarAlumno("Pedro", "García", "30003005", true);
        guardarAlumno("Luis",  "Pérez",  "30003006", true);

        // "garcía" coincide en el apellido de Ana y Pedro
        List<Alumno> resultado = repository.findAlumnosByNombreOrApellido("garcía");

        assertEquals(2, resultado.size());
    }

    @Test
    void findAlumnosByNombreOrApellido_or_encuentraPorNombreYApellido() {
        guardarAlumno("Martín", "García",  "30003007", true); // "mart" en nombre
        guardarAlumno("Ana",    "Martínez","30003008", true); // "mart" en apellido
        guardarAlumno("Luis",   "Pérez",   "30003009", true); // ninguna

        List<Alumno> resultado = repository.findAlumnosByNombreOrApellido("mart");

        assertEquals(2, resultado.size());
    }


    // ═══════════════════════════════════════════════════════════════════════
    // 7.7 — Filtros dinámicos: List<Predicate> acumulados en tiempo de ejecución
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void buscarMaterias_sinFiltros_traeTodo() {
        Carrera c = guardarCarrera("Test");
        guardarMateria("M1", 1, true,  c);
        guardarMateria("M2", 2, false, c);
        guardarMateria("M3", 3, true,  c);

        // cb.and() con lista vacía no agrega restricciones → trae todo
        List<Materia> resultado = repository.buscarMaterias(null, null, null, null);

        assertEquals(3, resultado.size());
    }

    @Test
    void buscarMaterias_soloFiltroObligatoria_retornaObligatorias() {
        Carrera c = guardarCarrera("Test");
        guardarMateria("Obligatoria 1", 1, true,  c);
        guardarMateria("Obligatoria 2", 2, true,  c);
        guardarMateria("Optativa",      3, false, c);

        List<Materia> resultado = repository.buscarMaterias(null, null, null, true);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Materia::isObligatoria));
    }

    @Test
    void buscarMaterias_soloFiltroNombre_retornaCoincidencias() {
        Carrera c = guardarCarrera("Test");
        guardarMateria("Programación 1", 1, true, c);
        guardarMateria("Programación 2", 2, true, c);
        guardarMateria("Bases de Datos", 2, true, c);

        List<Materia> resultado = repository.buscarMaterias("programación", null, null, null);

        assertEquals(2, resultado.size());
    }

    @Test
    void buscarMaterias_nombreYAnioMaxCombinados_aplicaAnd() {
        Carrera c = guardarCarrera("Test");
        guardarMateria("Programación 1", 1, true, c); // cumple ambos
        guardarMateria("Programación 2", 2, true, c); // cumple ambos
        guardarMateria("Programación 4", 4, true, c); // cumple nombre, no anio
        guardarMateria("Bases de Datos", 1, true, c); // cumple anio, no nombre

        // nombre LIKE "%prog%" AND anio <= 2 → solo los dos primeros
        List<Materia> resultado = repository.buscarMaterias("programación", null, 2, null);

        assertEquals(2, resultado.size());
    }

    @Test
    void buscarMaterias_anioMinYanioMax_actúaComoRango() {
        Carrera c = guardarCarrera("Test");
        guardarMateria("M1", 1, true, c);
        guardarMateria("M2", 2, true, c);
        guardarMateria("M3", 3, true, c);
        guardarMateria("M5", 5, true, c);

        // anioMin=2, anioMax=3 → anio >= 2 AND anio <= 3
        List<Materia> resultado = repository.buscarMaterias(null, 2, 3, null);

        assertEquals(2, resultado.size());
    }


    // ═══════════════════════════════════════════════════════════════════════
    // 7.8 — JOIN: root.join("relacion") para navegar entidades relacionadas
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void findMateriasByNombreCarrera_join1N_filtraPorCampoRelacionado() {
        // JOIN Materia → Carrera (relación @ManyToOne), filtra por Carrera.nombre
        Carrera ing = guardarCarrera("Ingeniería en Sistemas");
        Carrera lic = guardarCarrera("Licenciatura en Sistemas");
        guardarMateria("Programación 1", 1, true, ing);
        guardarMateria("Programación 2", 2, true, ing);
        guardarMateria("Análisis",       2, true, lic);

        List<Materia> resultado = repository.findMateriasByNombreCarrera("ingeniería");

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream()
            .allMatch(m -> m.getCarrera().getNombre().contains("Ingeniería")));
    }

    @Test
    void findMateriasByNombreCarrera_join1N_sinCoincidencias_retornaVacio() {
        Carrera c = guardarCarrera("Ingeniería en Sistemas");
        guardarMateria("M1", 1, true, c);

        List<Materia> resultado = repository.findMateriasByNombreCarrera("medicina");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findAlumnosByNombreMateria_joinNN_navegaTablaIntermedia() {
        // JOIN N a N: Hibernate genera el JOIN con "inscripciones" y luego con "materias"
        Carrera c    = guardarCarrera("Test");
        Materia prog = guardarMateria("Programación 1", 1, true, c);
        Materia bd   = guardarMateria("Bases de Datos",  2, true, c);

        Alumno ana    = guardarAlumno("Ana",    "García", "30004001", true);
        Alumno carlos = guardarAlumno("Carlos", "López",  "30004002", true);
        Alumno laura  = guardarAlumno("Laura",  "Pérez",  "30004003", true);

        repository.inscribirAlumno(ana.getId(),    prog.getId()); // Ana → Prog 1
        repository.inscribirAlumno(carlos.getId(), prog.getId()); // Carlos → Prog 1
        repository.inscribirAlumno(laura.getId(),  bd.getId());   // Laura → BD (no debe aparecer)

        List<Alumno> inscriptos = repository.findAlumnosByNombreMateria("Programación 1");

        assertEquals(2, inscriptos.size());
    }

    @Test
    void findAlumnosByNombreMateria_alumnoInscriptoEnVarias_apareceUnaVez() {
        // distinct=true en la query: un alumno inscripto en varias materias
        // que coinciden no debe aparecer duplicado.
        Carrera c  = guardarCarrera("Test");
        Materia m1 = guardarMateria("Programación 1", 1, true, c);
        Materia m2 = guardarMateria("Programación 2", 2, true, c);
        Alumno  a  = guardarAlumno("Ana", "García", "30004004", true);

        repository.inscribirAlumno(a.getId(), m1.getId());
        repository.inscribirAlumno(a.getId(), m2.getId());

        // "programación" coincide con ambas materias, pero Ana debe aparecer una sola vez
        List<Alumno> resultado = repository.findAlumnosByNombreMateria("programación");

        assertEquals(1, resultado.size());
    }


    // ═══════════════════════════════════════════════════════════════════════
    // 7.9 — GROUP BY y HAVING
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void countMateriasPorCarrera_groupBy_retornaTotalCorrecto() {
        Carrera c1 = guardarCarrera("Carrera A");
        Carrera c2 = guardarCarrera("Carrera B");
        guardarMateria("M1", 1, true, c1);
        guardarMateria("M2", 2, true, c1);
        guardarMateria("M3", 1, true, c2);

        List<Object[]> resultado = repository.countMateriasPorCarrera();

        // Debe haber dos filas (una por carrera)
        assertEquals(2, resultado.size());
        // Orden DESC por count: Carrera A (2 materias) primero
        assertEquals("Carrera A", resultado.get(0)[0]);
        assertEquals(2L, resultado.get(0)[1]);
        assertEquals("Carrera B", resultado.get(1)[0]);
        assertEquals(1L, resultado.get(1)[1]);
    }

    @Test
    void carrerasConMasDe2Materias_having_filtraGruposCorrectamente() {
        Carrera c1 = guardarCarrera("Con 3 materias");
        Carrera c2 = guardarCarrera("Con 2 materias");
        Carrera c3 = guardarCarrera("Con 1 materia");
        guardarMateria("M1", 1, true, c1);
        guardarMateria("M2", 2, true, c1);
        guardarMateria("M3", 3, true, c1);
        guardarMateria("M4", 1, true, c2);
        guardarMateria("M5", 2, true, c2);
        guardarMateria("M6", 1, true, c3);

        // HAVING COUNT(*) > 2 → solo "Con 3 materias" supera el umbral
        List<Object[]> resultado = repository.carrerasConMasDe2Materias();

        assertEquals(1, resultado.size());
        assertEquals("Con 3 materias", resultado.get(0)[0]);
        assertEquals(3L, resultado.get(0)[1]);
    }

    @Test
    void carrerasConMasDe2Materias_ningunaCalifica_retornaVacio() {
        Carrera c = guardarCarrera("Solo 2 materias");
        guardarMateria("M1", 1, true, c);
        guardarMateria("M2", 2, true, c);

        List<Object[]> resultado = repository.carrerasConMasDe2Materias();

        assertTrue(resultado.isEmpty());
    }


    // ═══════════════════════════════════════════════════════════════════════
    // 7.10 — Ordenamiento y paginación
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void findAlumnosOrdenadosPaginados_ordenaPorApellido_paginaCorrectamente() {
        // Se usan apellidos sin tildes para evitar diferencias de collation entre H2 y MySQL.
        // El objetivo del test es verificar paginación y orden ASC, no colación de caracteres.
        guardarAlumno("X", "Zapata",  "30005001", true);
        guardarAlumno("X", "Alvarez", "30005002", true);
        guardarAlumno("X", "Morales", "30005003", true);
        guardarAlumno("X", "Benitez", "30005004", true);

        // Página 0 (primeros 2 en orden ASC): Alvarez, Benitez
        List<Alumno> pagina0 = repository.findAlumnosOrdenadosPaginados(0, 2);
        // Página 1 (segundos 2): Morales, Zapata
        List<Alumno> pagina1 = repository.findAlumnosOrdenadosPaginados(1, 2);

        assertEquals(2, pagina0.size());
        assertEquals(2, pagina1.size());
        assertEquals("Alvarez", pagina0.get(0).getApellido());
        assertEquals("Benitez", pagina0.get(1).getApellido());
        assertEquals("Morales", pagina1.get(0).getApellido());
        assertEquals("Zapata",  pagina1.get(1).getApellido());
    }

    @Test
    void findAlumnosOrdenadosPaginados_paginaMayor_retornaVacio() {
        guardarAlumno("X", "García", "30005005", true);

        // Página 1 con tamaño 10: no hay suficientes registros
        List<Alumno> resultado = repository.findAlumnosOrdenadosPaginados(1, 10);

        assertTrue(resultado.isEmpty());
    }
}

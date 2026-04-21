package org.example.data;

import jakarta.persistence.criteria.*;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// CAPA DE DATOS — Repository (patrón Singleton)
//
// Concentra todas las operaciones de acceso a la base de datos.
// El resto de la aplicación (Service, Main) no conoce Hibernate ni Session.
//
// Este archivo tiene dos grandes bloques:
//   1. Operaciones CRUD básicas para cada entidad.
//   2. Consultas con CriteriaBuilder (secciones 7.1 a 7.10 de la teoría).
// ─────────────────────────────────────────────────────────────────────────────
public class Repository {

    private static final Repository INSTANCE = new Repository();
    private Repository() {}
    public static Repository getInstance() { return INSTANCE; }


    // ═══════════════════════════════════════════════════════════════════════
    // Carrera — CRUD
    // ═══════════════════════════════════════════════════════════════════════

    public void saveCarrera(Carrera carrera) {
        try (Session session = HibernateUtil.getSession()) {
            session.beginTransaction();
            session.persist(carrera);
            session.getTransaction().commit();
        }
    }

    public Carrera findCarreraById(Integer id) {
        try (Session session = HibernateUtil.getSession()) {
            return session.get(Carrera.class, id);
        }
    }

    public List<Carrera> findAllCarreras() {
        try (Session session = HibernateUtil.getSession()) {
            return session.createQuery("FROM Carrera", Carrera.class).getResultList();
        }
    }


    // ═══════════════════════════════════════════════════════════════════════
    // Materia — CRUD
    // ═══════════════════════════════════════════════════════════════════════

    public void saveMateria(Materia materia) {
        try (Session session = HibernateUtil.getSession()) {
            session.beginTransaction();
            session.persist(materia);
            session.getTransaction().commit();
        }
    }

    public Materia findMateriaById(Integer id) {
        try (Session session = HibernateUtil.getSession()) {
            return session.get(Materia.class, id);
        }
    }

    public List<Materia> findAllMaterias() {
        try (Session session = HibernateUtil.getSession()) {
            return session.createQuery("FROM Materia", Materia.class).getResultList();
        }
    }


    // ═══════════════════════════════════════════════════════════════════════
    // Alumno — CRUD
    // ═══════════════════════════════════════════════════════════════════════

    public void saveAlumno(Alumno alumno) {
        try (Session session = HibernateUtil.getSession()) {
            session.beginTransaction();
            // CascadeType.ALL en Alumno.perfil: si el alumno tiene un Perfil nuevo
            // (sin id), Hibernate lo persiste automáticamente antes del Alumno.
            session.persist(alumno);
            session.getTransaction().commit();
        }
    }

    public void updateAlumno(Alumno alumno) {
        try (Session session = HibernateUtil.getSession()) {
            session.beginTransaction();
            session.merge(alumno);
            session.getTransaction().commit();
        }
    }

    public Alumno findAlumnoById(Integer id) {
        try (Session session = HibernateUtil.getSession()) {
            return session.get(Alumno.class, id);
        }
    }

    public List<Alumno> findAllAlumnos() {
        try (Session session = HibernateUtil.getSession()) {
            return session.createQuery("FROM Alumno", Alumno.class).getResultList();
        }
    }

    // Inscribe un alumno en una materia (gestiona la tabla intermedia N:N).
    // Alumno y Materia deben cargarse en la misma sesión para que Hibernate
    // los considere "managed" y detecte el cambio en la colección.
    public void inscribirAlumno(Integer alumnoId, Integer materiaId) {
        try (Session session = HibernateUtil.getSession()) {
            session.beginTransaction();
            Alumno alumno   = session.get(Alumno.class, alumnoId);
            Materia materia = session.get(Materia.class, materiaId);
            if (alumno != null && materia != null && !alumno.getMaterias().contains(materia)) {
                // Al agregar la materia a la lista y hacer commit, Hibernate inserta
                // una fila en la tabla "inscripciones" (alumno_id, materia_id).
                alumno.getMaterias().add(materia);
            }
            session.getTransaction().commit();
        }
    }


    // ═══════════════════════════════════════════════════════════════════════
    // CRITERIABUILDER — Sección 7 de la teoría
    //
    // Estructura base (igual en todos los métodos):
    //   CriteriaBuilder cb  = session.getCriteriaBuilder();   // fábrica
    //   CriteriaQuery<T> cq = cb.createQuery(T.class);        // consulta
    //   Root<T> root        = cq.from(T.class);               // tabla raíz
    //   cq.select(root).where(predicado);
    //   session.createQuery(cq).getResultList();
    // ═══════════════════════════════════════════════════════════════════════

    // ── 7.1 Igualdad ─────────────────────────────────────────────────────────
    // cb.equal(campo, valor) → WHERE activo = true
    public List<Alumno> findAlumnosActivos() {
        try (Session session = HibernateUtil.getSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Alumno> cq = cb.createQuery(Alumno.class);
            Root<Alumno> root = cq.from(Alumno.class);

            Predicate soloActivos = cb.equal(root.get("activo"), true);
            cq.select(root).where(soloActivos);

            return session.createQuery(cq).getResultList();
        }
    }

    // ── 7.2 Comparaciones numéricas (BETWEEN) ────────────────────────────────
    // cb.between(campo, min, max) → WHERE anio BETWEEN anioMin AND anioMax
    public List<Materia> findMateriasByRangoAnio(int anioMin, int anioMax) {
        try (Session session = HibernateUtil.getSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Materia> cq = cb.createQuery(Materia.class);
            Root<Materia> root = cq.from(Materia.class);

            Predicate rango = cb.between(root.get("anio"), anioMin, anioMax);
            cq.select(root).where(rango);

            return session.createQuery(cq).getResultList();
        }
    }

    // ── 7.3 Búsqueda parcial de texto (LIKE) ─────────────────────────────────
    // cb.like(cb.lower(campo), "%valor%") → WHERE LOWER(nombre) LIKE '%valor%'
    // Se convierte a minúsculas en ambos lados para búsqueda case-insensitive.
    public List<Materia> findMateriasByNombre(String nombre) {
        try (Session session = HibernateUtil.getSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Materia> cq = cb.createQuery(Materia.class);
            Root<Materia> root = cq.from(Materia.class);

            Predicate like = cb.like(
                cb.lower(root.get("nombre")),
                "%" + nombre.toLowerCase() + "%"
            );
            cq.select(root).where(like);

            return session.createQuery(cq).getResultList();
        }
    }

    // ── 7.4 Valores nulos (IS NULL) ──────────────────────────────────────────
    // cb.isNull(campo) → WHERE perfil_id IS NULL
    // Encuentra alumnos que no tienen perfil de contacto asignado.
    public List<Alumno> findAlumnosSinPerfil() {
        try (Session session = HibernateUtil.getSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Alumno> cq = cb.createQuery(Alumno.class);
            Root<Alumno> root = cq.from(Alumno.class);

            Predicate sinPerfil = cb.isNull(root.get("perfil"));
            cq.select(root).where(sinPerfil);

            return session.createQuery(cq).getResultList();
        }
    }

    // ── 7.5 Listas de valores (IN) ────────────────────────────────────────────
    // root.get("campo").in(lista) → WHERE anio IN (1, 2)
    public List<Materia> findMateriasByAnios(List<Integer> anios) {
        try (Session session = HibernateUtil.getSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Materia> cq = cb.createQuery(Materia.class);
            Root<Materia> root = cq.from(Materia.class);

            Predicate enAnios = root.get("anio").in(anios);
            cq.select(root).where(enAnios);

            return session.createQuery(cq).getResultList();
        }
    }

    // ── 7.6 Combinación OR ────────────────────────────────────────────────────
    // cb.or(p1, p2) → WHERE LOWER(nombre) LIKE '%x%' OR LOWER(apellido) LIKE '%x%'
    public List<Alumno> findAlumnosByNombreOrApellido(String texto) {
        try (Session session = HibernateUtil.getSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Alumno> cq = cb.createQuery(Alumno.class);
            Root<Alumno> root = cq.from(Alumno.class);

            String patron = "%" + texto.toLowerCase() + "%";
            Predicate porNombre   = cb.like(cb.lower(root.get("nombre")),   patron);
            Predicate porApellido = cb.like(cb.lower(root.get("apellido")), patron);

            cq.select(root).where(cb.or(porNombre, porApellido));

            return session.createQuery(cq).getResultList();
        }
    }

    // ── 7.7 Filtros dinámicos (opcionales) ───────────────────────────────────
    // Se construye la lista de predicados en tiempo de ejecución.
    // Solo se agrega un predicado si el parámetro no es null.
    // cb.and(lista) sin elementos no agrega restricciones → trae todo.
    public List<Materia> buscarMaterias(String nombre, Integer anioMin, Integer anioMax, Boolean obligatoria) {
        try (Session session = HibernateUtil.getSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Materia> cq = cb.createQuery(Materia.class);
            Root<Materia> root = cq.from(Materia.class);

            List<Predicate> predicados = new ArrayList<>();

            if (nombre != null && !nombre.isBlank()) {
                predicados.add(cb.like(
                    cb.lower(root.get("nombre")),
                    "%" + nombre.toLowerCase() + "%"
                ));
            }
            if (anioMin != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("anio"), anioMin));
            }
            if (anioMax != null) {
                predicados.add(cb.lessThanOrEqualTo(root.get("anio"), anioMax));
            }
            if (obligatoria != null) {
                predicados.add(cb.equal(root.get("obligatoria"), obligatoria));
            }

            cq.select(root).where(cb.and(predicados.toArray(new Predicate[0])));
            return session.createQuery(cq).getResultList();
        }
    }

    // ── 7.8 JOIN con entidad relacionada ─────────────────────────────────────
    // root.join("carrera") genera un INNER JOIN entre materias y carreras.
    // Permite filtrar por campos de la entidad relacionada (Carrera.nombre).
    public List<Materia> findMateriasByNombreCarrera(String nombreCarrera) {
        try (Session session = HibernateUtil.getSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Materia> cq = cb.createQuery(Materia.class);
            Root<Materia> materiaRoot = cq.from(Materia.class);

            // JOIN: navega de Materia → Carrera (relación @ManyToOne)
            Join<Materia, Carrera> carreraJoin = materiaRoot.join("carrera");

            // Filtra por el nombre de la Carrera (campo de la entidad relacionada)
            Predicate filtro = cb.like(
                cb.lower(carreraJoin.get("nombre")),
                "%" + nombreCarrera.toLowerCase() + "%"
            );

            cq.select(materiaRoot).where(filtro);
            return session.createQuery(cq).getResultList();
        }
    }

    // 7.8 — JOIN en relación N a N
    // Navega de Alumno → Materia a través de la tabla intermedia inscripciones.
    // JoinType.INNER: solo devuelve alumnos que estén inscriptos en esa materia.
    public List<Alumno> findAlumnosByNombreMateria(String nombreMateria) {
        try (Session session = HibernateUtil.getSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Alumno> cq = cb.createQuery(Alumno.class);
            Root<Alumno> alumnoRoot = cq.from(Alumno.class);

            // JOIN N a N: Hibernate genera el join con la tabla inscripciones
            // y luego con materias, en una sola consulta SQL.
            Join<Alumno, Materia> materiaJoin = alumnoRoot.join("materias", JoinType.INNER);

            Predicate filtro = cb.like(
                cb.lower(materiaJoin.get("nombre")),
                "%" + nombreMateria.toLowerCase() + "%"
            );

            // distinct=true: evita duplicados cuando un alumno tiene múltiples materias
            cq.select(alumnoRoot).where(filtro).distinct(true);
            return session.createQuery(cq).getResultList();
        }
    }

    // ── 7.9 GROUP BY — COUNT por carrera ─────────────────────────────────────
    // Retorna Object[]: [nombreCarrera (String), cantidad (Long)]
    // Equivale a: SELECT c.nombre, COUNT(m) FROM Materia m JOIN m.carrera c GROUP BY c.nombre
    public List<Object[]> countMateriasPorCarrera() {
        try (Session session = HibernateUtil.getSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
            Root<Materia> root = cq.from(Materia.class);

            // LEFT JOIN para incluir carreras sin materias (JoinType.LEFT)
            Join<Materia, Carrera> carreraJoin = root.join("carrera", JoinType.LEFT);

            cq.multiselect(
                carreraJoin.get("nombre"),  // campo de agrupamiento
                cb.count(root)              // COUNT(*)
            );
            cq.groupBy(carreraJoin.get("nombre"));
            cq.orderBy(cb.desc(cb.count(root)));

            return session.createQuery(cq).getResultList();
        }
    }

    // ── 7.9 GROUP BY + HAVING ─────────────────────────────────────────────────
    // HAVING filtra sobre el resultado del grupo, no sobre filas individuales.
    // WHERE filtraría ANTES de agrupar; HAVING filtra DESPUÉS.
    // Solo devuelve carreras con MÁS de 2 materias.
    public List<Object[]> carrerasConMasDe2Materias() {
        try (Session session = HibernateUtil.getSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
            Root<Materia> root = cq.from(Materia.class);

            Join<Materia, Carrera> carreraJoin = root.join("carrera");

            // Guardar la expresión COUNT para reusarla en select, groupBy y having
            Expression<Long> cantidad = cb.count(root);

            cq.multiselect(carreraJoin.get("nombre"), cantidad);
            cq.groupBy(carreraJoin.get("nombre"));

            // HAVING COUNT(*) > 2
            cq.having(cb.greaterThan(cantidad, 2L));

            return session.createQuery(cq).getResultList();
        }
    }

    // ── 7.10 Ordenamiento y paginación ────────────────────────────────────────
    // ORDER BY apellido ASC, nombre ASC + LIMIT/OFFSET para paginación.
    // pagina=0 → primera página, pagina=1 → segunda, etc.
    public List<Alumno> findAlumnosOrdenadosPaginados(int pagina, int tamanio) {
        try (Session session = HibernateUtil.getSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Alumno> cq = cb.createQuery(Alumno.class);
            Root<Alumno> root = cq.from(Alumno.class);

            cq.select(root).orderBy(
                cb.asc(root.get("apellido")),
                cb.asc(root.get("nombre"))
            );

            return session.createQuery(cq)
                .setFirstResult(pagina * tamanio)   // OFFSET
                .setMaxResults(tamanio)              // LIMIT
                .getResultList();
        }
    }
}

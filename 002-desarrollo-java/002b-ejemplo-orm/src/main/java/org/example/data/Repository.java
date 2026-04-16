package org.example.data;

import org.hibernate.Session;

import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// CAPA DE DATOS — Repository (patrón Singleton)
//
// El Repository concentra todas las operaciones de acceso a la base de datos
// de la aplicación en un único archivo.
//
// Se implementa como Singleton para garantizar que exista una sola instancia
// durante toda la ejecución. Esto evita crear múltiples objetos innecesarios
// y es consistente con el ciclo de vida de la SessionFactory.
//
// Por qué un archivo por entidad en proyectos reales:
//   En aplicaciones con muchas entidades (Alumno, Materia, Inscripción, etc.)
//   conviene tener un AlumnoRepository, MateriaRepository, etc. para mantener
//   el código organizado. En este ejemplo hay una sola entidad, así que todo
//   queda en un único Repository.
//
// El resto de la aplicación (Service, Main) no conoce Hibernate ni Session:
// solo llama a los métodos de este Repository.
// ─────────────────────────────────────────────────────────────────────────────
public class Repository {

    // Única instancia de la clase. Es static para que pertenezca a la clase,
    // no a ningún objeto en particular.
    private static final Repository INSTANCE = new Repository();

    // Constructor privado: impide que otras clases creen instancias con new Repository().
    private Repository() {}

    // Método de acceso global a la única instancia.
    public static Repository getInstance() {
        return INSTANCE;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // User
    // ═══════════════════════════════════════════════════════════════════════

    // ── INSERT ────────────────────────────────────────────────────────────────
    public void saveUser(User user) {
        // try-with-resources garantiza que la Session se cierre al terminar,
        // incluso si ocurre una excepción.
        try (Session session = HibernateUtil.getSession()) {

            // Las operaciones que modifican datos (INSERT, UPDATE, DELETE)
            // deben ejecutarse dentro de una transacción.
            // Si algo falla antes del commit(), los cambios se descartan.
            session.beginTransaction();

            // persist() toma un objeto nuevo (sin id) y genera un INSERT.
            // Después del commit, Hibernate rellena el campo id con el valor
            // asignado por AUTO_INCREMENT.
            session.persist(user);

            session.getTransaction().commit();
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    public void updateUser(User user) {
        try (Session session = HibernateUtil.getSession()) {
            session.beginTransaction();

            // merge() se usa para actualizar un objeto que ya tiene id pero
            // que fue modificado fuera de una sesión activa ("detached").
            // Hibernate genera un UPDATE con los valores actuales del objeto.
            session.merge(user);

            session.getTransaction().commit();
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    public void deleteUser(Integer id) {
        try (Session session = HibernateUtil.getSession()) {
            session.beginTransaction();

            // Para eliminar, Hibernate necesita un objeto "managed" (gestionado
            // por la sesión actual). Por eso primero lo buscamos con get()
            // y luego lo pasamos a remove().
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
            }

            session.getTransaction().commit();
        }
    }

    // ── SELECT por id ─────────────────────────────────────────────────────────
    public User findUserById(Integer id) {
        try (Session session = HibernateUtil.getSession()) {

            // Las consultas de solo lectura no necesitan transacción explícita.
            // session.get() genera un SELECT WHERE id = ? y retorna null
            // si no existe ninguna fila con ese id.
            return session.get(User.class, id);
        }
    }

    // ── SELECT todos ──────────────────────────────────────────────────────────
    public List<User> findAllUsers() {
        try (Session session = HibernateUtil.getSession()) {

            // createQuery usa JPQL (Java Persistence Query Language), no SQL.
            // "FROM User" equivale a "SELECT * FROM users" pero opera sobre
            // la clase Java User, no directamente sobre la tabla.
            // Hibernate traduce esto al SQL específico del motor configurado.
            return session.createQuery("FROM User", User.class).getResultList();
        }
    }
}

package org.example.data;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

// ─────────────────────────────────────────────────────────────────────────────
// CAPA DE DATOS — Configuración de Hibernate (patrón Singleton)
//
// SessionFactory es el objeto central de Hibernate: lee la configuración,
// establece el pool de conexiones y sabe cómo mapear cada entidad.
// Crearlo es costoso, por eso se hace una sola vez y se reutiliza
// durante toda la vida de la aplicación.
// ─────────────────────────────────────────────────────────────────────────────
public class HibernateUtil {

    private static final SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar Hibernate", e);
        }
    }

    public static Session getSession() {
        return sessionFactory.openSession();
    }

    public static void shutdown() {
        sessionFactory.close();
    }
}

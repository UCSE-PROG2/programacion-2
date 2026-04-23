package org.example.data;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// ENTIDAD: Carrera
// Tabla: carreras
//
// Representa una carrera universitaria (ej: "Ingeniería en Sistemas").
// Participa en la relación 1 a N:
//   Carrera (1) ──────── (N) Materia
//   Una carrera tiene muchas materias; cada materia pertenece a una carrera.
// ─────────────────────────────────────────────────────────────────────────────
@Entity
@Table(name = "carreras")
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    // ── Relación 1 a N: una Carrera tiene muchas Materias ────────────────────
    //
    // @OneToMany indica que este lado tiene la colección.
    // mappedBy = "carrera": le dice a Hibernate que la FK vive en la tabla
    // materias, en la columna que mapea el campo "carrera" de Materia.java.
    // Sin mappedBy, Hibernate crearía una tabla intermedia innecesaria.
    //
    // FetchType.LAZY (default para colecciones): Hibernate NO carga las materias
    // al leer una Carrera. Solo las carga si se invoca getMaterias() dentro
    // de una sesión activa. Evita traer datos que quizás no se necesiten.
    @OneToMany(mappedBy = "carrera", fetch = FetchType.LAZY)
    private List<Materia> materias = new ArrayList<>();

    public Carrera() {}

    public Carrera(String nombre) {
        this.nombre = nombre;
    }

    public Integer getId()             { return id; }
    public String getNombre()          { return nombre; }
    public List<Materia> getMaterias() { return materias; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return "Carrera{ id=" + id + ", nombre='" + nombre + "' }";
    }
}

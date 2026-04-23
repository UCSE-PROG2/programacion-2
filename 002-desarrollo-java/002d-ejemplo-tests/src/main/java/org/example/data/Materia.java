package org.example.data;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// ENTIDAD: Materia
// Tabla: materias
//
// Representa una asignatura del plan de estudios.
// Participa en dos relaciones:
//
//   1 a N (lado N):  Carrera (1) ──── (N) Materia
//     Muchas materias pertenecen a una carrera.
//     La FK carrera_id vive en esta tabla (Materia es el lado propietario).
//
//   N a N (lado inverso): Alumno (N) ──── (N) Materia
//     Una materia puede tener muchos alumnos inscriptos.
//     Alumno es el propietario (tiene @JoinTable).
// ─────────────────────────────────────────────────────────────────────────────
@Entity
@Table(name = "materias")
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    // Año del plan de estudios en el que se cursa la materia (1 a 5).
    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "obligatoria", nullable = false)
    private boolean obligatoria;

    // ── Relación N a 1: muchas Materias pertenecen a una Carrera ─────────────
    //
    // @ManyToOne: este es el lado "muchos" de la relación 1-N.
    // @JoinColumn: especifica el nombre de la FK en la tabla materias.
    // FetchType.EAGER (default para @ManyToOne): Hibernate carga la Carrera
    // junto con la Materia en la misma consulta. Es apropiado aquí porque
    // siempre se necesita saber a qué carrera pertenece la materia.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "carrera_id")
    private Carrera carrera;

    // ── Lado INVERSO de la relación N a N con Alumno ─────────────────────────
    //
    // mappedBy = "materias": indica que Alumno.materias es el lado propietario
    // y es allí donde está definida la tabla intermedia (inscripciones).
    // FetchType.LAZY (default para colecciones): los alumnos no se cargan
    // hasta que se invoque getAlumnos() dentro de una sesión activa.
    @ManyToMany(mappedBy = "materias", fetch = FetchType.LAZY)
    private List<Alumno> alumnos = new ArrayList<>();

    public Materia() {}

    public Materia(String nombre, Integer anio, boolean obligatoria, Carrera carrera) {
        this.nombre     = nombre;
        this.anio       = anio;
        this.obligatoria = obligatoria;
        this.carrera    = carrera;
    }

    public Integer getId()          { return id; }
    public String getNombre()       { return nombre; }
    public Integer getAnio()        { return anio; }
    public boolean isObligatoria()  { return obligatoria; }
    public Carrera getCarrera()     { return carrera; }
    public List<Alumno> getAlumnos() { return alumnos; }
    public void setNombre(String nombre)         { this.nombre = nombre; }
    public void setAnio(Integer anio)            { this.anio = anio; }
    public void setObligatoria(boolean obl)      { this.obligatoria = obl; }
    public void setCarrera(Carrera carrera)      { this.carrera = carrera; }

    @Override
    public String toString() {
        String nombreCarrera = carrera != null ? carrera.getNombre() : "sin carrera";
        return "Materia{ id=" + id + ", nombre='" + nombre + "', anio=" + anio
                + ", obligatoria=" + obligatoria + ", carrera='" + nombreCarrera + "' }";
    }
}

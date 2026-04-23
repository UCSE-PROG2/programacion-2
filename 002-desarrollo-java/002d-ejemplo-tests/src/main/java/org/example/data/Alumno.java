package org.example.data;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// ENTIDAD: Alumno
// Tabla: alumnos
//
// Representa un estudiante universitario.
// Participa en dos relaciones:
//
//   1 a 1 (propietario):  Alumno (1) ──── (1) Perfil
//     La FK perfil_id vive en la tabla alumnos (Alumno es el propietario).
//     CascadeType.ALL: al guardar un Alumno con perfil asignado, Hibernate
//     también persiste el Perfil automáticamente.
//
//   N a N (propietario):  Alumno (N) ──── (N) Materia
//     La tabla intermedia "inscripciones" (alumno_id, materia_id) la define
//     este lado con @JoinTable. Alumno es el propietario porque conoce
//     la tabla y sus columnas FK.
// ─────────────────────────────────────────────────────────────────────────────
@Entity
@Table(name = "alumnos")
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "dni", nullable = false, unique = true, length = 10)
    private String dni;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    // ── Relación 1 a 1: Alumno es el lado PROPIETARIO ────────────────────────
    //
    // @JoinColumn: define la columna FK en la tabla alumnos.
    //   name = "perfil_id" → columna en la tabla alumnos que referencia perfiles.id
    //
    // CascadeType.ALL: cualquier operación sobre Alumno (persist, merge, remove)
    //   se propaga automáticamente al Perfil asociado. Al guardar un Alumno
    //   con un Perfil nuevo, Hibernate inserta el Perfil primero y luego el Alumno.
    //
    // Un alumno puede no tener perfil (nullable = true por defecto en @JoinColumn).
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "perfil_id")
    private Perfil perfil;

    // ── Relación N a N: Alumno es el lado PROPIETARIO ────────────────────────
    //
    // @JoinTable define la tabla intermedia que Hibernate crea y gestiona.
    //   name = "inscripciones"           → nombre de la tabla intermedia
    //   joinColumns                      → FK hacia la tabla alumnos
    //   inverseJoinColumns               → FK hacia la tabla materias
    //
    // Hibernate inserta/elimina filas en "inscripciones" automáticamente
    // cuando se agrega o elimina una Materia de esta lista.
    @ManyToMany
    @JoinTable(
        name = "inscripciones",
        joinColumns        = @JoinColumn(name = "alumno_id"),
        inverseJoinColumns = @JoinColumn(name = "materia_id")
    )
    private List<Materia> materias = new ArrayList<>();

    public Alumno() {}

    public Alumno(String nombre, String apellido, String dni, boolean activo) {
        this.nombre   = nombre;
        this.apellido = apellido;
        this.dni      = dni;
        this.activo   = activo;
    }

    public Integer getId()              { return id; }
    public String getNombre()           { return nombre; }
    public String getApellido()         { return apellido; }
    public String getDni()              { return dni; }
    public boolean isActivo()           { return activo; }
    public Perfil getPerfil()           { return perfil; }
    public List<Materia> getMaterias()  { return materias; }

    public void setNombre(String nombre)      { this.nombre = nombre; }
    public void setApellido(String apellido)  { this.apellido = apellido; }
    public void setActivo(boolean activo)     { this.activo = activo; }
    public void setPerfil(Perfil perfil)      { this.perfil = perfil; }

    @Override
    public String toString() {
        String perfilInfo = perfil != null ? perfil.getEmail() : "sin perfil";
        return "Alumno{ id=" + id + ", nombre='" + nombre + " " + apellido
                + "', dni='" + dni + "', activo=" + activo + ", perfil=" + perfilInfo + " }";
    }
}

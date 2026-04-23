package org.example.data;

import jakarta.persistence.*;

// ─────────────────────────────────────────────────────────────────────────────
// ENTIDAD: Perfil
// Tabla: perfiles
//
// Contiene datos de contacto de un alumno: email y teléfono.
// Participa en la relación 1 a 1:
//   Alumno (1) ──────── (1) Perfil
//   Un alumno tiene a lo sumo un perfil; un perfil pertenece a un alumno.
//
// Alumno es el lado PROPIETARIO (tiene la FK perfil_id en su tabla).
// Perfil es el lado INVERSO: declara mappedBy para indicarlo.
// ─────────────────────────────────────────────────────────────────────────────
@Entity
@Table(name = "perfiles")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    // ── Lado INVERSO de la relación 1 a 1 ────────────────────────────────────
    //
    // mappedBy = "perfil": la FK no está en la tabla perfiles sino en la tabla
    // alumnos (columna perfil_id, definida en Alumno.java con @JoinColumn).
    // Este campo permite navegar de Perfil → Alumno, pero Hibernate no genera
    // ninguna columna extra en la tabla perfiles por este campo.
    @OneToOne(mappedBy = "perfil")
    private Alumno alumno;

    public Perfil() {}

    public Perfil(String email, String telefono) {
        this.email     = email;
        this.telefono  = telefono;
    }

    public Integer getId()          { return id; }
    public String getEmail()        { return email; }
    public String getTelefono()     { return telefono; }
    public Alumno getAlumno()       { return alumno; }
    public void setEmail(String email)       { this.email = email; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    @Override
    public String toString() {
        return "Perfil{ id=" + id + ", email='" + email + "', telefono='" + telefono + "' }";
    }
}

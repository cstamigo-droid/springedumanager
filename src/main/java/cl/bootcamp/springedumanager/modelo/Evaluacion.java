package cl.bootcamp.springedumanager.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Evaluacion de un estudiante en un curso.
 * La situacion inicial del enunciado pide que el estudiante pueda
 * consultar sus evaluaciones, por eso es una entidad propia.
 */
@Entity
@Table(name = "evaluacion")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @NotNull(message = "La nota es obligatoria")
    @DecimalMin(value = "1.0", message = "La nota minima es 1.0")
    @DecimalMax(value = "7.0", message = "La nota maxima es 7.0")
    private Double nota;

    private LocalDate fecha = LocalDate.now();

    public Evaluacion() { }

    public Evaluacion(Estudiante estudiante, Curso curso, Double nota) {
        this.estudiante = estudiante;
        this.curso = curso;
        this.nota = nota;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    /** Regla academica del bootcamp: se aprueba con 4.0. */
    public boolean isAprobada() { return nota != null && nota >= 4.0; }
}

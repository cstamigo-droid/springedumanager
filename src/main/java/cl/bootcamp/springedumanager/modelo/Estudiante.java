package cl.bootcamp.springedumanager.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

/** Estudiante del bootcamp. Se relaciona con Curso (muchos a muchos). */
@Entity
@Table(name = "estudiante")
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato valido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "El RUT es obligatorio")
    @Size(max = 15)
    @Column(nullable = false, unique = true)
    private String rut;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "estudiante_curso",
            joinColumns = @JoinColumn(name = "estudiante_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id"))
    private List<Curso> cursos = new ArrayList<>();

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Evaluacion> evaluaciones = new ArrayList<>();

    public Estudiante() { }

    public Estudiante(String nombre, String email, String rut) {
        this.nombre = nombre;
        this.email = email;
        this.rut = rut;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
    public List<Curso> getCursos() { return cursos; }
    public void setCursos(List<Curso> cursos) { this.cursos = cursos; }
    public List<Evaluacion> getEvaluaciones() { return evaluaciones; }
    public void setEvaluaciones(List<Evaluacion> evaluaciones) { this.evaluaciones = evaluaciones; }

    @Override
    public String toString() { return nombre; }
}

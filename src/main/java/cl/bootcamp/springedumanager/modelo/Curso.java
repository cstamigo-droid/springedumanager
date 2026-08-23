package cl.bootcamp.springedumanager.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

/** Curso del bootcamp. Etapa 2 del proyecto (patron MVC). */
@Entity
@Table(name = "curso")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del curso es obligatorio")
    @Size(max = 100)
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El codigo es obligatorio")
    @Size(max = 20)
    @Column(nullable = false, unique = true)
    private String codigo;

    @NotNull(message = "Las horas son obligatorias")
    @Min(value = 1, message = "El curso debe tener al menos 1 hora")
    private Integer horas;

    @Size(max = 250)
    private String descripcion;

    /**
     * Lado inverso de la relacion. Se marca @JsonIgnore para cortar la
     * recursion infinita al serializar: Curso -> Estudiante -> Curso -> ...
     * Sin esto la API REST devuelve un JSON truncado e invalido.
     */
    @ManyToMany(mappedBy = "cursos")
    @JsonIgnore
    private List<Estudiante> estudiantes = new ArrayList<>();

    public Curso() { }

    public Curso(String nombre, String codigo, Integer horas, String descripcion) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.horas = horas;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Integer getHoras() { return horas; }
    public void setHoras(Integer horas) { this.horas = horas; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public List<Estudiante> getEstudiantes() { return estudiantes; }
    public void setEstudiantes(List<Estudiante> estudiantes) { this.estudiantes = estudiantes; }

    @Override
    public String toString() { return codigo + " - " + nombre; }
}

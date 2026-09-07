package cl.bootcamp.springedumanager.servicio;

import cl.bootcamp.springedumanager.modelo.Curso;
import cl.bootcamp.springedumanager.modelo.Estudiante;
import cl.bootcamp.springedumanager.modelo.Evaluacion;
import cl.bootcamp.springedumanager.repositorio.CursoRepository;
import cl.bootcamp.springedumanager.repositorio.EstudianteRepository;
import cl.bootcamp.springedumanager.repositorio.EvaluacionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Etapa 3: logica de negocio de las evaluaciones (la nota de un estudiante en un curso). */
@Service
public class EvaluacionService {

    private final EvaluacionRepository repositorio;
    private final EstudianteRepository estudiantes;
    private final CursoRepository cursos;

    public EvaluacionService(EvaluacionRepository repositorio, EstudianteRepository estudiantes,
                             CursoRepository cursos) {
        this.repositorio = repositorio;
        this.estudiantes = estudiantes;
        this.cursos = cursos;
    }

    @Transactional(readOnly = true)
    public List<Evaluacion> listar() { return repositorio.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Evaluacion> porId(Long id) { return repositorio.findById(id); }

    @Transactional(readOnly = true)
    public List<Evaluacion> porEstudiante(Long id) { return repositorio.findByEstudianteId(id); }

    /**
     * Registra una nota. Regla academica: solo se evalua a un estudiante en un curso en el
     * que esta matriculado; si no, se rechaza con un mensaje que el formulario y la API muestran.
     */
    @Transactional
    public Evaluacion registrar(Long estudianteId, Long cursoId, Double nota) {
        Estudiante e = estudiantes.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante"));
        Curso c = cursos.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el curso"));
        if (!e.getCursos().contains(c)) {
            throw new IllegalArgumentException(e.getNombre() + " no esta matriculado en " + c.getCodigo());
        }
        if (nota == null || nota < 1.0 || nota > 7.0) {
            throw new IllegalArgumentException("La nota debe estar entre 1.0 y 7.0");
        }
        return repositorio.save(new Evaluacion(e, c, nota));
    }

    @Transactional
    public Evaluacion guardar(Evaluacion evaluacion) { return repositorio.save(evaluacion); }

    @Transactional
    public void eliminar(Long id) { repositorio.deleteById(id); }
}

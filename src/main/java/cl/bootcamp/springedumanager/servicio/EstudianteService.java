package cl.bootcamp.springedumanager.servicio;

import cl.bootcamp.springedumanager.modelo.Curso;
import cl.bootcamp.springedumanager.modelo.Estudiante;
import cl.bootcamp.springedumanager.repositorio.CursoRepository;
import cl.bootcamp.springedumanager.repositorio.EstudianteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstudianteService {

    private final EstudianteRepository repositorio;
    private final CursoRepository cursoRepositorio;

    public EstudianteService(EstudianteRepository repositorio, CursoRepository cursoRepositorio) {
        this.repositorio = repositorio;
        this.cursoRepositorio = cursoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<Estudiante> listar() { return repositorio.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Estudiante> porId(Long id) { return repositorio.findById(id); }

    @Transactional(readOnly = true)
    public Optional<Estudiante> porEmail(String email) { return repositorio.findByEmail(email); }

    @Transactional
    public Estudiante guardar(Estudiante estudiante) {
        boolean emailDeOtro = repositorio.findByEmail(estudiante.getEmail())
                .map(otro -> !otro.getId().equals(estudiante.getId()))
                .orElse(false);
        if (emailDeOtro) {
            throw new IllegalArgumentException("Ya existe un estudiante con el email " + estudiante.getEmail());
        }
        return repositorio.save(estudiante);
    }

    /** Actualizacion (PUT de la API): verifica el email libre ANTES de modificar la entidad. */
    @Transactional
    public Optional<Estudiante> actualizar(Long id, Estudiante datos) {
        Optional<Estudiante> existente = repositorio.findById(id);
        if (existente.isEmpty()) return Optional.empty();
        boolean emailDeOtro = repositorio.findByEmail(datos.getEmail())
                .map(otro -> !otro.getId().equals(id))
                .orElse(false);
        if (emailDeOtro) {
            throw new IllegalArgumentException("Ya existe un estudiante con el email " + datos.getEmail());
        }
        Estudiante e = existente.get();
        e.setNombre(datos.getNombre());
        e.setEmail(datos.getEmail());
        e.setRut(datos.getRut());
        return Optional.of(repositorio.save(e));
    }

    /** Matricula: agrega un curso a la lista del estudiante, sin duplicar. */
    @Transactional
    public Estudiante matricular(Long estudianteId, Long cursoId) {
        Estudiante e = repositorio.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante"));
        Curso c = cursoRepositorio.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el curso"));
        if (!e.getCursos().contains(c)) {
            e.getCursos().add(c);
        }
        return repositorio.save(e);
    }

    @Transactional
    public void eliminar(Long id) { repositorio.deleteById(id); }
}

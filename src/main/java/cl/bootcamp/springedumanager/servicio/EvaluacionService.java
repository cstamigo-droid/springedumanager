package cl.bootcamp.springedumanager.servicio;

import cl.bootcamp.springedumanager.modelo.Evaluacion;
import cl.bootcamp.springedumanager.repositorio.EvaluacionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvaluacionService {

    private final EvaluacionRepository repositorio;

    public EvaluacionService(EvaluacionRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<Evaluacion> listar() { return repositorio.findAll(); }

    @Transactional(readOnly = true)
    public List<Evaluacion> porEstudiante(Long id) { return repositorio.findByEstudianteId(id); }

    @Transactional
    public Evaluacion guardar(Evaluacion evaluacion) { return repositorio.save(evaluacion); }

    @Transactional
    public void eliminar(Long id) { repositorio.deleteById(id); }
}

package cl.bootcamp.springedumanager.servicio;

import cl.bootcamp.springedumanager.modelo.Curso;
import cl.bootcamp.springedumanager.repositorio.CursoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Etapa 3: @Service para la logica de negocio de cursos. */
@Service
public class CursoService {

    private final CursoRepository repositorio;

    public CursoService(CursoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<Curso> listar() { return repositorio.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Curso> porId(Long id) { return repositorio.findById(id); }

    @Transactional(readOnly = true)
    public List<Curso> conHorasDesde(int minimo) { return repositorio.conHorasDesde(minimo); }

    @Transactional
    public Curso guardar(Curso curso) {
        if (curso.getId() == null && repositorio.existsByCodigo(curso.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un curso con el codigo " + curso.getCodigo());
        }
        return repositorio.save(curso);
    }

    @Transactional
    public void eliminar(Long id) { repositorio.deleteById(id); }
}

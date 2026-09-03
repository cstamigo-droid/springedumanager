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
        boolean codigoDeOtro = repositorio.findByCodigo(curso.getCodigo())
                .map(otro -> !otro.getId().equals(curso.getId()))
                .orElse(false);
        if (codigoDeOtro) {
            throw new IllegalArgumentException("Ya existe un curso con el codigo " + curso.getCodigo());
        }
        return repositorio.save(curso);
    }

    /**
     * Actualizacion (PUT de la API). La unicidad del codigo se verifica ANTES de
     * tocar la entidad: si se modifica primero, Hibernate hace flush del cambio al
     * ejecutar la consulta y la restriccion UNIQUE de la base explota con un 500
     * en vez de un 409 explicable.
     */
    @Transactional
    public Optional<Curso> actualizar(Long id, Curso datos) {
        Optional<Curso> existente = repositorio.findById(id);
        if (existente.isEmpty()) return Optional.empty();
        boolean codigoDeOtro = repositorio.findByCodigo(datos.getCodigo())
                .map(otro -> !otro.getId().equals(id))
                .orElse(false);
        if (codigoDeOtro) {
            throw new IllegalArgumentException("Ya existe un curso con el codigo " + datos.getCodigo());
        }
        Curso c = existente.get();
        c.setNombre(datos.getNombre());
        c.setCodigo(datos.getCodigo());
        c.setHoras(datos.getHoras());
        c.setDescripcion(datos.getDescripcion());
        return Optional.of(repositorio.save(c));
    }

    @Transactional
    public void eliminar(Long id) { repositorio.deleteById(id); }
}

package cl.bootcamp.springedumanager.repositorio;

import cl.bootcamp.springedumanager.modelo.Evaluacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {
    List<Evaluacion> findByEstudianteId(Long estudianteId);
    List<Evaluacion> findByCursoId(Long cursoId);
}

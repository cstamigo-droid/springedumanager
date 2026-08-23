package cl.bootcamp.springedumanager.repositorio;

import cl.bootcamp.springedumanager.modelo.Curso;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {
    Optional<Curso> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);

    /** Consulta personalizada con @Query, para cursos de carga horaria alta. */
    @Query("SELECT c FROM Curso c WHERE c.horas >= :minimo ORDER BY c.horas DESC")
    List<Curso> conHorasDesde(int minimo);
}

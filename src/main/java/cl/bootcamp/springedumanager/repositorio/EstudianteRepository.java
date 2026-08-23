package cl.bootcamp.springedumanager.repositorio;

import cl.bootcamp.springedumanager.modelo.Estudiante;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Etapa 3: repositorio JPA que extiende JpaRepository. */
@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    Optional<Estudiante> findByEmail(String email);
    Optional<Estudiante> findByRut(String rut);
    boolean existsByEmail(String email);
}

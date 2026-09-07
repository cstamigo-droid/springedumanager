package cl.bootcamp.springedumanager.rest;

import cl.bootcamp.springedumanager.modelo.Evaluacion;
import cl.bootcamp.springedumanager.servicio.EvaluacionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Etapa 5: API REST de evaluaciones. A diferencia de cursos y estudiantes, aqui la API
 * NO expone la entidad: usa DTOs (records) de entrada y de salida. Asi el JSON queda
 * plano (ids y nombres, sin las relaciones anidadas de JPA) y el contrato con quien
 * integra no cambia aunque cambie el modelo interno.
 */
@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionRestController {

    /** Lo que recibe la API para registrar una nota. */
    public record EvaluacionRequest(@NotNull Long estudianteId, @NotNull Long cursoId,
                                    @NotNull @DecimalMin("1.0") @DecimalMax("7.0") Double nota) { }

    /** Lo que devuelve la API: plano y sin entidades. */
    public record EvaluacionResponse(Long id, Long estudianteId, String estudiante, Long cursoId,
                                     String curso, Double nota, LocalDate fecha, boolean aprobada) {
        static EvaluacionResponse de(Evaluacion e) {
            return new EvaluacionResponse(e.getId(), e.getEstudiante().getId(), e.getEstudiante().getNombre(),
                    e.getCurso().getId(), e.getCurso().getCodigo(), e.getNota(), e.getFecha(), e.isAprobada());
        }
    }

    private final EvaluacionService servicio;

    public EvaluacionRestController(EvaluacionService servicio) { this.servicio = servicio; }

    @GetMapping
    public List<EvaluacionResponse> listar() {
        return servicio.listar().stream().map(EvaluacionResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluacionResponse> porId(@PathVariable Long id) {
        return servicio.porId(id).map(EvaluacionResponse::de).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Las evaluaciones de un estudiante: lo que "consultar sus evaluaciones" pide la situacion inicial. */
    @GetMapping("/estudiante/{estudianteId}")
    public List<EvaluacionResponse> porEstudiante(@PathVariable Long estudianteId) {
        return servicio.porEstudiante(estudianteId).stream().map(EvaluacionResponse::de).toList();
    }

    /** 201 creada · 400 campos invalidos · 409 si el estudiante no esta matriculado en el curso. */
    @PostMapping
    public ResponseEntity<EvaluacionResponse> crear(@Valid @RequestBody EvaluacionRequest req) {
        Evaluacion e = servicio.registrar(req.estudianteId(), req.cursoId(), req.nota());
        return ResponseEntity.status(201).body(EvaluacionResponse.de(e));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (servicio.porId(id).isEmpty()) return ResponseEntity.notFound().build();
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

package cl.bootcamp.springedumanager.rest;

import cl.bootcamp.springedumanager.modelo.Estudiante;
import cl.bootcamp.springedumanager.servicio.EstudianteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Etapa 5: API REST de estudiantes con CRUD completo. */
@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteRestController {

    private final EstudianteService servicio;

    public EstudianteRestController(EstudianteService servicio) { this.servicio = servicio; }

    @GetMapping
    public List<Estudiante> listar() { return servicio.listar(); }

    @GetMapping("/{id}")
    public ResponseEntity<Estudiante> porId(@PathVariable Long id) {
        return servicio.porId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /** 201 con el estudiante creado; email repetido -> 409 via ApiExceptionHandler. */
    @PostMapping
    public ResponseEntity<Estudiante> crear(@Valid @RequestBody Estudiante estudiante) {
        return ResponseEntity.status(201).body(servicio.guardar(estudiante));
    }

    /** 200 actualizado, 404 si no existe, 409 si el email es de otro estudiante. */
    @PutMapping("/{id}")
    public ResponseEntity<Estudiante> actualizar(@PathVariable Long id,
                                                 @Valid @RequestBody Estudiante datos) {
        return servicio.actualizar(id, datos).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (servicio.porId(id).isEmpty()) return ResponseEntity.notFound().build();
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

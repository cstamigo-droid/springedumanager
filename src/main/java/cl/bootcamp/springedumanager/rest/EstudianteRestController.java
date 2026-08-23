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

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Estudiante estudiante) {
        try {
            return ResponseEntity.status(201).body(servicio.guardar(estudiante));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Estudiante> actualizar(@PathVariable Long id,
                                                 @Valid @RequestBody Estudiante datos) {
        return servicio.porId(id).map(e -> {
            e.setNombre(datos.getNombre());
            e.setEmail(datos.getEmail());
            e.setRut(datos.getRut());
            return ResponseEntity.ok(servicio.guardar(e));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (servicio.porId(id).isEmpty()) return ResponseEntity.notFound().build();
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

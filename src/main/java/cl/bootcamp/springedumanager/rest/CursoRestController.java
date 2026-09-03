package cl.bootcamp.springedumanager.rest;

import cl.bootcamp.springedumanager.modelo.Curso;
import cl.bootcamp.springedumanager.servicio.CursoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Etapa 5: API REST de cursos. CRUD completo con los cuatro verbos HTTP,
 * respondiendo en JSON para permitir la interoperabilidad con otros sistemas.
 */
@RestController
@RequestMapping("/api/cursos")
public class CursoRestController {

    private final CursoService servicio;

    public CursoRestController(CursoService servicio) { this.servicio = servicio; }

    @GetMapping
    public List<Curso> listar() { return servicio.listar(); }

    @GetMapping("/{id}")
    public ResponseEntity<Curso> porId(@PathVariable Long id) {
        return servicio.porId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 201 con el curso creado. Si el codigo ya existe, el servicio lanza
     * IllegalArgumentException y ApiExceptionHandler responde 409 en JSON.
     */
    @PostMapping
    public ResponseEntity<Curso> crear(@Valid @RequestBody Curso curso) {
        return ResponseEntity.status(201).body(servicio.guardar(curso));
    }

    /** 200 con el curso actualizado, 404 si no existe, 409 si el codigo es de otro curso. */
    @PutMapping("/{id}")
    public ResponseEntity<Curso> actualizar(@PathVariable Long id, @Valid @RequestBody Curso datos) {
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

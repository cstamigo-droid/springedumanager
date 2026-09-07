package cl.bootcamp.springedumanager.controlador;

import cl.bootcamp.springedumanager.servicio.CursoService;
import cl.bootcamp.springedumanager.servicio.EstudianteService;
import cl.bootcamp.springedumanager.servicio.EvaluacionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Evaluaciones: todos las consultan; solo ADMIN registra o elimina notas (Etapa 4).
 */
@Controller
@RequestMapping("/evaluaciones")
public class EvaluacionController {

    private final EvaluacionService evaluaciones;
    private final EstudianteService estudiantes;
    private final CursoService cursos;

    public EvaluacionController(EvaluacionService evaluaciones, EstudianteService estudiantes, CursoService cursos) {
        this.evaluaciones = evaluaciones;
        this.estudiantes = estudiantes;
        this.cursos = cursos;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("evaluaciones", evaluaciones.listar());
        return "evaluaciones/lista";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasRole('ADMIN')")
    public String nueva(Model model) {
        model.addAttribute("estudiantes", estudiantes.listar());
        model.addAttribute("cursos", cursos.listar());
        return "evaluaciones/formulario";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@RequestParam Long estudianteId, @RequestParam Long cursoId,
                          @RequestParam Double nota, RedirectAttributes flash) {
        try {
            evaluaciones.registrar(estudianteId, cursoId, nota);
            flash.addFlashAttribute("exito", "Evaluacion registrada");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluaciones/nueva";
        }
        return "redirect:/evaluaciones";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        evaluaciones.eliminar(id);
        flash.addFlashAttribute("exito", "Evaluacion eliminada");
        return "redirect:/evaluaciones";
    }
}

package cl.bootcamp.springedumanager.controlador;

import cl.bootcamp.springedumanager.modelo.Estudiante;
import cl.bootcamp.springedumanager.servicio.CursoService;
import cl.bootcamp.springedumanager.servicio.EstudianteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/estudiantes")
public class EstudianteController {

    private final EstudianteService servicio;
    private final CursoService cursoService;

    public EstudianteController(EstudianteService servicio, CursoService cursoService) {
        this.servicio = servicio;
        this.cursoService = cursoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("estudiantes", servicio.listar());
        return "estudiantes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("estudiante", new Estudiante());
        return "estudiantes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("estudiante") Estudiante estudiante,
                          BindingResult resultado, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            return "estudiantes/formulario";
        }
        try {
            servicio.guardar(estudiante);
            flash.addFlashAttribute("exito", "Estudiante guardado correctamente");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/estudiantes";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Estudiante e = servicio.porId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante"));
        model.addAttribute("estudiante", e);
        model.addAttribute("cursosDisponibles", cursoService.listar());
        return "estudiantes/detalle";
    }

    @PostMapping("/{id}/matricular")
    public String matricular(@PathVariable Long id, @RequestParam Long cursoId,
                             RedirectAttributes flash) {
        servicio.matricular(id, cursoId);
        flash.addFlashAttribute("exito", "Estudiante matriculado en el curso");
        return "redirect:/estudiantes/" + id;
    }
}

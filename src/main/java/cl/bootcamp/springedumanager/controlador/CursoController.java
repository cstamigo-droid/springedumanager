package cl.bootcamp.springedumanager.controlador;

import cl.bootcamp.springedumanager.modelo.Curso;
import cl.bootcamp.springedumanager.servicio.CursoService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Etapa 2: controlador MVC de cursos, con @GetMapping y @PostMapping. */
@Controller
@RequestMapping("/cursos")
public class CursoController {

    private final CursoService servicio;

    public CursoController(CursoService servicio) { this.servicio = servicio; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cursos", servicio.listar());
        return "cursos/lista";
    }

    /** Solo ADMIN puede abrir el formulario de carga (Etapa 4). */
    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("curso", new Curso());
        return "cursos/formulario";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@Valid @ModelAttribute("curso") Curso curso,
                          BindingResult resultado, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            return "cursos/formulario";
        }
        try {
            servicio.guardar(curso);
            flash.addFlashAttribute("exito", "Curso guardado correctamente");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cursos";
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        servicio.eliminar(id);
        flash.addFlashAttribute("exito", "Curso eliminado");
        return "redirect:/cursos";
    }
}

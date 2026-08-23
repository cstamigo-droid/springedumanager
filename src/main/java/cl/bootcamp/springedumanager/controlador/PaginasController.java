package cl.bootcamp.springedumanager.controlador;

import cl.bootcamp.springedumanager.servicio.EvaluacionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginasController {

    private final EvaluacionService evaluacionService;

    public PaginasController(EvaluacionService evaluacionService) {
        this.evaluacionService = evaluacionService;
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/")
    public String inicio() { return "redirect:/cursos"; }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() { return "error/403"; }

    @GetMapping("/evaluaciones")
    public String evaluaciones(Model model) {
        model.addAttribute("evaluaciones", evaluacionService.listar());
        return "evaluaciones/lista";
    }
}

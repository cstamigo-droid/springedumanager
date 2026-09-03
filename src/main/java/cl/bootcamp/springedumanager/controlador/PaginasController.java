package cl.bootcamp.springedumanager.controlador;

import cl.bootcamp.springedumanager.servicio.EvaluacionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    /**
     * Pagina de acceso denegado. Se declara con @RequestMapping (cualquier metodo) y
     * no con @GetMapping: Spring Security llega aqui por un FORWARD que conserva el
     * metodo de la peticion original. Si un USER envia el formulario de cursos (POST)
     * o un POST llega con el token CSRF invalido, el forward es POST; con @GetMapping
     * respondia 405 "Method Not Allowed" en vez de la pagina con 403.
     */
    @RequestMapping("/acceso-denegado")
    public String accesoDenegado() { return "error/403"; }

    @GetMapping("/evaluaciones")
    public String evaluaciones(Model model) {
        model.addAttribute("evaluaciones", evaluacionService.listar());
        return "evaluaciones/lista";
    }
}

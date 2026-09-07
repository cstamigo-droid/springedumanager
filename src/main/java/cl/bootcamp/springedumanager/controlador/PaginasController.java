package cl.bootcamp.springedumanager.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PaginasController {

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

    /** Etapa 5 (plus): pantalla para pedir un JWT y llamar a la API desde el navegador. */
    @GetMapping("/api-lab")
    public String apiLab() { return "api-lab"; }

}

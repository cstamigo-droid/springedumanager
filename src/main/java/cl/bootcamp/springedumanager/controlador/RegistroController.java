package cl.bootcamp.springedumanager.controlador;

import cl.bootcamp.springedumanager.modelo.Estudiante;
import cl.bootcamp.springedumanager.servicio.EstudianteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Registro PUBLICO de estudiantes: la situacion inicial pide que los estudiantes puedan
 * registrarse en la plataforma. Crea el perfil academico (Estudiante); las credenciales de
 * acceso siguen configuradas en application.properties, como pide la Leccion 4.
 */
@Controller
public class RegistroController {

    private final EstudianteService servicio;

    public RegistroController(EstudianteService servicio) { this.servicio = servicio; }

    @GetMapping("/registro")
    public String formulario(Model model) {
        model.addAttribute("estudiante", new Estudiante());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("estudiante") Estudiante estudiante,
                            BindingResult resultado, Model model) {
        if (resultado.hasErrors()) {
            return "registro";
        }
        try {
            servicio.guardar(estudiante);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
        return "redirect:/login?registro";
    }
}

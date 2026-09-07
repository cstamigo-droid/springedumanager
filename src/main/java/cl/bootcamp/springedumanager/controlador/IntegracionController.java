package cl.bootcamp.springedumanager.controlador;

import cl.bootcamp.springedumanager.integracion.CampusService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Etapa 5: pantalla "Integracion". Muestra lo que SpringEduManager obtuvo de otros
 * sistemas usando RestTemplate. Devuelve ModelAndView (vista + modelo en un objeto)
 * como alternativa a recibir el Model por parametro.
 */
@Controller
public class IntegracionController {

    private final CampusService campus;

    public IntegracionController(CampusService campus) { this.campus = campus; }

    @GetMapping("/integracion")
    public ModelAndView integracion() {
        ModelAndView mv = new ModelAndView("integracion");
        mv.addObject("baseUrl", campus.baseUrl());
        try {
            mv.addObject("periodos", campus.calendarioDelCampus());
            mv.addObject("cursosApi", campus.cursosDesdeLaApi());
        } catch (RestClientException e) {
            // Un sistema externo caido no debe tumbar la pantalla: se informa y se sigue.
            mv.addObject("errorIntegracion", "No se pudo consumir el servicio: " + e.getMessage());
        }
        return mv;
    }
}

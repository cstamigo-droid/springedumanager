package cl.bootcamp.springedumanager.controlador;

import cl.bootcamp.springedumanager.repositorio.jdbc.ReporteDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Reporte academico calculado con JdbcTemplate (ver ReporteDao). Usa ModelMap,
 * la variante de Model con interfaz de mapa, para mostrar las dos formas.
 */
@Controller
public class ReporteController {

    private final ReporteDao reportes;

    public ReporteController(ReporteDao reportes) { this.reportes = reportes; }

    @GetMapping("/reportes")
    public String reportes(ModelMap modelo) {
        modelo.addAttribute("totales", reportes.totales());
        modelo.addAttribute("filas", reportes.resumenPorCurso());
        return "reportes/lista";
    }
}

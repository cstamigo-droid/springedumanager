package cl.bootcamp.springedumanager.integracion;

import cl.bootcamp.springedumanager.integracion.CampusService.PeriodoAcademico;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SIMULA el "Calendario Academico del campus": un sistema externo que publica sus
 * periodos en JSON. Existe para que la demostracion de RestTemplate funcione sin
 * internet y sin depender de un servicio ajeno. Es publico (sin login) como lo
 * seria un calendario institucional.
 */
@RestController
public class CampusDemoController {

    @GetMapping("/demo/campus/calendario")
    public List<PeriodoAcademico> calendario() {
        int anio = LocalDate.now().getYear();
        LocalDate hoy = LocalDate.now();
        return List.of(
            periodo(anio + "-1", "Primer semestre " + anio, LocalDate.of(anio, 3, 1), LocalDate.of(anio, 7, 15), hoy),
            periodo(anio + "-2", "Segundo semestre " + anio, LocalDate.of(anio, 8, 1), LocalDate.of(anio, 12, 15), hoy),
            periodo(anio + "-V", "Verano " + (anio + 1), LocalDate.of(anio + 1, 1, 5), LocalDate.of(anio + 1, 2, 20), hoy)
        );
    }

    private static PeriodoAcademico periodo(String codigo, String nombre, LocalDate ini, LocalDate fin, LocalDate hoy) {
        boolean vigente = !hoy.isBefore(ini) && !hoy.isAfter(fin);
        return new PeriodoAcademico(codigo, nombre, ini.toString(), fin.toString(), vigente);
    }
}

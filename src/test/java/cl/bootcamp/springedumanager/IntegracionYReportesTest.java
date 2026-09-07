package cl.bootcamp.springedumanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cl.bootcamp.springedumanager.integracion.CampusService;
import cl.bootcamp.springedumanager.repositorio.jdbc.ReporteDao;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Etapa 3 (JdbcTemplate) y Etapa 5 (RestTemplate): el reporte SQL contra la base real
 * y la interoperabilidad consumiendo servicios por HTTP real.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegracionYReportesTest {

    @LocalServerPort int puerto;
    @Autowired ReporteDao reportes;
    @Autowired CampusService campus;

    @Test
    void elReporteConJdbcTemplateCuadraConLaCargaInicial() {
        ReporteDao.Totales t = reportes.totales();
        assertTrue(t.cursos() >= 3);
        assertTrue(t.estudiantes() >= 2);
        assertTrue(t.evaluaciones() >= 3);

        List<ReporteDao.ResumenCurso> filas = reportes.resumenPorCurso();
        ReporteDao.ResumenCurso java = filas.stream().filter(f -> f.codigo().equals("JAVA-01")).findFirst().orElseThrow();
        assertEquals(1, java.evaluaciones(), "Cristian tiene una evaluacion en JAVA-01 (6.5)");
        assertEquals(100, java.porcentajeAprobacion());
        ReporteDao.ResumenCurso front = filas.stream().filter(f -> f.codigo().equals("FRONT-01")).findFirst().orElseThrow();
        assertEquals(0, front.porcentajeAprobacion(), "Ana tiene un 3.4 en FRONT-01: reprobada");
    }

    @Test
    void restTemplateConsumeElServicioExternoSimulado() {
        List<CampusService.PeriodoAcademico> periodos = campus.calendarioDelCampus();
        assertEquals(3, periodos.size());
        assertTrue(periodos.stream().allMatch(p -> p.codigo() != null && p.inicio() != null));
    }

    @Test
    void restTemplateConsumeLaApiPropiaConHttpBasic() {
        assertTrue(campus.cursosDesdeLaApi().stream().anyMatch(c -> c.getCodigo().equals("JAVA-01")));
    }

    @Test
    void elServicioSimuladoEsPublicoYElHealthTambien() {
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> cal = rt.getForEntity("http://localhost:" + puerto + "/demo/campus/calendario", String.class);
        assertEquals(HttpStatus.OK, cal.getStatusCode());
        assertTrue(cal.getBody().contains("\"codigo\""));
        ResponseEntity<String> health = rt.getForEntity("http://localhost:" + puerto + "/actuator/health", String.class);
        assertEquals(HttpStatus.OK, health.getStatusCode());
        assertTrue(health.getBody().contains("UP"));
    }
}

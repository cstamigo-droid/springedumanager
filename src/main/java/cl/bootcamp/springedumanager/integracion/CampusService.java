package cl.bootcamp.springedumanager.integracion;

import cl.bootcamp.springedumanager.modelo.Curso;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Etapa 5 (interoperabilidad): SpringEduManager consumiendo OTROS servicios por HTTP
 * con RestTemplate, y recibiendo JSON que Spring convierte en objetos Java.
 *
 * <pre>
 * SpringEduManager -> RestTemplate -> HTTP -> otro sistema -> JSON -> objeto Java
 * </pre>
 *
 * Dos demostraciones:
 * 1. El calendario academico del campus: un servicio externo SIMULADO dentro de esta
 *    misma aplicacion (/demo/campus/calendario), para que la demo funcione sin internet.
 * 2. La propia API REST de SpringEduManager (/api/cursos), consumida como lo haria
 *    otro sistema del campus: con HTTP Basic y deserializando el JSON a Curso[].
 */
@Service
public class CampusService {

    /** Un periodo del calendario academico, tal como lo publica el campus. */
    public record PeriodoAcademico(String codigo, String nombre, String inicio, String fin, boolean vigente) { }

    private final RestTemplate restTemplate;
    private final Environment env;
    private final String baseUrlConfigurada;
    private final String usuarioApi;
    private final String claveApi;

    public CampusService(RestTemplate restTemplate, Environment env,
                         @Value("${app.base-url:}") String baseUrlConfigurada,
                         @Value("${edumanager.usuarios.estudiante.nombre}") String usuarioApi,
                         @Value("${edumanager.usuarios.estudiante.clave}") String claveApi) {
        this.restTemplate = restTemplate;
        this.env = env;
        this.baseUrlConfigurada = baseUrlConfigurada;
        this.usuarioApi = usuarioApi;
        this.claveApi = claveApi;
    }

    /**
     * URL base de los servicios. Si app.base-url esta vacia, se arma con el puerto REAL
     * en el que arranco Tomcat (local.server.port), que recien existe cuando el servidor
     * ya levanto: por eso se resuelve al usarse y no en el constructor.
     */
    public String baseUrl() {
        if (baseUrlConfigurada != null && !baseUrlConfigurada.isBlank()) return baseUrlConfigurada;
        String puerto = env.getProperty("local.server.port", env.getProperty("server.port", "8080"));
        return "http://localhost:" + puerto;
    }

    /** Consume el calendario del campus (servicio externo simulado, sin credenciales). */
    public List<PeriodoAcademico> calendarioDelCampus() {
        PeriodoAcademico[] periodos = restTemplate.getForObject(baseUrl() + "/demo/campus/calendario",
                PeriodoAcademico[].class);
        return periodos == null ? List.of() : Arrays.asList(periodos);
    }

    /** Consume la API propia con HTTP Basic, como lo haria un sistema externo. */
    public List<Curso> cursosDesdeLaApi() {
        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.setBasicAuth(usuarioApi, claveApi);
        Curso[] cursos = restTemplate.exchange(baseUrl() + "/api/cursos", HttpMethod.GET,
                new HttpEntity<>(cabeceras), Curso[].class).getBody();
        return cursos == null ? List.of() : Arrays.asList(cursos);
    }

}

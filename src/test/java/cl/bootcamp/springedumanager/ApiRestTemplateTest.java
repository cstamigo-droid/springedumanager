package cl.bootcamp.springedumanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cl.bootcamp.springedumanager.modelo.Curso;
import cl.bootcamp.springedumanager.modelo.Estudiante;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Etapa 5 (Interoperabilidad): la API se consume desde un CLIENTE EXTERNO con
 * RestTemplate, como pide la Leccion 5. La aplicacion se levanta en un puerto
 * real y las peticiones viajan por HTTP: es lo mismo que haria otro sistema
 * del campus (o Postman) para integrarse.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiRestTemplateTest {

    @LocalServerPort
    int puerto;

    RestTemplate cliente = new RestTemplate();
    String base;

    @BeforeEach
    void url() { base = "http://localhost:" + puerto + "/api"; }

    /** Cabeceras de un cliente autenticado con HTTP Basic (admin / admin123). */
    private HttpHeaders comoAdmin() {
        HttpHeaders h = new HttpHeaders();
        h.setBasicAuth("admin", "admin123");
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    void getListaCursosDevuelveJson() {
        ResponseEntity<Curso[]> r = cliente.exchange(base + "/cursos", HttpMethod.GET,
                new HttpEntity<>(comoAdmin()), Curso[].class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertTrue(r.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertTrue(r.getBody().length >= 3, "la carga inicial trae 3 cursos");
    }

    @Test
    void crudCompletoDeCursoConLosCuatroVerbos() {
        // POST -> 201
        Curso nuevo = new Curso("Docker y Kubernetes", "OPS-77", 32, "Contenedores");
        ResponseEntity<Curso> creado = cliente.exchange(base + "/cursos", HttpMethod.POST,
                new HttpEntity<>(nuevo, comoAdmin()), Curso.class);
        assertEquals(HttpStatus.CREATED, creado.getStatusCode());
        Long id = creado.getBody().getId();
        assertNotNull(id);

        // GET uno -> 200
        ResponseEntity<Curso> uno = cliente.exchange(base + "/cursos/" + id, HttpMethod.GET,
                new HttpEntity<>(comoAdmin()), Curso.class);
        assertEquals("OPS-77", uno.getBody().getCodigo());

        // PUT -> 200 con los datos nuevos
        nuevo.setHoras(48);
        ResponseEntity<Curso> editado = cliente.exchange(base + "/cursos/" + id, HttpMethod.PUT,
                new HttpEntity<>(nuevo, comoAdmin()), Curso.class);
        assertEquals(HttpStatus.OK, editado.getStatusCode());
        assertEquals(48, editado.getBody().getHoras());

        // DELETE -> 204, y despues GET -> 404
        ResponseEntity<Void> borrado = cliente.exchange(base + "/cursos/" + id, HttpMethod.DELETE,
                new HttpEntity<>(comoAdmin()), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, borrado.getStatusCode());
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                cliente.exchange(base + "/cursos/" + id, HttpMethod.GET, new HttpEntity<>(comoAdmin()), Curso.class));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void crudDeEstudiantePorRest() {
        Estudiante e = new Estudiante("Pedro Rojas", "pedro.rest@bootcamp.cl", "11.222.333-4");
        ResponseEntity<Estudiante> creado = cliente.exchange(base + "/estudiantes", HttpMethod.POST,
                new HttpEntity<>(e, comoAdmin()), Estudiante.class);
        assertEquals(HttpStatus.CREATED, creado.getStatusCode());
        Long id = creado.getBody().getId();

        e.setNombre("Pedro Rojas Diaz");
        ResponseEntity<Estudiante> editado = cliente.exchange(base + "/estudiantes/" + id, HttpMethod.PUT,
                new HttpEntity<>(e, comoAdmin()), Estudiante.class);
        assertEquals("Pedro Rojas Diaz", editado.getBody().getNombre());

        ResponseEntity<Void> borrado = cliente.exchange(base + "/estudiantes/" + id, HttpMethod.DELETE,
                new HttpEntity<>(comoAdmin()), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, borrado.getStatusCode());
    }

    @Test
    void unPostInvalidoDevuelve400YDiceQueCampoFallo() {
        Curso vacio = new Curso("", "", 0, null);
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                cliente.exchange(base + "/cursos", HttpMethod.POST, new HttpEntity<>(vacio, comoAdmin()), Map.class));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        String cuerpo = ex.getResponseBodyAsString();
        assertTrue(cuerpo.contains("\"campos\""), "el 400 trae el detalle por campo: " + cuerpo);
        assertTrue(cuerpo.contains("horas"), "nombra el campo horas: " + cuerpo);
    }

    @Test
    void unCodigoDuplicadoDevuelve409() {
        Curso repetido = new Curso("Otro Java", "JAVA-01", 10, "codigo ya usado por la carga inicial");
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                cliente.exchange(base + "/cursos", HttpMethod.POST, new HttpEntity<>(repetido, comoAdmin()), Map.class));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void unPutConElCodigoDeOtroCursoDevuelve409YNo500() {
        // BD-01 (id 2) intenta quedarse con el codigo de JAVA-01
        Curso datos = new Curso("Bases de Datos", "JAVA-01", 60, "codigo ajeno");
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                cliente.exchange(base + "/cursos/2", HttpMethod.PUT, new HttpEntity<>(datos, comoAdmin()), Map.class));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        // y el curso 2 sigue intacto
        ResponseEntity<Curso> dos = cliente.exchange(base + "/cursos/2", HttpMethod.GET,
                new HttpEntity<>(comoAdmin()), Curso.class);
        assertEquals("BD-01", dos.getBody().getCodigo());
    }

    @Test
    void unPutAUnIdInexistenteDevuelve404() {
        Curso datos = new Curso("Nada", "NADA-1", 1, null);
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                cliente.exchange(base + "/cursos/999999", HttpMethod.PUT, new HttpEntity<>(datos, comoAdmin()), Curso.class));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void sinCredencialesElClienteRecibe401() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                cliente.getForEntity(base + "/cursos", String.class));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertNotNull(ex.getResponseHeaders().getFirst("WWW-Authenticate"));
    }
}

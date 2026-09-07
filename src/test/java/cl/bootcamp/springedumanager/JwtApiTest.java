package cl.bootcamp.springedumanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cl.bootcamp.springedumanager.modelo.Curso;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Etapa 5 (plus): la API protegida con JWT, probada por HTTP real como lo haria
 * un cliente externo: pedir el token, usarlo como Bearer, y ver que uno invalido
 * o ausente recibe 401.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtApiTest {

    @LocalServerPort
    int puerto;

    // Cliente HTTP de java.net.http: el HttpURLConnection clasico descarta el cuerpo de
    // un 401 cuando el POST va en streaming, y aca queremos leer el JSON del error.
    RestTemplate cliente = new RestTemplate(new JdkClientHttpRequestFactory());

    private String url(String ruta) { return "http://localhost:" + puerto + ruta; }

    @SuppressWarnings("unchecked")
    private String pedirToken(String usuario, String clave) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> r = cliente.exchange(url("/api/auth/token"), HttpMethod.POST,
                new HttpEntity<>(Map.of("username", usuario, "password", clave), h), Map.class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals("Bearer", r.getBody().get("tipo"));
        return (String) r.getBody().get("token");
    }

    @Test
    void conCredencialesCorrectasSeRecibeUnTokenFirmado() {
        String token = pedirToken("admin", "admin123");
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length, "un JWT tiene cabecera.payload.firma");
    }

    @Test
    void conElTokenComoBearerLaApiResponde200() {
        String token = pedirToken("estudiante", "est123");
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        ResponseEntity<Curso[]> r = cliente.exchange(url("/api/cursos"), HttpMethod.GET, new HttpEntity<>(h), Curso[].class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertTrue(r.getBody().length >= 3);
    }

    @Test
    void conClaveIncorrectaNoHayToken() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                cliente.exchange(url("/api/auth/token"), HttpMethod.POST,
                        new HttpEntity<>(Map.of("username", "admin", "password", "otra"), h), Map.class));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        String cuerpo = ex.getResponseBodyAsString();
        assertTrue(cuerpo.contains("incorrectos"), "cuerpo recibido: [" + cuerpo + "] cabeceras: " + ex.getResponseHeaders());
    }

    @Test
    void unTokenManipuladoRecibe401() {
        String token = pedirToken("admin", "admin123");
        String alterado = token.substring(0, token.length() - 4) + "abcd";   // rompe la firma
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(alterado);
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                cliente.exchange(url("/api/cursos"), HttpMethod.GET, new HttpEntity<>(h), String.class));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void httpBasicSigueFuncionandoJuntoAlJwt() {
        HttpHeaders h = new HttpHeaders();
        h.setBasicAuth("admin", "admin123");
        ResponseEntity<Curso[]> r = cliente.exchange(url("/api/cursos"), HttpMethod.GET, new HttpEntity<>(h), Curso[].class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
    }
}

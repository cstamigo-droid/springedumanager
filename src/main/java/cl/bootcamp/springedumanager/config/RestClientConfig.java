package cl.bootcamp.springedumanager.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Etapa 5 (interoperabilidad): un RestTemplate compartido, declarado como @Bean
 * en una clase @Configuration. Spring lo inyecta donde haga falta (CampusService)
 * en vez de crearlo con "new" en cada clase. Los timeouts evitan que un servicio
 * externo lento deje colgada a la aplicacion.
 */
@Configuration
public class RestClientConfig {

    @Bean
    RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(3));
        fabrica.setReadTimeout(Duration.ofSeconds(5));
        return new RestTemplate(fabrica);
    }
}

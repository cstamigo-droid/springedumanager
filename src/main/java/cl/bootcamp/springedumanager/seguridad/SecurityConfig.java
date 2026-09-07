package cl.bootcamp.springedumanager.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Etapa 4: control de acceso.
 *
 * Nota tecnica: los manuales muestran WebSecurityConfigurerAdapter, clase
 * eliminada en Spring Security 6. La forma vigente es declarar un
 * SecurityFilterChain como @Bean; las reglas son equivalentes.
 *
 * Reglas aplicadas:
 *   /cursos/nuevo y /cursos/guardar -> solo ADMIN (carga de datos protegida)
 *   /estudiantes/**, /evaluaciones  -> autenticado (ADMIN o USER)
 *   /api/auth/token                 -> publico (entrega el JWT)
 *   /api/**                         -> autenticado con HTTP Basic o con JWT Bearer,
 *                                      sin formulario ni redirecciones
 *   /evaluaciones/nueva, /guardar, /eliminar -> solo ADMIN (registrar notas)
 *   /login, /registro, /css/**, /js/**, /h2-console, /demo/**, /actuator/health -> publicos
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UsuarioDetailsService detailsService;

    public SecurityConfig(UsuarioDetailsService detailsService) {
        this.detailsService = detailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Lo usa AuthRestController para validar usuario y clave antes de emitir el JWT. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /** Orden 1: esta cadena se evalua primero, solo para /api/**. */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http.securityMatcher("/api/**")
            .userDetailsService(detailsService)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/auth/token").permitAll()
                .requestMatchers("/api/**").authenticated())
            .httpBasic(b -> b.authenticationEntryPoint(apiEntryPoint()))
            // El filtro JWT corre antes que el de Basic: si trae Bearer valido, autentica;
            // si no, Basic sigue funcionando igual que antes.
            .addFilterBefore(jwtFilter, BasicAuthenticationFilter.class)
            // Entry point explicito: 401 + WWW-Authenticate, nunca un redirect.
            .exceptionHandling(e -> e.authenticationEntryPoint(apiEntryPoint()));
        return http.build();
    }

    /** Orden 2: el resto de la aplicacion web, con formulario de login. */
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.userDetailsService(detailsService)
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .authorizeHttpRequests(a -> a
                // /error debe ser publico: cuando la API responde 401 con sendError(),
                // Tomcat re-despacha internamente a /error. Ese despacho vuelve a pasar
                // por esta cadena y, si exige autenticacion, sobrescribe el 401 con un
                // 302 al login. Era la causa real del hallazgo 1 (ver DEPURACION.md).
                // /demo/** simula un servicio externo del campus (interoperabilidad).
                .requestMatchers("/login", "/registro", "/error", "/css/**", "/js/**", "/h2-console/**",
                                 "/demo/**", "/actuator/health").permitAll()
                .requestMatchers("/cursos/nuevo", "/cursos/guardar", "/cursos/eliminar/**",
                                 "/evaluaciones/nueva", "/evaluaciones/guardar", "/evaluaciones/eliminar/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(f -> f
                .loginPage("/login")
                .defaultSuccessUrl("/cursos", true)
                .permitAll())
            .logout(o -> o
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            // Pagina propia cuando el rol no alcanza, en vez del error generico
            .exceptionHandling(e -> e.accessDeniedPage("/acceso-denegado"));
        return http.build();
    }

    /** Entry point de la API: responde 401 con WWW-Authenticate, nunca un redirect. */
    private BasicAuthenticationEntryPoint apiEntryPoint() {
        BasicAuthenticationEntryPoint ep = new BasicAuthenticationEntryPoint();
        ep.setRealmName("SpringEduManager API");
        return ep;
    }
}

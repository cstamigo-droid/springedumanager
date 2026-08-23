package cl.bootcamp.springedumanager.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
 *   /api/**                         -> autenticado, sin formulario (HTTP Basic)
 *   /login, /css/**, /h2-console    -> publicos
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

    /** Orden 1: esta cadena se evalua primero, solo para /api/**. */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
            .userDetailsService(detailsService)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/**").authenticated())
            .httpBasic(Customizer.withDefaults());
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
                .requestMatchers("/login", "/css/**", "/h2-console/**").permitAll()
                .requestMatchers("/cursos/nuevo", "/cursos/guardar", "/cursos/eliminar/**").hasRole("ADMIN")
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
}

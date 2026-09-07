package cl.bootcamp.springedumanager.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee la cabecera "Authorization: Bearer &lt;token&gt;" en cada peticion a la API.
 * Si el token es valido, deja al usuario autenticado en el SecurityContext; si
 * no lo es (o no viene), no hace nada y la cadena decide: el recurso protegido
 * respondera 401 por el entry point de la API.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = cabecera.substring(7);
            try {
                UserDetails usuario = userDetailsService.loadUserByUsername(jwtService.usuario(token));
                if (jwtService.valido(token, usuario)) {
                    var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (RuntimeException tokenInvalido) {
                // Firma incorrecta, token vencido o usuario inexistente: se sigue sin
                // autenticar y el entry point de la API responde 401.
            }
        }
        chain.doFilter(request, response);
    }
}

package cl.bootcamp.springedumanager.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Etapa 5 (plus): emision y validacion de JSON Web Tokens para la API REST.
 *
 * Flujo: usuario y clave -> POST /api/auth/token -> token firmado (HS256) ->
 * el cliente lo manda en "Authorization: Bearer ..." -> JwtAuthenticationFilter
 * lo valida y arma el contexto de seguridad. Es autenticacion SIN estado: el
 * servidor no guarda sesion, todo lo necesario viaja en el token firmado.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiracion-minutos}") long expiracionMinutos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMinutos = expiracionMinutos;
    }

    /** Genera un token con el usuario como subject y sus roles como claim. */
    public String generar(UserDetails usuario) {
        Instant ahora = Instant.now();
        List<String> roles = usuario.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("roles", roles)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el usuario del token. Lanza JwtException si la firma no cuadra o expiro. */
    public String usuario(String token) {
        return claims(token).getSubject();
    }

    public boolean valido(String token, UserDetails usuario) {
        Claims c = claims(token);
        return c.getSubject().equalsIgnoreCase(usuario.getUsername()) && c.getExpiration().after(new Date());
    }

    public long expiracionSegundos() { return expiracionMinutos * 60; }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }
}

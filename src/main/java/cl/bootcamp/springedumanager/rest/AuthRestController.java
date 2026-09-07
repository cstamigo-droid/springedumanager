package cl.bootcamp.springedumanager.rest;

import cl.bootcamp.springedumanager.seguridad.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Etapa 5 (plus): entrega un JWT a cambio de usuario y contrasena.
 *
 * <pre>
 * POST /api/auth/token   {"username":"admin","password":"admin123"}
 * 200  {"token":"eyJ...","tipo":"Bearer","expiraEnSegundos":7200}
 * 401  credenciales incorrectas
 * </pre>
 * Con el token, el cliente llama a la API con "Authorization: Bearer &lt;token&gt;".
 * HTTP Basic sigue funcionando: son dos formas de autenticarse contra la misma API.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    /** Cuerpo de la peticion de token. */
    public record TokenRequest(@NotBlank String username, @NotBlank String password) { }

    /** Respuesta con el token emitido. */
    public record TokenResponse(String token, String tipo, long expiraEnSegundos) { }

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthRestController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    public TokenResponse token(@Valid @RequestBody TokenRequest req) {
        // Si la clave no cuadra, lanza BadCredentialsException -> ApiExceptionHandler -> 401
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        UserDetails usuario = (UserDetails) auth.getPrincipal();
        return new TokenResponse(jwtService.generar(usuario), "Bearer", jwtService.expiracionSegundos());
    }
}

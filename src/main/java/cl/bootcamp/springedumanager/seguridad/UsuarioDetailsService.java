package cl.bootcamp.springedumanager.seguridad;

import cl.bootcamp.springedumanager.modelo.Usuario;
import cl.bootcamp.springedumanager.repositorio.UsuarioRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/** Etapa 4: Spring Security consulta esta clase durante el login. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repositorio;

    public UsuarioDetailsService(UsuarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = repositorio.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return User.withUsername(u.getUsername())
                .password(u.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())))
                .build();
    }
}

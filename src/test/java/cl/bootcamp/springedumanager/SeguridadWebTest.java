package cl.bootcamp.springedumanager;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Etapa 4 (Spring Security): las reglas de acceso se prueban contra la aplicacion,
 * no leyendo la configuracion. Cada prueba dice que rol entra, que ruta pide y
 * que codigo de estado debe recibir.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadWebTest {

    @Autowired
    MockMvc mvc;

    // --- Sin sesion ---

    @Test
    void sinSesionLaWebRedirigeAlLogin() throws Exception {
        mvc.perform(get("/cursos"))
           .andExpect(status().is3xxRedirection())
           .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    void elLoginEsPublico() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
    }

    @Test
    void laConsolaH2ExigeSesion() throws Exception {
        mvc.perform(get("/h2-console/")).andExpect(status().is3xxRedirection())
           .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    void sinCredencialesLaApiResponde401YNoRedirige() throws Exception {
        mvc.perform(get("/api/cursos"))
           .andExpect(status().isUnauthorized())
           .andExpect(header().exists("WWW-Authenticate"));
    }

    // --- Rol USER ---

    @Test
    @WithMockUser(username = "estudiante", roles = "USER")
    void userPuedeVerLasListas() throws Exception {
        mvc.perform(get("/cursos")).andExpect(status().isOk());
        mvc.perform(get("/estudiantes")).andExpect(status().isOk());
        mvc.perform(get("/evaluaciones")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "estudiante", roles = "USER")
    void userNoPuedeAbrirLaCargaDeCursos() throws Exception {
        mvc.perform(get("/cursos/nuevo")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "estudiante", roles = "USER")
    void userNoPuedeGuardarCursosNiPorPostDirecto() throws Exception {
        mvc.perform(post("/cursos/guardar").with(csrf())
                .param("nombre", "Intruso").param("codigo", "X-1").param("horas", "10"))
           .andExpect(status().isForbidden())
           .andExpect(forwardedUrl("/acceso-denegado"));
    }

    @Test
    @WithMockUser(username = "estudiante", roles = "USER")
    void laPaginaDeAccesoDenegadoAceptaPostPorqueElForwardConservaElMetodo() throws Exception {
        mvc.perform(post("/acceso-denegado").with(csrf()))
           .andExpect(status().isOk())
           .andExpect(view().name("error/403"));
    }

    // --- Rol ADMIN ---

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminAbreElFormularioDeCursos() throws Exception {
        mvc.perform(get("/cursos/nuevo"))
           .andExpect(status().isOk())
           .andExpect(view().name("cursos/formulario"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminGuardaUnCursoYVuelveALaLista() throws Exception {
        mvc.perform(post("/cursos/guardar").with(csrf())
                .param("nombre", "Seguridad Web").param("codigo", "SEC-01").param("horas", "24"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("/cursos*"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void unFormularioInvalidoSeQuedaEnElFormularioConElError() throws Exception {
        mvc.perform(post("/cursos/guardar").with(csrf())
                .param("nombre", "").param("codigo", "").param("horas", "0"))
           .andExpect(status().isOk())
           .andExpect(view().name("cursos/formulario"))
           .andExpect(content().string(containsString("obligatorio")));
    }

    // --- Login real contra la base (usuario de application.properties, clave BCrypt) ---

    @Test
    void laApiAceptaAlUsuarioConfiguradoEnApplicationProperties() throws Exception {
        mvc.perform(get("/api/cursos").with(httpBasic("admin", "admin123")))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void laApiRechazaUnaClaveIncorrecta() throws Exception {
        mvc.perform(get("/api/cursos").with(httpBasic("admin", "otra")))
           .andExpect(status().isUnauthorized());
    }
}

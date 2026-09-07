package cl.bootcamp.springedumanager;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import cl.bootcamp.springedumanager.servicio.EstudianteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Registro publico de estudiantes (situacion inicial: "que los estudiantes puedan registrarse")
 * y evaluaciones: alta solo ADMIN en la web, API con DTOs y regla de matricula.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RegistroYEvaluacionesTest {

    @Autowired MockMvc mvc;
    @Autowired EstudianteService estudiantes;

    // --- Registro publico ---

    @Test
    void elRegistroEsPublicoYCreaElEstudiante() throws Exception {
        mvc.perform(get("/registro")).andExpect(status().isOk()).andExpect(view().name("registro"));
        mvc.perform(post("/registro").with(csrf())
                .param("nombre", "Laura Paz").param("email", "laura.paz@bootcamp.cl").param("rut", "14.555.666-7"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/login?registro"));
        org.junit.jupiter.api.Assertions.assertTrue(estudiantes.porEmail("laura.paz@bootcamp.cl").isPresent());
    }

    @Test
    void elRegistroValidaEnElServidorYRechazaEmailRepetido() throws Exception {
        mvc.perform(post("/registro").with(csrf()).param("nombre", "").param("email", "no-es-email").param("rut", ""))
           .andExpect(status().isOk()).andExpect(view().name("registro"))
           .andExpect(content().string(containsString("obligatorio")));
        mvc.perform(post("/registro").with(csrf())
                .param("nombre", "Clon").param("email", "ana@bootcamp.cl").param("rut", "1-1"))
           .andExpect(status().isOk()).andExpect(content().string(containsString("Ya existe un estudiante")));
    }

    // --- Evaluaciones en la web ---

    @Test
    @WithMockUser(roles = "USER")
    void userVeLasEvaluacionesPeroNoPuedeRegistrarlas() throws Exception {
        mvc.perform(get("/evaluaciones")).andExpect(status().isOk());
        mvc.perform(get("/evaluaciones/nueva")).andExpect(status().isForbidden());
        mvc.perform(post("/evaluaciones/guardar").with(csrf())
                .param("estudianteId", "1").param("cursoId", "1").param("nota", "5.0"))
           .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRegistraUnaNotaYLaReglaDeMatriculaSeAplica() throws Exception {
        mvc.perform(get("/evaluaciones/nueva")).andExpect(status().isOk()).andExpect(view().name("evaluaciones/formulario"));
        // Cristian (1) esta en JAVA-01 (1): se registra
        mvc.perform(post("/evaluaciones/guardar").with(csrf())
                .param("estudianteId", "1").param("cursoId", "1").param("nota", "6.0"))
           .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/evaluaciones"));
        // Ana (2) NO esta en JAVA-01 (1): se rechaza y vuelve al formulario con el motivo
        mvc.perform(post("/evaluaciones/guardar").with(csrf())
                .param("estudianteId", "2").param("cursoId", "1").param("nota", "6.0"))
           .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/evaluaciones/nueva"));
    }

    // --- API de evaluaciones con DTOs ---

    @Test
    void laApiDevuelveEvaluacionesPlanas() throws Exception {
        mvc.perform(get("/api/evaluaciones").with(httpBasic("estudiante", "est123")))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].estudiante").exists())
           .andExpect(jsonPath("$[0].curso").exists())
           .andExpect(jsonPath("$[0].aprobada").exists())
           .andExpect(jsonPath("$[0].estudiante.cursos").doesNotExist());   // plano: sin entidades anidadas
        mvc.perform(get("/api/evaluaciones/estudiante/2").with(httpBasic("estudiante", "est123")))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].curso").value("FRONT-01"))
           .andExpect(jsonPath("$[0].aprobada").value(false));
    }

    @Test
    void laApiCreaYEliminaUnaEvaluacionYRechazaSinMatricula() throws Exception {
        String creada = mvc.perform(post("/api/evaluaciones").with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estudianteId\":1,\"cursoId\":2,\"nota\":6.8}"))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.curso").value("BD-01"))
           .andExpect(jsonPath("$.aprobada").value(true))
           .andReturn().getResponse().getContentAsString();
        String id = creada.replaceAll(".*\"id\":(\\d+).*", "$1");
        mvc.perform(delete("/api/evaluaciones/" + id).with(httpBasic("admin", "admin123")))
           .andExpect(status().isNoContent());
        mvc.perform(post("/api/evaluaciones").with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estudianteId\":2,\"cursoId\":1,\"nota\":5.0}"))
           .andExpect(status().isConflict())
           .andExpect(jsonPath("$.error").value(containsString("no esta matriculado")));
        mvc.perform(post("/api/evaluaciones").with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estudianteId\":1,\"cursoId\":1,\"nota\":9.5}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.campos.nota").exists());
    }
}

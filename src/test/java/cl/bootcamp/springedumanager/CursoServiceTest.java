package cl.bootcamp.springedumanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cl.bootcamp.springedumanager.modelo.Curso;
import cl.bootcamp.springedumanager.modelo.Estudiante;
import cl.bootcamp.springedumanager.servicio.CursoService;
import cl.bootcamp.springedumanager.servicio.EstudianteService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Etapa 3 (acceso a datos): la logica de negocio de los @Service y las consultas
 * de los repositorios JPA, contra la base H2 real. @Transactional deshace cada
 * prueba al terminar, asi ninguna deja rastro en la siguiente.
 */
@SpringBootTest
@Transactional
class CursoServiceTest {

    @Autowired CursoService cursos;
    @Autowired EstudianteService estudiantes;

    @Test
    void laCargaInicialDejaTresCursos() {
        assertTrue(cursos.listar().size() >= 3);
        assertTrue(cursos.listar().stream().anyMatch(c -> c.getCodigo().equals("JAVA-01")));
    }

    @Test
    void noSePuedeRepetirElCodigoDeUnCurso() {
        Curso repetido = new Curso("Otro", "JAVA-01", 5, null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> cursos.guardar(repetido));
        assertTrue(ex.getMessage().contains("JAVA-01"));
    }

    @Test
    void laConsultaPersonalizadaFiltraYOrdenaPorHoras() {
        List<Curso> largos = cursos.conHorasDesde(80);
        assertEquals(2, largos.size(), "JAVA-01 (120 h) y FRONT-01 (80 h)");
        assertEquals("JAVA-01", largos.get(0).getCodigo(), "el de mas horas va primero");
    }

    @Test
    void matricularDosVecesNoDuplicaElCurso() {
        Estudiante ana = estudiantes.porEmail("ana@bootcamp.cl").orElseThrow();
        Curso bd = cursos.listar().stream().filter(c -> c.getCodigo().equals("BD-01")).findFirst().orElseThrow();
        int antes = ana.getCursos().size();
        estudiantes.matricular(ana.getId(), bd.getId());
        estudiantes.matricular(ana.getId(), bd.getId());
        assertEquals(antes + 1, estudiantes.porId(ana.getId()).orElseThrow().getCursos().size());
    }

    @Test
    void noSePuedeRepetirElEmailDeUnEstudiante() {
        Estudiante otro = new Estudiante("Clon", "ana@bootcamp.cl", "1-9");
        assertThrows(IllegalArgumentException.class, () -> estudiantes.guardar(otro));
    }
}

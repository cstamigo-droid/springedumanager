package cl.bootcamp.springedumanager.repositorio.jdbc;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Etapa 3, la otra forma de acceder a datos: JdbcTemplate con SQL escrito a mano.
 *
 * JPA resuelve el CRUD de las entidades; para un REPORTE con agregaciones
 * (conteos, promedios, porcentajes) una consulta SQL directa es mas clara y mas
 * eficiente que traer las entidades y calcular en Java. JdbcTemplate se encarga
 * de la conexion, la ejecucion y el cierre de recursos; nosotros ponemos el SQL
 * y el mapeo fila -> objeto. El SQL es estandar: corre igual en H2, MariaDB,
 * MySQL y PostgreSQL.
 */
@Repository
public class ReporteDao {

    /** Una fila del reporte por curso. */
    public record ResumenCurso(String codigo, String nombre, long matriculados, long evaluaciones,
                               Double promedio, long aprobadas) {
        public int porcentajeAprobacion() {
            return evaluaciones == 0 ? 0 : (int) Math.round(aprobadas * 100.0 / evaluaciones);
        }
    }

    /** Totales generales del sistema. */
    public record Totales(long cursos, long estudiantes, long evaluaciones, Double promedioGeneral) { }

    private static final String SQL_RESUMEN = """
        SELECT c.codigo, c.nombre,
               (SELECT COUNT(*) FROM estudiante_curso ec WHERE ec.curso_id = c.id) AS matriculados,
               COUNT(e.id)                                   AS evaluaciones,
               AVG(e.nota)                                   AS promedio,
               SUM(CASE WHEN e.nota >= 4.0 THEN 1 ELSE 0 END) AS aprobadas
        FROM curso c
        LEFT JOIN evaluacion e ON e.curso_id = c.id
        GROUP BY c.id, c.codigo, c.nombre
        ORDER BY c.codigo
        """;

    private static final String SQL_TOTALES = """
        SELECT (SELECT COUNT(*) FROM curso)      AS cursos,
               (SELECT COUNT(*) FROM estudiante) AS estudiantes,
               (SELECT COUNT(*) FROM evaluacion) AS evaluaciones,
               (SELECT AVG(nota) FROM evaluacion) AS promedio_general
        """;

    private final JdbcTemplate jdbc;

    public ReporteDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<ResumenCurso> resumenPorCurso() {
        return jdbc.query(SQL_RESUMEN, (rs, i) -> new ResumenCurso(
                rs.getString("codigo"), rs.getString("nombre"),
                rs.getLong("matriculados"), rs.getLong("evaluaciones"),
                rs.getObject("promedio") == null ? null : rs.getDouble("promedio"),
                rs.getLong("aprobadas")));
    }

    public Totales totales() {
        return jdbc.queryForObject(SQL_TOTALES, (rs, i) -> new Totales(
                rs.getLong("cursos"), rs.getLong("estudiantes"), rs.getLong("evaluaciones"),
                rs.getObject("promedio_general") == null ? null : rs.getDouble("promedio_general")));
    }
}

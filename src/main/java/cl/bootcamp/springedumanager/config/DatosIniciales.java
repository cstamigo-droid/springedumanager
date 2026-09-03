package cl.bootcamp.springedumanager.config;

import cl.bootcamp.springedumanager.modelo.*;
import cl.bootcamp.springedumanager.repositorio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carga inicial: dos usuarios (ADMIN y USER), cursos, estudiantes y evaluaciones,
 * para que la aplicacion se pueda revisar sin tener que cargar datos a mano.
 *
 * Etapa 4: los usuarios y sus claves se CONFIGURAN en application.properties
 * (edumanager.usuarios.*), como pide la Leccion 4. Aqui solo se leen y se
 * guardan en la base con la clave hasheada en BCrypt.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepo;
    private final CursoRepository cursoRepo;
    private final EstudianteRepository estudianteRepo;
    private final EvaluacionRepository evaluacionRepo;
    private final PasswordEncoder encoder;

    @Value("${edumanager.usuarios.admin.nombre:admin}")
    private String adminNombre;
    @Value("${edumanager.usuarios.admin.clave:admin123}")
    private String adminClave;
    @Value("${edumanager.usuarios.estudiante.nombre:estudiante}")
    private String estudianteNombre;
    @Value("${edumanager.usuarios.estudiante.clave:est123}")
    private String estudianteClave;

    public DatosIniciales(UsuarioRepository usuarioRepo, CursoRepository cursoRepo,
                          EstudianteRepository estudianteRepo, EvaluacionRepository evaluacionRepo,
                          PasswordEncoder encoder) {
        this.usuarioRepo = usuarioRepo;
        this.cursoRepo = cursoRepo;
        this.estudianteRepo = estudianteRepo;
        this.evaluacionRepo = evaluacionRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepo.count() > 0) return;

        usuarioRepo.save(new Usuario(adminNombre, encoder.encode(adminClave), Rol.ADMIN));
        usuarioRepo.save(new Usuario(estudianteNombre, encoder.encode(estudianteClave), Rol.USER));

        Curso java = cursoRepo.save(new Curso("Java Full Stack", "JAVA-01", 120,
                "Fundamentos de Java, JEE y Spring Framework"));
        Curso bd = cursoRepo.save(new Curso("Bases de Datos", "BD-01", 60,
                "Modelo relacional, SQL y JPA"));
        Curso front = cursoRepo.save(new Curso("Frontend Web", "FRONT-01", 80,
                "HTML, CSS, JavaScript y Bootstrap"));

        Estudiante cristian = new Estudiante("Cristian Amigo", "cristian@bootcamp.cl", "12.345.678-9");
        cristian.getCursos().add(java);
        cristian.getCursos().add(bd);
        estudianteRepo.save(cristian);

        Estudiante ana = new Estudiante("Ana Soto", "ana@bootcamp.cl", "9.876.543-2");
        ana.getCursos().add(front);
        estudianteRepo.save(ana);

        evaluacionRepo.save(new Evaluacion(cristian, java, 6.5));
        evaluacionRepo.save(new Evaluacion(cristian, bd, 5.8));
        evaluacionRepo.save(new Evaluacion(ana, front, 3.4));

        System.out.println("=== SpringEduManager listo ===");
        System.out.println("  " + adminNombre + " / " + adminClave + "        (ROLE_ADMIN)");
        System.out.println("  " + estudianteNombre + " / " + estudianteClave + "     (ROLE_USER)");
    }
}

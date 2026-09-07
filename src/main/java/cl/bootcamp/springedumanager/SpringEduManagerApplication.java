package cl.bootcamp.springedumanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringEduManagerApplication {

	/** Motores con perfil propio (application-<motor>.properties). */
	static final java.util.Set<String> MOTORES = java.util.Set.of("h2", "mariadb", "mysql", "postgresql");

	public static void main(String[] args) {
		String motor = System.getenv().getOrDefault("DB_ENGINE", "h2");
		if (!validarMotor(motor)) {
			System.err.println("DB_ENGINE=" + motor + " no es un motor conocido. Usar uno de: " + MOTORES);
			System.exit(1);
		}
		SpringApplication.run(SpringEduManagerApplication.class, args);
	}

	/**
	 * Sin esta comprobacion, un DB_ENGINE mal escrito activa un perfil que no existe y Spring
	 * arranca igual con un H2 embebido de nombre aleatorio: el operador cree estar en su motor
	 * y en realidad esta en una base fantasma. Mejor fallar al arrancar, con el mensaje claro.
	 */
	static boolean validarMotor(String motor) {
		return motor != null && MOTORES.contains(motor.trim().toLowerCase());
	}

}

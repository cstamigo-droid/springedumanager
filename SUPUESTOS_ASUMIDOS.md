# Supuestos asumidos

Decisiones que el enunciado deja abiertas y cómo se resolvieron. Están para que quien evalúe no
tenga que adivinar por qué el proyecto hace lo que hace.

- **Evaluación = la nota de un estudiante en un curso** (1,0 a 7,0; se aprueba con 4,0). El
  enunciado menciona "evaluaciones" en el objetivo pero no las define; se modeló como lo que un
  estudiante consulta ("mis notas"), con fecha. Regla académica: solo se evalúa a un estudiante en un
  curso en el que está **matriculado**; si no, la web y la API rechazan con el motivo.
- **Registro de estudiantes ≠ usuario de acceso.** La situación inicial pide que los estudiantes
  puedan registrarse; `/registro` (público) crea el perfil académico. Las credenciales de login
  siguen **configuradas en `application.properties`**, que es lo que exige la Lección 4. Conectar
  registro y credencial queda para un módulo futuro (el `Usuario` ya existe en la base con BCrypt).
- **Matrícula (estudiante ↔ curso) modelada** para poder "visualizar sus cursos": relación N:M
  en `estudiante_curso`, con la acción "Matricular" en el detalle del estudiante.
- **H2 es el motor por defecto** (arranca sin instalar nada). MySQL es el segundo que pide la pauta;
  MariaDB y PostgreSQL se agregan como extensión y se eligen con `DB_ENGINE`. Solo H2 y MariaDB se
  verificaron corriendo (los otros dos tienen su perfil y driver, sin servidor para probarlos).
- **La API expone entidades en cursos y estudiantes, y DTOs en evaluaciones.** Cursos y estudiantes
  son planos y sirven tal cual (con `@JsonIgnore` en el lado inverso); en evaluaciones el JSON con las
  entidades anidadas sería confuso, así que se devuelve un `EvaluacionResponse` plano y se recibe un
  `EvaluacionRequest` con ids. Es el mismo criterio que muestra el solucionario del módulo al pasar
  de *junior* a *middle*: desacoplar la API del modelo donde el modelo no es un buen contrato.
- **JWT es un plus, no reemplaza a HTTP Basic.** La API acepta ambos; así Postman, `curl` y el
  API Lab funcionan sin pasos previos, y el token demuestra autenticación sin estado.
- **El servicio externo para RestTemplate se simula dentro del proyecto** (`/demo/campus/calendario`):
  la demostración no puede depender de que haya internet o de un tercero el día de la evaluación.
- **Spring Boot 4.1.1** (el actual de Spring Initializr al crear el proyecto). El starter web se
  llama `spring-boot-starter-webmvc`; equivale al `spring-boot-starter-web` de las versiones 2 y 3.
- **La clave del JWT y las contraseñas de los usuarios demo están en el properties a propósito**: es
  un proyecto de clase. En un despliegue real se pasan por variables de entorno (`APP_JWT_SECRET`,
  `DB_PASSWORD`), que el properties ya lee.

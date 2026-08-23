# SpringEduManager

Aplicación web educativa para gestionar **estudiantes, cursos y evaluaciones** de un bootcamp
de programación.

**Proyecto de evaluación del Módulo 6 — Desarrollo de aplicaciones JEE con Spring Framework**
Bootcamp Desarrollo Full Stack Java Trainee · Autor: **Cristian Amigo**

---

## El problema que resuelve

La Coordinación Académica gestionaba cursos, prácticas y evaluaciones con hojas de cálculo y
formularios aislados. SpringEduManager reúne todo en una sola plataforma web interna, donde
los estudiantes pueden registrarse, ver sus cursos y consultar sus evaluaciones, y donde el
personal académico administra el catálogo. Además expone una **API REST** para integrarse con
otros sistemas del campus.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Web | Spring MVC + Thymeleaf |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | H2 (embebida) — conmutable a MySQL |
| Seguridad | Spring Security + BCrypt |
| API | REST con respuestas JSON |
| Gestor de dependencias | Maven |

## Cómo ejecutar

```bash
mvn clean package
java -jar target/springedumanager-0.0.1-SNAPSHOT.jar
```

Abrir **http://localhost:8080** — redirige al login.

### Usuarios de prueba

| Usuario | Contraseña | Rol | Puede |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Todo, incluida la carga de cursos |
| `estudiante` | `est123` | USER | Consultar cursos, estudiantes y evaluaciones |

Las contraseñas se guardan **hasheadas con BCrypt**, nunca en texto plano.

---

## Estructura del proyecto

```
src/main/java/cl/bootcamp/springedumanager/
├── modelo/          Entidades JPA: Estudiante, Curso, Evaluacion, Usuario, Rol
├── repositorio/     Interfaces que extienden JpaRepository
├── servicio/        Lógica de negocio con @Service y @Transactional
├── controlador/     Controladores MVC (@Controller) que devuelven vistas
├── rest/            Controladores REST (@RestController) que devuelven JSON
├── seguridad/       SecurityConfig y UsuarioDetailsService
└── config/          Carga de datos iniciales

src/main/resources/
├── templates/       Vistas Thymeleaf
├── static/css/      Hoja de estilos
└── application.properties
```

El flujo respeta la separación en capas: **controlador → servicio → repositorio → base de datos**.
El controlador nunca consulta la base directamente, y la vista nunca contiene lógica de negocio.

## Modelo de datos

- **Estudiante** ↔ **Curso**: relación *muchos a muchos* (un estudiante toma varios cursos y un
  curso tiene varios estudiantes), materializada en la tabla `estudiante_curso`.
- **Evaluacion** → **Estudiante** y **Curso**: *muchos a uno* hacia cada lado. Cada evaluación
  es la nota de un estudiante en un curso, con su fecha.

---

## Las 5 etapas del proyecto

### Etapa 1 — El gestor de proyectos
Proyecto creado con Maven desde Spring Initializr, con las dependencias necesarias para todas
las etapas siguientes: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`,
`spring-boot-starter-security`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-validation`
y `h2`. Ciclo de vida verificado con `mvn clean`, `mvn install` y `mvn package`.

### Etapa 2 — Spring MVC
Entidades `Estudiante` y `Curso` con sus controladores y vistas. Formularios en Thymeleaf para
ingresar datos, rutas con `@GetMapping` y `@PostMapping`, y listados en pantalla. Los formularios
validan en el servidor con Bean Validation y muestran el mensaje de error junto al campo.

### Etapa 3 — Acceso a datos
`EstudianteRepository`, `CursoRepository` y `EvaluacionRepository` extendiendo `JpaRepository`.
Base H2 embebida. Lógica en clases `@Service` con `@Transactional` — `readOnly = true` en las
consultas. Incluye búsquedas por convención (`findByEmail`, `findByCodigo`) y una consulta
personalizada con `@Query`.

### Etapa 4 — Spring Security
Autenticación por formulario contra la base de datos, con `UserDetailsService` propio y
contraseñas BCrypt. Dos roles:

| Ruta | ADMIN | USER |
|---|---|---|
| `/cursos` (ver) | Sí | Sí |
| `/cursos/nuevo` (cargar) | Sí | **403** |
| `/estudiantes`, `/evaluaciones` | Sí | Sí |
| `/api/**` | Requiere autenticación | Requiere autenticación |

La protección se aplica por dos vías: reglas en `SecurityConfig` y `@PreAuthorize` sobre los
métodos del controlador. Login y logout funcionales.

### Etapa 5 — Interoperabilidad (REST)

| Método | Endpoint | Acción | Respuesta |
|---|---|---|---|
| GET | `/api/cursos` | Listar | 200 + JSON |
| GET | `/api/cursos/{id}` | Obtener uno | 200 / 404 |
| POST | `/api/cursos` | Crear | 201 + JSON |
| PUT | `/api/cursos/{id}` | Actualizar | 200 / 404 |
| DELETE | `/api/cursos/{id}` | Eliminar | 204 / 404 |

Los mismos cinco endpoints existen para `/api/estudiantes`. La API usa HTTP Basic, de modo que
se puede consumir desde Postman o desde otro sistema con `RestTemplate`.

```bash
curl -u admin:admin123 http://localhost:8080/api/cursos
```

---

## Decisiones técnicas que vale la pena explicar

**`SecurityFilterChain` en vez de `WebSecurityConfigurerAdapter`.** Los manuales del módulo
muestran la segunda, pero fue eliminada en Spring Security 6. La forma vigente es declarar un
`@Bean` de tipo `SecurityFilterChain`; las reglas que se escriben adentro son equivalentes.

**Dos cadenas de seguridad separadas, con `@Order`.** La API REST y la aplicación web tienen
necesidades distintas: la web usa formulario de login y la API usa HTTP Basic sin redirecciones.
Se declaran dos cadenas y se les da orden explícito, porque de lo contrario la primera que
coincida atiende todas las peticiones.

**`@JsonIgnore` en el lado inverso de la relación.** `Curso` conoce a sus `Estudiante` y cada
`Estudiante` conoce sus `Curso`. Al serializar a JSON esto genera una recursión infinita que
devuelve una respuesta truncada e inválida. Marcando el lado inverso con `@JsonIgnore` se corta
el ciclo y el JSON queda bien formado.

**H2 en vez de MySQL.** La aplicación corre sin instalar ningún motor. El cambio a MySQL son
cuatro líneas de `application.properties`, ya documentadas en ese archivo.

## Verificación

Todo lo anterior fue probado sobre la aplicación en ejecución:

```
/cursos sin sesión            302  → redirige al login
/api/cursos sin autenticar    401  → protegida
login admin  → /cursos/nuevo  200  → puede cargar cursos
login estudiante → /cursos/nuevo  403  → bloqueado por rol
POST /api/cursos              201  → creado
PUT  /api/cursos/{id}         200  → actualizado
DELETE /api/cursos/{id}       204  → eliminado
```

Las capturas del flujo están en `CAPTURAS/`.

# SpringEduManager

Aplicación web educativa para gestionar **estudiantes, cursos y evaluaciones** de un bootcamp
de programación.

**Proyecto de evaluación del Módulo 6 — Desarrollo de aplicaciones JEE con Spring Framework**
Bootcamp Desarrollo Full Stack Java Trainee · Autor: **Cristian Amigo**
Repositorio: https://github.com/cstamigo-droid/springedumanager

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
| Gestor de dependencias | Maven (con Maven Wrapper incluido) |
| Pruebas | JUnit 5 + MockMvc + RestTemplate (26 pruebas) |

## Cómo ejecutar

Requiere **JDK 21** (el del ambiente del curso) y conexión a internet la primera vez, para que
Maven descargue las dependencias.

```bash
mvn clean install package
java -jar target/springedumanager-0.0.1-SNAPSHOT.jar
```

Si Maven no está instalado, el proyecto trae el wrapper: `mvnw.cmd clean install package`
(Windows) o `./mvnw clean install package` (Linux/macOS).

Abrir **http://localhost:8080** — redirige al login. Si el 8080 está ocupado:
`java -jar target/springedumanager-0.0.1-SNAPSHOT.jar --server.port=8095`.

### Usuarios de prueba

| Usuario | Contraseña | Rol | Puede |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Todo, incluida la carga y eliminación de cursos |
| `estudiante` | `est123` | USER | Consultar cursos, estudiantes y evaluaciones |

Los usuarios se **configuran en `application.properties`** (`edumanager.usuarios.*`). Al arrancar,
`DatosIniciales` los lee y los guarda en la tabla `usuario` con la contraseña **hasheada con
BCrypt**, nunca en texto plano. Cambiar una clave es editar una línea del archivo.

---

## Estructura del proyecto

```
src/main/java/cl/bootcamp/springedumanager/
├── modelo/          Entidades JPA: Estudiante, Curso, Evaluacion, Usuario, Rol
├── repositorio/     Interfaces que extienden JpaRepository
├── servicio/        Lógica de negocio con @Service y @Transactional
├── controlador/     Controladores MVC (@Controller) que devuelven vistas
├── rest/            Controladores REST (@RestController) que devuelven JSON + ApiExceptionHandler
├── seguridad/       SecurityConfig y UsuarioDetailsService
└── config/          Carga de datos iniciales (usuarios, cursos, estudiantes, evaluaciones)

src/main/resources/
├── templates/       Vistas Thymeleaf (cursos, estudiantes, evaluaciones, login, error 403)
├── static/css/      Hoja de estilos
└── application.properties

src/test/java/cl/bootcamp/springedumanager/
├── SeguridadWebTest.java        Reglas de acceso por rol, login, 401 de la API (MockMvc)
├── ApiRestTemplateTest.java     Consumo de la API con RestTemplate, CRUD completo por HTTP
├── CursoServiceTest.java        Lógica de negocio y consultas JPA contra H2
└── SpringEduManagerApplicationTests.java

postman/         Colección Postman con los 12 endpoints y sus aserciones
CAPTURAS/        14 capturas del flujo + _GUION_CAPTURAS.md (qué muestra cada una)
herramientas/    capturar_flujo.py — regenera las capturas recorriendo la app en Chrome
pruebas_flujos.sh  38 comprobaciones HTTP sobre la aplicación corriendo
DEPURACION.md    Revisión técnica: 7 hallazgos con causa raíz y corrección
GUIA_TEST_DE_USO.md  Guion del test de uso con una persona externa
```

El flujo respeta la separación en capas: **controlador → servicio → repositorio → base de datos**.
El controlador nunca consulta la base directamente, y la vista nunca contiene lógica de negocio.

## Modelo de datos

- **Estudiante** ↔ **Curso**: relación *muchos a muchos* (un estudiante toma varios cursos y un
  curso tiene varios estudiantes), materializada en la tabla `estudiante_curso`.
- **Evaluacion** → **Estudiante** y **Curso**: *muchos a uno* hacia cada lado. Cada evaluación
  es la nota de un estudiante en un curso, con su fecha.
- **Usuario** con `Rol` (`ADMIN` / `USER`) para la autenticación.

---

## Las 5 etapas del proyecto (una por lección del módulo)

### Etapa 1 — El gestor de proyectos (Lección 1)
Proyecto creado con Maven desde Spring Initializr. El `pom.xml` declara las dependencias de
todas las etapas siguientes: `spring-boot-starter-webmvc` (en Spring Boot 4 es el nombre del
starter web; equivale a `spring-boot-starter-web` de las versiones 2 y 3), `spring-boot-starter-data-jpa`,
`spring-boot-starter-security`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-validation`
y `h2`. Ciclo de vida verificado desde consola con `mvn clean`, `mvn install` y `mvn package`
(el `install` corre las 26 pruebas antes de instalar el artefacto en el repositorio local).

### Etapa 2 — Spring MVC (Lección 2)
Entidades `Estudiante` y `Curso` con sus controladores (`@Controller`, `@GetMapping`,
`@PostMapping`) y vistas Thymeleaf. Formularios para ingresar estudiantes y cursos, y listados
en pantalla. Los formularios validan en el servidor con Bean Validation y muestran el mensaje de
error junto al campo (captura 06).

### Etapa 3 — Acceso a datos (Lección 3)
`EstudianteRepository`, `CursoRepository`, `EvaluacionRepository` y `UsuarioRepository`
extendiendo `JpaRepository`, anotados con `@Repository`. Base H2 embebida (consola en
`/h2-console`). Lógica en clases `@Service` con `@Transactional` — `readOnly = true` en las
consultas. Incluye búsquedas por convención (`findByEmail`, `findByCodigo`) y una consulta
personalizada con `@Query`. Lo que se ingresa en los formularios se persiste y se consulta desde
la base (capturas 05, 07, 08, 09).

### Etapa 4 — Spring Security (Lección 4)
Dependencia `spring-boot-starter-security`. Usuarios configurados en `application.properties`,
autenticación por formulario contra la base de datos con `UserDetailsService` propio y
contraseñas BCrypt. Dos roles:

| Ruta | ADMIN | USER |
|---|---|---|
| `/cursos`, `/estudiantes`, `/evaluaciones` (ver) | Sí | Sí |
| `/cursos/nuevo`, `/cursos/guardar` (cargar cursos) | Sí | **403** |
| `/cursos/eliminar/{id}` | Sí | **403** |
| `/api/**` | HTTP Basic | HTTP Basic |

La protección se aplica por dos vías: reglas en `SecurityConfig` y `@PreAuthorize("hasRole('ADMIN')")`
sobre los métodos del controlador. Login y logout funcionales (capturas 01, 02, 12). Un USER que
intenta cargar cursos recibe una página de acceso denegado propia (captura 11).

### Etapa 5 — Interoperabilidad (Lección 5)

| Método | Endpoint | Acción | Respuesta |
|---|---|---|---|
| GET | `/api/cursos` | Listar | 200 + JSON |
| GET | `/api/cursos/{id}` | Obtener uno | 200 / 404 |
| POST | `/api/cursos` | Crear | 201 + JSON · 400 si faltan campos · 409 si el código ya existe |
| PUT | `/api/cursos/{id}` | Actualizar | 200 / 404 · 409 si el código es de otro curso |
| DELETE | `/api/cursos/{id}` | Eliminar | 204 / 404 |

Los mismos cinco endpoints existen para `/api/estudiantes`. La API usa HTTP Basic y responde
siempre en JSON, incluidos los errores (`{"error": "...", "campos": {...}}`).

**Consumo validado de dos formas, como pide la lección:**
- **Postman:** importar `postman/SpringEduManager.postman_collection.json` (File → Import). Trae
  los 12 endpoints con aserciones automáticas; el botón *Run collection* los ejecuta en orden.
- **RestTemplate:** `ApiRestTemplateTest` levanta la aplicación en un puerto real y la consume con
  `org.springframework.web.client.RestTemplate`, como lo haría otro sistema: CRUD completo,
  401 sin credenciales, 400 con detalle por campo, 404 y 409.

```bash
curl -u admin:admin123 http://localhost:8080/api/cursos
```

JWT (plus opcional de la lección) no se implementó; la API queda protegida con HTTP Basic.

---

## Decisiones técnicas que vale la pena explicar

**`SecurityFilterChain` en vez de `WebSecurityConfigurerAdapter`.** Los manuales del módulo
muestran la segunda, pero fue eliminada en Spring Security 6. La forma vigente es declarar un
`@Bean` de tipo `SecurityFilterChain`; las reglas que se escriben adentro son equivalentes.

**Dos cadenas de seguridad separadas, con `@Order`.** La API REST y la aplicación web tienen
necesidades distintas: la web usa formulario de login y la API usa HTTP Basic sin redirecciones.
Se declaran dos cadenas y se les da orden explícito, porque de lo contrario la primera que
coincida atiende todas las peticiones.

**Usuarios en `application.properties`, pero guardados en la base.** La lección pide configurar
los usuarios en el archivo de propiedades. Usar directamente `spring.security.user.*` habría dado
un solo usuario en memoria y sin roles distintos; en cambio se leen propiedades propias
(`edumanager.usuarios.*`) y se persisten con BCrypt, así la aplicación queda lista para que el
registro de estudiantes cree usuarios reales en módulos futuros.

**`@JsonIgnore` en el lado inverso de la relación.** `Curso` conoce a sus `Estudiante` y cada
`Estudiante` conoce sus `Curso`. Al serializar a JSON esto genera una recursión infinita que
devuelve una respuesta truncada e inválida. Marcando el lado inverso con `@JsonIgnore` se corta
el ciclo y el JSON queda bien formado.

**La unicidad se verifica antes de modificar la entidad.** Si un `PUT` cambia el código de un
curso por uno que ya usa otro, y la comprobación se hace después de modificar el objeto, Hibernate
hace *flush* del cambio al ejecutar la consulta y la restricción `UNIQUE` de la base falla con un
500. Por eso `CursoService.actualizar` consulta primero y modifica después, y el cliente recibe un
409 con el motivo.

**H2 en vez de MySQL.** La aplicación corre sin instalar ningún motor. El cambio a MySQL son
cuatro líneas de `application.properties`, ya documentadas en ese archivo.

## Verificación

**Pruebas automáticas — 26, todas en verde con `mvn install`:**

| Clase | Pruebas | Qué demuestra |
|---|---|---|
| `SeguridadWebTest` | 12 | 302 al login sin sesión · 401 en la API · USER 403 en la carga (también por POST directo, reenviado a la página propia) · ADMIN 200 · formulario inválido se queda con el error · login real contra BCrypt |
| `ApiRestTemplateTest` | 8 | CRUD de cursos y estudiantes con `RestTemplate` por HTTP real · 400 con campos · 404 · 409 en POST y PUT · 401 sin credenciales |
| `CursoServiceTest` | 5 | Carga inicial · código/email repetido rechazado · `@Query` filtra y ordena · matricular no duplica |
| `SpringEduManagerApplicationTests` | 1 | El contexto arranca |

**Sobre la aplicación corriendo — `pruebas_flujos.sh`, 38 comprobaciones HTTP, 38 OK:**

```
/cursos sin sesión                302  → redirige al login
/api/cursos sin autenticar        401  → con WWW-Authenticate, sin redirect
login admin  → /cursos/nuevo      200  → puede cargar cursos
login estudiante → /cursos/nuevo  403  → bloqueado por rol, página propia
POST /cursos/guardar como USER    403  → misma página (no 405), también con CSRF inválido
POST /cursos/guardar vacío        200  → se queda en el formulario con los mensajes
POST /api/cursos                  201  → creado
POST /api/cursos inválido         400  → {"campos": {"nombre": "...", "codigo": "...", "horas": "..."}}
POST /api/cursos código repetido  409  → {"error": "Ya existe un curso con el codigo ..."}
PUT  /api/cursos/{id}             200  → actualizado
DELETE /api/cursos/{id}           204  → eliminado
logout                            302  → /login?logout, y /cursos vuelve a pedir login
```

```bash
bash pruebas_flujos.sh                      # contra http://localhost:8080
bash pruebas_flujos.sh http://localhost:8095
```

Las 14 capturas del flujo están en `CAPTURAS/`, con `_GUION_CAPTURAS.md` explicando qué muestra
cada una y a qué lección responde.

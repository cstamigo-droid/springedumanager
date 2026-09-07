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
personal académico administra el catálogo. Además expone una **API REST** (HTTP Basic o **JWT**)
para integrarse con otros sistemas del campus, y ella misma **consume otros servicios** con
RestTemplate.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Web | Spring MVC + Thymeleaf (+ jQuery/AJAX en API Lab) |
| Persistencia | Spring Data JPA + Hibernate (CRUD) · **JdbcTemplate** (reportes) |
| Base de datos | **H2** por defecto · MariaDB · MySQL · PostgreSQL, elegible con `DB_ENGINE` |
| Seguridad | Spring Security + BCrypt · roles ADMIN/USER · API con HTTP Basic y **JWT** |
| API | REST con respuestas JSON · interoperabilidad con **RestTemplate** |
| Observabilidad | Actuator (`/actuator/health`) · log en `logs/` |
| Gestor de dependencias | Maven (con Maven Wrapper incluido) · JaCoCo · JavaDoc |
| Pruebas | JUnit 5 + MockMvc + RestTemplate por HTTP real (**43 pruebas**, cobertura 86 %) |

## Cómo ejecutar

Requiere **JDK 21** (el del ambiente del curso) y conexión a internet la primera vez, para que
Maven descargue las dependencias.

```bash
mvn clean install package
java -jar target/springedumanager-0.0.1-SNAPSHOT.jar
```

Si Maven no está instalado, el proyecto trae el wrapper: `mvnw.cmd clean install package`
(Windows) o `./mvnw clean install package` (Linux/macOS). Desde Eclipse/STS: *Import → Existing
Maven Projects* y luego *Run As → Spring Boot App* (hay configuraciones `.launch` para H2 y MariaDB).

Abrir **http://localhost:8080** — redirige al login. Si el 8080 está ocupado:
`java -jar target/springedumanager-0.0.1-SNAPSHOT.jar --server.port=8095`.

### Usuarios de prueba

| Usuario | Contraseña | Rol | Puede |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Todo, incluida la carga y eliminación de cursos |
| `estudiante` | `est123` | USER | Consultar cursos, estudiantes, evaluaciones, reportes e integración |

Además, cualquier persona puede **registrarse como estudiante** en `/registro` (público): crea su perfil académico; la credencial de acceso la entrega la Coordinación (usuarios en el properties, Lección 4).

Los usuarios se **configuran en `application.properties`** (`edumanager.usuarios.*`). Al arrancar,
`DatosIniciales` los lee y los guarda en la tabla `usuario` con la contraseña **hasheada con
BCrypt**, nunca en texto plano. Cambiar una clave es editar una línea del archivo.

### Cambiar el motor de base de datos

Por defecto corre con **H2 en memoria**: no hay que instalar nada. Para otro motor, crear la base
`edumanager` y elegir el perfil con la variable `DB_ENGINE`:

```bash
set DB_ENGINE=mariadb         # Windows      (Linux/macOS: export DB_ENGINE=mariadb)
java -jar target/springedumanager-0.0.1-SNAPSHOT.jar
```

Cada motor tiene su archivo `application-<motor>.properties` con URL, usuario y clave (también
por variables `DB_URL`, `DB_USER`, `DB_PASSWORD`). Un `DB_ENGINE` que no sea uno de los cuatro
detiene el arranque con un mensaje claro, en vez de caer en silencio a una H2 sin nombre. Controladores, servicios, repositorios y vistas
**no cambian**: solo cambia el `DataSource`. Verificado con H2 y con **MariaDB 12.3 real**: las
mismas comprobaciones HTTP pasan en los dos motores y las 5 tablas se crean solas.

---

## Estructura del proyecto

```
src/main/java/cl/bootcamp/springedumanager/
├── modelo/          Entidades JPA: Estudiante, Curso, Evaluacion, Usuario, Rol
├── repositorio/     Interfaces que extienden JpaRepository
│   └── jdbc/        ReporteDao: reporte académico con JdbcTemplate y SQL directo
├── servicio/        Lógica de negocio con @Service y @Transactional
├── controlador/     Controladores MVC (@Controller): cursos, estudiantes, evaluaciones, registro, reportes, integración
├── rest/            Controladores REST (@RestController): cursos, estudiantes, evaluaciones (DTOs), auth (JWT) + ApiExceptionHandler
├── integracion/     CampusService (RestTemplate) y el servicio externo simulado del campus
├── seguridad/       SecurityConfig, UsuarioDetailsService, JwtService, JwtAuthenticationFilter
└── config/          DatosIniciales (carga inicial) y RestClientConfig (@Bean RestTemplate)

src/main/resources/
├── templates/       Vistas Thymeleaf (cursos, estudiantes, evaluaciones, registro, reportes, integracion, api-lab, login, 403)
├── static/          css/estilo.css · js/vendor/jquery-3.6.1.min.js
├── application.properties            configuración común, usuarios, JWT
└── application-{h2,mariadb,mysql,postgresql}.properties   un DataSource por motor

src/test/java/cl/bootcamp/springedumanager/
├── SeguridadWebTest.java        Reglas de acceso por rol, login, 401 de la API (MockMvc) — 12
├── ApiRestTemplateTest.java     CRUD completo consumido con RestTemplate por HTTP real — 8
├── JwtApiTest.java              Token JWT, Bearer válido/alterado, Basic sigue funcionando — 5
├── IntegracionYReportesTest.java  JdbcTemplate contra la base, RestTemplate contra los servicios — 4
├── RegistroYEvaluacionesTest.java  Registro público, notas solo ADMIN, ficha con notas, API con DTOs — 7
├── CursoServiceTest.java        Lógica de negocio y consultas JPA — 5
├── MotorDeBaseTest.java         DB_ENGINE inválido detiene el arranque — 2
└── SpringEduManagerApplicationTests.java — 1

postman/         Colección Postman: 23 peticiones con aserciones (Basic, JWT, evaluaciones, interoperabilidad)
CAPTURAS/        21 capturas del flujo + _GUION_CAPTURAS.md (qué muestra cada una)
herramientas/    capturar_flujo.py — regenera las capturas recorriendo la app en Chrome
pruebas_flujos.sh  64 comprobaciones HTTP sobre la aplicación corriendo
DEPURACION.md    Revisión técnica: hallazgos con causa raíz y corrección
GUIA_TEST_DE_USO.md  Guion del test de uso con una persona externa
CHECKLIST_CIERRE.md  Checklist de cierre del producto, 18/18 con evidencia
evidencias/      Devolución del test de uso con una persona externa
SUPUESTOS_ASUMIDOS.md  Decisiones que el enunciado dejaba abiertas y por qué se tomaron
*.launch         Configuraciones de ejecución para Eclipse/STS (H2 y MariaDB)
```

El flujo respeta la separación en capas: **controlador → servicio → repositorio → base de datos**.
El controlador nunca consulta la base directamente, y la vista nunca contiene lógica de negocio.
Spring crea e inyecta los componentes por constructor (inversión de control): en el código no
hay `new CursoService()` ni `new RestTemplate()` sueltos; el `RestTemplate` es un `@Bean` de
`RestClientConfig`.

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
`spring-boot-starter-jdbc`, `spring-boot-starter-security`, `spring-boot-starter-thymeleaf`,
`spring-boot-starter-validation`, `spring-boot-starter-actuator`, `jjwt` y los drivers de H2,
MariaDB, MySQL y PostgreSQL. Plugins: Spring Boot, JaCoCo y JavaDoc. Ciclo de vida verificado
desde consola con `mvn clean`, `mvn install` y `mvn package` (el `install` corre las 43 pruebas
antes de instalar el artefacto en el repositorio local).

```bash
mvn clean test jacoco:report    # cobertura en target/site/jacoco/index.html (86 % de instrucciones)
mvn javadoc:javadoc             # documentación en target/reports/apidocs/index.html
```

### Etapa 2 — Spring MVC (Lección 2)
Entidades `Estudiante` y `Curso` con sus controladores (`@Controller`, `@GetMapping`,
`@PostMapping`) y vistas Thymeleaf (`th:each`, `th:if`, `th:field`, `th:action`, `th:href`).
Formularios para ingresar estudiantes y cursos (y evaluaciones), listados en pantalla, y un **registro público** de estudiantes (`/registro`, captura 02) que responde a la situación inicial. Los formularios validan
en el servidor con Bean Validation y muestran el mensaje de error junto al campo (captura 06).
Los controladores muestran las tres formas de entregar datos a la vista: `Model`, `ModelMap`
(`ReporteController`) y `ModelAndView` (`IntegracionController`).

### Etapa 3 — Acceso a datos (Lección 3)
`EstudianteRepository`, `CursoRepository`, `EvaluacionRepository` y `UsuarioRepository`
extendiendo `JpaRepository`, anotados con `@Repository`. Lógica en clases `@Service` con
`@Transactional` — `readOnly = true` en las consultas. Incluye búsquedas por convención
(`findByEmail`, `findByCodigo`) y una consulta personalizada con `@Query`. Lo que se ingresa en
los formularios se persiste y se consulta desde la base (capturas 05, 07, 08, 09).

**Las dos formas de acceder a datos, comparadas en el mismo proyecto:** el CRUD usa JPA; el
**reporte académico** (`/reportes`, captura 10) usa `JdbcTemplate` con SQL escrito a mano para
las agregaciones (totales, promedio por curso, % de aprobación). Para un reporte, una consulta
con `GROUP BY` es más clara y más eficiente que traer las entidades y calcular en Java.

Base H2 embebida por defecto (consola en `/h2-console`, solo con sesión iniciada), conmutable a MariaDB, MySQL o
PostgreSQL con `DB_ENGINE` (ver arriba).

### Etapa 4 — Spring Security (Lección 4)
Dependencia `spring-boot-starter-security`. Usuarios configurados en `application.properties`,
autenticación por formulario contra la base de datos con `UserDetailsService` propio y
contraseñas BCrypt. Dos roles:

| Ruta | ADMIN | USER |
|---|---|---|
| `/cursos`, `/estudiantes`, `/evaluaciones`, `/reportes`, `/integracion`, `/api-lab` (ver) | Sí | Sí |
| `/cursos/nuevo`, `/cursos/guardar` (cargar cursos) | Sí | **403** |
| `/cursos/eliminar/{id}` | Sí | **403** |
| `/evaluaciones/nueva`, `/evaluaciones/guardar`, `/evaluaciones/eliminar/{id}` (registrar notas) | Sí | **403** |
| `/registro` (registro público de estudiantes) | público | público |
| `/api/**` | HTTP Basic **o** JWT Bearer | HTTP Basic **o** JWT Bearer |
| `/api/auth/token`, `/demo/**`, `/actuator/health` | públicos | públicos |
| `/h2-console` | con sesión | con sesión |

La protección se aplica por dos vías: reglas en `SecurityConfig` y `@PreAuthorize("hasRole('ADMIN')")`
sobre los métodos del controlador. Login y logout funcionales (capturas 01, 02, 15). Un USER que
intenta cargar cursos recibe una página de acceso denegado propia (captura 14).

### Etapa 5 — Interoperabilidad (Lección 5)

**La API REST que expone SpringEduManager:**

| Método | Endpoint | Acción | Respuesta |
|---|---|---|---|
| POST | `/api/auth/token` | Obtener un JWT con usuario y clave | 200 `{token, tipo, expiraEnSegundos}` · 401 |
| GET | `/api/cursos` | Listar | 200 + JSON |
| GET | `/api/cursos/{id}` | Obtener uno | 200 / 404 |
| POST | `/api/cursos` | Crear | 201 + JSON · 400 si faltan campos · 409 si el código ya existe |
| PUT | `/api/cursos/{id}` | Actualizar | 200 / 404 · 409 si el código es de otro curso |
| DELETE | `/api/cursos/{id}` | Eliminar | 204 / 404 |

Los mismos cinco endpoints existen para `/api/estudiantes`. **`/api/evaluaciones`** (GET lista, GET `/{id}`, GET `/estudiante/{id}`, POST, DELETE) usa **DTOs** (`EvaluacionRequest` con ids, `EvaluacionResponse` plano con `aprobada`) en vez de la entidad, y responde 409 si el estudiante no está matriculado en el curso. La API responde siempre en JSON,
incluidos los errores (`{"error": "...", "campos": {...}}`), y acepta dos formas de autenticación:

```bash
curl -u admin:admin123 http://localhost:8080/api/cursos                       # HTTP Basic
curl -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' \
     http://localhost:8080/api/auth/token                                       # -> JWT
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/cursos      # JWT (plus)
```

**JWT (plus de la lección):** `JwtService` firma el token (HS256) con el usuario y sus roles;
`JwtAuthenticationFilter` lo valida en cada petición a `/api/**` y arma el contexto de seguridad.
Es autenticación *sin estado*: el servidor no guarda sesión. Un token alterado o vencido recibe 401.

**Consumo validado de tres formas, como pide la lección:**
- **Postman:** importar `postman/SpringEduManager.postman_collection.json` (File → Import). 23
  peticiones con aserciones automáticas, incluida la carpeta *JWT* que pide el token y lo reutiliza.
- **RestTemplate (cliente externo):** `ApiRestTemplateTest` y `JwtApiTest` levantan la aplicación
  en un puerto real y la consumen con `org.springframework.web.client.RestTemplate`.
- **API Lab** (`/api-lab`, captura 12): pantalla propia con jQuery + AJAX que pide el token, lo
  muestra y llama a la API con `Authorization: Bearer`, sin instalar nada.

**SpringEduManager como cliente de otros sistemas** (`/integracion`, captura 11): `CampusService`
usa el `RestTemplate` del `@Bean` para consumir (1) el *calendario académico del campus*, un
servicio externo **simulado dentro de la misma aplicación** (`/demo/campus/calendario`) para que la
demostración funcione sin internet, y (2) la propia API con HTTP Basic, como lo haría otro sistema.
El flujo es `RestTemplate → HTTP → JSON → objeto Java` (records `PeriodoAcademico` y `Curso[]`).

---

## Decisiones técnicas que vale la pena explicar

**`SecurityFilterChain` en vez de `WebSecurityConfigurerAdapter`.** Los manuales del módulo
muestran la segunda, pero fue eliminada en Spring Security 6. La forma vigente es declarar un
`@Bean` de tipo `SecurityFilterChain`; las reglas que se escriben adentro son equivalentes.

**Dos cadenas de seguridad separadas, con `@Order`.** La API REST y la aplicación web tienen
necesidades distintas: la web usa formulario de login y la API usa HTTP Basic/JWT sin
redirecciones. Se declaran dos cadenas y se les da orden explícito, porque de lo contrario la
primera que coincida atiende todas las peticiones. El filtro JWT se inserta solo en la cadena de
la API, antes del filtro de Basic: si viene un Bearer válido autentica; si no, Basic sigue igual.

**Usuarios en `application.properties`, pero guardados en la base.** La lección pide configurar
los usuarios en el archivo de propiedades. Usar directamente `spring.security.user.*` habría dado
un solo usuario en memoria y sin roles distintos; en cambio se leen propiedades propias
(`edumanager.usuarios.*`) y se persisten con BCrypt, así la aplicación queda lista para que el
registro de estudiantes cree usuarios reales en módulos futuros.

**JPA y JdbcTemplate conviven.** No es uno u otro: JPA para el CRUD de entidades, JdbcTemplate
para el reporte con agregaciones. Cada herramienta donde rinde más.

**El servicio externo se simula dentro del proyecto.** La demostración de RestTemplate no puede
depender de que haya internet o de que un tercero esté disponible el día de la evaluación. El
endpoint `/demo/campus/calendario` juega el papel del "otro sistema"; cambiar la URL es una línea.

**`@JsonIgnore` en el lado inverso de la relación.** `Curso` conoce a sus `Estudiante` y cada
`Estudiante` conoce sus `Curso`. Al serializar a JSON esto genera una recursión infinita que
devuelve una respuesta truncada e inválida. Marcando el lado inverso con `@JsonIgnore` se corta
el ciclo y el JSON queda bien formado.

**La unicidad se verifica antes de modificar la entidad.** Si un `PUT` cambia el código de un
curso por uno que ya usa otro, y la comprobación se hace después de modificar el objeto, Hibernate
hace *flush* del cambio al ejecutar la consulta y la restricción `UNIQUE` de la base falla con un
500. Por eso `CursoService.actualizar` consulta primero y modifica después, y el cliente recibe un
409 con el motivo.

**`/acceso-denegado` acepta cualquier método.** Spring Security llega ahí por un *forward* que
conserva el método de la petición original; con `@GetMapping`, un POST rechazado por rol o por
CSRF respondía 405 en vez de la página de acceso denegado. Detalle en `DEPURACION.md`.

## Verificación

**Pruebas automáticas — 43, todas en verde con `mvn install` (cobertura JaCoCo 86 %):**

| Clase | Pruebas | Qué demuestra |
|---|---|---|
| `SeguridadWebTest` | 12 | 302 al login sin sesión · 401 en la API · USER 403 en la carga (también por POST directo, reenviado a la página propia) · ADMIN 200 · formulario inválido se queda con el error · login real contra BCrypt |
| `ApiRestTemplateTest` | 8 | CRUD de cursos y estudiantes con `RestTemplate` por HTTP real · 400 con campos · 404 · 409 en POST y PUT · 401 sin credenciales |
| `JwtApiTest` | 5 | Token con credenciales correctas · Bearer válido 200 · clave incorrecta 401 en JSON · token alterado 401 · Basic sigue funcionando |
| `IntegracionYReportesTest` | 4 | Reporte JdbcTemplate cuadra con la carga inicial · RestTemplate consume el servicio simulado y la API propia · `/demo/**` y `/actuator/health` públicos |
| `RegistroYEvaluacionesTest` | 7 | `/registro` público crea el estudiante y valida · USER 403 al registrar notas · ADMIN registra y la regla de matrícula rechaza · API de evaluaciones plana, 201/204/409/400 |
| `CursoServiceTest` | 5 | Carga inicial · código/email repetido rechazado · `@Query` filtra y ordena · matricular no duplica |
| `MotorDeBaseTest` | 2 | Solo los 4 motores con perfil pasan la validación de `DB_ENGINE` |
| `SpringEduManagerApplicationTests` | 1 | El contexto arranca |

**Sobre la aplicación corriendo — `pruebas_flujos.sh`, 64 comprobaciones HTTP, 64 OK en H2 y en MariaDB:**

```
/cursos sin sesión                302  → redirige al login
/api/cursos sin autenticar        401  → con WWW-Authenticate, sin redirect
login admin  → /cursos/nuevo      200  → puede cargar cursos
login estudiante → /cursos/nuevo  403  → bloqueado por rol, página propia
POST /cursos/guardar como USER    403  → misma página (no 405), también con CSRF inválido
POST /api/auth/token              200  → JWT de 3 partes · 401 en JSON con clave mala
GET /api/cursos con Bearer        200  → y 401 con el token alterado
POST /api/cursos                  201  → creado · 400 con campos · 409 código repetido
PUT / DELETE /api/cursos/{id}     200 / 204
/registro                         200  → público; POST crea y redirige a /login?registro
/evaluaciones/nueva               200 ADMIN · 403 USER; nota a no matriculado vuelve al formulario
GET /api/evaluaciones             200  → DTO plano; POST sin matrícula 409
/integracion, /reportes, /api-lab 200  → con sesión; 302 sin sesión
/demo/campus/calendario · /actuator/health   200 públicos
logout                            302  → /login?logout, y /cursos vuelve a pedir login
```

```bash
bash pruebas_flujos.sh                      # contra http://localhost:8080
bash pruebas_flujos.sh http://localhost:8095
```

Las 21 capturas del flujo están en `CAPTURAS/`, con `_GUION_CAPTURAS.md` explicando qué muestra
cada una y a qué lección responde.

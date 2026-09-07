# Depuración y mejora del producto digital — SpringEduManager

> Ejercicio de aplicación #2 · Módulo 7 · Lección 2
> Producto evaluado: **SpringEduManager** (proyecto del Módulo 6)
> Fecha de la revisión: **2026-08-22** · Todo lo que sigue se probó con la aplicación corriendo.

---

## Cómo se probó (para que se pueda repetir)

La aplicación se levantó de verdad y se ejerció por HTTP. No se marcó nada "a ojo".

```bash
mvn clean package
java -jar target/springedumanager-0.0.1-SNAPSHOT.jar --server.port=8095
```

> ⚠️ **Se usó el puerto 8095, no el 8080.** El 8080 lo tenía ocupado el Tomcat del Módulo 5, y
> `curl localhost:8080` respondía **200 igual** — pero era el otro servidor. Un código de estado
> no dice *quién* respondió. Validar contra el proceso equivocado es un falso positivo silencioso.

Las **35 comprobaciones** están en `pruebas_flujos.sh` (incluido en esta carpeta) y cubren:
acceso sin sesión, login correcto e incorrecto, las tres listas, alta de curso, código duplicado,
formulario inválido, matrícula, autorización por rol, la API REST y el cierre de sesión.

**Resultado final: 35 OK / 0 fallas.**

---

## Paso 1 — Revisión general del producto

| Pregunta del enunciado | Respuesta, con lo que se midió |
|---|---|
| ¿Están todas las funcionalidades esperadas? | **Sí.** Las 6 etapas del M6 responden: MVC con Thymeleaf, capa de servicio, JPA, Spring Security con dos roles, API REST y validación de formularios. |
| ¿Cumple los objetivos planteados al inicio? | **Sí.** Gestiona estudiantes, cursos y evaluaciones, y expone la API REST para integrarse con otros sistemas, que era el objetivo declarado en el README. |
| ¿El flujo de uso es claro y coherente? | **Sí, con una excepción que se corrigió** (ver hallazgo 1). En la web el recorrido login → cursos → estudiantes → evaluaciones es lineal y cada acción vuelve a su lista con un mensaje. |

---

## Paso 2 — Depuración técnica

Se probaron todos los flujos, los casos límite y las rutas de error. **Aparecieron dos hallazgos
reales.** Cuatro comprobaciones más fallaron en la primera pasada y resultaron ser errores del
guion de prueba, no del producto; quedan documentados abajo porque distinguirlos fue parte del trabajo.

### 🔴 Hallazgo 1 — La API devolvía 302 hacia una página HTML en vez de 401

**Síntoma.** Un cliente que llamaba a la API sin credenciales recibía esto:

```
HTTP/1.1 302
WWW-Authenticate: Basic realm="Realm"
Location: http://localhost:8095/login;jsessionid=...
```

La cabecera `WWW-Authenticate` decía "autenticate con Basic", pero el código de estado decía
"andá a esta página". Contradictorio. Un programa que consuma la API sigue el redirect y recibe
**el HTML del formulario de login con estado 200**, así que no tiene forma de distinguir "no
estás autenticado" de "acá está tu respuesta". El comentario del propio `SecurityConfig` prometía
*"/api/** → autenticado, **sin formulario**"*: el código no cumplía lo que el comentario declaraba.

**Por qué pasaba (la causa real, no la primera hipótesis).** El primer intento fue declarar un
`authenticationEntryPoint` explícito en la cadena de la API. **No sirvió: siguió devolviendo 302.**
La causa está un nivel más abajo:

1. `BasicAuthenticationEntryPoint` responde con `response.sendError(401)`.
2. `sendError` hace que **Tomcat re-despache la petición internamente hacia `/error`**.
3. Ese re-despacho **vuelve a pasar por el filtro de seguridad**, pero ahora la ruta es `/error`,
   que ya no calza con `/api/**` — así que la atiende la **cadena web**.
4. La cadena web tenía `anyRequest().authenticated()`, no reconoció `/error` como pública, y
   **sobrescribió el 401 con un 302 al login**.

O sea: el 401 sí se generaba, y después algo se lo comía. La cabecera sobrevivía porque ya estaba
escrita; el código de estado no.

**Corrección.** `/error` pasa a ser público en la cadena web, y la cadena de la API declara su
entry point explícito:

```java
.requestMatchers("/login", "/error", "/css/**", "/h2-console/**").permitAll()
```

**Verificado después del cambio:**

| Comprobación | Antes | Ahora |
|---|---|---|
| `GET /api/cursos` sin credenciales | **302** | **401** |
| ¿Redirige a una página HTML? | sí, a `/login` | **no redirige** |
| ¿Trae `WWW-Authenticate`? | sí | sí |
| `GET /api/cursos` con Basic auth | 200 | **200** (sin cambios) |
| Login web por formulario | 302 a `/cursos` | **302 a `/cursos`** (sin cambios) |

Se comprobó además que abrir `/error` sin sesión **no filtra nada**: devuelve un cuerpo genérico
sin rutas, datos ni configuración.

### 🟠 Hallazgo 2 — El 400 de la API no decía qué campo estaba mal

**Síntoma.** Un POST inválido a `/api/cursos` devolvía el cuerpo por defecto de Spring:

```json
{"timestamp":"...","status":400,"error":"Bad Request","path":"/api/cursos"}
```

El código de estado era correcto, pero **inservible para quien integra**: no dice qué campo falló
ni por qué. Con tres campos obligatorios, el que llama tiene que adivinar cuál rechazó el servidor.
Lo llamativo es que los mensajes **ya existían**, escritos en las anotaciones de las entidades
(`"El curso debe tener al menos 1 hora"`), pero no llegaban a la respuesta.

**Corrección.** Se agregó `ApiExceptionHandler` (`@RestControllerAdvice` acotado al paquete `rest`),
que traduce el fallo de validación a los mensajes que ya estaban declarados. No se inventó texto
nuevo: se expuso el que existía.

```json
{"error":"Los datos enviados no son validos",
 "campos":{"nombre":"El nombre del curso es obligatorio",
           "codigo":"El codigo es obligatorio",
           "horas":"El curso debe tener al menos 1 hora"}}
```

**Verificado:** POST válido sigue devolviendo **201**; POST inválido sigue devolviendo **400**,
pero ahora nombra los tres campos. El formulario web no se tocó y sigue comportándose igual.

### ⚪ Cuatro fallas que NO eran del producto

Distinguirlas importa: una prueba mal escrita produce un "error" que suena idéntico a uno real.

| Falla observada | Qué era en realidad |
|---|---|
| "El código duplicado creó un segundo curso" | La prueba contaba apariciones del texto `QA-101` en el **HTML**, y la plantilla lo muestra dos veces por fila. Contado contra la API: **hay 1 solo curso**. El rechazo del duplicado siempre funcionó. |
| "La API no devuelve JSON válido" | La prueba abría `/tmp/api.json` con Python **nativo de Windows**, que resuelve esa ruta a `C:\tmp\` y no la encuentra. La API siempre devolvió JSON. |
| "POST REST válido no crea (400)" | El JSON del `curl` iba con las comillas escapadas dentro de comillas simples, y llegaba malformado. Enviado desde archivo: **201**. |
| "POST REST inválido rechaza (400)" — *pasaba, pero por el motivo equivocado* | Devolvía 400 por **JSON malformado**, no por la validación. Es una prueba que aprobaba sin probar nada. Rehecha con JSON bien formado, ahora sí mide la validación. |

> La cuarta es la más peligrosa de las cuatro: **una prueba en verde que no está midiendo lo que
> dice medir**. Las tres primeras hacían ruido; esa daba tranquilidad falsa.

---

## Paso 3 — Mejora y optimización

| Mejora aplicada | Por qué suma |
|---|---|
| **La API responde 401 en vez de redirigir** (hallazgo 1) | Es lo que hace que la API sea *consumible por un programa* y no sólo por un navegador. Sin esto, la promesa de interoperabilidad del README no se sostenía. |
| **Los errores de validación dicen qué campo falló** (hallazgo 2) | Quien integra deja de adivinar. El costo fue una clase; el beneficio lo recibe todo el que llame a la API. |
| **`/error` declarado explícitamente como público** | Cierra una clase entera de fallas, no sólo esta: cualquier `sendError` futuro (404, 500) habría sufrido el mismo secuestro. |
| **Guion de pruebas reutilizable** (`pruebas_flujos.sh`, 35 comprobaciones) | La revisión deja de ser un recorrido manual que hay que recordar: se vuelve a correr en un comando. |

**Lo que se decidió NO hacer, y por qué.** El `WARN` de arranque sobre `spring.jpa.open-in-view`
es una advertencia de configuración por defecto, no un fallo: la aplicación funciona y desactivarlo
obligaría a revisar cada vista Thymeleaf por si carga una relación perezosa. Cambiarlo al cierre del
proyecto es más riesgo que beneficio. Queda anotado como observación, no como pendiente.

---

## Paso 4 — Feedback externo

⚠️ **Este paso requiere la acción personal de Cristian.** El enunciado pide mostrar el producto a
una persona externa, idealmente no técnica, hacer un test de uso guiado y tomar nota **sin
justificarse**. Eso no lo puedo hacer por él.

Lo que sí queda preparado, en `GUIA_TEST_DE_USO.md`: las cuatro tareas que hay que pedirle a la
persona, qué observar mientras las hace, y la tabla para anotar. La instrucción central de la
consigna es **no explicar y no justificarse**: si la persona se traba, eso *es* el resultado.

---

## Paso 5 — Ajustes finales

| Revisión | Estado |
|---|---|
| Textos, botones y mensajes del sistema | Revisados. Los mensajes de éxito y error son frases completas en español y dicen qué pasó (`"Curso guardado correctamente"`, `"Ya existe un curso con el codigo QA-101"`). |
| Errores visibles o experiencias frustrantes | Ninguno en los flujos web. Los dos que había estaban en la API y se corrigieron. |
| Presentación coherente | Sí: una sola hoja de estilos, misma estructura en las tres listas, y página propia de acceso denegado en vez del error genérico del servidor. |
| Rol sin permisos | Un usuario `USER` que intenta entrar a `/cursos/nuevo` recibe **403 y una página explicativa**, no una pantalla de error crudo. Verificado. |

---

## Paso 6 — Preparación del entregable

**Cambios de esta sesión** (2 archivos):

| Archivo | Cambio |
|---|---|
| `seguridad/SecurityConfig.java` | `/error` público + entry point explícito para la API |
| `rest/ApiExceptionHandler.java` | **nuevo** — errores de validación con el detalle por campo |

**El entregable ya estaba armado y sigue completo:** repositorio Git con historial, `README.md` con
el problema que resuelve, las tecnologías, cómo ejecutar y los usuarios de prueba, y **13 capturas**
en `CAPTURAS/`. Se agrega este documento y el guion de pruebas.

**Cómo se ejecuta** (está en el README del proyecto y se verificó en esta sesión):

```bash
mvn clean package
java -jar target/springedumanager-0.0.1-SNAPSHOT.jar
```

⚠️ **Al empaquetar, `JAVA_HOME` debe apuntar al JDK 21.** El `java` del PATH resuelve al JDK 26 de
Oracle y el resultado queda en bytecode 70, que no arranca donde se lo evalúa. En esta sesión se
verificó antes de compilar: `mvn -v` reporta *runtime 21.0.11 (Temurin)*.

---

## Lo que este ejercicio dejó como aprendizaje

**Un código de estado correcto puede estar generado por el motivo equivocado.** El 400 de la
validación estaba en verde y no probaba la validación: probaba que un JSON roto se rechaza. Y el
401 de la API sí se generaba — pero un re-despacho interno lo pisaba tres pasos después. En los dos
casos, mirar sólo el número final habría dado por bueno algo que no funcionaba.

---

## Revisión final contra el enunciado oficial — 2026-09-03

El PDF *💼 Proyecto Módulo #6 · ABP* se revisó ítem por ítem sobre la aplicación corriendo
(`mvn clean install package` con JDK 21, `pruebas_flujos.sh` 35/35 y 25 pruebas automáticas).
Aparecieron **cuatro hallazgos**, todos corregidos y con prueba que los cubre:

| # | Hallazgo | Causa raíz | Corrección |
|---|---|---|---|
| 3 | **5 de las 13 capturas eran la misma imagen** (la pantalla de login) y otras 2 estaban repetidas. | El script de capturas hacía clic en `button[type=submit]` a secas, y el **primer** botón de esa clase en la página es "Salir" de la cabecera: cerraba la sesión en vez de guardar el curso. Las capturas siguientes mostraban el login. | `herramientas/capturar_flujo.py` apunta al botón **del formulario** y **comprueba la URL** antes de guardar cada imagen: si una pantalla autenticada termina en `/login`, se detiene con el número del paso. 14 capturas nuevas, ninguna repetida (`md5sum`). |
| 4 | El 409 de "código duplicado" que declara `ApiExceptionHandler` **nunca se disparaba**: el POST devolvía 400 con texto plano. | El controlador atrapaba `IllegalArgumentException` antes de que llegara al `@RestControllerAdvice`. El handler era código muerto para ese caso. | Se quitó el `try/catch` de los `@PostMapping` REST. Ahora el duplicado responde **409 en JSON** (`{"error": "Ya existe un curso con el codigo JAVA-01"}`). |
| 5 | **`PUT /api/cursos/2` con el código de otro curso daba 500** (`JdbcSQLIntegrityConstraintViolationException`). | El controlador modificaba la entidad y recién después llamaba a `guardar()`, que verificaba el código libre. Hibernate hace *flush* del cambio pendiente antes de ejecutar esa consulta, y la restricción `UNIQUE` explota antes de que la verificación pueda responder. | La actualización se movió a `CursoService.actualizar` / `EstudianteService.actualizar`: **consultar primero, modificar después**. Responde 409, y el curso queda intacto (probado en `unPutConElCodigoDeOtroCursoDevuelve409YNo500`). |
| 6 | La Lección 4 pide *"configurar usuarios en application.properties"* y estaban fijos en código; la Lección 5 pide *"validar el consumo desde Postman o RestTemplate"* y no había evidencia de ninguno. | — | Usuarios en `edumanager.usuarios.*` de `application.properties`, leídos por `DatosIniciales`. Colección Postman (`postman/`, 12 peticiones con aserciones) y `ApiRestTemplateTest` que consume la API con `RestTemplate` por HTTP real. |

Además, `pruebas_flujos.sh` ahora **sí está en esta carpeta** (antes este documento lo afirmaba y
el archivo vivía fuera del repositorio) y acepta la URL como parámetro.

Lo que dejó esta revisión: **una captura que "se ve bien" no prueba nada si nadie comprobó en qué
URL estaba el navegador**. Cinco imágenes idénticas pasaron una revisión visual porque cada una,
mirada sola, parecía razonable. El `md5sum` las delató en un segundo; la guarda de URL evita que
vuelva a pasar.

### 🔴 Hallazgo 7 (revisor externo, 03-09) — un POST rechazado devolvía 405 en vez de la página de acceso denegado

**Síntoma.** `POST /cursos/guardar` como USER (rol insuficiente) o como ADMIN con token CSRF
inválido respondía `405 Method Not Allowed · Allow: GET`, no el 403 con la página propia. El
revisor lo vio como "intermitente" porque con `curl` el token CSRF se desalinea fácil entre
sesiones; en el navegador le pasa a cualquier USER que envíe el formulario.

**Causa raíz.** `accessDeniedPage("/acceso-denegado")` llega a esa ruta por un **forward** del
servlet, y el forward **conserva el método** de la petición original. El handler era
`@GetMapping`, así que un forward POST no tenía quien lo atendiera: 405. Las pruebas con MockMvc
no lo veían porque MockMvc no ejecuta forwards; solo la aplicación real lo hacía.

**Corrección.** `@RequestMapping("/acceso-denegado")` (cualquier método). Verificado por HTTP real
en `pruebas_flujos.sh` (3 comprobaciones nuevas: USER POST → 403 con la página propia, CSRF
inválido → 403) y en `SeguridadWebTest` (el 403 de USER se reenvía a `/acceso-denegado`, y esa
ruta acepta POST).

---

## Revisión contra el material del profesor — 2026-09-07

El 05-09 el profesor publicó dos laboratorios (Soporte Fácil, GeekVault) con su **matriz de
cumplimiento del módulo**, y el solucionario de SpringEduManager en cuatro niveles (trainee /
junior / middle / senior). Según su propia descripción, el nivel *middle* agrega **JWT,
JdbcTemplate y RestTemplate**, y ambos laboratorios traen perfiles multi-motor, JaCoCo, Actuator y
un "API Lab". Nuestro proyecto cumplía la pauta entera pero no tenía nada de eso. Se agregó, con
la misma estructura que usa el profesor, sin tocar lo ya verificado:

| Agregado | Dónde | Cómo se verificó |
|---|---|---|
| **JWT** (plus de la L5) | `JwtService`, `JwtAuthenticationFilter`, `POST /api/auth/token` | `JwtApiTest` (5) + 6 checks HTTP: token de 3 partes, Bearer 200, alterado 401, clave mala 401 en JSON, Basic sigue |
| **JdbcTemplate** | `repositorio/jdbc/ReporteDao` + `/reportes` | test contra la base + check HTTP + captura 10 |
| **RestTemplate en la app** | `RestClientConfig` (@Bean) + `CampusService` + `/integracion` + servicio externo simulado `/demo/campus/calendario` | test por HTTP real + 4 checks + captura 11 |
| **API Lab** (jQuery + AJAX) | `/api-lab` | captura 12 tomada con el flujo real: token → GET con Bearer → JSON |
| **Perfiles por motor** | `application-{h2,mariadb,mysql,postgresql}.properties`, `DB_ENGINE` | **MariaDB 12.3 real**: 5 tablas creadas, 53/53 checks, hash BCrypt en `usuario` |
| JaCoCo · JavaDoc · Actuator · log en archivo · `.launch` | `pom.xml`, `application.properties` | cobertura 86 % · `/actuator/health` UP |

### Dos trampas que aparecieron al hacerlo

**`local.server.port` no existe cuando se crea el bean.** El primer `CampusService` leía la URL base
con `@Value` en el constructor; en los tests con puerto aleatorio el contexto no arrancaba
(*Could not resolve placeholder 'local.server.port'*). El puerto real recién existe cuando Tomcat
levantó, así que la URL se resuelve **al usarla**, leyendo `Environment` en ese momento.

**El cliente HTTP clásico de Java descarta el cuerpo de un 401.** `POST /api/auth/token` con clave
mala responde 401 con `{"error":"Usuario o contrasena incorrectos"}` (verificado con `curl`), pero
el test lo recibía vacío: `HttpURLConnection` en modo streaming no expone el cuerpo de error. El
test usa `JdkClientHttpRequestFactory` (java.net.http). El servidor estaba bien; el instrumento no.

Y una tercera, vieja conocida: el guion `pruebas_flujos.sh` extraía el JWT con el Python de
Windows sobre una ruta `/tmp` de Git Bash — la misma falla del 22-ago, ahora sin Python (`grep`).

### Y contra el solucionario en 4 niveles (mismo dia, mas tarde)

Con el zip `entrega-final-springedumanager-m6.zip` en mano se cotejo nivel por nivel. Dos cosas
que el profesor implementa en LOS CUATRO niveles y aqui faltaban: el **registro publico de
estudiantes** (`/registro`, porque la situacion inicial dice "que los estudiantes puedan
registrarse") y **evaluaciones con alta** (nosotros solo listabamos). Se agregaron: `/registro`
(publico, crea el perfil; la credencial sigue en el properties, como el mismo lo asume en su
`SUPUESTOS_ASUMIDOS.md`), `/evaluaciones/nueva` solo ADMIN con la regla "solo a matriculados", y
`/api/evaluaciones` con **DTOs** de entrada y salida (el salto junior -> middle de su comparacion).
Verificado: 41 pruebas, 63 comprobaciones HTTP, 21 capturas sin repetidas. Lo que el tiene y aqui
no: prefijo `/api/v1/`, Problem Details, `docker-compose` para las bases, y en *senior* auditoria y
bloqueo optimista. Se dejan fuera a proposito: no los pide la pauta y cambiar el prefijo romperia
la coleccion Postman, el guion y las capturas ya verificadas.

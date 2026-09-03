# Capturas del flujo funcional — SpringEduManager (Módulo 6)

Generadas el 03-09-2026 recorriendo la aplicación **realmente corriendo** (`java -jar`, JDK 21)
con Chrome a 1280x800. Cada imagen se tomó después de comprobar la URL en la que quedó el
navegador, así que ninguna es una pantalla equivocada.

| # | Archivo | Qué muestra | Lección evaluada |
|---|---|---|---|
| 01 | `01_login.png` | Formulario de ingreso | L4 · login |
| 02 | `02_login_incorrecto.png` | Clave incorrecta → "Usuario o contrasena incorrectos" | L4 · autenticación contra la BD |
| 03 | `03_cursos_como_admin.png` | Lista de cursos con el botón "Nuevo curso" y "Eliminar" (solo ADMIN) | L2 · listado · L4 · rol |
| 04 | `04_formulario_curso.png` | Formulario de curso lleno (DEVOPS-01), antes de enviar | L2 · formulario Thymeleaf |
| 05 | `05_curso_creado.png` | DEVOPS-01 ya en la lista + "Curso guardado correctamente" | L3 · persistencia JPA |
| 06 | `06_validacion_servidor.png` | Formulario vacío rechazado por el servidor, mensaje junto a cada campo | L2 · Bean Validation |
| 07 | `07_estudiantes.png` | Lista de estudiantes con cantidad de cursos | L2/L3 |
| 08 | `08_detalle_estudiante.png` | Detalle con cursos matriculados y formulario de matrícula | L3 · relación N:M |
| 09 | `09_evaluaciones.png` | Notas con Aprobada/Reprobada | L3 · relación N:1 |
| 10 | `10_cursos_como_user.png` | Misma lista como USER: **sin** botón "Nuevo curso" ni "Eliminar" | L4 · `sec:authorize` |
| 11 | `11_user_bloqueado_403.png` | USER pide `/cursos/nuevo` por URL → página de acceso denegado (403) | L4 · `@PreAuthorize` + SecurityConfig |
| 12 | `12_sesion_cerrada.png` | Logout → "Sesion cerrada correctamente" | L4 · logout |
| 13 | `13_api_rest_json.png` | `GET /api/cursos` con HTTP Basic → JSON | L5 · REST |
| 14 | `14_api_rest_estudiantes.png` | `GET /api/estudiantes` → JSON con los cursos anidados | L5 · REST |

## Cómo se regeneran

```bash
java -jar target/springedumanager-0.0.1-SNAPSHOT.jar
python herramientas/capturar_flujo.py            # o ...capturar_flujo.py http://localhost:8095
```

Requiere `pip install playwright` y Chrome instalado. Si una pantalla autenticada termina en
`/login`, el script se detiene con el mensaje del paso en vez de guardar una imagen equivocada.

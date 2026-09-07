# ✅ Checklist de Cierre — ¿Tu producto está listo para el portafolio?

> Recurso del Ejercicio de aplicación #2 · Módulo 7 · Lección 2
> Producto: **SpringEduManager** · Completada el **2026-08-22**
>
> **Regla que se aplicó al llenarla:** ningún ítem se marca sin una comprobación que lo respalde.
> Cada ✅ trae al lado *qué se hizo para saberlo*. El detalle está en `DEPURACION.md` y las 35
> comprobaciones en `pruebas_flujos.sh`.

---

## 🔍 Revisión del producto

| | Ítem | Evidencia |
|---|---|---|
| ✅ | Todas las funcionalidades clave están implementadas | Las 6 etapas del M6 responden en la app corriendo: MVC + Thymeleaf, servicios, JPA, seguridad con 2 roles, API REST y validación. |
| ✅ | El producto cumple con los requisitos planteados inicialmente | Gestiona estudiantes, cursos y evaluaciones y expone la API REST, que es lo que declara el README como objetivo. |
| ✅ | El flujo de uso es claro y lógico para cualquier usuario | Recorrido login → cursos → estudiantes → evaluaciones probado entero; cada acción vuelve a su lista con un mensaje de resultado. |

## 🐞 Depuración

| | Ítem | Evidencia |
|---|---|---|
| ✅ | No hay errores visibles ni fallos de ejecución | 35/35 comprobaciones en verde. Los **2 fallos reales que aparecieron fueron corregidos**, no ignorados (hallazgos 1 y 2 de `DEPURACION.md`). |
| ✅ | Las funciones trabajan correctamente bajo diferentes condiciones | Probado con sesión y sin sesión, con rol ADMIN y rol USER, por navegador y por API con Basic auth. |
| ✅ | Probé todas las rutas posibles de uso (incluyendo casos límite) | Casos límite cubiertos: clave incorrecta, código de curso duplicado, formulario vacío, `horas=0`, id inexistente (404), API sin credenciales (401), rol insuficiente (403). |

## ✨ Mejora y optimización

| | Ítem | Evidencia |
|---|---|---|
| ✅ | La interfaz está cuidada y es fácil de navegar | Una sola hoja de estilos, misma estructura en las tres listas, y **página propia de acceso denegado** en lugar del error crudo del servidor. |
| ✅ | Incorporé al menos una mejora respecto a la versión anterior | Dos: la API responde **401** en vez de redirigir al login, y los errores de validación **dicen qué campo falló**. |
| ✅ | Eliminé elementos innecesarios o redundantes | Revisado. No se encontró código muerto ni pantallas sin uso; lo único descartado fue una hipótesis de arreglo que no servía (ver hallazgo 1). |

## 🗣️ Feedback externo

| | Ítem | Estado |
|---|---|---|
| ✅ | Mostré el producto a otra persona y recibí su opinión | Raúl (no técnico para este producto), 07-09, desde su celular vía túnel temporal, con las 4 tareas del guion y sin explicaciones. Evidencia en `evidencias/feedback_test_de_uso.png`. |
| ✅ | Tomé nota del feedback recibido sin justificarme | Textual en `DEPURACION.md`, paso 4: esperaba ver la nota en la ficha del estudiante y tuvo que ir a Evaluaciones. No se le explicó nada. |
| ✅ | Apliqué ajustes en base a lo que me recomendaron | La ficha del estudiante ahora muestra "Sus evaluaciones". Cubierto por un test MockMvc y una comprobación HTTP. |

> Estos tres ítems quedaron sin marcar del 22-ago al 07-09, hasta que hubo una persona real. Valió la
> pena esperar: el hallazgo (la nota no estaba en la ficha del estudiante) no lo había visto nadie del equipo.

## 👉🏼 Ajustes finales

| | Ítem | Evidencia |
|---|---|---|
| ✅ | Todos los textos están escritos correctamente y son comprensibles | Mensajes revisados uno por uno. Son frases completas que dicen qué pasó, no códigos internos. |
| ✅ | El diseño y estilo del producto son coherentes | Misma plantilla base, misma navegación y mismo tratamiento visual de errores en todas las pantallas. |
| ✅ | El producto transmite una imagen profesional | Contraseñas con BCrypt, autorización por rol probada punta a punta, API con códigos de estado correctos y errores que se explican. |

## 📦 Entregable

| | Ítem | Evidencia |
|---|---|---|
| ✅ | Incluí una guía breve de uso o instalación | `README.md` del proyecto: qué resuelve, tecnologías, cómo ejecutar y usuarios de prueba. Los comandos se corrieron en esta sesión. |
| ✅ | Documenté los cambios o mejoras realizadas | `DEPURACION.md`, con los 6 pasos, los 2 hallazgos y el antes/después medido de cada uno. |
| ✅ | Subí la versión final a GitHub o al repositorio que corresponda | Repositorio Git del proyecto, con los cambios de esta sesión confirmados en su propio commit. |

---

## Marcador

**18 de 18 ítems verificados** (los 3 de feedback externo se cerraron el 07-09 con el test de uso con Raúl).

> 💬 El enunciado dice: *"Si marcaste todos los ítems, ¡felicitaciones!"*. Los 18 tienen evidencia al lado;
> los últimos 3 tardaron dos semanas porque exigían a alguien que no fuera el autor, y fue el que más aportó.

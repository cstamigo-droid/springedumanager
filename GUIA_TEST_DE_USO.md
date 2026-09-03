# Guía del test de uso con un usuario externo

> Paso 4 del ejercicio. **Esto lo hace Cristian** — necesita una persona real, idealmente
> **no técnica**. Toma unos 15 minutos.

## Antes de empezar (2 minutos)

```bash
cd JAVA/Proyecto_M6_SpringEduManager
mvn clean package
java -jar target/springedumanager-0.0.1-SNAPSHOT.jar
```

Abrir `http://localhost:8080` y dejar la pantalla de login puesta. Tener a mano el usuario
`admin` / `admin123`.

> Si el 8080 está ocupado por el Tomcat del M5, agregar `--server.port=8095` al final del comando
> y abrir esa dirección.

## La única regla

**No expliques nada y no te justifiques.** Ni antes, ni mientras, ni después. Si la persona se
traba, esa traba *es* el resultado del test. En cuanto uno dice "claro, es que ahí hay que...",
el dato se perdió: ya no estás midiendo el producto, estás compensándolo con tu presencia.

Pedile que **piense en voz alta**: qué está buscando, qué espera que pase al hacer clic.

## Lo que hay que pedirle que haga

Se lee la tarea y se calla. Nada más.

| # | Consigna textual para leerle | Qué mirar mientras la hace |
|---|---|---|
| 1 | *"Entrá al sistema con el usuario admin y la clave admin123."* | ¿Encuentra dónde escribir? ¿Duda con las mayúsculas? |
| 2 | *"Decime cuántos cursos hay cargados y cuántas horas dura el más largo."* | ¿La lista se entiende sin que nadie la explique? ¿Sabe qué columna mirar? |
| 3 | *"Agregá un curso nuevo llamado 'Introducción a Python', de 40 horas."* | ¿Encuentra el botón? ¿Qué hace con el campo **código**, que nadie le dijo qué es? |
| 4 | *"Fijate qué nota sacó el primer estudiante de la lista."* | ¿Va a Evaluaciones o a Estudiantes? ¿Adivina bien dónde vive ese dato? |

**Tarea trampa, si da el tiempo:** *"Intentá agregar un curso sin ponerle nombre."* — Lo que
importa no es que falle, sino si **entiende el mensaje de error** que aparece.

## Dónde anotar

| # | Tarea | ¿La completó? | Dónde dudó o se trabó | Qué dijo (textual) |
|---|---|---|---|---|
| 1 | Iniciar sesión | | | |
| 2 | Leer la lista de cursos | | | |
| 3 | Agregar un curso | | | |
| 4 | Encontrar una nota | | | |
| 5 | Curso sin nombre (error) | | | |

**Tres preguntas al final, en este orden:**

1. ¿Para qué te parece que sirve este sistema?
2. ¿Hubo algún momento en que no supieras qué hacer?
3. Si tuvieras que cambiar una sola cosa, ¿cuál sería?

## Después

Lo que salga de acá va a la sección **🗣️ Feedback externo** de `CHECKLIST_CIERRE.md`, que hoy
tiene sus tres ítems sin marcar. Si aparece un ajuste que valga la pena, se aplica y se anota en
`DEPURACION.md`, en el Paso 5.

> Sospecha razonable de dónde va a trabarse: el campo **código** del formulario de curso. Es
> obligatorio, único, y en ninguna parte dice qué formato espera. Para quien construyó la
> aplicación es evidente; para quien la ve por primera vez, probablemente no. Anotarlo como
> predicción sirve para dos cosas: si se cumple, confirma el diagnóstico; si no se cumple,
> avisa que el problema real estaba en otro lado.

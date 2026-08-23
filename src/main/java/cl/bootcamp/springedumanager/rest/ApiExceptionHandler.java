package cl.bootcamp.springedumanager.rest;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Mejora del cierre (M7 L2, Paso 3).
 *
 * Antes, un POST invalido a /api/** devolvia el cuerpo por defecto de Spring:
 *   {"timestamp":"...","status":400,"error":"Bad Request","path":"/api/cursos"}
 * Correcto como codigo de estado, pero inutil para quien consume la API: no dice
 * QUE campo fallo ni por que. Quien integra tiene que adivinar.
 *
 * Ahora se devuelve el motivo por campo, usando los mismos mensajes que ya
 * estaban declarados en las anotaciones de las entidades. No se inventa texto
 * nuevo: se expone el que ya existia.
 */
@RestControllerAdvice(basePackages = "cl.bootcamp.springedumanager.rest")
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError e : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(e.getField(), e.getDefaultMessage());
        }
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("error", "Los datos enviados no son validos");
        cuerpo.put("campos", campos);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    /** El servicio lanza esto cuando el codigo o el email ya existen. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> conflicto(IllegalArgumentException ex) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(cuerpo);
    }
}

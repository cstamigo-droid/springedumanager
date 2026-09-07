#!/bin/bash
# Uso: bash pruebas_flujos.sh [URL]   (por defecto http://localhost:8080)
B=${1:-http://localhost:8080}
J=$(mktemp); JU=$(mktemp)
DIR=$(mktemp -d)
PASS=0; FAIL=0
chk(){ # nombre  esperado  obtenido
  if [ "$2" == "$3" ]; then echo "  OK   $1  ($3)"; PASS=$((PASS+1));
  else echo "  FALLA $1  esperado=$2 obtenido=$3"; FAIL=$((FAIL+1)); fi; }
csrf(){ grep -o 'name="_csrf" value="[^"]*"' "$1" | head -1 | sed 's/.*value="//;s/"//'; }

echo "--- 1. Acceso sin sesion ---"
chk "GET / sin sesion redirige" 302 "$(curl -s -o /dev/null -w %{http_code} $B/)"
chk "GET /cursos sin sesion redirige" 302 "$(curl -s -o /dev/null -w %{http_code} $B/cursos)"
chk "GET /login es publico" 200 "$(curl -s -o /dev/null -w %{http_code} $B/login)"

echo "--- 2. Login ---"
curl -s -c $J $B/login -o /tmp/lg.html
T=$(csrf /tmp/lg.html)
LOC=$(curl -s -b $J -c $J -o /dev/null -w %{redirect_url} -d "username=admin&password=INCORRECTA&_csrf=$T" $B/login)
chk "login con clave mala rechaza" "$B/login?error" "$LOC"
curl -s -c $J $B/login -o /tmp/lg.html; T=$(csrf /tmp/lg.html)
LOC=$(curl -s -b $J -c $J -o /dev/null -w %{redirect_url} -d "username=admin&password=admin123&_csrf=$T" $B/login)
chk "login admin correcto" "$B/cursos" "$LOC"

echo "--- 3. Navegacion autenticada (ADMIN) ---"
chk "GET /cursos" 200 "$(curl -s -b $J -o /tmp/c.html -w %{http_code} $B/cursos)"
chk "GET /estudiantes" 200 "$(curl -s -b $J -o /tmp/e.html -w %{http_code} $B/estudiantes)"
chk "GET /evaluaciones" 200 "$(curl -s -b $J -o /tmp/v.html -w %{http_code} $B/evaluaciones)"
chk "la lista de cursos trae datos" "SI" "$(grep -qi 'java' /tmp/c.html && echo SI || echo NO)"
chk "la lista de estudiantes trae datos" "SI" "$(grep -qiE '@' /tmp/e.html && echo SI || echo NO)"
chk "las evaluaciones traen notas" "SI" "$(grep -qE '[0-9],[0-9]|[0-9]\.[0-9]' /tmp/v.html && echo SI || echo NO)"

echo "--- 4. Alta de curso (ADMIN) ---"
chk "GET /cursos/nuevo como ADMIN" 200 "$(curl -s -b $J -o /tmp/f.html -w %{http_code} $B/cursos/nuevo)"
T=$(csrf /tmp/f.html)
LOC=$(curl -s -b $J -o /dev/null -w %{redirect_url} -d "nombre=Pruebas+automatizadas&codigo=QA-101&horas=30&descripcion=JUnit+y+Mockito&_csrf=$T" $B/cursos/guardar)
chk "POST curso valido redirige (302)" "$B/cursos" "$LOC"
chk "el curso nuevo aparece en la lista" "SI" "$(curl -s -b $J $B/cursos | grep -q 'QA-101' && echo SI || echo NO)"

echo "--- 5. Casos limite del formulario ---"
curl -s -b $J $B/cursos/nuevo -o /tmp/f.html; T=$(csrf /tmp/f.html)
R=$(curl -s -b $J -o /tmp/dup.html -w %{http_code} -d "nombre=Otro&codigo=QA-101&horas=10&_csrf=$T" $B/cursos/guardar)
chk "codigo duplicado no crea otro curso" "1" "$(curl -s -u admin:admin123 $B/api/cursos | py -3 -c "import json,sys; print(sum(1 for c in json.load(sys.stdin) if c['codigo']=='QA-101'))")"
curl -s -b $J $B/cursos/nuevo -o /tmp/f.html; T=$(csrf /tmp/f.html)
R=$(curl -s -b $J -o /tmp/inv.html -w %{http_code} -d "nombre=&codigo=&horas=0&_csrf=$T" $B/cursos/guardar)
chk "formulario invalido NO redirige (200)" 200 "$R"
chk "formulario invalido muestra el mensaje" "SI" "$(grep -qi 'obligatorio\|al menos 1 hora' /tmp/inv.html && echo SI || echo NO)"

echo "--- 6. Matricula de estudiante ---"
curl -s -b $J $B/estudiantes/1 -o /tmp/d.html; T=$(csrf /tmp/d.html)
CID=$(curl -s -b $J -u admin:admin123 $B/api/cursos | py -3 -c "import json,sys; print(json.load(sys.stdin)[0]['id'])")
curl -s -b $J -o /dev/null -d "cursoId=$CID&_csrf=$T" $B/estudiantes/1/matricular
chk "el estudiante queda matriculado" "SI" "$(curl -s -b $J $B/estudiantes/1 | grep -qi 'curso' && echo SI || echo NO)"

echo "--- 7. Autorizacion por rol (USER) ---"
curl -s -c $JU $B/login -o /tmp/lg.html; T=$(csrf /tmp/lg.html)
LOC=$(curl -s -b $JU -c $JU -o /dev/null -w %{redirect_url} -d "username=estudiante&password=est123&_csrf=$T" $B/login)
chk "login estudiante correcto" "$B/cursos" "$LOC"
chk "USER puede ver /cursos" 200 "$(curl -s -b $JU -o /dev/null -w %{http_code} $B/cursos)"
chk "USER NO puede abrir /cursos/nuevo" 403 "$(curl -s -b $JU -o /tmp/den.html -w %{http_code} $B/cursos/nuevo)"
chk "muestra la pagina propia de acceso denegado" "SI" "$(grep -qi 'acceso\|permiso\|403' /tmp/den.html && echo SI || echo NO)"
chk "USER NO puede eliminar cursos" 403 "$(curl -s -b $JU -o /dev/null -w %{http_code} $B/cursos/eliminar/$CID)"
curl -s -b $JU $B/cursos -o /tmp/cu.html; TU=$(csrf /tmp/cu.html)
chk "USER POST directo a /cursos/guardar -> 403 (no 405)" 403 "$(curl -s -b $JU -o /tmp/den2.html -w %{http_code} -d "nombre=Intruso&codigo=X-1&horas=10&_csrf=$TU" $B/cursos/guardar)"
chk "ese 403 muestra la pagina propia" "SI" "$(grep -qi 'acceso denegado' /tmp/den2.html && echo SI || echo NO)"
chk "POST con token CSRF invalido -> 403 (no 405)" 403 "$(curl -s -b $J -o /dev/null -w %{http_code} -d "nombre=A&codigo=A-1&horas=10&_csrf=basura" $B/cursos/guardar)"

echo "--- 8. API REST ---"
chk "API sin credenciales responde 401 (no 302)" 401 "$(curl -s -o /dev/null -w %{http_code} $B/api/cursos)"
chk "el 401 trae WWW-Authenticate" "SI" "$(curl -s -o /dev/null -D - $B/api/cursos | grep -qi 'WWW-Authenticate' && echo SI || echo NO)"
chk "el 401 NO redirige a una pagina HTML" "" "$(curl -s -o /dev/null -w %{redirect_url} $B/api/cursos)"
chk "API con Basic auth responde" 200 "$(curl -s -u admin:admin123 -o /tmp/api.json -w %{http_code} $B/api/cursos)"
chk "la API devuelve JSON valido" "SI" "$(cat /tmp/api.json | py -3 -c "import json,sys; json.load(sys.stdin); print('SI')" 2>/dev/null || echo NO)"
COD="API-$(date +%s | tail -c 5)"   # codigo unico por corrida: el guion se puede repetir contra la misma instancia
printf '{"nombre":"API REST","codigo":"%s","horas":20}' "$COD" > $DIR/ok.json
printf '{"nombre":"","codigo":"","horas":0}' > $DIR/bad.json
chk "POST REST valido crea (201)" 201 "$(curl -s -u admin:admin123 -o /dev/null -w %{http_code} -H 'Content-Type: application/json' --data-binary @$DIR/ok.json $B/api/cursos)"
chk "POST REST invalido rechaza (400)" 400 "$(curl -s -u admin:admin123 -o $DIR/bad_resp.json -w %{http_code} -H 'Content-Type: application/json' --data-binary @$DIR/bad.json $B/api/cursos)"
chk "el 400 dice QUE campo fallo" "SI" "$(grep -q '"campos"' $DIR/bad_resp.json && echo SI || echo NO)"
chk "el 400 nombra el campo horas" "SI" "$(grep -q 'horas' $DIR/bad_resp.json && echo SI || echo NO)"
chk "GET REST id inexistente (404)" 404 "$(curl -s -u admin:admin123 -o /dev/null -w %{http_code} $B/api/cursos/999999)"

echo "--- 8b. JWT (plus) ---"
printf '{"username":"admin","password":"admin123"}' > $DIR/cred.json
printf '{"username":"admin","password":"otra"}' > $DIR/mal.json
chk "POST /api/auth/token con clave mala -> 401" 401 "$(curl -s -o $DIR/tk_mal.json -w %{http_code} -H 'Content-Type: application/json' --data-binary @$DIR/mal.json $B/api/auth/token)"
chk "ese 401 viene en JSON con motivo" "SI" "$(grep -q 'incorrectos' $DIR/tk_mal.json && echo SI || echo NO)"
chk "POST /api/auth/token correcto -> 200" 200 "$(curl -s -o $DIR/tk.json -w %{http_code} -H 'Content-Type: application/json' --data-binary @$DIR/cred.json $B/api/auth/token)"
JWT=$(grep -o '"token":"[^"]*"' $DIR/tk.json | sed 's/.*:"//;s/"$//')   # sin python: /tmp de Git Bash no existe para el Python de Windows
chk "el token tiene 3 partes (header.payload.firma)" 3 "$(echo -n "$JWT" | awk -F. '{print NF}')"
chk "GET /api/cursos con Bearer -> 200" 200 "$(curl -s -o /dev/null -w %{http_code} -H "Authorization: Bearer $JWT" $B/api/cursos)"
chk "GET /api/cursos con Bearer alterado -> 401" 401 "$(curl -s -o /dev/null -w %{http_code} -H "Authorization: Bearer ${JWT%????}abcd" $B/api/cursos)"

echo "--- 8c. Interoperabilidad, reportes, API Lab, estado ---"
chk "servicio externo simulado /demo/campus/calendario es publico" 200 "$(curl -s -o $DIR/cal.json -w %{http_code} $B/demo/campus/calendario)"
chk "el calendario trae periodos en JSON" "SI" "$(grep -q '"codigo"' $DIR/cal.json && echo SI || echo NO)"
chk "GET /integracion con sesion" 200 "$(curl -s -b $J -o $DIR/int.html -w %{http_code} $B/integracion)"
chk "la pantalla muestra lo consumido por RestTemplate" "SI" "$(grep -q 'JAVA-01' $DIR/int.html && grep -qi 'semestre' $DIR/int.html && echo SI || echo NO)"
chk "GET /reportes con sesion (JdbcTemplate)" 200 "$(curl -s -b $J -o $DIR/rep.html -w %{http_code} $B/reportes)"
chk "el reporte trae promedio y % de aprobacion" "SI" "$(grep -q '%' $DIR/rep.html && grep -q 'JAVA-01' $DIR/rep.html && echo SI || echo NO)"
chk "GET /api-lab con sesion" 200 "$(curl -s -b $J -o /dev/null -w %{http_code} $B/api-lab)"
chk "GET /integracion sin sesion redirige" 302 "$(curl -s -o /dev/null -w %{http_code} $B/integracion)"
chk "/actuator/health responde UP" "SI" "$(curl -s $B/actuator/health | grep -q UP && echo SI || echo NO)"

echo "--- 9. Cierre de sesion ---"
curl -s -b $J $B/cursos -o /tmp/c.html; T=$(csrf /tmp/c.html)
LOC=$(curl -s -b $J -c $J -o /dev/null -w %{redirect_url} -d "_csrf=$T" $B/logout)
chk "logout redirige a /login?logout" "$B/login?logout" "$LOC"
chk "tras el logout /cursos vuelve a pedir login" 302 "$(curl -s -b $J -o /dev/null -w %{http_code} $B/cursos)"

echo ""
echo "RESULTADO: $PASS OK / $FAIL FALLAS"

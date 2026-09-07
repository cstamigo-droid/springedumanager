"""Recorre SpringEduManager en Chrome y guarda las capturas del flujo evaluado en CAPTURAS/.

Uso:  python herramientas/capturar_flujo.py [URL]      (por defecto http://localhost:8080)
Requiere: la aplicacion corriendo y  pip install playwright  (usa el Chrome instalado).

Cada captura se toma DESPUES de comprobar la URL en la que quedo el navegador: si una
pantalla autenticada termino en /login, el script falla en vez de guardar una imagen
equivocada. Paso el 22-ago: 5 de 13 capturas eran la pantalla de login, porque el clic
en 'button[type=submit]' caia en el boton "Salir" de la cabecera y cerraba la sesion.
"""
import sys
from pathlib import Path
from playwright.sync_api import sync_playwright

BASE = sys.argv[1].rstrip("/") if len(sys.argv) > 1 else "http://localhost:8080"
DESTINO = Path(__file__).resolve().parent.parent / "CAPTURAS"
paso = 0


def capturar(page, nombre, espera_url=None, espera=400):
    """Guarda NN_nombre.png; si espera_url viene, exige que la URL actual la contenga."""
    global paso
    paso += 1
    page.wait_for_timeout(espera)
    if espera_url and espera_url not in page.url:
        raise SystemExit(f"[{paso:02d}] {nombre}: esperaba '{espera_url}' y el navegador esta en {page.url}")
    if espera_url is None and "/login" in page.url and "login" not in nombre and "sesion_cerrada" not in nombre:
        raise SystemExit(f"[{paso:02d}] {nombre}: la sesion se perdio, el navegador esta en {page.url}")
    page.screenshot(path=str(DESTINO / f"{paso:02d}_{nombre}.png"))
    print(f"  [{paso:02d}] {nombre:28s} {page.url}")


def entrar(page, usuario, clave):
    page.goto(f"{BASE}/login", wait_until="networkidle")
    page.fill('input[name="username"]', usuario)
    page.fill('input[name="password"]', clave)
    page.click('button[type="submit"]')
    page.wait_for_load_state("networkidle")


def salir(page):
    """El logout es un POST con token CSRF: se envia el formulario de la cabecera."""
    page.goto(f"{BASE}/cursos", wait_until="networkidle")
    page.click('form[action$="/logout"] button')
    page.wait_for_load_state("networkidle")


def main():
    DESTINO.mkdir(parents=True, exist_ok=True)
    for viejo in DESTINO.glob("*.png"):
        viejo.unlink()
    with sync_playwright() as p:
        nav = p.chromium.launch(channel="chrome", headless=True)
        ctx = nav.new_context(viewport={"width": 1280, "height": 800})
        page = ctx.new_page()

        page.goto(f"{BASE}/login", wait_until="networkidle")
        capturar(page, "login", "/login")

        page.fill('input[name="username"]', "admin")
        page.fill('input[name="password"]', "claveMala")
        page.click('button[type="submit"]')
        page.wait_for_load_state("networkidle")
        capturar(page, "login_incorrecto", "/login?error")

        entrar(page, "admin", "admin123")
        capturar(page, "cursos_como_admin", "/cursos")

        page.goto(f"{BASE}/cursos/nuevo", wait_until="networkidle")
        page.fill('input[name="codigo"]', "DEVOPS-01")
        page.fill('input[name="nombre"]', "DevOps y Cloud")
        page.fill('input[name="horas"]', "40")
        page.fill('textarea[name="descripcion"]', "Integracion continua y contenedores")
        capturar(page, "formulario_curso", "/cursos/nuevo")

        # OJO: 'button[type=submit]' a secas es el boton "Salir" de la cabecera (va primero
        # en el DOM) y cerraba la sesion. Hay que apuntar al boton DEL FORMULARIO.
        page.click('form.tarjeta button[type="submit"]')
        page.wait_for_load_state("networkidle")
        capturar(page, "curso_creado", "/cursos")

        page.goto(f"{BASE}/cursos/nuevo", wait_until="networkidle")
        page.click('form.tarjeta button[type="submit"]')   # vacio: lo rechaza el servidor
        page.wait_for_load_state("networkidle")
        capturar(page, "validacion_servidor", "/cursos/guardar")

        page.goto(f"{BASE}/estudiantes", wait_until="networkidle")
        capturar(page, "estudiantes", "/estudiantes")

        page.goto(f"{BASE}/estudiantes/1", wait_until="networkidle")
        capturar(page, "detalle_estudiante", "/estudiantes/1")

        page.goto(f"{BASE}/evaluaciones", wait_until="networkidle")
        capturar(page, "evaluaciones", "/evaluaciones")

        page.goto(f"{BASE}/reportes", wait_until="networkidle")
        capturar(page, "reportes_jdbctemplate", "/reportes")

        page.goto(f"{BASE}/integracion", wait_until="networkidle")
        capturar(page, "integracion_resttemplate", "/integracion")

        # API Lab: pedir el JWT y llamar a la API desde el navegador
        page.goto(f"{BASE}/api-lab", wait_until="networkidle")
        page.click("#btnToken")
        page.wait_for_selector("#btnCursos:not([disabled])", timeout=8000)
        page.click("#btnCursos")
        page.wait_for_function("document.querySelector('#salida').textContent.includes('JAVA-01')", timeout=8000)
        capturar(page, "api_lab_jwt", "/api-lab")
        ctx.close()

        # Rol USER: mismo recorrido, pero bloqueado en la carga
        ctx2 = nav.new_context(viewport={"width": 1280, "height": 800})
        page2 = ctx2.new_page()
        entrar(page2, "estudiante", "est123")
        capturar(page2, "cursos_como_user", "/cursos")

        page2.goto(f"{BASE}/cursos/nuevo", wait_until="networkidle")
        capturar(page2, "user_bloqueado_403", "/cursos/nuevo")

        salir(page2)
        capturar(page2, "sesion_cerrada", "/login?logout")
        ctx2.close()

        # API REST vista desde un cliente con HTTP Basic
        ctx3 = nav.new_context(viewport={"width": 1280, "height": 800},
                               http_credentials={"username": "admin", "password": "admin123"})
        page3 = ctx3.new_page()
        page3.goto(f"{BASE}/api/cursos", wait_until="networkidle")
        capturar(page3, "api_rest_json", "/api/cursos")
        page3.goto(f"{BASE}/api/estudiantes", wait_until="networkidle")
        capturar(page3, "api_rest_estudiantes", "/api/estudiantes")
        ctx3.close()
        nav.close()
    print(f"\n{paso} capturas en {DESTINO}")


if __name__ == "__main__":
    main()

# Servidor (`servidor/` o `server/`)

El servidor expone endpoints REST (temperatura por ciudad, pronóstico) y responde con JSON.

## Archivos que deberían ir en esta carpeta

- **Application.java** o **Main.java** — Punto de entrada del servidor (Spring Boot, Javalin, etc.).
- **ClimaController.java** o **ClimaResource.java** — Rutas: `GET /clima?ciudad=...`, `GET /pronostico/{ciudad}`.
- **ServicioClima.java** — Lógica: obtener datos (mock, archivo o API externa) y devolver DTOs.
- **ClimaDTO.java** — Objeto con temperatura, ciudad, fecha, etc., para serializar a JSON.
- **pom.xml** o **build.gradle** — Dependencias (p. ej. Spring Web o Javalin, Jackson). Puede estar en la raíz del módulo servidor.

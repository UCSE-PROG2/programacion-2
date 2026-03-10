# Ejemplo: API REST de clima (Cliente-Servidor)

Este ejemplo aplica el **patrón cliente-servidor**: el servidor expone servicios (API de clima) y el cliente los consume (app o navegador).

## Papel de cada parte

- **Servidor**: expone endpoints REST (p. ej. temperatura por ciudad, pronóstico). Responde con JSON.
- **Cliente**: hace peticiones HTTP (GET/POST), parsea la respuesta y muestra datos al usuario.

## Archivos que deberían ir en esta carpeta

### Servidor (`servidor/` o `server/`)
- `Application.java` o `Main.java` — Punto de entrada del servidor (Spring Boot, Javalin, etc.).
- `ClimaController.java` o `ClimaResource.java` — Rutas: `GET /clima?ciudad=...`, `GET /pronostico/{ciudad}`.
- `ServicioClima.java` — Lógica: obtener datos (mock, archivo o API externa) y devolver DTOs.
- `ClimaDTO.java` — Objeto con temperatura, ciudad, fecha, etc., para serializar a JSON.
- `pom.xml` o `build.gradle` — Dependencias (p. ej. Spring Web o Javalin, Jackson).

### Cliente (`cliente/` o `client/`)
- `ClienteClima.java` — Usa `HttpClient` (Java 11+) o similar para llamar a `GET /clima?ciudad=...`.
- `Main.java` o clase con `main` — Ejecuta el cliente, imprime resultado o muestra en consola.
- Opcional: `ClimaResponse.java` — POJO para deserializar el JSON de la respuesta.

### Configuración
- `config.properties` o constantes con `URL_BASE` del servidor (ej. `http://localhost:8080`) para que el cliente sepa a dónde conectarse.

## Cómo comprobar que está bien aplicado

- El servidor puede ejecutarse solo y responder a `curl` o Postman.
- El cliente no contiene lógica de negocio del clima; solo consume la API.
- Si cambias el puerto o la URL del servidor, solo se toca la configuración del cliente, no su lógica.

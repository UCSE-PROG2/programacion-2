# Cliente (`cliente/` o `client/`)

El cliente hace peticiones HTTP (GET/POST), parsea la respuesta y muestra datos al usuario.

## Archivos que deberían ir en esta carpeta

- **ClienteClima.java** — Usa `HttpClient` (Java 11+) o similar para llamar a `GET /clima?ciudad=...`.
- **Main.java** o clase con `main` — Ejecuta el cliente, imprime resultado o muestra en consola.
- Opcional: **ClimaResponse.java** — POJO para deserializar el JSON de la respuesta.

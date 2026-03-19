# Servicio de Autenticación (`servicio-auth/`)

Punto de entrada, controladores, lógica y persistencia de usuarios/tokens. Cada servicio tiene su propio despliegue y dependencias.

## Archivos que deberían ir en esta carpeta

- **AuthApplication.java** — Punto de entrada (Spring Boot u otro).
- **AuthController.java** — `POST /login`, `POST /register`, `GET /usuarios/me` (con token).
- **AuthService.java**, **UsuarioRepository.java** — Lógica y persistencia de usuarios/tokens.
- **pom.xml** o **build.gradle** — Dependencias solo de este servicio.

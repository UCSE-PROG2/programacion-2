# Servicio de Autenticación (`servicio-autenticacion/`)

Expone operaciones de login, logout y validación de token.

## Archivos que deberían ir en esta carpeta

- **AutenticacionResource.java** o **AutenticacionEndpoint.java** — Rutas: `POST /auth/login` (usuario/contraseña), `POST /auth/logout`, `GET /auth/validar?token=...`.
- **AutenticacionService.java** — Lógica: validar credenciales, generar y validar token (JWT o sesión).
- **UsuarioDTO.java**, **TokenDTO.java** — Objetos para request/response.

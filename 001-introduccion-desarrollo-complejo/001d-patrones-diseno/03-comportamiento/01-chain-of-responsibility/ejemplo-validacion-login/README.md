# Ejemplo: Validación de login (Chain of Responsibility)

**Chain of Responsibility** pasa una solicitud por una cadena de manejadores; cada uno decide si la procesa o la pasa al siguiente. Aquí: validación de login (usuario no vacío, formato email, contraseña segura, usuario existe en BD); cada validador es un eslabón.

## Archivos que deberían ir en esta carpeta

### Handler (eslabón)
- `ValidadorLogin.java` — Clase abstracta (o interfaz) con: atributo `ValidadorLogin siguiente`, setter `setSiguiente(ValidadorLogin)`. Método `validar(String usuario, String password)` que, si este manejador no puede validar o falla, llama a `siguiente.validar(usuario, password)`; si no hay siguiente, termina (éxito o error según diseño).

### Handlers concretos
- `ValidadorNoVacio.java` — Extiende `ValidadorLogin`; comprueba que usuario y password no estén vacíos; si falla, lanza o devuelve error; si pasa, delega en `siguiente`.
- `ValidadorFormatoEmail.java` — Comprueba que usuario tenga formato email.
- `ValidadorContrasenaSegura.java` — Comprueba longitud y caracteres.
- `ValidadorExisteEnBD.java` — Último eslabón; comprueba contra base de datos (o mock).

### Cliente
- `ServicioLogin.java` o `Main.java` — Construye la cadena: `v1.setSiguiente(v2); v2.setSiguiente(v3); ...` y llama a `v1.validar(usuario, password)`. No conoce el orden ni la cantidad de validadores; solo el primero.

## Cómo comprobar que está bien aplicado

- Añadir o quitar un validador es crear una clase y enlazarla en la cadena; el cliente no cambia.
- Cada manejador tiene una única responsabilidad; la solicitud fluye en un solo sentido.

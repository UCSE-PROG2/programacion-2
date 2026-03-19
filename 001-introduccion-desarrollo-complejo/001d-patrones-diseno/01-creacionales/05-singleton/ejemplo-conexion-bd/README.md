# Ejemplo: Conexión a base de datos (Singleton)

**Singleton** asegura que una clase tenga una única instancia y un punto de acceso global. Aquí: un gestor de conexión a BD del que toda la aplicación usa la misma instancia (pool o conexión única).

## Archivos que deberían ir en esta carpeta

### Singleton
- `ConexionBD.java` — Atributo estático privado: `instancia` (tipo `ConexionBD`). Constructor privado (para que nadie haga `new ConexionBD()`). Método estático público `getInstancia()` que, si `instancia` es null, la crea y la asigna, y luego la devuelve. Métodos de uso: `getConnection()` (o `ejecutarConsulta(String)`) que usen esa única conexión. Opcional: sincronización en `getInstancia()` si hay hilos.

### Cliente
- `UsuarioRepository.java` o `Main.java` — Obtiene la conexión mediante `ConexionBD.getInstancia().getConnection()` (o equivalente); nunca instancia `ConexionBD` con `new`.

## Cómo comprobar que está bien aplicado

- No existe forma pública de crear una segunda instancia; el constructor es privado.
- Todas las llamadas a `getInstancia()` devuelven el mismo objeto (se puede comprobar con `==` en tests).

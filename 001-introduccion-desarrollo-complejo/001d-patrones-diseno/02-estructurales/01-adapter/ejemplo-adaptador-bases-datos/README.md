# Ejemplo: Adaptador entre dos tipos de bases de datos (Adapter)

**Adapter** permite colaborar a objetos con interfaces incompatibles. Aquí: tu código espera una interfaz común de “conexión a BD”; una BD es SQL y otra NoSQL; el adaptador adapta la NoSQL a la misma interfaz que usa el cliente (como en el ejercicio de la PPT).

## Archivos que deberían ir en esta carpeta

### Interfaz esperada por el cliente (target)
- `ConexionBD.java` — Interfaz con métodos: `Resultado ejecutarConsulta(String query)`, `void conectar()`, `void desconectar()` (o equivalente). Es lo que usan los repositorios de la aplicación.

### Implementación existente (adaptee) incompatible
- `ConexionMySQL.java` — Ya existe; tiene `executeQuery(String sql)` y métodos con nombres distintos. No implementa `ConexionBD`.
- `ConexionMongoDB.java` — Otra implementación; API distinta (ej. `find(String collection, String filter)`). No implementa `ConexionBD`.

### Adaptadores
- `AdaptadorMySQL.java` — Implementa `ConexionBD`; internamente tiene un `ConexionMySQL` y delega: `ejecutarConsulta(String)` llama a `executeQuery(String)` del MySQL y adapta el resultado a `Resultado`.
- `AdaptadorMongoDB.java` — Implementa `ConexionBD`; envuelve `ConexionMongoDB` y traduce `ejecutarConsulta(String)` a llamadas MongoDB y devuelve un `Resultado` compatible.

### Cliente
- `RepositorioUsuarios.java` — Recibe `ConexionBD` por constructor; usa solo `ejecutarConsulta(...)`. Puede trabajar con cualquiera de las dos BDs gracias a los adaptadores.

## Cómo comprobar que está bien aplicado

- El cliente no conoce MySQL ni MongoDB; solo la interfaz `ConexionBD`.
- Añadir otra BD implica crear un nuevo adaptador que implemente `ConexionBD`; el cliente no cambia.

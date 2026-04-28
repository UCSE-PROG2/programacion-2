# Unidad 3 — Spring Boot


## 1. ¿Qué es Spring Boot?

Spring Boot es un framework de Java que permite crear aplicaciones web (APIs REST, servicios, etc.) con muy poca configuración.

Antes de Spring Boot, configurar una aplicación Spring requería decenas de archivos XML. Spring Boot elimina esa fricción con el principio **convention over configuration**: si no configurás nada, usa valores por defecto razonables.

### ¿Qué resuelve?

| Problema | Solución Spring Boot |
|----------|---------------------|
| Configurar un servidor web | Incluye Tomcat embebido |
| Conectar una base de datos | `application.properties` con pocas líneas |
| Definir dependencias compatibles | Starters (grupos de dependencias testeadas juntas) |
| Arrancar la app | Una sola clase `main` |

### Spring Boot vs Spring Framework

Spring Framework es el ecosistema completo. Spring Boot es una capa encima que lo hace usable sin configuración manual. En la práctica, en Programación 2 siempre usamos Spring Boot.

---

## 2. ¿Qué es una API REST?

Antes de arrancar con Spring Boot, necesitamos entender qué vamos a construir.

### El problema

Una aplicación web tiene dos partes: el **frontend** (lo que ve el usuario: HTML, botones, formularios) y el **backend** (la lógica y los datos). Estas dos partes necesitan comunicarse.

La pregunta es: ¿cómo se comunican? Necesitan un contrato: qué puede pedir el frontend, cómo lo pide, y qué le devuelve el backend.

Ese contrato es una **API** (Application Programming Interface).

![Diagrama API cliente-servidor](https://53.fs1.hubspotusercontent-na1.net/hub/53/hubfs/Google%20Drive%20Integration/api%20diagram.png)
*El cliente (app, navegador) hace un pedido a la API; la API responde con los datos. Fuente: [HubSpot](https://blog.hubspot.com/website/api-diagram)*

![Arquitectura REST](assets/que-es-rest-api.png)

### HTTP como base

La comunicación se hace sobre HTTP, el mismo protocolo que usan los navegadores. Cada request tiene una URL, un método y opcionalmente un cuerpo con datos; el servidor responde con un código de estado y los datos pedidos.

![Ciclo request-response HTTP](https://toolsqa.com/gallery/Client%20Server/HTTP-Request.png)
*Ciclo completo: el cliente envía una HTTP Request, el servidor procesa y devuelve una HTTP Response. Fuente: [ToolsQA](https://toolsqa.com/client-server/client-server-architecture-and-model)*

Cada interacción tiene:

- Una **URL** que identifica el recurso: `/productos`, `/usuarios/5`
- Un **método** que indica la acción:

| Método | Acción |
|--------|--------|
| GET | Leer / consultar |
| POST | Crear |
| PUT | Actualizar (reemplazar) |
| DELETE | Eliminar |

- Un **código de respuesta** que indica el resultado (200 OK, 404 Not Found, etc.)
- Un **cuerpo** opcional con datos en formato JSON

### JSON

JSON es el formato que se usa para intercambiar datos. Es texto plano, legible, y todos los lenguajes lo entienden:

```json
{
  "id": 1,
  "nombre": "Laptop",
  "precio": 1500.0
}
```

Una lista de productos sería un array:

```json
[
  { "id": 1, "nombre": "Laptop", "precio": 1500.0 },
  { "id": 2, "nombre": "Mouse", "precio": 25.0 }
]
```

### REST

REST no es una tecnología, es un **estilo de diseño** para APIs. Las reglas principales son:

- Cada recurso tiene su propia URL (`/productos`, `/categorias`)
- El método HTTP indica la operación (no poner `/crearProducto` en la URL)
- El servidor no guarda estado del cliente entre requests (cada request es independiente)
- Las respuestas usan JSON

### Ejemplo completo

Así se vería una API de productos bien diseñada:

| Request | Respuesta |
|---------|-----------|
| `GET /productos` | Lista de todos los productos en JSON |
| `GET /productos/5` | El producto con ID 5 |
| `POST /productos` + JSON en el cuerpo | Crea el producto, devuelve el creado con su ID |
| `PUT /productos/5` + JSON en el cuerpo | Reemplaza los datos del producto 5 |
| `DELETE /productos/5` | Elimina el producto 5 |

Esta es exactamente la API que vamos a construir con Spring Boot.

---

## 3. Crear un proyecto

La forma más simple es usar [Spring Initializr](https://start.spring.io):

- **Project**: Gradle - Groovy (o Maven, cualquiera sirve)
- **Language**: Java
- **Spring Boot**: última versión estable
- **Dependencies**: las siguientes

| Dependencia | Para qué sirve |
|-------------|----------------|
| **Spring Web** | Exponer endpoints REST (controllers, HTTP) |
| **Spring Data JPA** | Acceso a base de datos con repositorios y entidades |
| **H2 Database** | Base de datos en memoria para desarrollo y tests |
| **Lombok** | Elimina código repetitivo: getters, setters, constructores |

Esto genera un ZIP con la estructura base lista para abrir en IntelliJ.

### Lombok

Lombok es una librería que genera automáticamente el código boilerplate de las clases Java. En lugar de escribir getters, setters y constructores a mano, se usan anotaciones:

```java
// Sin Lombok — mucho código repetitivo
public class Producto {
    private Long id;
    private String nombre;
    private Double precio;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
}

// Con Lombok — limpio y equivalente
@Getter
@Setter
public class Producto {
    private Long id;
    private String nombre;
    private Double precio;
}
```

Las anotaciones más usadas:

| Anotación | Qué genera |
|-----------|-----------|
| `@Getter` | Getters para todos los campos |
| `@Setter` | Setters para todos los campos |
| `@NoArgsConstructor` | Constructor sin argumentos |
| `@AllArgsConstructor` | Constructor con todos los argumentos |
| `@RequiredArgsConstructor` | Constructor con los campos `final` |
| `@Data` | `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode` |
| `@Builder` | Patrón builder para construir objetos |

> **Importante**: IntelliJ requiere el plugin de Lombok instalado para que el código compile correctamente. Ir a `Settings → Plugins → Lombok` e instalarlo.

### Estructura generada

```
src/
  main/
    java/com/ejemplo/
      EjemploApplication.java   ← punto de entrada
    resources/
      application.properties    ← configuración
  test/
    java/com/ejemplo/
      EjemploApplicationTests.java
build.gradle
```

### Clase principal

```java
@SpringBootApplication
public class EjemploApplication {
    public static void main(String[] args) {
        SpringApplication.run(EjemploApplication.class, args);
    }
}
```

`@SpringBootApplication` es un atajo que combina tres anotaciones:
- `@Configuration` — esta clase define beans
- `@EnableAutoConfiguration` — activa la configuración automática
- `@ComponentScan` — escanea el paquete en busca de componentes

---

## 3. Arquitectura en capas

Spring Boot promueve separar la aplicación en tres capas:

```
Cliente (Postman / navegador)
        ↓  HTTP
   Controller         ← recibe la request, devuelve la response
        ↓
    Service           ← lógica de negocio
        ↓
   Repository         ← acceso a la base de datos
        ↓
   Base de datos
```

Cada capa tiene una responsabilidad única. El Controller no habla directo con la base de datos; el Repository no hace lógica de negocio. Esta separación hace el código más fácil de testear y mantener.

---

## 4. Controller — Exponiendo una API REST

Un Controller recibe requests HTTP y devuelve responses.

```java
@RestController
@RequestMapping("/productos")
public class ProductoController {

    @GetMapping
    public List<String> listar() {
        return List.of("Laptop", "Mouse", "Teclado");
    }

    @GetMapping("/{id}")
    public String buscar(@PathVariable Long id) {
        return "Producto " + id;
    }

    @PostMapping
    public String crear(@RequestBody String nombre) {
        return "Creado: " + nombre;
    }
}
```

### Anotaciones clave

| Anotación | Qué hace |
|-----------|----------|
| `@RestController` | Marca la clase como controller REST (combina `@Controller` + `@ResponseBody`) |
| `@RequestMapping("/ruta")` | Ruta base para todos los métodos del controller |
| `@GetMapping` | Mapea un método HTTP GET |
| `@PostMapping` | Mapea un método HTTP POST |
| `@PutMapping` | Mapea un método HTTP PUT |
| `@DeleteMapping` | Mapea un método HTTP DELETE |
| `@PathVariable` | Lee un valor de la URL (`/productos/5` → `id = 5`) |
| `@RequestBody` | Lee el cuerpo JSON de la request |
| `@RequestParam` | Lee un query parameter (`/productos?nombre=Laptop`) |

### Códigos de respuesta HTTP

| Código | Significado | Cuándo usarlo |
|--------|-------------|---------------|
| 200 OK | Éxito | GET exitoso |
| 201 Created | Recurso creado | POST exitoso |
| 204 No Content | Éxito sin cuerpo | DELETE exitoso |
| 400 Bad Request | Request inválida | Datos incorrectos |
| 404 Not Found | No encontrado | Recurso inexistente |
| 500 Internal Server Error | Error del servidor | Excepción no manejada |

Para devolver códigos específicos:

```java
@PostMapping
public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
    Producto guardado = service.guardar(producto);
    return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
}
```

---

## 5. Entidades y JPA

JPA (Java Persistence API) es el estándar de Java para mapear clases a tablas de base de datos. Spring Boot usa Hibernate como implementación.

```java
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Double precio;

    // getters y setters
}
```

### Anotaciones de entidad

| Anotación | Qué hace |
|-----------|----------|
| `@Entity` | Marca la clase como tabla en la base de datos |
| `@Table(name = "...")` | Nombre de la tabla (opcional, por defecto usa el nombre de la clase) |
| `@Id` | Define la clave primaria |
| `@GeneratedValue` | Autogenera el ID (autoincremental) |
| `@Column(name = "...")` | Nombre de columna personalizado |
| `@Column(nullable = false)` | Columna NOT NULL |

---

## 6. Repository — Acceso a datos

Spring Data JPA provee repositorios que generan las queries automáticamente:

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByNombre(String nombre);

    List<Producto> findByPrecioLessThan(Double precio);
}
```

Solo con declarar la interfaz y extender `JpaRepository`, Spring genera:
- `findAll()` — trae todos
- `findById(id)` — busca por ID
- `save(entidad)` — inserta o actualiza
- `deleteById(id)` — elimina por ID
- `count()` — cuenta registros
- Y cualquier método que declaremos siguiendo la convención de nombres

### Convención de nombres

| Método | SQL equivalente |
|--------|----------------|
| `findByNombre(String n)` | `WHERE nombre = ?` |
| `findByNombreAndPrecio(...)` | `WHERE nombre = ? AND precio = ?` |
| `findByPrecioLessThan(Double p)` | `WHERE precio < ?` |
| `findByNombreContaining(String s)` | `WHERE nombre LIKE %?%` |
| `findByActivoTrue()` | `WHERE activo = true` |

---

## 7. Service — Lógica de negocio

El Service es donde va la lógica que no es ni HTTP ni base de datos:

```java
@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<Producto> listarTodos() {
        return repository.findAll();
    }

    public Producto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public Producto guardar(Producto producto) {
        return repository.save(producto);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
```

`@Service` le dice a Spring que esta clase es un componente de lógica de negocio. Spring la instancia automáticamente y la inyecta donde sea necesario.

### Inyección de dependencias

#### El concepto general

Cuando una clase necesita usar otra clase para funcionar, se dice que tiene una **dependencia**. La forma obvia de resolverla es crearla adentro:

```java
public class ProductoService {
    private ProductoRepository repository = new ProductoRepository(); // dependencia creada adentro
}
```

Esto funciona, pero tiene un problema: `ProductoService` está **acoplado** a `ProductoRepository`. Si queremos cambiar la implementación, o testear `ProductoService` de forma aislada, tenemos que modificar la clase.

La **Inyección de Dependencias (DI)** invierte esa responsabilidad: en lugar de que la clase cree sus dependencias, alguien externo se las entrega.

```java
// La dependencia llega desde afuera — el que construye el objeto decide qué inyectar
public class ProductoService {
    private ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }
}
```

Ahora `ProductoService` no sabe ni le importa cómo se crea el `ProductoRepository`. Solo lo usa. Esto hace el código más flexible y testeable.

#### En Spring Boot

Spring implementa DI a través de su **contenedor de IoC** (Inversion of Control). El contenedor es el componente de Spring que:

1. Escanea el proyecto buscando clases anotadas con `@Component`, `@Service`, `@Repository`, `@Controller`
2. Las instancia automáticamente (las llama **beans**)
3. Detecta qué dependencias necesita cada bean y las inyecta

Esto significa que nunca escribimos `new MiServicio()` — Spring lo hace por nosotros.

```java
// MAL — instanciar a mano rompe el contenedor de Spring
ProductoRepository repo = new ProductoRepository();

// BIEN — Spring inyecta la instancia via @Autowired en el campo
@Autowired
private ProductoRepository repository;

// MEJOR — inyección por constructor (forma recomendada)
public ProductoService(ProductoRepository repository) {
    this.repository = repository;
}
```

La inyección por constructor es preferida porque las dependencias son explícitas, el campo puede ser `final`, y los tests pueden pasar un mock sin necesidad de Spring.

Con **Lombok** se simplifica aún más usando `@RequiredArgsConstructor`, que genera el constructor de los campos `final` automáticamente:

```java
@Service
@RequiredArgsConstructor  // genera el constructor con ProductoRepository
public class ProductoService {

    private final ProductoRepository repository; // Spring inyecta esto

    public List<Producto> listarTodos() {
        return repository.findAll();
    }
}
```

---

## 8. CRUD completo — Ejemplo integrado

### Entidad

```java
@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Double precio;
    // getters y setters
}
```

### Repository

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> {}
```

### Service

```java
@Service
public class ProductoService {
    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<Producto> listarTodos() { return repository.findAll(); }

    public Producto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("No encontrado: " + id));
    }

    public Producto guardar(Producto producto) { return repository.save(producto); }

    public Producto actualizar(Long id, Producto datos) {
        Producto existente = buscarPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setPrecio(datos.getPrecio());
        return repository.save(existente);
    }

    public void eliminar(Long id) { repository.deleteById(id); }
}
```

### Controller

```java
@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Producto> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public Producto buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(producto));
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return service.actualizar(id, producto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 9. Configuración — application.properties

```properties
# Puerto del servidor (default: 8080)
server.port=8080

# Base de datos H2 en memoria (para desarrollo)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Consola H2 (http://localhost:8080/h2-console)
spring.h2.console.enabled=true
```

### Valores de `ddl-auto`

| Valor | Comportamiento |
|-------|----------------|
| `none` | No hace nada con el esquema |
| `validate` | Valida que las tablas existan |
| `update` | Agrega columnas nuevas, no borra |
| `create` | Crea tablas al iniciar (borra si existen) |
| `create-drop` | Crea al iniciar, borra al cerrar |

En desarrollo: `create-drop`. En producción: `none` o `validate`.

---

## 10. Validaciones

Agregar la dependencia `spring-boot-starter-validation` permite validar los datos de entrada:

```java
@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Positive(message = "El precio debe ser positivo")
    private Double precio;
}
```

En el controller, activar la validación con `@Valid`:

```java
@PostMapping
public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(producto));
}
```

### Anotaciones de validación comunes

| Anotación | Qué valida |
|-----------|-----------|
| `@NotNull` | No puede ser null |
| `@NotBlank` | No puede ser null ni string vacío |
| `@Size(min, max)` | Longitud de un string |
| `@Min(n)` / `@Max(n)` | Valor numérico mínimo/máximo |
| `@Positive` | Número mayor a 0 |
| `@Email` | Formato de email válido |

---

## 11. Manejo de errores

Por defecto, si lanza una excepción, Spring devuelve un JSON de error genérico con código 500. Para controlarlo mejor:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errores.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errores);
    }
}
```

`@RestControllerAdvice` intercepta excepciones de todos los controllers y permite responder con el código HTTP y cuerpo adecuados.

---

## 12. Testear la API

### Con H2 Console

Acceder a `http://localhost:8080/h2-console` con:
- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: (vacío)

### Con curl

```bash
# Listar todos
curl http://localhost:8080/productos

# Crear
curl -X POST http://localhost:8080/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Laptop","precio":1500.0}'

# Buscar por ID
curl http://localhost:8080/productos/1

# Actualizar
curl -X PUT http://localhost:8080/productos/1 \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Laptop Pro","precio":2000.0}'

# Eliminar
curl -X DELETE http://localhost:8080/productos/1
```

### Con Postman

Importar las mismas requests como colección. Es más cómodo para explorar la API durante la práctica.

---

## 13. Ejercicio integrador

Desarrollar una API REST para una **distribuidora de productos**:

### Requerimientos

1. Entidad `Producto` con: `id`, `nombre`, `precio`, `stock`, `categoria`
2. Entidad `Categoria` con: `id`, `nombre`
3. Un producto pertenece a una categoría (`@ManyToOne`)
4. CRUD completo de productos
5. Endpoint para listar productos por categoría
6. Endpoint para listar productos con stock menor a N
7. Validar que nombre no sea vacío y precio sea positivo
8. Devolver 404 con mensaje cuando un producto no existe

### Relación entre entidades

```java
@Entity
public class Producto {
    // ...
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}

@Entity
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
}
```

### Endpoints esperados

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/productos` | Listar todos |
| GET | `/productos/{id}` | Buscar por ID |
| GET | `/productos?categoria=1` | Filtrar por categoría |
| GET | `/productos/bajo-stock?limite=10` | Stock menor a N |
| POST | `/productos` | Crear |
| PUT | `/productos/{id}` | Actualizar |
| DELETE | `/productos/{id}` | Eliminar |
| GET | `/categorias` | Listar categorías |
| POST | `/categorias` | Crear categoría |

---

## Resumen de anotaciones

| Anotación | Capa | Qué hace |
|-----------|------|----------|
| `@SpringBootApplication` | Main | Punto de entrada |
| `@RestController` | Controller | Expone endpoints REST |
| `@RequestMapping` | Controller | Ruta base |
| `@GetMapping` / `@PostMapping` / ... | Controller | Mapea método HTTP |
| `@PathVariable` | Controller | Lee de la URL |
| `@RequestBody` | Controller | Lee el JSON de la request |
| `@RequestParam` | Controller | Lee query param |
| `@Service` | Service | Lógica de negocio |
| `@Autowired` | Cualquiera | Inyección de dependencia |
| `@Entity` | Modelo | Mapea clase a tabla |
| `@Id` / `@GeneratedValue` | Modelo | Clave primaria |
| `@ManyToOne` / `@OneToMany` | Modelo | Relaciones entre tablas |
| `@Valid` | Controller | Activa validaciones |
| `@RestControllerAdvice` | Handler | Manejo global de errores |

---

## Recursos

- [Spring Initializr](https://start.spring.io) — generar proyectos
- [Spring Guides](https://spring.io/guides) — tutoriales oficiales
- [Spring Data JPA Docs](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/) — métodos de repositorio
- [H2 Database](https://www.h2database.com) — base de datos en memoria para desarrollo

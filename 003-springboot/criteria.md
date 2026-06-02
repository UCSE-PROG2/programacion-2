# Consultas dinámicas con Criteria Builder en Spring Boot

Material de apoyo para la **Unidad 3** de **Programación 2** - Ingeniería en Computación (UCSE).

---

## Índice

1. [El problema: filtros opcionales](#1-el-problema-filtros-opcionales)
2. [¿Qué es Criteria Builder?](#2-qué-es-criteria-builder)
3. [Ingredientes necesarios](#3-ingredientes-necesarios)
4. [Construir una consulta paso a paso](#4-construir-una-consulta-paso-a-paso)
5. [Filtros opcionales con predicados](#5-filtros-opcionales-con-predicados)
6. [Integrar Criteria Builder en Spring Boot](#6-integrar-criteria-builder-en-spring-boot)
7. [Ejemplo completo: API de productos con filtros](#7-ejemplo-completo-api-de-productos-con-filtros)
8. [Criteria Builder vs otras alternativas](#8-criteria-builder-vs-otras-alternativas)
9. [Resumen de métodos útiles](#9-resumen-de-métodos-útiles)

---

## 1. El problema: filtros opcionales

Cuando diseñamos un endpoint con filtros opcionales, como:

```
GET /productos?nombre=laptop&precioMax=2000&categoria=electronica
```

…queremos que cada parámetro sea opcional: si el usuario no lo envía, no se aplica ese filtro. Si no envía ninguno, se devuelven todos los productos.

La primera idea que surge es usar los métodos derivados de `JpaRepository`:

```java
List<Producto> findByNombreAndPrecioLessThanAndCategoria(String nombre, Double precio, String categoria);
```

Esto funciona si los tres filtros son siempre obligatorios. Pero si alguno puede ser `null`, el método devuelve resultados incorrectos porque `WHERE nombre = null` no es lo mismo que omitir esa condición.

Algunas personas intentan solucionar esto con muchos métodos:

```java
List<Producto> findByNombre(String nombre);
List<Producto> findByPrecioLessThan(Double precio);
List<Producto> findByNombreAndPrecioLessThan(String nombre, Double precio);
// ... y así por cada combinación posible
```

Con tres filtros opcionales hay **8 combinaciones posibles**. Con cuatro son **16**. Esto no escala.

**Criteria Builder** resuelve exactamente este problema: permite construir la consulta dinámicamente en tiempo de ejecución, agregando solo las condiciones que aplican.

---

## 2. ¿Qué es Criteria Builder?

Criteria Builder es parte de la especificación **JPA** (Java Persistence API). Permite construir consultas a la base de datos **en código Java**, en lugar de escribirlas como Strings en JPQL o SQL.

En lugar de escribir esto:

```sql
SELECT * FROM producto WHERE precio < 2000 AND categoria = 'electronica'
```

Lo construimos así:

```java
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<Producto> query = cb.createQuery(Producto.class);
Root<Producto> root = query.from(Producto.class);

query.select(root).where(
    cb.and(
        cb.lessThan(root.get("precio"), 2000.0),
        cb.equal(root.get("categoria"), "electronica")
    )
);
```

La ventaja principal es que podemos agregar o no cada condición dependiendo de si el parámetro fue enviado, todo con código Java común (if, listas, etc.).

---

## 3. Ingredientes necesarios

Para construir una consulta con Criteria Builder necesitamos tres objetos:

| Objeto | Qué es | Para qué sirve |
|--------|--------|----------------|
| `EntityManager` | Administrador de la sesión JPA | Punto de entrada para crear consultas |
| `CriteriaBuilder` | Fábrica de condiciones y expresiones | Crear comparaciones: `equal`, `lessThan`, `like`, etc. |
| `CriteriaQuery<T>` | La consulta en construcción | Define qué tipo de objeto devuelve la consulta |
| `Root<T>` | La tabla principal de la consulta | Acceder a los campos de la entidad |

### Cómo se conecta el EntityManager con la base de datos

El `EntityManager` no se configura manualmente: Spring Boot lo crea automáticamente a partir de las propiedades de conexión definidas en `application.properties`.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mibase
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

Con esas propiedades, Spring Boot:
1. Crea un `DataSource` (la conexión a MySQL)
2. Crea un `EntityManagerFactory` a partir del `DataSource`
3. A partir de la factory, produce instancias de `EntityManager` para cada operación

El desarrollador no toca ninguno de esos pasos. Solo declara `@PersistenceContext` y Spring inyecta el `EntityManager` listo para usar, ya conectado a la base de datos configurada.

```
application.properties
        ↓
   DataSource (conexión a MySQL)
        ↓
   EntityManagerFactory
        ↓
   EntityManager  ←  @PersistenceContext lo inyecta aquí
```

### Cómo obtenerlos en código

```java
// Spring inyecta el EntityManager ya conectado a la base de datos
@PersistenceContext
private EntityManager entityManager;

// Los demás se obtienen a partir de él
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<Producto> query = cb.createQuery(Producto.class);
Root<Producto> root = query.from(Producto.class);
```

### La analogía

Pensarlo como armar una oración:
- `CriteriaBuilder` es el vocabulario (las palabras que podemos usar)
- `CriteriaQuery` es la oración completa
- `Root` es el sujeto de la oración (la tabla que estamos consultando)
- Los `Predicate` son las condiciones del `WHERE`

---

## 4. Construir una consulta paso a paso

### Paso 1 — Obtener el CriteriaBuilder

```java
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
```

### Paso 2 — Crear la CriteriaQuery con el tipo de retorno

```java
CriteriaQuery<Producto> query = cb.createQuery(Producto.class);
```

### Paso 3 — Definir la tabla de origen (FROM)

```java
Root<Producto> root = query.from(Producto.class);
```

### Paso 4 — Definir las condiciones (WHERE)

```java
Predicate condicion = cb.equal(root.get("nombre"), "Laptop");
query.select(root).where(condicion);
```

### Paso 5 — Ejecutar la consulta

```java
List<Producto> resultados = entityManager.createQuery(query).getResultList();
```

### Consulta completa

```java
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<Producto> query = cb.createQuery(Producto.class);
Root<Producto> root = query.from(Producto.class);

Predicate condicion = cb.equal(root.get("nombre"), "Laptop");
query.select(root).where(condicion);

List<Producto> resultados = entityManager.createQuery(query).getResultList();
```

Esto equivale a: `SELECT * FROM producto WHERE nombre = 'Laptop'`

---

## 5. Filtros opcionales con predicados

La clave está en acumular condiciones solo cuando el parámetro tiene valor. Se usa una `List<Predicate>` que se va llenando:

```java
List<Predicate> predicados = new ArrayList<>();

if (nombre != null && !nombre.isBlank()) {
    predicados.add(cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
}

if (precioMax != null) {
    predicados.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
}

if (categoria != null && !categoria.isBlank()) {
    predicados.add(cb.equal(root.get("categoria"), categoria));
}

// Combinar todos los predicados con AND
query.select(root).where(cb.and(predicados.toArray(new Predicate[0])));
```

Si `predicados` está vacío, `cb.and()` sin argumentos devuelve una condición siempre verdadera: se devuelven todos los registros. Exactamente lo que queremos.

### Predicados disponibles más comunes

| Método | SQL equivalente | Ejemplo de uso |
|--------|----------------|----------------|
| `cb.equal(campo, valor)` | `campo = valor` | Filtrar por categoría exacta |
| `cb.like(campo, patron)` | `campo LIKE '%..%'` | Buscar por nombre parcial |
| `cb.lessThan(campo, valor)` | `campo < valor` | Precio menor a X |
| `cb.lessThanOrEqualTo(campo, valor)` | `campo <= valor` | Precio máximo |
| `cb.greaterThan(campo, valor)` | `campo > valor` | Stock mayor a X |
| `cb.greaterThanOrEqualTo(campo, valor)` | `campo >= valor` | Stock mínimo |
| `cb.isTrue(campo)` | `campo = true` | Solo activos |
| `cb.isFalse(campo)` | `campo = false` | Solo inactivos |
| `cb.isNull(campo)` | `campo IS NULL` | Sin categoría asignada |
| `cb.isNotNull(campo)` | `campo IS NOT NULL` | Con categoría asignada |
| `cb.and(p1, p2)` | `p1 AND p2` | Combinar condiciones |
| `cb.or(p1, p2)` | `p1 OR p2` | Condición alternativa |

---

## 6. Integrar Criteria Builder en Spring Boot

La forma de integrar Criteria Builder en Spring Boot es crear un repositorio personalizado. El repositorio estándar (`JpaRepository`) sigue haciendo el CRUD; el repositorio personalizado se encarga de la consulta dinámica.

Spring Data JPA permite extender los repositorios con implementaciones propias. Requiere tres archivos, pero la separación es clara: la interfaz declara el contrato, la implementación construye la consulta, y el repositorio principal reúne todo.

**1. Definir la interfaz con el método a implementar:**

```java
public interface ProductoRepositoryCustom {
    List<Producto> buscarConFiltros(String nombre, Double precioMax, String categoria);
}
```

**2. Implementar la interfaz:**

```java
@Repository
public class ProductoRepositoryImpl implements ProductoRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Producto> buscarConFiltros(String nombre, Double precioMax, String categoria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Producto> query = cb.createQuery(Producto.class);
        Root<Producto> root = query.from(Producto.class);

        List<Predicate> predicados = new ArrayList<>();

        if (nombre != null && !nombre.isBlank()) {
            predicados.add(cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
        }

        if (precioMax != null) {
            predicados.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
        }

        if (categoria != null && !categoria.isBlank()) {
            predicados.add(cb.equal(root.get("categoria"), categoria));
        }

        query.select(root).where(cb.and(predicados.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }
}
```

**3. Hacer que el repositorio principal extienda ambas interfaces:**

```java
public interface ProductoRepository extends JpaRepository<Producto, Long>, ProductoRepositoryCustom {
    // hereda tanto los métodos de JpaRepository como los de ProductoRepositoryCustom
}
```

**4. Usar desde el service normalmente:**

```java
List<Producto> resultados = repository.buscarConFiltros(nombre, precioMax, categoria);
```

Spring detecta automáticamente que `ProductoRepositoryImpl` implementa `ProductoRepositoryCustom` y lo vincula al repositorio. La convención de nombre `NombreRepositoryImpl` es importante: Spring la usa para encontrar la implementación.

---

## 7. Ejemplo completo: API de productos con filtros

### Entidad

```java
@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String categoria;
    private Double precio;
    private Integer stock;
}
```

### Repositorio (interfaz custom + JpaRepository)

```java
// ProductoRepositoryCustom.java
public interface ProductoRepositoryCustom {
    List<Producto> buscarConFiltros(String nombre, Double precioMax, String categoria, Integer stockMinimo);
}

// ProductoRepository.java
public interface ProductoRepository extends JpaRepository<Producto, Long>, ProductoRepositoryCustom {
}

// ProductoRepositoryImpl.java
@Repository
public class ProductoRepositoryImpl implements ProductoRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Producto> buscarConFiltros(String nombre, Double precioMax, String categoria, Integer stockMinimo) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Producto> query = cb.createQuery(Producto.class);
        Root<Producto> root = query.from(Producto.class);

        List<Predicate> predicados = new ArrayList<>();

        if (nombre != null && !nombre.isBlank()) {
            predicados.add(cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
        }

        if (precioMax != null) {
            predicados.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
        }

        if (categoria != null && !categoria.isBlank()) {
            predicados.add(cb.equal(root.get("categoria"), categoria));
        }

        if (stockMinimo != null) {
            predicados.add(cb.greaterThanOrEqualTo(root.get("stock"), stockMinimo));
        }

        query.select(root).where(cb.and(predicados.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }
}
```

### Service

```java
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository repository;

    public List<Producto> buscar(String nombre, Double precioMax, String categoria, Integer stockMinimo) {
        return repository.buscarConFiltros(nombre, precioMax, categoria, stockMinimo);
    }

    public Producto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
    }

    public Producto guardar(Producto producto) {
        return repository.save(producto);
    }

    public void eliminar(Long id) {
        buscarPorId(id); // valida que exista antes de eliminar
        repository.deleteById(id);
    }
}
```

### Controller

```java
@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    @GetMapping
    public List<Producto> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Integer stockMinimo) {
        return service.buscar(nombre, precioMax, categoria, stockMinimo);
    }

    @GetMapping("/{id}")
    public Producto buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Requests de ejemplo

```
GET /productos                                         → todos
GET /productos?nombre=lap                              → contiene "lap" en el nombre
GET /productos?precioMax=1500                          → precio <= 1500
GET /productos?categoria=electronica                   → solo electrónica
GET /productos?precioMax=2000&categoria=electronica    → precio <= 2000 Y categoría electrónica
GET /productos?nombre=lap&precioMax=1500&stockMinimo=5 → los tres filtros combinados
```

---

## 8. Criteria Builder vs otras alternativas

| Alternativa | Cuándo usarla | Limitación |
|-------------|--------------|------------|
| Métodos derivados (`findByNombre`) | Filtros fijos y obligatorios | No soporta parámetros opcionales |
| `@Query` con JPQL | Consultas fijas y complejas | El `WHERE` es estático; no puede cambiar en runtime |
| Criteria Builder | Filtros opcionales o dinámicos | Más verboso que JPQL, pero completamente flexible |
| JPA Specifications | Alternativa a Criteria Builder, más componible | Requiere extender `JpaSpecificationExecutor`; curva de aprendizaje similar |

En los TPs de la cursada, Criteria Builder es la herramienta correcta cuando el enunciado dice "los parámetros son opcionales" o "si no se informa, no se aplica ese filtro".

---

## 9. Resumen de métodos útiles

### Predicados de comparación

```java
cb.equal(root.get("campo"), valor)
cb.notEqual(root.get("campo"), valor)
cb.like(root.get("campo"), "%patron%")
cb.lessThan(root.get("campo"), valor)
cb.lessThanOrEqualTo(root.get("campo"), valor)
cb.greaterThan(root.get("campo"), valor)
cb.greaterThanOrEqualTo(root.get("campo"), valor)
cb.between(root.get("campo"), min, max)
cb.isNull(root.get("campo"))
cb.isNotNull(root.get("campo"))
cb.isTrue(root.get("campo"))
cb.isFalse(root.get("campo"))
```

### Combinar predicados

```java
cb.and(predicado1, predicado2)         // ambos deben cumplirse
cb.or(predicado1, predicado2)          // al menos uno debe cumplirse
cb.not(predicado)                       // negar una condición

// Combinar una lista dinámica
cb.and(lista.toArray(new Predicate[0]))
```

### Funciones sobre strings

```java
cb.lower(root.get("nombre"))           // convertir a minúsculas (para búsquedas case-insensitive)
cb.upper(root.get("nombre"))           // convertir a mayúsculas
cb.length(root.get("nombre"))          // largo del string
cb.trim(root.get("nombre"))            // quitar espacios
cb.concat(expr1, expr2)                // concatenar
```

### Ordenar resultados

```java
query.orderBy(cb.asc(root.get("nombre")));     // ascendente
query.orderBy(cb.desc(root.get("precio")));    // descendente
```

### Acceder a campos de una entidad relacionada (JOIN)

Cuando la entidad tiene un `@ManyToOne` y necesitamos filtrar por un campo de la entidad relacionada:

```java
// Entidad: Producto tiene @ManyToOne → Categoria
Join<Producto, Categoria> categoriaJoin = root.join("categoria");
predicados.add(cb.equal(categoriaJoin.get("nombre"), nombreCategoria));
```

Esto equivale a: `JOIN categoria ON ... WHERE categoria.nombre = ?`

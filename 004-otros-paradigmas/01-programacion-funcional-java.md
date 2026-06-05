# Programación Funcional con Java

Material de apoyo para **Programación 2** — Ingeniería en Computación (UCSE).

---

## Índice

1. [¿Qué es la programación funcional?](#1-qué-es-la-programación-funcional)
2. [Declarativo vs Imperativo](#2-declarativo-vs-imperativo)
3. [Expresiones Lambda](#3-expresiones-lambda)
4. [Tipos genéricos](#4-tipos-genéricos)
5. [Interfaces del JDK: Predicate, Function, Consumer, Supplier](#5-interfaces-del-jdk)
6. [Method References](#6-method-references)
7. [Optional — evitar null](#7-optional--evitar-null)
8. [Stream API](#8-stream-api)
9. [Collectors](#9-collectors)
10. [Comparación imperativa vs funcional](#10-comparación-imperativa-vs-funcional)
11. [Ejercicios](#11-ejercicios)

---

## 1. ¿Qué es la programación funcional?

La **programación funcional** es un paradigma donde los programas se construyen componiendo **funciones puras**. Nació en la teoría matemática del **cálculo lambda** (Alonzo Church, 1930s) y hoy está presente en lenguajes como Haskell, Scala, y como extensión de Java desde la versión 8 (2014).

La idea central: en lugar de describir *cómo* hacer algo paso a paso, describimos *qué* queremos obtener.

```
Imperativo:   "itera la lista, si el elemento es par, agrégalo a otra lista"
Funcional:    "dame los elementos pares de esta lista"
```

### Pilares de la programación funcional

| Concepto | Definición | Ejemplo |
|----------|-----------|---------|
| **Función pura** | Mismo resultado para mismos argumentos, sin efectos secundarios | `int sumar(int a, int b) { return a + b; }` |
| **Inmutabilidad** | Los datos no se modifican; se crean nuevos valores | `List.of(...)` es inmutable |
| **Transparencia referencial** | Una expresión puede reemplazarse por su valor sin cambiar el comportamiento | `sumar(2, 3)` siempre es `5` |
| **Funciones de orden superior** | Funciones que reciben otras funciones como argumentos | `lista.stream().filter(...)` |
| **Composición** | Funciones simples que se combinan para formar comportamientos complejos | `f.andThen(g)` |

### Función pura vs impura

```java
// ✅ Función PURA — predecible, sin efectos secundarios
int sumar(int a, int b) {
    return a + b;
}

// ❌ Función IMPURA — modifica estado externo
int total = 0;
void sumarAlTotal(int valor) {
    total += valor; // efecto secundario
}
```

---

## 2. Declarativo vs Imperativo

| | Imperativo | Funcional (Declarativo) |
|--|-----------|------------------------|
| **Pregunta** | ¿*Cómo* lo hago? | ¿*Qué* quiero obtener? |
| **Foco** | Instrucciones y estado | Transformaciones |
| **Variables** | Mutables | Inmutables |
| **Iteración** | `for`, `while` | `map`, `filter`, recursión |
| **Código** | Más detallado | Más conciso |

**Ejemplo concreto**: filtrar nombres que empiezan con 'A' y pasarlos a mayúsculas.

```java
List<String> nombres = List.of("Ana", "Bruno", "Alicia", "Carlos", "Alberto");

// Estilo IMPERATIVO — el programador controla el loop
List<String> resultado = new ArrayList<>();
for (String nombre : nombres) {
    if (nombre.startsWith("A")) {
        resultado.add(nombre.toUpperCase());
    }
}

// Estilo FUNCIONAL — el programador describe qué quiere
List<String> resultado = nombres.stream()
    .filter(nombre -> nombre.startsWith("A"))
    .map(String::toUpperCase)
    .collect(Collectors.toList());
// → ["ANA", "ALICIA", "ALBERTO"]
```

El código funcional es más corto y comunica la intención directamente: *filtrar* → *transformar* → *colectar*.

---

## 3. Expresiones Lambda

Las **expresiones lambda** son el corazón de la programación funcional en Java. Permiten tratar bloques de código como valores: pasarlos como argumentos, asignarlos a variables, retornarlos desde funciones.

### Sintaxis

```
(parámetros) -> cuerpo
```

| Forma | Ejemplo | Cuándo usarla |
|-------|---------|---------------|
| Sin parámetros | `() -> "Hola"` | Cuando no se recibe ningún argumento |
| Un parámetro | `x -> x * 2` | Paréntesis opcionales con un solo param |
| Múltiples parámetros | `(a, b) -> a + b` | Separados por coma |
| Cuerpo con bloque | `(a, b) -> { int c = a + b; return c; }` | Cuando el cuerpo tiene más de una expresión |
| Tipos explícitos | `(int a, int b) -> a + b` | Cuando el compilador no puede inferirlos |

### Lambda vs clase anónima (el antes y el después)

```java
// ❌ Antes de Java 8: clase anónima — verbose
List<String> lista = Arrays.asList("Carlos", "Ana", "Bruno");
Collections.sort(lista, new Comparator<String>() {
    @Override
    public int compare(String s1, String s2) {
        return s1.compareTo(s2);
    }
});

// ✅ Con lambda — limpio y expresivo
lista.sort((s1, s2) -> s1.compareTo(s2));

// ✅✅ Aún más conciso con method reference
lista.sort(String::compareTo);
```

### Ejemplos de uso

```java
// Lambda asignada a una variable
Runnable saludar = () -> System.out.println("¡Hola, mundo!");
saludar.run(); // ¡Hola, mundo!

// Lambda para ordenar
List<Integer> numeros = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6);
numeros.sort((a, b) -> a - b); // orden ascendente

// Lambda con lógica
List<String> nombres = List.of("Ana", "Bruno", "Carlos");
nombres.forEach(nombre -> {
    String saludo = "Hola, " + nombre + "!";
    System.out.println(saludo);
});
```

### Captura de variables

Una lambda puede capturar variables del contexto externo, pero deben ser **efectivamente finales** (no se reasignan después de su declaración):

```java
String prefijo = "Sr.";  // ✅ efectivamente final
nombres.forEach(nombre -> System.out.println(prefijo + " " + nombre));

// ❌ Esto NO compila — prefijo se reasigna
String prefijo = "Sr.";
prefijo = "Sra.";  // reasignación
nombres.forEach(nombre -> System.out.println(prefijo + " " + nombre)); // ERROR
```

> **¿Por qué?** Las lambdas se pueden ejecutar en un contexto diferente al de su declaración (por ejemplo, en otro hilo). Si la variable pudiera cambiar, el comportamiento sería impredecible.

---

## 4. Tipos genéricos

Antes de ver `Predicate<T>`, `Function<T, R>` y el resto, es necesario entender qué significa la `T` que aparece entre los `<>`.

### El problema que resuelven

Sin genéricos, si queremos una caja que pueda guardar cualquier cosa, usamos `Object`:

```java
// Sin genéricos — inseguro
Object caja = "Hola";
String valor = (String) caja; // cast manual, puede explotar en runtime
```

Si alguien mete un `Integer` en esa caja y después intentamos hacer el cast a `String`, el programa compila pero falla en ejecución. Los **tipos genéricos** llevan ese error al momento de compilación, donde es fácil de corregir.

### Sintaxis básica

Un **parámetro de tipo** es un nombre de marcador de posición que se reemplaza por un tipo concreto cuando se usa la clase o método:

```java
// T es el parámetro de tipo — puede llamarse como quiera, por convención:
// T = Type, E = Element, K = Key, V = Value, R = Return type

class Caja<T> {
    private T contenido;

    public void guardar(T valor) { this.contenido = valor; }
    public T obtener()           { return contenido; }
}

// Al instanciar, T se reemplaza por el tipo concreto
Caja<String>  cajaCadena = new Caja<>();
Caja<Integer> cajaNúmero = new Caja<>();

cajaCadena.guardar("Hola");
String s = cajaCadena.obtener(); // no necesita cast — el compilador sabe que es String

cajaNúmero.guardar(42);
Integer n = cajaNúmero.obtener(); // ídem
```

### Múltiples parámetros de tipo

Una clase o interfaz puede tener más de uno. `Function<T, R>` es el ejemplo clásico: recibe algo de tipo `T` y devuelve algo de tipo `R`:

```java
// Con dos parámetros de tipo
interface Conversor<T, R> {
    R convertir(T entrada);
}

// T = String, R = Integer
Conversor<String, Integer> longitud = s -> s.length();
longitud.convertir("Hola"); // 4

// T = Integer, R = String
Conversor<Integer, String> etiqueta = n -> "Número: " + n;
etiqueta.convertir(42); // "Número: 42"
```

### Cómo se lee `Predicate<T>`

Con esto en mente, `Predicate<T>` se lee así: *"una interfaz que trabaja con elementos de tipo T, donde T es cualquier tipo que yo elija al usarla"*.

```java
Predicate<String>  esMayorDeCinco = s -> s.length() > 5;  // T = String
Predicate<Integer> esPar          = n -> n % 2 == 0;       // T = Integer
Predicate<Persona> esMayor        = p -> p.getEdad() >= 18; // T = Persona
```

El compilador garantiza que dentro de la lambda, el parámetro es del tipo que declaramos — sin casteos, sin errores en runtime.

### Genéricos en las APIs que vamos a usar

Dos casos concretos que van a aparecer cuando construyamos APIs REST con Spring Boot:

---

#### `Optional<T>` — buscar por identificador sin devolver null

Cuando buscamos un recurso por ID (un producto, un usuario, etc.), ese recurso puede existir o no. La forma ingenua es devolver `null` si no se encuentra, pero eso obliga al caller a recordar siempre checkear `null` — y si se olvida, el programa explota con un `NullPointerException`.

`Optional<T>` es un genérico que envuelve el resultado: comunica explícitamente que puede haber o no un valor, y el compilador obliga a manejarlo.

```java
// ❌ Devolver null — peligroso y ambiguo
public Producto buscarPorId(Long id) {
    return repository.findById(id); // ¿null significa "no existe" o "error"?
}

// ✅ Devolver Optional<Producto> — explícito y seguro
public Optional<Producto> buscarPorId(Long id) {
    return repository.findById(id); // Spring Data ya devuelve Optional
}
```

El que llama a este método no puede ignorar el caso vacío — tiene que decidir qué hacer:

```java
// En el Service o Controller
Optional<Producto> resultado = repository.findById(id);

// Opción A: lanzar excepción si no existe (el más común en APIs REST)
Producto producto = resultado
    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));

// Opción B: devolver un valor por defecto
Producto producto = resultado.orElse(new Producto("Sin nombre", 0.0));

// Opción C: transformar solo si existe, sin tocar el Optional
Optional<String> nombre = resultado.map(Producto::getNombre);
```

`Optional<T>` es genérico: el `T` puede ser `Producto`, `Usuario`, `Pedido` — cualquier entidad. Spring Data JPA ya devuelve `Optional<T>` en `findById`, por lo que en la práctica solo hay que saber cómo consumirlo.

---

#### `Page<T>` y `Pageable` — paginar listas para no traer todo

Cuando una tabla tiene miles de registros, hacer `findAll()` y devolver la lista completa es un problema: consume mucha memoria, la respuesta es lenta y el frontend no puede mostrar miles de ítems de golpe. La solución es la **paginación**: traer solo una "página" de resultados por vez.

Spring Data JPA resuelve esto con dos genéricos:

- **`Pageable`** — describe *qué página* queremos: número de página, tamaño y criterio de ordenamiento.
- **`Page<T>`** — contiene los resultados de esa página más metadatos: total de elementos, total de páginas, si hay página siguiente, etc.

```java
// Repository — solo cambiar la firma, Spring genera el SQL automáticamente
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Sin paginación — trae todo
    List<Producto> findAll();

    // Con paginación — trae una página
    Page<Producto> findAll(Pageable pageable);

    // Paginación con filtro
    Page<Producto> findByCategoria(String categoria, Pageable pageable);
}
```

```java
// Service — construir el Pageable y llamar al repository
public Page<Producto> listar(int pagina, int tamaño) {
    Pageable pageable = PageRequest.of(pagina, tamaño, Sort.by("nombre").ascending());
    return repository.findAll(pageable);
}
```

```java
// Controller — recibir los parámetros de la request
@GetMapping("/productos")
public Page<Producto> listar(
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "10") int tamaño) {
    return service.listar(pagina, tamaño);
}
```

La respuesta JSON que devuelve `Page<T>` incluye automáticamente:

```json
{
  "content": [ { "id": 1, "nombre": "Laptop" }, ... ],
  "totalElements": 247,
  "totalPages": 25,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false
}
```

El cliente usa `totalPages` y `number` para construir los controles de paginación ("página 1 de 25", botones anterior/siguiente).

---

#### Resumen: los tres genéricos clave

| Tipo | Propósito | Aparece en |
|------|-----------|-----------|
| `Optional<T>` | Resultado que puede no existir | `repository.findById(id)` |
| `Page<T>` | Una página de resultados con metadatos | Respuesta del endpoint de listado |
| `Pageable` | Descripción de qué página traer | Parámetro del repository y el service |

---

## 5. Interfaces del JDK

Java incluye en el paquete `java.util.function` un conjunto de interfaces funcionales genéricas listas para usar. Las cuatro principales:

```
┌─────────────────────────────────────────────────────────────┐
│                   java.util.function                        │
│                                                             │
│  Predicate<T>   →  T → boolean   (filtrar, validar)        │
│  Function<T,R>  →  T → R         (transformar)             │
│  Consumer<T>    →  T → void      (imprimir, guardar)        │
│  Supplier<T>    →  () → T        (crear, generar)           │
└─────────────────────────────────────────────────────────────┘
```

### 5.1 `Predicate<T>` — condición booleana

Recibe un `T` y devuelve `boolean`. Se usa para **filtrar** o **validar**.

```java
Predicate<String> esVacio      = String::isEmpty;
Predicate<Integer> esMayorDe10 = n -> n > 10;
Predicate<String> esLargo      = s -> s.length() > 5;

// Evaluación
System.out.println(esVacio.test(""));        // true
System.out.println(esMayorDe10.test(15));    // true
System.out.println(esLargo.test("Java"));    // false

// Combinación lógica con and, or, negate
Predicate<String> noEsVacio    = esVacio.negate();
Predicate<String> esLargoYlleno = esLargo.and(noEsVacio);

Predicate<Integer> esPequenoONegativo = esMayorDe10.negate()
                                                   .or(n -> n < 0);
```

### 5.2 `Function<T, R>` — transformación

Recibe un `T` y devuelve un `R`. Se usa para **transformar** datos.

```java
Function<String, Integer> longitud    = String::length;
Function<String, String>  mayusculas  = String::toUpperCase;
Function<Integer, String> intAString  = n -> "Número: " + n;

// Aplicación
System.out.println(longitud.apply("Hola"));           // 4
System.out.println(mayusculas.apply("java"));         // "JAVA"
System.out.println(intAString.apply(42));             // "Número: 42"

// Composición con andThen (primero esta, luego la siguiente)
Function<String, String> procesarNombre = mayusculas.andThen(String::trim);

// Composición con compose (primero la siguiente, luego esta)
Function<String, Integer> contarLetras = longitud.compose(String::trim);
System.out.println(contarLetras.apply("  hola  ")); // 4 (sin espacios)
```

### 5.3 `Consumer<T>` — efecto secundario

Recibe un `T` y no devuelve nada (`void`). Se usa para **imprimir**, **guardar**, o realizar acciones con efectos.

```java
Consumer<String> imprimir         = System.out::println;
Consumer<String> imprimirConTag   = s -> System.out.println("[LOG] " + s);

// Uso
imprimir.accept("Hola");         // "Hola"
imprimirConTag.accept("Evento"); // "[LOG] Evento"

// Encadenamiento: primero uno, luego el otro
Consumer<String> doble = imprimir.andThen(imprimirConTag);
doble.accept("Test"); // imprime "Test" y luego "[LOG] Test"

// Uso típico: forEach
List.of("Ana", "Bruno", "Carlos").forEach(imprimir);
```

### 5.4 `Supplier<T>` — generación de valores

No recibe argumentos y devuelve un `T`. Se usa para **crear** o **generar** valores de forma diferida.

```java
Supplier<String>    saludo    = () -> "Hola, mundo!";
Supplier<LocalDate> hoy       = LocalDate::now;
Supplier<List<String>> lista  = ArrayList::new;

// Uso
System.out.println(saludo.get()); // "Hola, mundo!"
System.out.println(hoy.get());    // fecha actual

// Uso típico: orElseGet en Optional
Optional<String> nombre = Optional.empty();
String resultado = nombre.orElseGet(() -> generarNombreDefault());
```

### Resumen de interfaces

| Interfaz | Entrada | Salida | Método | Uso típico |
|---------|---------|--------|--------|-----------|
| `Predicate<T>` | `T` | `boolean` | `test(T)` | Filtrar, validar |
| `Function<T,R>` | `T` | `R` | `apply(T)` | Transformar |
| `Consumer<T>` | `T` | `void` | `accept(T)` | Imprimir, guardar |
| `Supplier<T>` | — | `T` | `get()` | Crear, generar |
| `BiFunction<T,U,R>` | `T`, `U` | `R` | `apply(T,U)` | Transformar con 2 args |
| `UnaryOperator<T>` | `T` | `T` | `apply(T)` | Transformar al mismo tipo |
| `BinaryOperator<T>` | `T`, `T` | `T` | `apply(T,T)` | Combinar dos del mismo tipo |

---

## 6. Method References

Una **referencia a método** es una forma abreviada de escribir una lambda que solo llama a un método ya existente. Son equivalentes a una lambda, pero más legibles.

```
Clase::método   →   equivalente a   →   x -> Clase.método(x)
```

### Tipos de references

| Tipo | Sintaxis | Equivalente lambda |
|------|---------|-------------------|
| Método estático | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Método de instancia (objeto fijo) | `prefijo::concat` | `s -> prefijo.concat(s)` |
| Método de instancia (tipo arbitrario) | `String::toUpperCase` | `s -> s.toUpperCase()` |
| Constructor | `ArrayList::new` | `() -> new ArrayList<>()` |

### Ejemplos

```java
List<String> nombres = List.of("ana", "bruno", "carlos");

// Lambda explícita
nombres.stream().map(s -> s.toUpperCase()).collect(Collectors.toList());

// Method reference equivalente — más limpia
nombres.stream().map(String::toUpperCase).collect(Collectors.toList());

// Método estático
Function<String, Integer> parsear = Integer::parseInt;
parsear.apply("42"); // 42

// Método de instancia (objeto fijo)
String tag = "[INFO] ";
Function<String, String> agregarTag = tag::concat;
agregarTag.apply("inicio del proceso"); // "[INFO] inicio del proceso"

// Constructor reference
Supplier<ArrayList<String>> nuevaLista = ArrayList::new;
ArrayList<String> lista = nuevaLista.get();

// Comparator
List<String> lista2 = Arrays.asList("Carlos", "Ana", "Bruno");
lista2.sort(String::compareToIgnoreCase); // orden alfabético, case-insensitive
```

---

## 7. Optional — evitar null

`Optional<T>` es un contenedor que puede tener un valor o estar vacío. Hace **explícito** el hecho de que un valor puede no existir, forzando al caller a manejarlo.

```
┌─────────────────────────────────────────────────────┐
│                   Optional<T>                       │
│                                                     │
│   Optional.of("Hola")     →  presente: "Hola"      │
│   Optional.empty()        →  vacío                 │
│   Optional.ofNullable(x)  →  presente si x != null │
└─────────────────────────────────────────────────────┘
```

### El problema con null

```java
// Sin Optional — riesgo de NullPointerException
public String getNombreUsuario(Long id) {
    Usuario usuario = repo.findById(id); // puede devolver null
    return usuario.getNombre();          // ¡NullPointerException si es null!
}

// Con Optional — el compilador nos obliga a manejar el caso vacío
public Optional<String> getNombreUsuario(Long id) {
    return repo.findById(id).map(Usuario::getNombre);
}
```

### Creación

```java
Optional<String> con      = Optional.of("Hola");           // valor no nulo (lanza NPE si es null)
Optional<String> vacio    = Optional.empty();              // sin valor
Optional<String> nullable = Optional.ofNullable(null);     // puede ser nulo → empty
Optional<String> nullable2 = Optional.ofNullable("Hola"); // → presente
```

### Operaciones principales

```java
Optional<String> nombre = Optional.of("  Java  ");

// Verificar presencia
nombre.isPresent(); // true
nombre.isEmpty();   // false (Java 11+)

// Obtener el valor
nombre.get(); // "  Java  "  — solo si se sabe que está presente

// Alternativas seguras
Optional.<String>empty().orElse("Default");              // "Default"
Optional.<String>empty().orElseGet(() -> calcular());    // llama al Supplier
Optional.<String>empty().orElseThrow(NoSuchElementException::new);

// Transformar con map
Optional<Integer> longitud = nombre.map(String::trim).map(String::length); // 4

// Filtrar
Optional<String> largo = nombre.filter(s -> s.trim().length() > 5); // empty

// Acción si presente
nombre.ifPresent(n -> System.out.println("Nombre: " + n.trim()));

// Acción si presente o si no
nombre.ifPresentOrElse(
    n -> System.out.println("Hola, " + n.trim()),
    () -> System.out.println("Sin nombre")
);
```

### Uso en Spring Boot

```java
// Service — devuelve Optional
public Optional<Producto> buscarPorId(Long id) {
    return repository.findById(id);
}

// Controller — maneja el caso de no encontrado
@GetMapping("/{id}")
public ResponseEntity<Producto> buscar(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}
```

---

## 8. Stream API

Un **Stream** es una secuencia de elementos sobre la cual se pueden aplicar operaciones de forma declarativa y encadenada. Es el mecanismo central del estilo funcional en Java.

> Un Stream **no almacena datos** — los procesa. La fuente de datos sigue siendo la colección original.

### Pipeline: la arquitectura de un Stream

```
Fuente (Collection, Array, Stream.of)
   │
   ├── filter(...)      ← operación intermedia (lazy)
   ├── map(...)         ← operación intermedia (lazy)
   ├── sorted(...)      ← operación intermedia (lazy)
   │
   └── collect(...)     ← operación TERMINAL → dispara la ejecución
```

Las operaciones intermedias son **lazy**: no se ejecutan hasta que hay una operación terminal. Esto permite optimizaciones como cortocircuito.

### Crear un Stream

```java
// Desde una colección
Stream<String> s1 = List.of("a", "b", "c").stream();

// Desde valores directos
Stream<String> s2 = Stream.of("a", "b", "c");

// Desde un array
Stream<String> s3 = Arrays.stream(new String[]{"a", "b", "c"});

// Stream de primitivos (más eficiente)
IntStream    ints    = IntStream.range(1, 6);      // 1, 2, 3, 4, 5
LongStream   longs   = LongStream.rangeClosed(1, 5); // 1, 2, 3, 4, 5
DoubleStream doubles = DoubleStream.of(1.0, 2.0, 3.0);

// Stream infinito
Stream<Integer> naturales = Stream.iterate(0, n -> n + 1);
Stream<Double>  aleatorios = Stream.generate(Math::random);
```

---

### Operaciones intermedias

#### `filter` — retener elementos que cumplen una condición

```java
List<Integer> numeros = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

List<Integer> pares = numeros.stream()
    .filter(n -> n % 2 == 0)
    .collect(Collectors.toList());
// → [2, 4, 6, 8, 10]
```

#### `map` — transformar cada elemento

```java
List<String> nombres = List.of("ana", "bruno", "carlos");

// String → String
List<String> enMayusculas = nombres.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());
// → ["ANA", "BRUNO", "CARLOS"]

// String → Integer (cambio de tipo)
List<Integer> longitudes = nombres.stream()
    .map(String::length)
    .collect(Collectors.toList());
// → [3, 5, 6]
```

#### `flatMap` — aplanar streams anidados

```java
// Cada elemento es una lista — queremos aplanar todo en un único stream
List<List<Integer>> listas = List.of(
    List.of(1, 2, 3),
    List.of(4, 5),
    List.of(6)
);

List<Integer> todos = listas.stream()
    .flatMap(Collection::stream)
    .collect(Collectors.toList());
// → [1, 2, 3, 4, 5, 6]
```

#### `sorted` — ordenar

```java
List<String> nombres = List.of("Carlos", "Ana", "Bruno");

nombres.stream().sorted().collect(Collectors.toList());
// → ["Ana", "Bruno", "Carlos"]  (orden natural)

nombres.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
// → ["Carlos", "Bruno", "Ana"]

nombres.stream()
       .sorted(Comparator.comparing(String::length))
       .collect(Collectors.toList());
// → ["Ana", "Bruno", "Carlos"]  (por longitud)
```

#### `distinct` — eliminar duplicados

```java
List.of(1, 2, 2, 3, 3, 3, 4).stream()
    .distinct()
    .collect(Collectors.toList());
// → [1, 2, 3, 4]
```

#### `limit` y `skip`

```java
// Los primeros 3 elementos
List.of(10, 20, 30, 40, 50).stream()
    .limit(3)
    .collect(Collectors.toList()); // [10, 20, 30]

// Saltar los primeros 2
List.of(10, 20, 30, 40, 50).stream()
    .skip(2)
    .collect(Collectors.toList()); // [30, 40, 50]

// Útil con streams infinitos: los primeros 5 naturales impares a partir del 10
Stream.iterate(1, n -> n + 2)  // 1, 3, 5, 7, 9, 11, ...
      .filter(n -> n > 10)     // 11, 13, 15, ...
      .limit(5)                 // tomar solo 5
      .collect(Collectors.toList()); // [11, 13, 15, 17, 19]
```

#### `peek` — observar sin modificar (debug)

```java
nombres.stream()
    .peek(n -> System.out.println("  Entrando: " + n))
    .map(String::toUpperCase)
    .peek(n -> System.out.println("  Transformado: " + n))
    .collect(Collectors.toList());
```

---

### Operaciones terminales

#### `collect` — acumular en una colección

```java
// A List
List<String> lista = stream.collect(Collectors.toList());

// A Set (elimina duplicados, no garantiza orden)
Set<String> conjunto = stream.collect(Collectors.toSet());

// Joining — concatenar strings
String unido = Stream.of("Hola", "mundo", "Java")
    .collect(Collectors.joining(", ", "[", "]"));
// → "[Hola, mundo, Java]"
```

#### `forEach` — ejecutar acción por cada elemento

```java
List.of("Ana", "Bruno", "Carlos").stream()
    .map(String::toUpperCase)
    .forEach(System.out::println);
```

#### `count` — contar elementos

```java
long cantidad = nombres.stream()
    .filter(n -> n.startsWith("A"))
    .count();
```

#### `findFirst` / `findAny` — obtener un elemento

```java
Optional<String> primero = nombres.stream()
    .filter(n -> n.length() > 4)
    .findFirst(); // el primero que cumpla

Optional<String> cualquiera = nombres.stream()
    .filter(n -> n.length() > 4)
    .findAny(); // cualquiera (más eficiente en streams paralelos)
```

#### `anyMatch` / `allMatch` / `noneMatch`

```java
List<Integer> numeros = List.of(1, 2, 3, 4, 5);

boolean alguno  = numeros.stream().anyMatch(n -> n > 4);  // true (hay un 5)
boolean todos   = numeros.stream().allMatch(n -> n > 0);  // true (todos > 0)
boolean ninguno = numeros.stream().noneMatch(n -> n < 0); // true (ninguno negativo)
```

#### `reduce` — combinar todos los elementos en uno

```java
// Suma con identidad 0
int suma = List.of(1, 2, 3, 4, 5).stream()
    .reduce(0, Integer::sum);     // 15

// Producto
int producto = List.of(1, 2, 3, 4, 5).stream()
    .reduce(1, (a, b) -> a * b);  // 120

// Sin identidad — devuelve Optional (puede no haber elementos)
Optional<Integer> max = List.of(3, 1, 4, 1, 5, 9).stream()
    .reduce(Integer::max);        // Optional[9]
```

#### `min` / `max` — mínimo y máximo

```java
Optional<String> masCorto = nombres.stream()
    .min(Comparator.comparing(String::length));

Optional<Integer> mayor = numeros.stream()
    .max(Comparator.naturalOrder());
```

---

## 9. Collectors

`Collectors` es una clase con colectores predefinidos para los casos más comunes.

### Agrupación con `groupingBy`

```java
record Persona(String nombre, String ciudad, int edad) {}

List<Persona> personas = List.of(
    new Persona("Ana",     "Buenos Aires", 25),
    new Persona("Bruno",   "Córdoba",      30),
    new Persona("Alicia",  "Buenos Aires", 28),
    new Persona("Carlos",  "Córdoba",      35)
);

// Agrupar por ciudad → Map<String, List<Persona>>
Map<String, List<Persona>> porCiudad = personas.stream()
    .collect(Collectors.groupingBy(Persona::ciudad));
// {"Buenos Aires": [Ana, Alicia], "Córdoba": [Bruno, Carlos]}

// Agrupar y contar → Map<String, Long>
Map<String, Long> cantidadPorCiudad = personas.stream()
    .collect(Collectors.groupingBy(Persona::ciudad, Collectors.counting()));
// {"Buenos Aires": 2, "Córdoba": 2}

// Agrupar y sumar edades
Map<String, Integer> edadTotalPorCiudad = personas.stream()
    .collect(Collectors.groupingBy(
        Persona::ciudad,
        Collectors.summingInt(Persona::edad)
    ));
// {"Buenos Aires": 53, "Córdoba": 65}
```

### Estadísticas con `summarizingInt`

```java
IntSummaryStatistics stats = personas.stream()
    .collect(Collectors.summarizingInt(Persona::edad));

System.out.println("Mínimo:  " + stats.getMin());     // 25
System.out.println("Máximo:  " + stats.getMax());     // 35
System.out.println("Promedio:" + stats.getAverage()); // 29.5
System.out.println("Suma:    " + stats.getSum());     // 118
System.out.println("Cantidad:" + stats.getCount());   // 4
```

### `toMap` — crear un Map

```java
// Nombre → Edad
Map<String, Integer> nombreAEdad = personas.stream()
    .collect(Collectors.toMap(
        Persona::nombre,  // key
        Persona::edad     // value
    ));
// {"Ana": 25, "Bruno": 30, "Alicia": 28, "Carlos": 35}
```

### `partitioningBy` — dividir en dos grupos

```java
// Mayores y menores de 30
Map<Boolean, List<Persona>> particion = personas.stream()
    .collect(Collectors.partitioningBy(p -> p.edad() >= 30));

List<Persona> mayoresDe30 = particion.get(true);  // [Bruno, Carlos]
List<Persona> menoresDe30 = particion.get(false); // [Ana, Alicia]
```

---

## 10. Comparación imperativa vs funcional

### Caso real: procesar pedidos

```java
record Pedido(String cliente, double total, String estado) {}

List<Pedido> pedidos = List.of(
    new Pedido("Ana",    1500.0, "COMPLETADO"),
    new Pedido("Bruno",   800.0, "PENDIENTE"),
    new Pedido("Carlos", 3200.0, "COMPLETADO"),
    new Pedido("Diana",   200.0, "CANCELADO")
);
```

**Objetivo**: sumar el total de pedidos completados.

```java
// ━━━━━━━━ Estilo IMPERATIVO ━━━━━━━━
double totalImperativo = 0;
for (Pedido p : pedidos) {
    if ("COMPLETADO".equals(p.estado())) {
        totalImperativo += p.total();
    }
}
// totalImperativo = 4700.0

// ━━━━━━━━ Estilo FUNCIONAL ━━━━━━━━
double totalFuncional = pedidos.stream()
    .filter(p -> "COMPLETADO".equals(p.estado()))
    .mapToDouble(Pedido::total)
    .sum();
// totalFuncional = 4700.0
```

**Objetivo 2**: los clientes de pedidos completados, ordenados por nombre.

```java
// IMPERATIVO
List<String> clientesImp = new ArrayList<>();
for (Pedido p : pedidos) {
    if ("COMPLETADO".equals(p.estado())) {
        clientesImp.add(p.cliente());
    }
}
Collections.sort(clientesImp);

// FUNCIONAL
List<String> clientesFun = pedidos.stream()
    .filter(p -> "COMPLETADO".equals(p.estado()))
    .map(Pedido::cliente)
    .sorted()
    .collect(Collectors.toList());
// → ["Ana", "Carlos"]
```

### Cheat sheet — patrones más comunes

```java
// Filtrar y transformar
List<String> nombres = empleados.stream()
    .filter(e -> e.getSalario() > 50000)
    .map(Empleado::getNombre)
    .collect(Collectors.toList());

// Agrupar
Map<String, List<Empleado>> porDepto = empleados.stream()
    .collect(Collectors.groupingBy(Empleado::getDepartamento));

// El de mayor salario
Optional<Empleado> mejor = empleados.stream()
    .max(Comparator.comparing(Empleado::getSalario));

// Suma de salarios
double totalSalarios = empleados.stream()
    .mapToDouble(Empleado::getSalario)
    .sum();

// ¿Todos son mayores de 18?
boolean todosMayores = empleados.stream()
    .allMatch(e -> e.getEdad() >= 18);

// ¿Existe alguno en el departamento "IT"?
boolean hayIT = empleados.stream()
    .anyMatch(e -> "IT".equals(e.getDepartamento()));
```

---

## 11. Ejercicios

### Ejercicio 1 — Básico con números

Dada la lista `List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)`:

1. Filtrar los números **pares**
2. Elevar cada uno al **cuadrado**
3. **Sumar** todos los resultados

Resultado esperado: `220` (4 + 16 + 36 + 64 + 100)

---

### Ejercicio 2 — Strings

Dada la lista `List.of("hola", "mundo", "java", "funcional", "stream", "api")`:

1. Filtrar las palabras con **más de 4 letras**
2. Convertirlas a **mayúsculas**
3. Ordenarlas **alfabéticamente**
4. Unirlas en un string separado por `" | "`

Resultado esperado: `"FUNCIONAL | MUNDO | STREAM"`

---

### Ejercicio 3 — Objetos

```java
record Producto(String nombre, String categoria, double precio) {}

List<Producto> productos = List.of(
    new Producto("Laptop",      "Electrónica", 1200.0),
    new Producto("Mouse",       "Electrónica",   25.0),
    new Producto("Silla",       "Mobiliario",   350.0),
    new Producto("Escritorio",  "Mobiliario",   800.0),
    new Producto("Auriculares", "Electrónica",  150.0)
);
```

Con esta lista:

1. Agrupar productos por **categoría**
2. Calcular el **precio promedio** de Electrónica
3. Obtener el **producto más caro** de Mobiliario
4. Crear un `Map<String, Double>` con `{nombre → precio}` solo de Electrónica

---

### Ejercicio 4 — Predicate y Function

Modelar el siguiente pipeline usando `Predicate<String>` y `Function<String, String>`:

1. Eliminar espacios al inicio y final (`trim`)
2. Convertir a mayúsculas
3. Filtrar los que tienen **más de 3 caracteres**
4. Ordenar por longitud descendente

Aplicarlo sobre: `List.of("  java  ", "go", "  python  ", "  c  ", "kotlin")`

Resultado esperado: `["PYTHON", "KOTLIN", "JAVA"]`

---

### Ejercicio 5 — Composición con Optional

```java
public Optional<Usuario> findByEmail(String email) { ... }
```

Usando la función anterior:
1. Buscar un usuario por email
2. Si existe, obtener su nombre completo en mayúsculas
3. Si no existe, retornar `"USUARIO DESCONOCIDO"`

Hacerlo **en una sola cadena** de operaciones con `Optional`, sin `if/else`.

---

## Referencias

- [Lambda Expressions — Oracle Java Tutorials](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html)
- [Java Functional Interfaces — Baeldung](https://www.baeldung.com/java-8-functional-interfaces)
- [Java Stream API — Baeldung](https://www.baeldung.com/java-8-streams)
- [Java Optional — Baeldung](https://www.baeldung.com/java-optional)
- [Deep Dive into Java Stream API — Medium](https://medium.com/@kacar7/deep-dive-into-java-stream-api-understanding-map-filter-reduce-and-more-c8ae26d8dc41)

# Tests unitarios con Spring Boot

Material de apoyo para la **Unidad 3** de **Programación 2** — Ingeniería en Computación (UCSE).

---

## Índice

1. [¿Qué es un test unitario?](#1-qué-es-un-test-unitario)
2. [Dependencias necesarias](#2-dependencias-necesarias)
3. [Estructura de carpetas](#3-estructura-de-carpetas)
4. [Primer test: sin Spring, sin base de datos](#4-primer-test-sin-spring-sin-base-de-datos)
5. [El problema de las dependencias](#5-el-problema-de-las-dependencias)
6. [Mocks: qué son y cómo funcionan](#6-mocks-qué-son-y-cómo-funcionan)
7. [Testear un Service con Mockito](#7-testear-un-service-con-mockito)
8. [Testear con H2 en memoria](#8-testear-con-h2-en-memoria)
9. [Cuándo usar Mockito vs H2](#9-cuándo-usar-mockito-vs-h2)
10. [Resumen de anotaciones](#10-resumen-de-anotaciones)

---

## 1. ¿Qué es un test unitario?

Un **test unitario** es un método Java que verifica automáticamente que otra pieza de código se comporta como se espera.

En lugar de levantar la app y probar con Postman cada vez que cambiás algo, escribís un test que lo hace por vos en segundos.

```java
// Esto es un test: un método anotado con @Test que verifica algo
@Test
void dos_mas_dos_es_cuatro() {
    int resultado = 2 + 2;
    assertEquals(4, resultado); // si no es 4, el test falla
}
```

Cuando corrés los tests, cada método `@Test` se ejecuta y reporta `PASSED` o `FAILED`. Si algo rompiste sin darte cuenta, lo sabés antes de subir el código.

### ¿Qué testeamos en Spring Boot?

La capa más importante de testear es el **Service**, porque ahí vive la lógica de negocio. El Controller solo delega, y el Repository solo accede a datos — el Service es donde se toman las decisiones.

---

## 2. Dependencias necesarias

Spring Boot ya incluye todo lo necesario para testear en el starter `spring-boot-starter-test`. Cuando creás el proyecto desde [Spring Initializr](https://start.spring.io), esta línea aparece sola en el `build.gradle`:

```groovy
dependencies {
    // ...otras dependencias...

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

Este starter incluye automáticamente:
- **JUnit 5** — el framework de tests (anotaciones `@Test`, `@BeforeEach`, etc.)
- **Mockito** — la librería para crear mocks
- **AssertJ** — assertions más legibles (opcional, pero útil)

No hay que agregar nada más para los tests del Service.

Si también querés testear con base de datos en memoria, agregás H2:

```groovy
dependencies {
    // ...otras dependencias...

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'com.h2database:h2'  // solo si usás H2 para tests
}
```

---

## 3. Estructura de carpetas

Un proyecto Spring Boot generado por Initializr ya tiene la carpeta de tests creada:

```
src/
├── main/
│   ├── java/com/ejemplo/
│   │   ├── EjemploApplication.java
│   │   ├── controller/
│   │   │   └── ProductoController.java
│   │   ├── service/
│   │   │   └── ProductoService.java
│   │   ├── repository/
│   │   │   └── ProductoRepository.java
│   │   └── model/
│   │       └── Producto.java
│   └── resources/
│       └── application.properties       ← configuración de producción
│
└── test/
    ├── java/com/ejemplo/
    │   ├── EjemploApplicationTests.java  ← test de contexto generado por Initializr
    │   └── service/
    │       └── ProductoServiceTest.java  ← acá van los tests del service
    └── resources/
        └── application.properties       ← configuración solo para tests (H2, logs, etc.)
```

### Reglas de ubicación

- Los tests van en `src/test/java/`, **en el mismo paquete** que la clase que testean.
  - Si la clase es `com.ejemplo.service.ProductoService`, el test va en `com.ejemplo.service.ProductoServiceTest`.
- Los archivos de configuración para tests van en `src/test/resources/`. Gradle los usa automáticamente al ejecutar tests, sin pisar los de producción.
- El nombre de la clase de test por convención termina en `Test`: `ProductoServiceTest`.

---

## 4. Primer test: sin Spring, sin base de datos

Antes de ver mocks, veamos el test más simple posible: testear un método que no depende de nada externo.

Supongamos que `ProductoService` tiene este método:

```java
// ProductoService.java
public boolean esCaro(Producto producto) {
    return producto.getPrecio() > 1000.0;
}
```

El test de ese método no necesita base de datos ni Spring:

```java
// ProductoServiceTest.java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductoServiceTest {

    // Creamos el service directamente, sin Spring
    private final ProductoService service = new ProductoService(null);

    @Test
    void producto_con_precio_mayor_a_1000_es_caro() {
        Producto producto = new Producto();
        producto.setPrecio(1500.0);

        boolean resultado = service.esCaro(producto);

        assertTrue(resultado);
    }

    @Test
    void producto_con_precio_menor_a_1000_no_es_caro() {
        Producto producto = new Producto();
        producto.setPrecio(500.0);

        boolean resultado = service.esCaro(producto);

        assertFalse(resultado);
    }
}
```

### Assertions más usadas

| Método | Qué verifica |
|--------|--------------|
| `assertEquals(esperado, real)` | Que los valores sean iguales |
| `assertNotNull(valor)` | Que el valor no sea null |
| `assertTrue(condicion)` | Que la condición sea verdadera |
| `assertFalse(condicion)` | Que la condición sea falsa |
| `assertThrows(Excepcion.class, () -> ...)` | Que se lance una excepción específica |

---

## 5. El problema de las dependencias

El `ProductoService` real no es tan simple. Depende de `ProductoRepository` para buscar datos:

```java
@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public Producto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
    }
}
```

Si queremos testear `buscarPorId`, el método llama a `repository.findById()`. El problema: el `ProductoRepository` real habla con MySQL. Para que el test funcione, necesitaríamos levantar Spring, conectar a la base de datos, tener datos cargados... eso hace el test lento, frágil y dependiente de infraestructura externa.

La solución son los **mocks**.

---

## 6. Mocks: qué son y cómo funcionan

Un **mock** es un objeto falso que reemplaza a una dependencia real durante el test. No hace nada por sí mismo — vos le decís qué tiene que devolver cuando se le llama un método.

Pensalo así: en lugar del repositorio real que va a la base de datos, le pasamos al service un repositorio falso que nosotros controlamos.

```
Test
 └── ProductoService (real)
      └── ProductoRepository (FALSO — mock controlado por el test)
```

### Crear un mock con Mockito

```java
// Crea un objeto falso del tipo ProductoRepository
ProductoRepository repositorioFalso = Mockito.mock(ProductoRepository.class);
```

El `repositorioFalso` tiene todos los métodos del `ProductoRepository`, pero por defecto no hacen nada y devuelven `null` (o listas vacías).

### Configurar qué devuelve el mock

Con `when(...).thenReturn(...)` le decís qué tiene que responder cuando se le llama un método:

```java
Producto productoPrueba = new Producto();
productoPrueba.setId(1L);
productoPrueba.setNombre("Laptop");

// "Cuando alguien llame a findById(1L), devolvé este producto"
when(repositorioFalso.findById(1L)).thenReturn(Optional.of(productoPrueba));
```

A partir de ahí, cualquier llamada a `repositorioFalso.findById(1L)` devuelve el `productoPrueba` que vos definiste, sin tocar ninguna base de datos.

### Verificar que se llamó a un método

También podés verificar que el service efectivamente llamó al repositorio:

```java
// Verifica que findById(1L) fue llamado exactamente una vez
verify(repositorioFalso).findById(1L);
```

---

## 7. Testear un Service con Mockito

Esta es la forma recomendada de testear la capa Service en Spring Boot. No levanta el contexto de Spring — es un test Java puro, rápido y aislado.

### Configuración de la clase de test

```java
// ProductoServiceTest.java
package com.ejemplo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)  // activa Mockito para esta clase
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;  // mock del repositorio

    @InjectMocks
    private ProductoService service;  // service real, con el mock inyectado

    // ...tests acá
}
```

**`@ExtendWith(MockitoExtension.class)`** — le dice a JUnit que use Mockito para procesar las anotaciones `@Mock` e `@InjectMocks`.

**`@Mock`** — crea un objeto falso del tipo indicado. Mockito lo instancia automáticamente.

**`@InjectMocks`** — crea el objeto real e inyecta los mocks declarados en esta clase. Reemplaza el `new ProductoService(repository)` que haríamos a mano.

### Tests completos

```java
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    @InjectMocks
    private ProductoService service;

    // ────────────────────────────────────────────────────
    // Tests de buscarPorId
    // ────────────────────────────────────────────────────

    @Test
    void buscarPorId_devuelve_producto_cuando_existe() {
        // Preparar — definimos qué devuelve el mock
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop");
        when(repository.findById(1L)).thenReturn(Optional.of(producto));

        // Ejecutar
        Producto resultado = service.buscarPorId(1L);

        // Verificar
        assertNotNull(resultado);
        assertEquals("Laptop", resultado.getNombre());
    }

    @Test
    void buscarPorId_lanza_excepcion_cuando_no_existe() {
        // Preparar — el repositorio devuelve vacío
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Verificar que se lanza la excepción correcta
        assertThrows(RuntimeException.class, () -> service.buscarPorId(99L));
    }

    // ────────────────────────────────────────────────────
    // Tests de listarTodos
    // ────────────────────────────────────────────────────

    @Test
    void listarTodos_devuelve_lista_de_productos() {
        // Preparar
        List<Producto> productos = List.of(
            new Producto(), new Producto()
        );
        when(repository.findAll()).thenReturn(productos);

        // Ejecutar
        List<Producto> resultado = service.listarTodos();

        // Verificar
        assertEquals(2, resultado.size());
    }

    @Test
    void listarTodos_devuelve_lista_vacia_cuando_no_hay_productos() {
        when(repository.findAll()).thenReturn(List.of());

        List<Producto> resultado = service.listarTodos();

        assertTrue(resultado.isEmpty());
    }

    // ────────────────────────────────────────────────────
    // Tests de guardar
    // ────────────────────────────────────────────────────

    @Test
    void guardar_retorna_producto_guardado() {
        Producto producto = new Producto();
        producto.setNombre("Mouse");

        Producto guardado = new Producto();
        guardado.setId(1L);
        guardado.setNombre("Mouse");

        when(repository.save(producto)).thenReturn(guardado);

        Producto resultado = service.guardar(producto);

        assertNotNull(resultado.getId());
        assertEquals("Mouse", resultado.getNombre());
    }

    // ────────────────────────────────────────────────────
    // Tests de eliminar
    // ────────────────────────────────────────────────────

    @Test
    void eliminar_llama_al_repositorio() {
        service.eliminar(1L);

        // Verifica que el service efectivamente llamó a deleteById
        verify(repository).deleteById(1L);
    }
}
```

### El patrón de los tres bloques

Cada test sigue el mismo esquema. Se lo llama **Arrange-Act-Assert** (o "Preparar-Ejecutar-Verificar"):

```java
@Test
void nombre_descriptivo_del_escenario() {
    // Preparar — configurar el mock y los datos de entrada
    when(repository.findById(1L)).thenReturn(Optional.of(producto));

    // Ejecutar — llamar al método que estamos testeando
    Producto resultado = service.buscarPorId(1L);

    // Verificar — confirmar que el resultado es el esperado
    assertEquals("Laptop", resultado.getNombre());
}
```

Siempre los tres bloques, en ese orden. Hace los tests fáciles de leer y de mantener.

---

## 8. Testear con H2 en memoria

La alternativa a Mockito es usar una **base de datos en memoria** (H2) que se levanta y destruye con cada ejecución de tests. En lugar de mockear el repositorio, lo usamos real pero apuntando a H2 en vez de MySQL.

Este enfoque levanta el contexto de Spring Boot completo, por eso es más lento — pero es más cercano al comportamiento real de la app.

### Dependencia

En `build.gradle`, H2 ya está disponible si se agregó desde Initializr. Si no está, agregar:

```groovy
testImplementation 'com.h2database:h2'
```

### Configuración para tests

Crear el archivo `src/test/resources/application.properties` con la configuración de H2:

```properties
# Base de datos H2 en memoria (reemplaza a MySQL solo durante los tests)
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Dialecto H2
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Crear las tablas al iniciar y borrarlas al terminar
spring.jpa.hibernate.ddl-auto=create-drop

# Opcional: ver las queries SQL en consola durante los tests
spring.jpa.show-sql=true
```

Este archivo solo se usa cuando se ejecutan tests — no pisa el `application.properties` de `src/main/resources/`.

### Clase de test con H2

```java
// ProductoServiceIntegrationTest.java
package com.ejemplo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest  // levanta el contexto completo de Spring con H2
class ProductoServiceIntegrationTest {

    @Autowired
    private ProductoService service;  // Spring inyecta el service real

    @Autowired
    private ProductoRepository repository;  // repositorio real apuntando a H2

    @Test
    void guardar_y_recuperar_producto() {
        Producto producto = new Producto();
        producto.setNombre("Teclado");
        producto.setPrecio(150.0);

        Producto guardado = service.guardar(producto);

        assertNotNull(guardado.getId());
        Producto recuperado = service.buscarPorId(guardado.getId());
        assertEquals("Teclado", recuperado.getNombre());
    }

    @Test
    void buscarPorId_lanza_excepcion_cuando_no_existe() {
        assertThrows(RuntimeException.class, () -> service.buscarPorId(9999L));
    }
}
```

### Limpiar datos entre tests

Con H2 y `create-drop`, las tablas se crean al iniciar y se destruyen al terminar. Pero si varios tests guardan datos en la misma sesión de tests, pueden interferir entre sí.

Para limpiar la base entre cada test, usar `@Transactional`:

```java
@SpringBootTest
@Transactional  // hace rollback automático después de cada test
class ProductoServiceIntegrationTest {

    @Autowired
    private ProductoService service;

    @Test
    void guardar_producto() {
        // Al terminar este test, el INSERT hace rollback automáticamente
        // → el próximo test arranca con la base limpia
        Producto producto = new Producto();
        producto.setNombre("Monitor");
        service.guardar(producto);

        assertEquals(1, service.listarTodos().size());
    }
}
```

---

## 9. Cuándo usar Mockito vs H2

| | Mockito | H2 |
|---|---|---|
| **Velocidad** | Muy rápido (milisegundos) | Más lento (levanta Spring) |
| **Aislamiento** | Total — solo se testea el Service | Parcial — Service + Repository + BD |
| **Configuración** | Solo anotaciones | Necesita `application.properties` en test |
| **Qué valida** | Lógica de negocio | Lógica + acceso a datos + queries JPA |
| **Cuándo usarlo** | Siempre para la lógica del Service | Cuando querés validar queries derivadas o `@Query` custom |

**Regla general**: usá Mockito para testear la lógica del Service. Usá H2 cuando necesitás verificar que las queries JPA devuelven lo que esperas.

En la materia, cualquiera de los dos enfoques es válido para los preparciales.

---

## 10. Resumen de anotaciones

### JUnit 5

| Anotación | Dónde va | Qué hace |
|-----------|----------|----------|
| `@Test` | Método | Marca el método como un test |
| `@BeforeEach` | Método | Se ejecuta antes de cada `@Test` |
| `@AfterEach` | Método | Se ejecuta después de cada `@Test` |

### Mockito

| Anotación | Dónde va | Qué hace |
|-----------|----------|----------|
| `@ExtendWith(MockitoExtension.class)` | Clase | Activa Mockito para la clase de test |
| `@Mock` | Campo | Crea un objeto falso del tipo declarado |
| `@InjectMocks` | Campo | Crea el objeto real e inyecta los `@Mock` |

### Spring Boot Test

| Anotación | Dónde va | Qué hace |
|-----------|----------|----------|
| `@SpringBootTest` | Clase | Levanta el contexto completo de Spring |
| `@Autowired` | Campo | Inyecta un bean del contexto de Spring |
| `@Transactional` | Clase o método | Hace rollback automático al terminar el test |

### Métodos clave de Mockito

```java
// Configurar respuesta del mock
when(mock.metodo(argumento)).thenReturn(valor);

// Configurar que lance una excepción
when(mock.metodo(argumento)).thenThrow(new RuntimeException("error"));

// Verificar que se llamó al método
verify(mock).metodo(argumento);

// Verificar que NO se llamó al método
verify(mock, never()).metodo(argumento);

// Aceptar cualquier argumento (cuando no importa el valor exacto)
when(mock.findById(anyLong())).thenReturn(Optional.of(producto));
```

---

## Ejemplo completo de referencia

A continuación, el test completo de un `ProductoService` con los casos más comunes, listo para copiar y adaptar:

```java
package com.ejemplo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    @InjectMocks
    private ProductoService service;

    @Test
    void buscarPorId_devuelve_producto_cuando_existe() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop");
        when(repository.findById(1L)).thenReturn(Optional.of(producto));

        Producto resultado = service.buscarPorId(1L);

        assertEquals("Laptop", resultado.getNombre());
    }

    @Test
    void buscarPorId_lanza_excepcion_cuando_no_existe() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.buscarPorId(1L));
    }

    @Test
    void listarTodos_devuelve_todos_los_productos() {
        when(repository.findAll()).thenReturn(List.of(new Producto(), new Producto()));

        List<Producto> resultado = service.listarTodos();

        assertEquals(2, resultado.size());
    }

    @Test
    void guardar_retorna_el_producto_con_id() {
        Producto entrada = new Producto();
        entrada.setNombre("Mouse");

        Producto conId = new Producto();
        conId.setId(5L);
        conId.setNombre("Mouse");

        when(repository.save(entrada)).thenReturn(conId);

        Producto resultado = service.guardar(entrada);

        assertEquals(5L, resultado.getId());
    }

    @Test
    void eliminar_llama_deleteById_con_el_id_correcto() {
        service.eliminar(3L);

        verify(repository).deleteById(3L);
    }
}
```

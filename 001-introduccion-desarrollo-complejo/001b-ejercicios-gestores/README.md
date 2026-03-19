# Ejercicios: Gestores de Dependencias

Ejercicios prácticos para aprender a instalar y usar librerías externas con **Maven** y **Gradle**.

---

## Ejercicios Maven

### Ejercicio 1 — Gson: Convertir objetos Java a JSON

**Gson** es una librería de Google para serializar y deserializar objetos Java a formato JSON.

#### Instalación

Agregar en el `pom.xml`, dentro de la sección `<dependencies>`:

```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.11.0</version>
</dependency>
```

#### Uso

```java
import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) {
        Gson gson = new Gson();

        // Objeto Java → JSON
        String[] frutas = {"manzana", "pera", "uva"};
        String json = gson.toJson(frutas);
        System.out.println(json); // ["manzana","pera","uva"]

        // JSON → objeto Java
        String jsonEntrada = "[\"rojo\",\"verde\",\"azul\"]";
        String[] colores = gson.fromJson(jsonEntrada, String[].class);
        System.out.println(colores[0]); // rojo
    }
}
```

**Consigna:** Crear una clase `Persona` con los atributos `nombre`, `edad` y `email`. Instanciar un objeto y convertirlo a JSON con Gson. Luego tomar ese JSON y reconstruir el objeto.

---

### Ejercicio 2 — Apache Commons Lang: Utilidades de String

**Apache Commons Lang** ofrece métodos utilitarios que Java estándar no incluye, especialmente para trabajar con `String`.

#### Instalación

Agregar en el `pom.xml`, dentro de la sección `<dependencies>`:

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.14.0</version>
</dependency>
```

#### Uso

```java
import org.apache.commons.lang3.StringUtils;

public class Main {
    public static void main(String[] args) {
        // Verificar si un String está vacío o es solo espacios
        System.out.println(StringUtils.isBlank(""));        // true
        System.out.println(StringUtils.isBlank("   "));     // true
        System.out.println(StringUtils.isBlank("hola"));    // false

        // Invertir un String
        System.out.println(StringUtils.reverse("Java"));    // avaJ

        // Repetir un String
        System.out.println(StringUtils.repeat("ab", 3));    // ababab

        // Capitalizar
        System.out.println(StringUtils.capitalize("hola")); // Hola
    }
}
```

**Consigna:** Pedir al usuario que ingrese su nombre y apellido. Usar `StringUtils` para: (1) validar que no estén vacíos, (2) mostrar el nombre capitalizado, y (3) mostrar cuántas veces aparece la letra 'a' usando `StringUtils.countMatches`.

---

## Ejercicios Gradle

### Ejercicio 3 — Guava: Colecciones y utilidades de Google

**Guava** es la librería de utilidades de Google para Java. Incluye estructuras de datos extra, manipulación de colecciones y más.

#### Instalación

Agregar en el `build.gradle`, dentro de la sección `dependencies`:

```groovy
dependencies {
    implementation 'com.google.guava:guava:33.2.1-jre'
}
```

#### Uso

```java
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.base.Joiner;

public class Main {
    public static void main(String[] args) {
        // Crear una lista mutable fácilmente
        var numeros = Lists.newArrayList(10, 20, 30, 40);
        numeros.add(50);
        System.out.println(numeros); // [10, 20, 30, 40, 50]

        // Lista inmutable (no se puede modificar)
        ImmutableList<String> paises = ImmutableList.of("Argentina", "Brasil", "Chile");
        System.out.println(paises.get(0)); // Argentina

        // Unir elementos con un separador
        String resultado = Joiner.on(" - ").join(paises);
        System.out.println(resultado); // Argentina - Brasil - Chile
    }
}
```

**Consigna:** Crear una lista de nombres de ciudades usando `Lists.newArrayList`. Usar `Joiner` para imprimirla separada por comas. Luego crear una `ImmutableList` con las ciudades que empiecen con la letra 'B' y mostrarla.

---

### Ejercicio 4 — Java Faker: Generación de datos falsos

**Java Faker** genera datos aleatorios y realistas: nombres, direcciones, emails, fechas, etc. Es muy útil para poblar bases de datos de prueba.

#### Instalación

Agregar en el `build.gradle`, dentro de la sección `dependencies`:

```groovy
dependencies {
    implementation 'com.github.javafaker:javafaker:1.0.2'
}
```

#### Uso

```java
import com.github.javafaker.Faker;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        Faker faker = new Faker(new Locale("es"));

        // Generar una persona ficticia
        System.out.println(faker.name().fullName());         // ej: Juan López
        System.out.println(faker.internet().emailAddress()); // ej: juan.lopez@example.com
        System.out.println(faker.phoneNumber().cellPhone()); // ej: 011-4523-8761

        // Generar una dirección
        System.out.println(faker.address().streetAddress()); // ej: Av. Corrientes 1234
        System.out.println(faker.address().city());          // ej: Córdoba

        // Número en un rango
        System.out.println(faker.number().numberBetween(18, 65)); // ej: 34
    }
}
```

**Consigna:** Usar Java Faker para generar una lista de 5 usuarios ficticios. Por cada usuario mostrar: nombre completo, email, ciudad y edad (número entre 18 y 65). Guardar los usuarios en un `ArrayList` de objetos `Usuario`.

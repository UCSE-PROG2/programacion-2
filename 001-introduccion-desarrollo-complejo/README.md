# Unidad 1 - Introducción a Desarrollo Complejo

Material de apoyo para la **Unidad 1** de **Programación 2** - Ingeniería en Computación (UCSE).

---

## Índice

1. [Complejidad del software](#1-complejidad-del-software)
2. [Atributos de sistemas complejos](#2-atributos-de-sistemas-complejos)
3. [Complejidad organizada](#3-complejidad-organizada)
4. [Principios SOLID](#4-principios-solid)
5. [Introducción a Java](#5-introducción-a-java)
6. [Gestores de dependencias](#6-gestores-de-dependencias)
7. [Patrones](#7-patrones)
8. [Patrones de arquitectura](#8-patrones-de-arquitectura)
9. [Patrones de diseño](#9-patrones-de-diseño)
10. [Ejercicios y material de la unidad](#10-ejercicios-y-material-de-la-unidad)

---

## 1. Complejidad del software

### Software simple o artesanal

- No son complejos
- Suelen ser construidos por una sola persona
- Ciclo de vida corto
- No necesitan grandes esfuerzos de análisis y diseño
- Pueden construirse aplicaciones alternativas en periodos de tiempo cortos

### Software Complejo

También denominados de **dimensión industrial**:

- Un solo desarrollador no puede comprender todas las utilidades del diseño
- La complejidad es la propiedad esencial
- Ejemplos: sistema de reservas, compras en línea, búsqueda (Despegar, Booking, MercadoLibre)

### ¿Por qué un software es complejo?

La complejidad inherente al software se deriva de cuatro elementos:

1. **La complejidad del dominio del problema**
2. **La dificultad de gestionar el proceso de desarrollo**
3. **El detalle que se puede alcanzar a través del software**
4. **El problema de caracterizar el comportamiento de sistemas discretos**

---

### Complejidad del dominio

- Muchos requerimientos, variados o cambiantes
- *Communication gap* entre programadores y usuarios
- Los requerimientos cambian durante el proceso de diseño / desarrollo / pruebas: se crean nuevos, se eliminan otros
- Regulaciones o componentes externos no relacionados al desarrollo de software

### Dificultades en la gestión

- El software crece hasta un punto donde se necesita un equipo de desarrollo
- El equipo demanda trabajo extra (dirección, comunicación, integridad de diseño)
- Manejar gran cantidad de código (repositorios, herramientas, frameworks, lenguajes de programación, reutilización de código, patrones de diseño, etc.)

### Nivel de detalle

> "No volver a inventar la rueda"

Utilizar estándares o patrones (verificar previamente).

### Comportamiento de sistemas discretos

El software es discreto por naturaleza: su ejecución es una transición entre estados finitos y definidos. Precisamente esa discretitud lo hace difícil de predecir y controlar a escala.

- Cada estado contiene variables, su valor, direcciones en tiempo de ejecución
- Pequeñas variaciones en la entrada pueden producir grandes variaciones en las salidas
- Diseñar el sistema de forma que el comportamiento de una parte tenga mínimo impacto en otra
- La transición de estados debe ser determinista (a veces no lo es)
- Necesidad de testing automático y progresivo

### Complejidad ilimitada o infinita

> "Cuanto más complejo es un sistema más probable es que se caiga"

- **Crisis del software**: sucesivos fracasos de las distintas metodologías para dominar la complejidad
- Retrasos, desviaciones, diferencias entre la entrega y los requisitos iniciales
- Problemas económicos relacionados a esos retrasos o desviaciones
- Causas producidas por mitos: de gestión, del cliente, de los desarrolladores

---

## 2. Atributos de sistemas complejos

Los sistemas complejos presentan cinco atributos fundamentales:

| Atributo | Descripción |
|----------|-------------|
| **Jerarquía** | Un sistema complejo se compone de subsistemas relacionados que tienen a su vez sus propios subsistemas, hasta llegar a componentes elementales |
| **Componentes (Primitivas relativas)** | La elección de qué componentes son primitivos depende de la decisión del analista. Lo que para una persona es primitivo, para otro puede no serlo |
| **Enlaces (Separación de preocupaciones)** | El software puede ser modularizado aunque los módulos tengan dependencias. Los vínculos internos de un módulo son más fuertes que los externos (menor acoplamiento) |
| **Patrones** | Los sistemas complejos tienen patrones comunes que permiten la reutilización de componentes |
| **Evolución** | Los sistemas complejos que funcionan son la evolución de sistemas simples. **Es muy difícil producir un sistema complejo desde cero** |

### Forma canónica de un sistema complejo

La **forma canónica** es la forma menos compleja de definir un sistema. El descubrimiento de abstracciones y mecanismos comunes facilita la comprensión de sistemas complejos.

- **Jerarquía "parte de" ↔ estructura de objetos**: representa los objetos que interactúan en el sistema
- **Jerarquía "es un" ↔ estructura de clases**: captura propiedades comunes de los objetos

> Combinando ambos tipos de jerarquías con los cinco atributos de un sistema complejo se pueden definir todos los sistemas complejos de forma canónica.
>
> **Se denomina arquitectura de un sistema a la estructura de clases y objetos.**

### Limitaciones humanas para lidiar con la complejidad

- La complejidad desorganizada es la que se presenta antes de determinar las abstracciones y jerarquías
- En los sistemas discretos hay que enfrentarse con un espacio de estados grande, intrincado y a veces no determinista
- Una persona no puede dominar todos los detalles de un sistema complejo
- La complejidad se organizará según el **modelo de objetos**

---

## 3. Complejidad organizada

Para dominar la complejidad se usan tres mecanismos fundamentales:

### Descomposición

> *Divide et impera* (divide y vencerás)

**Descomposición algorítmica**: enfoca el problema en el orden de los eventos (diseño estructurado descendente).

**Descomposición orientada a objetos**: identifica objetos que hacen cosas cuando reciben mensajes.

**¿Algorítmica u OO?**
- La visión algorítmica resalta el orden de los eventos
- La visión orientada a objetos enfatiza los agentes que causan o padecen acciones
- **Ambas visiones son importantes y complementarias**

### Abstracción

- Permite ignorar los detalles no esenciales de un objeto complejo
- Permite modelos generalizados e idealizados de objetos
- Reduce la complejidad
- Los objetos son abstracciones del mundo real que agrupan información

### Jerarquía

- Reconocer explícitamente las jerarquías de clases y objetos incrementa el contenido semántico de la información del sistema
- La **jerarquía de objetos** ilustra cómo colaboran entre sí a través de patrones de interacción denominados **mecanismos**
- La **jerarquía de clases** resalta la estructura y los comportamientos comunes en el interior del sistema
- **La jerarquía de clases y objetos incluye herencia y composición**

---

### Conceptos clave de diseño modular

Estos conceptos son transversales a toda la unidad y al diseño de software en general:

| Concepto | Definición |
|----------|------------|
| **Modularización** | Propiedad que permite subdividir una aplicación en partes más pequeñas (módulos), cada una tan independiente como sea posible. Según Bertrand Meyer: *"crea una serie de límites bien definidos y documentados en el programa"* |
| **Cohesión** | Medida de qué tan relacionadas están las responsabilidades de un módulo entre sí. **Alta cohesión = mejor diseño**: módulos simples, más fáciles de mantener y reutilizar |
| **Acoplamiento** | Medida de la interdependencia entre módulos. **Bajo acoplamiento = mejor diseño**: cambios en un módulo impactan menos en otros |
| **Independencia funcional** | Módulos con propósito bien definido y acotado. No debería haber módulos que realicen funciones similares a otros |
| **Reutilización de código** | Generar componentes con propósitos bien definidos que puedan usarse en distintas partes del sistema. Evita duplicar código y mejora la mantenibilidad |
| **Ortogonalidad** | Capacidad de combinar características de un lenguaje en todas las combinaciones posibles de forma que todas tengan significado. Ejemplo en Java: `==` puede comparar cualquier tipo |

> **Regla de oro**: Alta cohesión + Bajo acoplamiento = sistema bien estructurado y buen diseño de software.

**Ventajas de la cohesión alta:**
- Reducción de complejidad de módulo (son más simples, con menor número de operaciones)
- Mejor mantenimiento: los cambios en el dominio afectan a un menor número de módulos
- Aumento de la reutilización: el componente que se necesita es más visible y fácil de usar

---

## 4. Principios SOLID

Los principios SOLID son cinco reglas de diseño orientado a objetos que guían la construcción de sistemas **mantenibles, extensibles y robustos**. Son el fundamento teórico que explica por qué los patrones de diseño están diseñados como están.

| Principio | Nombre | Definición |
|-----------|--------|------------|
| **S** | Single Responsibility | Una clase debe tener una única razón para cambiar (una sola responsabilidad) |
| **O** | Open/Closed | Las entidades deben estar abiertas a extensión pero cerradas a modificación |
| **L** | Liskov Substitution | Los objetos de una subclase deben poder reemplazar a los de la clase padre sin alterar el comportamiento del programa |
| **I** | Interface Segregation | Es mejor tener muchas interfaces específicas que una sola interfaz general |
| **D** | Dependency Inversion | Depender de abstracciones, no de implementaciones concretas |

### Ejemplos rápidos en Java

**S - Single Responsibility:**
```java
// MAL: una clase hace dos cosas
class Reporte {
    void calcularTotales() { ... }
    void imprimirPDF() { ... }  // responsabilidad ajena
}

// BIEN: separar responsabilidades
class Reporte { void calcularTotales() { ... } }
class ReportePDF { void imprimir(Reporte r) { ... } }
```

**O - Open/Closed:**
```java
// Agregar nuevo comportamiento extendiendo, sin modificar la clase base
abstract class Forma { abstract double area(); }
class Circulo extends Forma { double area() { return Math.PI * r * r; } }
class Rectangulo extends Forma { double area() { return base * altura; } }
```

**L - Liskov Substitution:**
```java
// Cualquier subclase debe poder usarse donde se espera la superclase
Forma f = new Circulo();   // funciona
Forma f = new Rectangulo(); // también funciona
```

**I - Interface Segregation:**
```java
// MAL: interfaz demasiado grande
interface Animal { void comer(); void volar(); void nadar(); }

// BIEN: interfaces específicas
interface Comedor { void comer(); }
interface Volador { void volar(); }
interface Nadador { void nadar(); }
class Pato implements Comedor, Volador, Nadador { ... }
```

**D - Dependency Inversion:**
```java
// MAL: depende de la implementación concreta
class Servicio { MySQLDatabase db = new MySQLDatabase(); }

// BIEN: depende de una abstracción
class Servicio { Database db; Servicio(Database db) { this.db = db; } }
```

> Los patrones de diseño GoF son, en gran medida, aplicaciones concretas de estos principios.

---

## 5. Introducción a Java

### ¿Qué es Java?

Es un lenguaje de programación de alto nivel, compilado, creado por **Sun Microsystems**. Permite crear aplicaciones en una variedad de plataformas, desde dispositivos móviles hasta servidores. El código se ejecuta sobre una **máquina virtual** (JVM), que interpreta los archivos `.class` y genera el código de CPU correspondiente.

Sintaxis muy parecida a la de C o C++, e incorpora características propias como gestión de hilos, ejecución remota, etc.

### Instalación

Descargar e instalar **Java 21 - Zulu** desde [azul.com](https://www.azul.com/downloads/).

> Java usa la variable de entorno `JAVA_HOME` para definir el directorio de instalación. Si el instalador no la configura automáticamente, se puede añadir manualmente.

**Verificar la instalación:**
```bash
java -version    # verifica la JVM (ejecución)
javac -version   # verifica el compilador (desarrollo)
```

Ambos comandos deben mostrar la versión 21.x.x.

### JDK, JRE y JVM

| Herramienta | Descripción |
|-------------|-------------|
| **JVM** (Java Virtual Machine) | Interpreta el bytecode (`.class`) y lo ejecuta en el CPU. Es la que hace Java multiplataforma: *"write once, run anywhere"* |
| **JRE** (Java Runtime Environment) | Incluye la JVM + bibliotecas estándar. Solo permite **ejecutar** programas Java |
| **JDK** (Java Development Kit) | Incluye el JRE + compilador (`javac`) + depurador + herramientas de documentación. Permite **desarrollar** en Java |

> El JDK es el superconjunto: contiene todo lo necesario para desarrollar y ejecutar aplicaciones Java. Para este curso siempre se instala el JDK.

### Vendors y versión recomendada

Un **vendor** es una empresa que proporciona una implementación Java con características específicas (JVM, bibliotecas estándar, herramientas de desarrollo). Las versiones de Java salen cada 6 meses.

| Vendor | Descripción |
|--------|-------------|
| Oracle | Vendor original de Java |
| OpenJDK | Implementación de código abierto |
| IBM | Implementación IBM |
| Red Hat | Implementación Red Hat |
| AWS Corretto | Implementación de Amazon Web Services |
| **Zulu (Azul)** | Fork de OpenJDK — **el que usamos en la materia** |

> **La versión con la que trabajamos es JAVA 21 - Zulu** (versión LTS: Long Term Support).

### IDEs recomendados

| IDE | Observación |
|-----|-------------|
| **IntelliJ IDEA (Community)** | Recomendado para la materia |
| Eclipse | Alternativa popular |
| BlueJ | Bueno para aprender |
| Netbeans | Alternativa completa |

### Componentes del lenguaje

**Paquetes**: equivalentes a los `using` de C#, permiten utilizar clases en otras y llamarlas de forma abreviada.
```java
import java.util.*;
```

**Clases**:
```java
public class MiClase {}
```

**Campos**: Constantes, variables y elementos de información.
```java
public int a;
```

**Métodos**: Para las funciones que devuelvan algún tipo de valor, es imprescindible colocar una sentencia `return`.
```java
public void imprimirA() { }
```

**Constructores**: Un tipo de método que siempre tiene el mismo nombre que la clase. Se pueden definir uno o varios.
```java
MiClase miClase = new MiClase();
```

> No hay que preocuparse de liberar la memoria del objeto al dejar de utilizarlo. Esto lo hace automáticamente el **garbage collector**.

**Herencia**: Se utiliza la palabra `extends`.
```java
class Pato extends Animal { }
```

**Paquetes** (namespaces):
```java
package paquete1.subpaquete1;
public class MiClase1_1 { }
```

### Objetivos mínimos con Java en esta unidad

Al finalizar deberías poder:
1. Pedir datos por pantalla
2. Hacer transformaciones de tipos (string a bool, string a int)
3. Hacer procesamientos y validaciones
4. Aplicar ciclos de distintos tipos (`for`, `while`, `for-each`)
5. Imprimir resultados

---

## 6. Gestores de dependencias

### ¿Qué es un gestor de dependencias?

Una herramienta que permite administrar las librerías externas que utiliza un proyecto Java. En lugar de manejar archivos `.jar` manualmente, el desarrollador:
- **Declara** qué necesita (dependencias)
- El sistema se encarga de descargarlas, versionarlas y agregarlas al proyecto

**Funciones principales:**
- Descargar librerías automáticamente
- Gestionar versiones y resolver conflictos entre dependencias
- Resolver dependencias transitivas
- Automatizar el build del proyecto (compilar, testear, empaquetar)
- Reproducibilidad: mismo resultado en cualquier máquina

### Dependencias

Una **dependencia** es una librería externa que el proyecto utiliza para resolver un problema específico.

Ejemplos comunes:
- Manejo de JSON → Jackson / Gson
- APIs REST → Spring Boot
- Acceso a base de datos → Hibernate

> **Permite no reinventar la rueda, usar código de propósito general en múltiples proyectos.**

### Dependencias transitivas

Una dependencia puede necesitar otras dependencias. El gestor resuelve toda la cadena automáticamente.

**Ejemplo:**
```
Agrego Spring Boot
  → Spring Boot necesita otras librerías (netty, jackson, etc.)
  → el gestor descarga todas automáticamente
```

Sin gestor: hay que descubrir y descargar todo manualmente.

### Cómo funciona un gestor

**Flujo básico:**
1. El desarrollador define dependencias en un archivo (`pom.xml` o `build.gradle`)
2. El gestor consulta repositorios remotos (ej: Maven Central)
3. Descarga las librerías necesarias y las guarda en caché local
4. Las agrega al proyecto y resuelve dependencias transitivas

Los gestores utilizan repositorios remotos principales:
- **Maven Central** — repositorio público principal
- **Repositorios privados** — usados por empresas para librerías internas

---

### Maven

Uno de los gestores más utilizados en Java.

- Basado en **XML** (`pom.xml`)
- Fuerte uso de convenciones ("convention over configuration")
- Estructura de proyecto estándar
- Amplio uso en entornos corporativos

> **Idea clave: Maven prioriza orden y estandarización. Archivo principal: `pom.xml`**

**Ejemplo `pom.xml`:**
```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
      <version>3.2.0</version>
    </dependency>
  </dependencies>
</project>
```

**Conceptos clave:**
- `groupId` → organización (ej: `org.springframework.boot`)
- `artifactId` → nombre de la librería (ej: `spring-boot-starter-web`)
- `version` → versión específica (ej: `3.2.0`)

**Ciclo de vida (lifecycle):**
```
compile  → compilar código
test     → ejecutar tests
package  → generar artefacto (jar/war)
install  → instalar localmente
```

**Uso típico:**
```bash
mvn clean install   # limpia, compila, testea y genera el .jar
mvn test            # ejecutar tests
mvn compile         # solo compilar
```

---

### Gradle

Gestor moderno orientado a flexibilidad y performance.

- Usa **DSL** (Groovy o Kotlin)
- Configuración más expresiva y concisa
- Builds más rápidos (incrementales: solo ejecuta lo que cambió)
- Muy usado en Android y proyectos modernos

> **Idea clave: Gradle prioriza flexibilidad y performance**

**Ejemplo `build.gradle`:**
```groovy
plugins {
    id 'java'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web:3.2.0'
}
```

**Tareas principales:**
- `compileJava` — compilar
- `test` — ejecutar tests
- `build` — compilar + testear + empaquetar

**Uso típico:**
```bash
gradle build   # compila, testea y genera el .jar
gradle test    # solo ejecutar tests
```

---

### Maven vs Gradle

**Similitudes:**
- Gestionan dependencias automáticamente
- Usan repositorios remotos (Maven Central)
- Soportan build, testing y packaging
- Integran con pipelines CI/CD
- Permiten proyectos reproducibles

**Diferencias:**

| Aspecto | Maven | Gradle |
|---------|-------|--------|
| Configuración | XML | DSL (Groovy/Kotlin) |
| Flexibilidad | Baja | Alta |
| Velocidad | Media | Alta |
| Curva de aprendizaje | Baja | Media |
| Verbosidad | Alta | Baja |

**¿Cuál conviene usar?**

| Elegir Maven si... | Elegir Gradle si... |
|--------------------|---------------------|
| Buscás simplicidad | Proyecto grande |
| Proyecto académico o inicial | Necesitás performance |
| Equipo con poca experiencia | Configuración compleja |
| | Ecosistema Android / moderno |

---

## 7. Patrones

### ¿Qué son los patrones?

Los patrones son **soluciones comprobadas a problemas recurrentes**. Una colección de decisiones que surgen de lecciones aprendidas.

Ofrecen una **forma común** de abordar un problema, lo que permite a los desarrolladores trabajar con mayor **eficiencia y eficacia** y comunicarse con un lenguaje compartido.

**Se utilizan para:**
- **Mejorar la calidad del software**: código fácil de entender, mantener y extender
- **Aumentar la reutilización**: reutilizar soluciones probadas en lugar de reinventar la rueda (DRY)
- **Acelerar el desarrollo**: reducir el tiempo dedicado a problemas ya resueltos
- **Facilitar la comunicación**: un equipo que comparte el vocabulario de patrones se comunica con más precisión

### Patrones de arquitectura vs. patrones de diseño

Es importante distinguir en qué nivel del sistema actúa cada tipo:

| | Patrones de arquitectura | Patrones de diseño |
|--|--------------------------|-------------------|
| **Nivel** | Macro — estructura global del sistema | Micro — soluciones de implementación dentro de un componente |
| **Afectan a** | Cómo se dividen y comunican los grandes módulos | Cómo se relacionan clases y objetos dentro de un módulo |
| **Ejemplos** | MVC, Microservicios, Capas | Singleton, Observer, Factory |
| **Decisión tomada por** | Arquitecto de software | Desarrollador |

---

## 8. Patrones de arquitectura

Los patrones de arquitectura definen la **estructura global** de un sistema: cómo se organizan los grandes componentes y cómo se comunican entre sí.

### Arquitectura en Capas

Divide una aplicación en capas independientes donde cada capa se comunica con la adyacente inmediata.

```
┌─────────────────────────────┐
│   Capa de Interfaz de Usuario│
├─────────────────────────────┤
│      Capa de Negocio         │  ← Capa de Entidades
├─────────────────────────────┤
│      Capa de Datos           │
├─────────────────────────────┤
│       Base de Datos          │
└─────────────────────────────┘
```

**Ejemplo:** una aplicación de comercio electrónico dividida en capa de presentación, capa de lógica empresarial y capa de almacenamiento de datos.

**Ejemplo en el repo:** [`001c-patrones-arquitectura/01-arquitectura-en-capas/`](001c-patrones-arquitectura/01-arquitectura-en-capas/)

---

### Arquitectura Cliente-Servidor

Se utiliza para diseñar aplicaciones distribuidas en las que el **servidor proporciona servicios** y el **cliente los consume**.

**Ejemplo:** una aplicación web donde el servidor provee los datos y el cliente los consume a través de un navegador.

**Ejemplo en el repo:** [`001c-patrones-arquitectura/02-cliente-servidor/`](001c-patrones-arquitectura/02-cliente-servidor/)

---

### Modelo-Vista-Controlador (MVC)

Separa la lógica empresarial de la interfaz de usuario y la gestión de datos.

| Componente | Rol |
|-----------|-----|
| **Modelo** | Datos y lógica empresarial |
| **Vista** | Interfaz de usuario (presenta el estado actual del modelo) |
| **Controlador** | Intermediario (cerebro): controla y decide cómo se muestran los datos |

**Usado en:** aplicaciones web y de escritorio.

**Ejemplo en el repo:** [`001c-patrones-arquitectura/03-mvc/`](001c-patrones-arquitectura/03-mvc/)

---

### Modelo-Vista-Presentador (MVP)

Similar al MVC, pero el **presentador** actúa como intermediario entre la vista y el modelo. El presentador recupera los datos del modelo y los presenta en la vista.

**Diferencia clave con MVC:** la Vista no conoce al Modelo directamente; toda la lógica pasa por el Presentador.

**Usado en:** aplicaciones de escritorio.

**Ejemplo en el repo:** [`001c-patrones-arquitectura/04-mvp/`](001c-patrones-arquitectura/04-mvp/)

---

### Modelo-Vista-ViewModel (MVVM)

Similar al MVP, pero el **ViewModel** actúa como intermediario y utiliza *data binding* para sincronizar la Vista automáticamente cuando cambian los datos.

**Usado en:** aplicaciones de escritorio y móviles.

**Ejemplo en el repo:** [`001c-patrones-arquitectura/05-mvvm/`](001c-patrones-arquitectura/05-mvvm/)

---

### Pub/Sub (Publicador-Suscriptor)

Los componentes se comunican mediante mensajes a través de un canal. Un componente **publica** un mensaje y otros componentes se **suscriben** al canal para recibirlo.

```
Producers → Message Bus → Consumers
```

**Usado en:** aplicaciones de alta escalabilidad.

**Ejemplo en el repo:** [`001c-patrones-arquitectura/06-pub-sub/`](001c-patrones-arquitectura/06-pub-sub/)

---

### Arquitectura Orientada a Servicios (SOA)

Los componentes se exponen como **servicios independientes** y se comunican a través de estándares abiertos (interoperabilidad).

- Servicios independientes y autónomos
- Comunicación simple y estandarizada
- Interfaz estándar con protocolos como **SOAP** o **REST**

**Ejemplo en el repo:** [`001c-patrones-arquitectura/07-soa/`](001c-patrones-arquitectura/07-soa/)

---

### Arquitectura Orientada a Eventos

Los componentes se comunican mediante **eventos**. Emiten eventos cuando ocurren cambios de estado; otros componentes interesados se suscriben y actúan en consecuencia.

- Los eventos son independientes de los componentes que los emiten o reciben
- Utilizado en aplicaciones en tiempo real: financieras, monitoreo, control industrial

```
Event Producers → Event Ingestion → Event Consumers
```

**Ejemplo en el repo:** [`001c-patrones-arquitectura/08-orientada-eventos/`](001c-patrones-arquitectura/08-orientada-eventos/)

---

### Arquitectura Basada en Componentes

Se construyen aplicaciones a partir de **componentes reutilizables y autónomos**. Cada componente es una unidad independiente que se puede ensamblar en una aplicación más grande, definido por sus **interfaces**.

**Ejemplo en el repo:** [`001c-patrones-arquitectura/09-basada-componentes/`](001c-patrones-arquitectura/09-basada-componentes/)

---

### Arquitectura Pipeline (Tubería)

Modelo para procesar grandes cantidades de datos en una serie de **pasos secuenciales y consecutivos**. Cada etapa realiza una tarea específica y pasa los resultados a la siguiente.

```
stage 1 → stage 2 → stage 3 → stage 4   (linear pipeline)

stage 1 → stage 2 → stage 3a → stage 4
                  → stage 3b ↗           (nonlinear pipeline)
```

**Ejemplo en el repo:** [`001c-patrones-arquitectura/10-pipeline/`](001c-patrones-arquitectura/10-pipeline/)

---

### Microservicios

Divide una aplicación en **pequeños servicios independientes y escalables**. Cada servicio tiene una responsabilidad única y se comunica con otros mediante una API.

> **Es una especialización del patrón basado en componentes, llevada al extremo.**

**Ejemplo:** una banca en línea dividida en servicios de autenticación, transferencias y pagos.

**Ejemplo en el repo:** [`001c-patrones-arquitectura/11-microservicios/`](001c-patrones-arquitectura/11-microservicios/)

---

## 9. Patrones de diseño

Los **patrones de diseño** (design patterns) son soluciones habituales a problemas comunes en el diseño de clases y objetos. Cada patrón es como un plano que se puede personalizar para resolver un problema particular de tu código.

**Ventajas:**
- Brindan soluciones probadas a problemas habituales
- Definen un lenguaje común que mejora la comunicación del equipo
- Facilitan el mantenimiento y la extensión del código

Los patrones se clasifican en tres grupos según su propósito:

### ¿Cuándo NO usar un patrón?

Un patrón no debe aplicarse si el problema no lo requiere. **Agregar complejidad innecesaria es un antipatrón en sí mismo** (over-engineering). Antes de aplicar un patrón, preguntate:
- ¿El problema que resuelve este patrón existe en mi código?
- ¿La solución simple sin patrón es suficiente?

> Si la respuesta a la segunda pregunta es sí, no uses el patrón.

---

### Creacionales

Se enfocan en **cómo se crean los objetos**. En lugar de instanciar objetos directamente con `new`, estos patrones controlan el proceso de creación.

**¿Para qué sirven?**
- Centralizar la creación de objetos
- Evitar dependencias fuertes entre clases
- Mejorar la flexibilidad del sistema
- Facilitar cambios en el tipo de objeto creado

**¿Cuándo usarlos?**
- Cuando la creación es compleja
- Cuando hay múltiples tipos de objetos
- Cuando querés desacoplar la instanciación del uso

| Patrón | Descripción |
|--------|-------------|
| [**Factory Method**](https://refactoring.guru/es/design-patterns/factory-method) | Define una interfaz para crear un objeto, pero deja que las subclases decidan qué clase instanciar |
| [**Abstract Factory**](https://refactoring.guru/es/design-patterns/abstract-factory) | Produce familias de objetos relacionados sin especificar sus clases concretas |
| [**Builder**](https://refactoring.guru/es/design-patterns/builder) | Permite construir objetos complejos paso a paso |
| [**Prototype**](https://refactoring.guru/es/design-patterns/prototype) | Permite copiar objetos existentes sin que el código dependa de sus clases |
| [**Singleton**](https://refactoring.guru/es/design-patterns/singleton) | Garantiza que una clase tenga una sola instancia y proporciona un punto de acceso global |

**Ejemplos en el repo:** [`001d-patrones-diseno/01-creacionales/`](001d-patrones-diseno/01-creacionales/)

---

### Estructurales

Se enfocan en **cómo se organizan y combinan las clases y objetos**. Permiten construir estructuras más flexibles sin modificar el código existente.

**¿Para qué sirven?**
- Simplificar relaciones entre clases
- Adaptar interfaces incompatibles
- Reutilizar código sin duplicación
- Mejorar la mantenibilidad

**¿Cuándo usarlos?**
- Cuando hay muchas clases relacionadas
- Cuando querés simplificar el uso de un sistema
- Cuando necesitás integrar código existente

| Patrón | Descripción |
|--------|-------------|
| [**Adapter**](https://refactoring.guru/es/design-patterns/adapter) | Permite la colaboración entre objetos con interfaces incompatibles |
| [**Bridge**](https://refactoring.guru/es/design-patterns/bridge) | Divide una clase grande en dos jerarquías separadas (abstracción e implementación) que pueden desarrollarse independientemente |
| [**Composite**](https://refactoring.guru/es/design-patterns/composite) | Permite componer objetos en estructuras de árbol y trabajar con esas estructuras como si fueran objetos individuales |
| [**Decorator**](https://refactoring.guru/es/design-patterns/decorator) | Permite añadir funcionalidades a objetos colocándolos dentro de objetos encapsuladores especiales |
| [**Facade**](https://refactoring.guru/es/design-patterns/facade) | Proporciona una interfaz simplificada a una biblioteca, framework o grupo complejo de clases |
| [**Flyweight**](https://refactoring.guru/es/design-patterns/flyweight) | Permite mantener más objetos en RAM compartiendo partes comunes del estado entre ellos |
| [**Proxy**](https://refactoring.guru/es/design-patterns/proxy) | Proporciona un sustituto de otro objeto para controlar el acceso a él |

**Ejemplos en el repo:** [`001d-patrones-diseno/02-estructurales/`](001d-patrones-diseno/02-estructurales/)

---

### Comportamiento

Se enfocan en **cómo interactúan y se comunican los objetos**. Definen responsabilidades y flujos de ejecución.

**¿Para qué sirven?**
- Controlar la comunicación entre objetos
- Desacoplar emisores y receptores
- Definir comportamientos dinámicos e intercambiables
- Mejorar la extensibilidad

**¿Cuándo usarlos?**
- Cuando hay múltiples formas de ejecutar algo
- Cuando querés desacoplar lógica
- Cuando necesitás eventos o notificaciones

| Patrón | Descripción |
|--------|-------------|
| [**Chain of Responsibility**](https://refactoring.guru/es/design-patterns/chain-of-responsibility) | Permite pasar solicitudes a lo largo de una cadena de manejadores |
| [**Command**](https://refactoring.guru/es/design-patterns/command) | Convierte una solicitud en un objeto independiente que contiene toda la información sobre dicha solicitud |
| [**Iterator**](https://refactoring.guru/es/design-patterns/iterator) | Permite recorrer elementos de una colección sin exponer su representación subyacente |
| [**Mediator**](https://refactoring.guru/es/design-patterns/mediator) | Reduce las dependencias caóticas entre objetos mediante un objeto mediador |
| [**Memento**](https://refactoring.guru/es/design-patterns/memento) | Permite guardar y restaurar el estado previo de un objeto sin revelar los detalles de su implementación |
| [**Observer**](https://refactoring.guru/es/design-patterns/observer) | Define un mecanismo de suscripción para notificar a múltiples objetos sobre eventos |
| [**State**](https://refactoring.guru/es/design-patterns/state) | Permite a un objeto alterar su comportamiento cuando su estado interno cambia |
| [**Strategy**](https://refactoring.guru/es/design-patterns/strategy) | Permite definir una familia de algoritmos, encapsular cada uno y hacerlos intercambiables |
| [**Template Method**](https://refactoring.guru/es/design-patterns/template-method) | Define el esqueleto de un algoritmo en la superclase pero permite que las subclases sobreescriban pasos específicos |
| [**Visitor**](https://refactoring.guru/es/design-patterns/visitor) | Permite separar algoritmos de los objetos sobre los que operan |

**Ejemplos en el repo:** [`001d-patrones-diseno/03-comportamiento/`](001d-patrones-diseno/03-comportamiento/)

---

### Recurso de referencia

Para ver detalles de implementación de cada patrón de diseño con ejemplos en múltiples lenguajes:

> **Refactoring Guru** (disponible en español): referencia completa con explicaciones y ejemplos de todos los patrones GoF.

---

## 10. Ejercicios y material de la unidad

| Carpeta | Descripción |
|---------|-------------|
| [`001c-patrones-arquitectura/`](001c-patrones-arquitectura/) | 11 patrones de arquitectura con ejemplos y guías de implementación |
| [`001d-patrones-diseno/`](001d-patrones-diseno/) | 23 patrones de diseño (creacionales, estructurales y de comportamiento) con ejemplos |
| [`001a-ejercicios-java/`](001a-ejercicios-java/) | Ejercicios introductorios de Java |
| [`001b-ejercicios-gestores/`](001b-ejercicios-gestores/) | Ejercicios de Maven y Gradle |
| [`001e-ejercicio-patrones/`](001e-ejercicio-patrones/) | Ejercicio integrador de patrones de diseño |

Cada subcarpeta contiene ejemplos distintos y un `README.md` que indica qué archivos implementar en cada caso.

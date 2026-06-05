# Unidad 4 — Otros paradigmas de programación

Material de apoyo para la **Unidad 5** de **Programación 2** — Ingeniería en Computación (UCSE).

---

## ¿Por qué otros paradigmas?

Durante la carrera aprendemos principalmente **programación orientada a objetos** (Java) y **programación imperativa**. Sin embargo, existen otros enfoques para resolver problemas que en ciertos contextos son más naturales, expresivos y poderosos.

```
Problema
   │
   ├── ¿Cómo ejecuto paso a paso?      → Imperativo / OOP
   │
   ├── ¿Qué transformación necesito?   → Funcional
   │
   └── ¿Qué relaciones se cumplen?     → Lógico
```

| Paradigma | Foco | Pregunta clave | Ejemplos |
|-----------|------|---------------|----------|
| **Imperativo** | Secuencia de instrucciones | *¿Cómo lo hago paso a paso?* | C, Pascal |
| **Orientado a objetos** | Objetos que colaboran | *¿Quién hace qué?* | Java, Python, C++ |
| **Funcional** | Transformación de datos sin estado mutable | *¿Qué quiero obtener?* | Haskell, Scala, Java 8+ |
| **Lógico** | Hechos, reglas y deducción | *¿Qué es verdad dado lo que sé?* | Prolog, Datalog |

Aprender múltiples paradigmas hace al programador más flexible: le da más herramientas para atacar diferentes tipos de problemas.

---

## Contenido de esta unidad

### 1. [Programación Funcional con Java](01-programacion-funcional-java.md)

Java incorporó características funcionales desde la versión 8 (2014). Hoy es uno de los estilos más usados para procesar colecciones de datos en Java.

Temas:
- Paradigma funcional: funciones puras, inmutabilidad, transparencia referencial
- **Expresiones Lambda** — la base de todo
- **Interfaces funcionales**: `Predicate`, `Function`, `Consumer`, `Supplier`
- **Method References** — lambdas más concisas
- **Optional\<T\>** — evitar null de forma elegante
- **Stream API** — procesar colecciones con `filter`, `map`, `reduce`, `collect`
- Comparación imperativo vs funcional con ejemplos reales
- Ejercicios prácticos

### 2. [Programación Lógica con Prolog](02-programacion-logica.md)

Prolog es el lenguaje paradigmático de la programación lógica. Se usa en inteligencia artificial, procesamiento de lenguaje natural y sistemas expertos.

Temas:
- Paradigma lógico: hechos, reglas, consultas
- Sintaxis de Prolog
- Unificación y backtracking
- Recursión sobre listas
- Cómo ejecutar Prolog con Docker
- Ejercicios prácticos

---

## Recursos generales

- [Programación Funcional en Java — Oracle Docs](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html)
- [Baeldung — Java Functional Interfaces](https://www.baeldung.com/java-8-functional-interfaces)
- [Supertutorial de Prolog en español](https://blog.adrianistan.eu/supertutorial-prolog/)
- [SWI-Prolog (intérprete online)](https://swish.swi-prolog.org/)

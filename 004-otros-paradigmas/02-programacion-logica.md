# Programación Lógica con Prolog

Material de apoyo para **Programación 2** — Ingeniería en Computación (UCSE).

---

## Índice

1. [¿Qué es la programación lógica?](#1-qué-es-la-programación-lógica)
2. [Prolog — características principales](#2-prolog--características-principales)
3. [Conceptos fundamentales](#3-conceptos-fundamentales)
4. [Hechos](#4-hechos)
5. [Reglas](#5-reglas)
6. [Consultas](#6-consultas)
7. [Variables y Unificación](#7-variables-y-unificación)
8. [Backtracking — búsqueda automática](#8-backtracking--búsqueda-automática)
9. [Recursión](#9-recursión)
10. [Listas](#10-listas)
11. [Ejecutar con Docker](#11-ejecutar-con-docker)
12. [Ejercicios](#12-ejercicios)

---

## 1. ¿Qué es la programación lógica?

La **programación lógica** es un paradigma donde los programas se expresan como un conjunto de **hechos** y **reglas** lógicas, y la computación consiste en **deducir** respuestas a consultas a partir de esa base de conocimiento.

En lugar de decirle a la computadora *cómo* resolver un problema, le decimos *qué sabemos* y *qué relaciones existen*, y ella deduce las respuestas.

```
                    ┌──────────────────────────┐
                    │     Base de conocimiento  │
                    │                          │
                    │  animal(leon).           │
                    │  animal(ballena).        │
                    │  carnivoro(leon).        │
                    │                          │
                    │  depredador(X) :-        │
                    │    carnivoro(X).         │
                    └──────────────────────────┘
                              │
              Consulta: ?- depredador(leon).
                              │
                              ▼
                           true
```

### ¿Para qué se usa?

| Área | Ejemplo |
|------|---------|
| **Inteligencia Artificial** | Sistemas expertos, motores de inferencia |
| **Bases de datos deductivas** | Razonamiento sobre datos relacionales |
| **Procesamiento de lenguaje natural** | Parsers, gramáticas |
| **Verificación formal** | Probar propiedades de sistemas |
| **Juegos y puzzles** | Resolver Sudoku, crucigramas, laberintos |

---

## 2. Prolog — características principales

**Prolog** (PROgramming in LOGic) fue creado en 1972 por Alain Colmerauer. Es el lenguaje de programación lógica más conocido y difundido.

```
┌─────────────────────────────────────────────────────────────┐
│                    Características de Prolog                 │
│                                                             │
│  ✔ Declarativo: describís qué es verdad, no cómo computarlo│
│  ✔ Basado en reglas: hechos + reglas lógicas               │
│  ✔ Búsqueda automática: Prolog busca las soluciones solo    │
│  ✔ Backtracking: si falla, vuelve atrás y prueba otra vía   │
│  ✔ Unificación: mecanismo central para hacer "match"        │
└─────────────────────────────────────────────────────────────┘
```

### Implementaciones

La implementación más usada en la actualidad es **SWI-Prolog** (de código abierto, multiplataforma).

---

## 3. Conceptos fundamentales

Prolog trabaja con **términos**. Todo en Prolog es un término:

| Tipo de término | Sintaxis | Ejemplos |
|----------------|---------|---------|
| **Átomo** | minúsculas, o entre comillas simples | `leon`, `'Buenos Aires'`, `true` |
| **Número** | entero o decimal | `42`, `3.14` |
| **Variable** | comienza con Mayúscula o `_` | `X`, `Animal`, `_` |
| **Estructura / functor** | `nombre(arg1, arg2, ...)` | `persona(juan, 30)`, `punto(1, 2)` |

> **Regla clave**: en Prolog, las **variables empiezan con Mayúscula** y los **átomos con minúscula**.

```prolog
juan.          % átomo
Juan.          % variable (¡diferente!)
persona(juan). % estructura/hecho con un argumento
persona(X).    % estructura con variable
```

---

## 4. Hechos

Los **hechos** son afirmaciones sobre el mundo que se consideran verdaderas. Son la base del conocimiento.

### Sintaxis

```prolog
predicado(argumento1, argumento2, ...).
```

El punto `.` al final es **obligatorio**.

### Ejemplos

```prolog
% Hechos sobre animales
animal(leon).
animal(ballena).
animal(aguila).

% Hechos con múltiples argumentos
padre(juan, pedro).       % juan es padre de pedro
madre(maria, pedro).      % maria es madre de pedro
hermano(pedro, lucia).    % pedro es hermano de lucia

% Hechos con propiedades
color(manzana, rojo).
color(limon, amarillo).
precio(laptop, 1200).
precio(mouse, 25).

% Familia de animales
mamifero(leon).
mamifero(delfin).
ave(aguila).
ave(paloma).
```

---

## 5. Reglas

Las **reglas** definen relaciones derivadas de otras. Una regla dice: "X es verdad **si** Y es verdad".

### Sintaxis

```prolog
cabeza :- cuerpo.
```

- `cabeza`: lo que queremos concluir
- `:-`: se lee "**si**"
- `cuerpo`: las condiciones que deben cumplirse (separadas por `,` = Y lógico)

### Ejemplos básicos

```prolog
% Hechos
humano(socrates).
humano(platon).

% Regla: todo humano es mortal
mortal(X) :- humano(X).

% Consulta: ?- mortal(socrates).  → true
% Consulta: ?- mortal(aristoteles). → false (no está en los hechos)
```

### Reglas con múltiples condiciones

```prolog
% Hechos
padre(carlos, juan).
padre(carlos, maria).
madre(elena, juan).
madre(elena, maria).

% Reglas
progenitor(X, Y) :- padre(X, Y).
progenitor(X, Y) :- madre(X, Y).

hermano(X, Y) :-
    padre(P, X),        % P es padre de X
    padre(P, Y),        % P es padre de Y
    X \= Y.             % X e Y son distintos (\= es "distinto")

abuelo(X, Y) :-
    padre(X, Z),        % X es padre de Z
    progenitor(Z, Y).   % Z es progenitor de Y
```

### Reglas con varias cláusulas (disyunción)

Múltiples cláusulas para el mismo predicado funcionan como OR lógico:

```prolog
% Hechos
carnivoro(leon).
carnivoro(lobo).
herbivoro(vaca).
herbivoro(conejo).

% Mamífero: puede ser carnívoro O herbívoro
mamifero(X) :- carnivoro(X).
mamifero(X) :- herbivoro(X).

% Consulta: ?- mamifero(leon).  → true (por la primera cláusula)
% Consulta: ?- mamifero(vaca).  → true (por la segunda cláusula)
```

---

## 6. Consultas

Las **consultas** le preguntan a Prolog si algo es verdad, o piden los valores que lo hacen verdad.

### En el intérprete

```prolog
?- animal(leon).
% true

?- animal(tiburon).
% false

?- padre(carlos, X).
% X = juan ;
% X = maria ;
% false
```

El `;` significa "siguiente solución". Prolog busca **todas** las soluciones posibles.

### Tipos de consultas

**Verificación** (respuesta sí/no):
```prolog
?- mortal(socrates).
% true
```

**Búsqueda** (¿qué valor satisface esto?):
```prolog
?- padre(carlos, Hijo).
% Hijo = juan ;
% Hijo = maria
```

**Múltiples variables**:
```prolog
?- padre(Padre, Hijo).
% Padre = carlos, Hijo = juan ;
% Padre = carlos, Hijo = maria
```

**Consulta con condiciones**:
```prolog
?- animal(X), mamifero(X).
% Busca animales que también sean mamíferos
```

### Imprimir resultados

```prolog
?- animal(X), write(X), nl, fail.
% leon
% ballena
% aguila
% false
```

- `write(X)`: imprime el valor de X
- `nl`: salto de línea
- `fail`: fuerza backtracking para seguir buscando más soluciones

---

## 7. Variables y Unificación

### Variables

En Prolog las variables son **marcadores de posición** que se instancian (ligan) con valores durante la resolución.

```prolog
% X es una variable libre — puede tomar cualquier valor
?- padre(carlos, X).
% Prolog instancia X = juan (luego X = maria con ;)

% La variable anónima _ se usa cuando el valor no importa
?- padre(_, pedro).  % ¿alguien es padre de pedro?
% true
```

### Unificación

La **unificación** es el proceso por el que Prolog determina si dos términos pueden ser iguales, y qué valores deben tomar las variables para lograrlo.

```prolog
% El operador = intenta unificar
?- X = 5.
% X = 5

?- persona(juan, 30) = persona(X, Y).
% X = juan, Y = 30

?- punto(1, Y) = punto(X, 2).
% X = 1, Y = 2

?- punto(1, 2) = punto(1, 3).
% false (no pueden unificarse)
```

### Aritmética

En Prolog, la evaluación aritmética requiere el operador `is`:

```prolog
?- X is 3 + 4.
% X = 7

?- X is 2 ** 10.
% X = 1024

% = no evalúa, solo unifica
?- X = 3 + 4.
% X = 3+4    (¡es la expresión, no el resultado!)

% Comparación aritmética
?- 3 + 2 =:= 5.  % true   (igualdad aritmética)
?- 3 + 2 =\= 6.  % true   (desigualdad aritmética)
?- 4 > 3.         % true
?- 4 < 3.         % false
```

---

## 8. Backtracking — búsqueda automática

El **backtracking** es el mecanismo por el cual Prolog, cuando falla una vía de búsqueda, **retrocede** y prueba otra alternativa.

### Cómo funciona

```prolog
% Hechos
color(rojo).
color(verde).
color(azul).

% Consulta
?- color(X).
% Prolog prueba: X = rojo → éxito
% Con ; prueba: X = verde → éxito
% Con ; prueba: X = azul  → éxito
% Con ; → false (no hay más)
```

### Ejemplo con árbol de búsqueda

```prolog
padre(tom, bob).
padre(tom, liz).
padre(bob, ann).
padre(bob, pat).

abuelo(X, Z) :- padre(X, Y), padre(Y, Z).
```

Al consultar `?- abuelo(tom, Q)`:

```
abuelo(tom, Q)
    │
    ├─ padre(tom, Y) → Y = bob
    │       │
    │       └─ padre(bob, Z) → Z = ann  ✔  Q = ann
    │                       → Z = pat  ✔  Q = pat
    │
    └─ padre(tom, Y) → Y = liz
            │
            └─ padre(liz, Z) → falla (liz no es padre de nadie)
                             → backtrack
```

```prolog
?- abuelo(tom, Q).
% Q = ann ;
% Q = pat ;
% false
```

### Corte (`!`)

El **corte** (`!`) detiene el backtracking. Cuando Prolog lo encuentra, compromete las elecciones hechas hasta ese punto y no retrocede más allá del predicado que lo contiene.

```prolog
% Sin corte: busca todos los precios
precio_maximo(X, Precio) :-
    precio(X, Precio),
    \+ (precio(X, P2), P2 > Precio).

% Con corte: toma el primero que encuentra y para
clasificar(X, caro) :- precio(X, P), P > 1000, !.
clasificar(X, normal) :- precio(X, P), P > 100, !.
clasificar(X, barato).
```

---

## 9. Recursión

La recursión es la única forma de hacer "iteración" en Prolog. Se define un **caso base** y un **caso recursivo**.

### Factorial

```prolog
% Caso base
factorial(0, 1) :- !.

% Caso recursivo
factorial(N, F) :-
    N > 0,
    N1 is N - 1,
    factorial(N1, F1),
    F is N * F1.

% Uso:
% ?- factorial(5, F).
% F = 120
```

### Fibonacci

```prolog
fibonacci(0, 0) :- !.
fibonacci(1, 1) :- !.
fibonacci(N, F) :-
    N > 1,
    N1 is N - 1,
    N2 is N - 2,
    fibonacci(N1, F1),
    fibonacci(N2, F2),
    F is F1 + F2.

% ?- fibonacci(7, F).
% F = 13
```

### Camino en un grafo

```prolog
% Hechos: aristas del grafo
arista(a, b).
arista(b, c).
arista(c, d).
arista(a, d).

% Camino directo
camino(X, Y) :- arista(X, Y).

% Camino indirecto (recursivo)
camino(X, Y) :-
    arista(X, Z),
    camino(Z, Y).

% ?- camino(a, d).
% true (directo por arista(a,d), o por a→b→c→d)
```

### Clasificación de animales (ejemplo del slide)

```prolog
% Hechos
mamifero(animal1).
tiene_pezuñas(animal1).
tiene_rayas_negras(animal1).

% Reglas
ungulado(X) :- mamifero(X), tiene_pezuñas(X).

cebra(X) :- ungulado(X), tiene_rayas_negras(X).

% Consulta
% ?- cebra(animal1).
% true
```

---

## 10. Listas

Las **listas** son la estructura de datos principal en Prolog.

### Sintaxis

```prolog
[]             % lista vacía
[1, 2, 3]      % lista con tres elementos
[a, b, c, d]   % lista con átomos
[H | T]        % cabeza H y cola T (pattern matching)
```

### Pattern matching en listas

```prolog
% Descomponer una lista
?- [H | T] = [1, 2, 3, 4].
% H = 1
% T = [2, 3, 4]

?- [H1, H2 | T] = [a, b, c, d].
% H1 = a
% H2 = b
% T = [c, d]

?- [H | _] = [manzana, pera, naranja].
% H = manzana  (solo nos importa la cabeza)
```

### Predicados sobre listas

#### Verificar si un elemento pertenece a la lista

```prolog
% miembro/2: ¿X pertenece a la lista L?
miembro(X, [X | _]).              % X es la cabeza → sí
miembro(X, [_ | T]) :- miembro(X, T). % buscar en la cola

% Uso:
% ?- miembro(2, [1, 2, 3]).   → true
% ?- miembro(5, [1, 2, 3]).   → false
% ?- miembro(X, [a, b, c]).   → X = a ; X = b ; X = c
```

#### Longitud de una lista

```prolog
longitud([], 0).
longitud([_ | T], N) :-
    longitud(T, N1),
    N is N1 + 1.

% ?- longitud([a, b, c, d], N).
% N = 4
```

#### Concatenar listas

```prolog
% append/3: AppendAB es la concatenación de A y B
append([], B, B).
append([H | T], B, [H | R]) :- append(T, B, R).

% ?- append([1, 2], [3, 4], L).
% L = [1, 2, 3, 4]

% También puede usarse para dividir:
% ?- append(X, Y, [1, 2, 3]).
% X = [], Y = [1, 2, 3] ;
% X = [1], Y = [2, 3] ;
% ...
```

#### Sumar los elementos de una lista

```prolog
suma_lista([], 0).
suma_lista([H | T], S) :-
    suma_lista(T, S1),
    S is S1 + H.

% ?- suma_lista([1, 2, 3, 4, 5], S).
% S = 15
```

#### Revertir una lista

```prolog
revertir([], []).
revertir([H | T], R) :-
    revertir(T, RT),
    append(RT, [H], R).

% ?- revertir([1, 2, 3], R).
% R = [3, 2, 1]
```

### Predicados de listas ya incluidos en SWI-Prolog

| Predicado | Uso |
|-----------|-----|
| `length(L, N)` | Longitud de L es N |
| `append(L1, L2, L3)` | L3 = L1 + L2 |
| `member(X, L)` | X pertenece a L |
| `nth0(I, L, E)` | Elemento en índice I (base 0) |
| `nth1(I, L, E)` | Elemento en índice I (base 1) |
| `last(L, X)` | X es el último elemento de L |
| `reverse(L, R)` | R es L al revés |
| `msort(L, S)` | Ordena L en S (mantiene duplicados) |
| `sort(L, S)` | Ordena L en S (elimina duplicados) |
| `flatten(L, F)` | Aplana listas anidadas |
| `sumlist(L, S)` | Suma los elementos de L |
| `max_list(L, M)` | Máximo de la lista |

---

## 11. Ejecutar con Docker

### Abrir el intérprete de SWI-Prolog

```bash
docker run -it --rm swipl
```

Esto abre el intérprete interactivo donde se pueden escribir hechos, reglas y consultas.

### Trabajar con un archivo

**1. Crear el archivo** `base.pl` localmente:

```prolog
% base.pl
animal(leon).
animal(ballena).
animal(aguila).

mamifero(leon).
mamifero(ballena).
ave(aguila).

carnivoro(leon).

depredador(X) :- carnivoro(X).
```

**2. Montar el archivo en el contenedor y cargar**:

```bash
docker run -it --rm -v $(pwd):/app swipl swipl /app/base.pl
```

**3. Comandos dentro del intérprete**:

```prolog
% Cargar un archivo (si no se cargó al inicio)
?- consult('/app/base.pl').

% Ver todos los hechos y reglas
?- listing.

% Ver solo un predicado
?- listing(animal).

% Hacer consultas
?- depredador(X).
% X = leon

% Ver todos los animales
?- animal(X), write(X), nl, fail ; true.

% Salir
?- halt.
```

### Dockerfile para ejecutar un archivo Prolog

```dockerfile
FROM swipl:latest

WORKDIR /app
COPY programa.pl .

CMD ["swipl", "-g", "halt", "programa.pl"]
```

Para ejecutar consultas al inicio, usar `-g`:

```dockerfile
CMD ["swipl", "-g", "consult('programa.pl'), depredador(X), write(X), nl, halt"]
```

---

## 12. Ejercicios

### Ejercicio 1 — Base de conocimiento básica

Crear una base de conocimiento `familia.pl` con:

**Hechos**:
- `padre(roberto, carlos)` — Roberto es padre de Carlos
- `padre(roberto, ana)`
- `padre(carlos, pedro)`
- `madre(claudia, carlos)`
- `madre(claudia, ana)`
- `madre(ana, lucia)`

**Reglas**:
1. `progenitor(X, Y)` — X es progenitor de Y (padre o madre)
2. `hermano(X, Y)` — X e Y son hermanos (mismo padre, distintas personas)
3. `abuelo(X, Y)` — X es abuelo de Y
4. `descendiente(X, Y)` — X es descendiente de Y (recursiva)

**Consultas a probar**:
```prolog
?- hermano(carlos, ana).
?- abuelo(roberto, pedro).
?- descendiente(lucia, roberto).
?- progenitor(X, pedro).
```

---

### Ejercicio 2 — Clasificación de animales

Crear `animales.pl` con la siguiente base de conocimiento:

**Hechos**:
```prolog
tiene_pelo(leon).
tiene_pelo(tigre).
tiene_pelo(jirafa).
da_leche(leon).
da_leche(tigre).
tiene_plumas(aguila).
tiene_plumas(pinguino).
vuela(aguila).
come_carne(leon).
come_carne(tigre).
```

**Reglas a construir**:
1. `mamifero(X)` — si tiene pelo y da leche
2. `ave(X)` — si tiene plumas
3. `depredador(X)` — si come carne
4. `peligroso(X)` — si es depredador y es mamífero

**Consulta objetivo**: listar todos los animales peligrosos.

---

### Ejercicio 3 — Listas

Implementar en Prolog:

1. `ultimo(L, X)` — X es el último elemento de la lista L
2. `penultimo(L, X)` — X es el penúltimo elemento
3. `nth(N, L, X)` — X es el N-ésimo elemento (base 1)
4. `contar(X, L, N)` — N es la cantidad de veces que X aparece en L
5. `sin_duplicados(L, R)` — R es L sin elementos repetidos

**Pruebas**:
```prolog
?- ultimo([1, 2, 3, 4], X).        % X = 4
?- penultimo([a, b, c, d], X).     % X = c
?- nth(2, [a, b, c], X).           % X = b
?- contar(a, [a, b, a, c, a], N).  % N = 3
?- sin_duplicados([1, 2, 1, 3, 2], R). % R = [1, 2, 3]
```

---

### Ejercicio 4 — Puzzle lógico

> "María, Juan y Carlos tienen diferentes profesiones: médico, ingeniero y artista. Sabemos que María no es ingeniero. Juan no es artista. ¿Quién es quién?"

Modelar el puzzle en Prolog:

```prolog
% Una posible estructura:
profesion(maria, P1), profesion(juan, P2), profesion(carlos, P3),
distinto(P1, P2), distinto(P2, P3), distinto(P1, P3),
miembro(P1, [medico, ingeniero, artista]),
...
```

Consulta: `?- profesiones(Maria, Juan, Carlos).`

---

## Comparación de paradigmas

| | Imperativo (Java) | Funcional (Java) | Lógico (Prolog) |
|--|-------------------|-----------------|-----------------|
| **Cómo se programa** | Instrucciones paso a paso | Composición de funciones | Declaración de hechos y reglas |
| **Control de flujo** | `if`, `for`, `while` | `map`, `filter`, recursión | Backtracking automático |
| **Estado** | Variables mutables | Inmutabilidad preferida | No hay estado mutable |
| **Resultado** | Calculado explícitamente | Transformado | Deducido |
| **Mejor para** | Sistemas, GUIs, APIs | Procesamiento de datos | IA, puzzles, deducción |

---

## Referencias

- [Supertutorial de Prolog en español — Adrianistán](https://blog.adrianistan.eu/supertutorial-prolog/)
- [Tutorial Prolog — Universidad de Colombia (PDF)](https://ferestrepoca.github.io/paradigmas-de-programacion/proglogica/tutoriales/prolog-gh-pages/TutorialProlog2017I.pdf)
- [SWI-Prolog Online (SWISH)](https://swish.swi-prolog.org/)
- [SWI-Prolog Documentación oficial](https://www.swi-prolog.org/pldoc/man?section=overview)
- [Hechos, Reglas y Consultas — Campus Empresa](https://campusempresa.com/cursos/prolog/01-05-facts-rules-queries)

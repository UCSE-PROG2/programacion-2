# Clase 2 — Estado, eventos y formularios controlados

## Por qué hace falta "estado"

Una función de React, por sí sola, siempre devuelve lo mismo para los mismos `props` — es una función pura. Para que la UI **cambie con el tiempo** (un contador que sube, un input que se llena, una lista que crece) React necesita un lugar donde guardar esos valores que cambian y una forma de decirle "esto cambió, volvé a renderizar". Eso es el **estado**, y el hook para manejarlo es `useState`.

```tsx
import { useState } from "react";

function Contador() {
  const [valor, setValor] = useState<number>(0);
  //     ^valor actual      ^función para actualizarlo   ^tipo del estado

  return (
    <button onClick={() => setValor(valor + 1)}>
      Clickeado {valor} veces
    </button>
  );
}
```

`useState<number>(0)` inicializa el estado en `0` y fija su tipo en `number` — TypeScript ya no deja asignarle un `string` más adelante. La sintaxis `<number>` es un genérico, igual que `List<Integer>` en Java. Llamar a `setValor` no muta `valor` in-place: le pide a React que vuelva a ejecutar el componente con el nuevo valor, y ahí es donde el DOM se actualiza (ver Clase 1 — "React vs. manipulación manual del DOM").

> **Concepto clave — inferencia de tipos**: el `<Tipo>` explícito no siempre hace falta. `useState("")` ya queda tipado como `string` sin escribir `useState<string>("")`, porque TypeScript infiere el tipo a partir del valor inicial (mismo mecanismo que `var` en Java). Hace falta ponerlo explícito cuando el valor inicial no alcanza para describir todos los valores futuros: por ejemplo `useState<string | null>(null)` (Clase 3) para un estado que empieza en `null` pero después puede guardar un `string`.

> **Cuidado (el error más común)**: `setValor` es **asíncrono** en cuanto a cuándo el nuevo valor está disponible en `valor` — no esperes que `valor` refleje el cambio en la línea siguiente al `setValor(...)` dentro del mismo evento. Si el nuevo valor depende del anterior, usar la forma funcional: `setValor(v => v + 1)`.

> **Concepto clave — funciones flecha (`=>`)**: `() => setValor(valor + 1)` es una función anónima, el equivalente a un lambda de Java (`() -> setValor(valor + 1)`) pero con `=>` en vez de `->`. Con un solo parámetro, los paréntesis son opcionales (`v => v + 1` es lo mismo que `(v) => v + 1`); con cero o más de uno, van siempre. Si el cuerpo es una única expresión (sin llaves `{ }`), esa expresión es el valor de retorno implícito — por eso `(e) => setTexto(e.target.value)` no necesita `return`. Se puede escribir el manejador como función flecha inline (como acá) o como función nombrada aparte (como en la sección siguiente); ambas formas son válidas, y una función nombrada suele preferirse cuando el cuerpo tiene más de una línea.

## Eventos, tipados

Los manejadores de eventos de React (`onClick`, `onChange`, `onSubmit`, ...) reciben un objeto de evento **sintético** (una envoltura de React sobre el evento nativo del DOM, con la misma API pero normalizada entre navegadores). TypeScript exige tipar ese parámetro según el elemento y el evento:

```tsx
import { useState, type ChangeEvent, type FormEvent } from "react";

function Formulario() {
  const [texto, setTexto] = useState("");

  function manejarCambio(e: ChangeEvent<HTMLInputElement>) {
    setTexto(e.target.value); // e.target es el <input> que disparó el evento; .value, su contenido actual
  }

  function manejarEnvio(e: FormEvent<HTMLFormElement>) {
    e.preventDefault(); // evita el comportamiento por defecto del navegador (recargar la página)
    console.log("enviado:", texto);
  }

  return (
    <form onSubmit={manejarEnvio}>
      <input value={texto} onChange={manejarCambio} />
      <button type="submit">Enviar</button>
    </form>
  );
}
```

> **Concepto clave**: `ChangeEvent`, `FormEvent`, etc. son tipos que hay que importar de `"react"` (como en la línea `import ... type ChangeEvent ...` de arriba) — es la misma convención que usan `LoginPage.tsx` y `RegistroPage.tsx` en el ejemplo de la unidad. Es habitual verlos también escritos como `React.ChangeEvent<...>`, calificados con el namespace `React`; para eso hace falta `import type React from "react"` en vez del import nombrado. Ambas formas son equivalentes — esta unidad usa la forma con import nombrado.

> **Concepto clave**: `HTMLInputElement`, `HTMLFormElement` y `HTMLButtonElement` son tipos que ya vienen definidos por TypeScript para describir al DOM del navegador — no hace falta instalarlos ni importarlos aparte, TypeScript los conoce de fábrica. Cada uno determina qué propiedades expone `e.target` (un `<input>` tiene `.value`; un `<form>`, no).

| Tipo de evento | Cuándo se usa |
|---|---|
| `ChangeEvent<HTMLInputElement>` | `onChange` de un `<input>` |
| `FormEvent<HTMLFormElement>` | `onSubmit` de un `<form>` |
| `MouseEvent<HTMLButtonElement>` | `onClick` de un `<button>` (importado igual, de `"react"` — no confundir con el `MouseEvent` global del navegador) |

## Formularios controlados

Un input es **controlado** cuando su valor en pantalla viene siempre del estado de React (`value={texto}`), y cada tecleo actualiza ese estado (`onChange`) — React es la única fuente de verdad sobre "qué hay escrito ahí", nunca el DOM por su cuenta. Es el patrón que se usa en toda la unidad (y en el ejemplo — login y registro son formularios controlados).

```tsx
function LoginFormulario() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  return (
    <form>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />
    </form>
  );
}
```

> **Concepto clave**: si un `<input>` tiene `value={...}` sin `onChange`, React lo deja de solo lectura — no hay forma de tipear ahí (el input siempre "vuelve" al valor del estado). El par `value` + `onChange` va siempre junto.

## Renderizado condicional

No hay una sentencia `if` dentro del JSX (Clase 1 — "una expresión, no una sentencia"), así que el condicional se resuelve con expresiones de TypeScript:

```tsx
function Estado({ cargando, error }: { cargando: boolean; error: string | null }) {
  if (cargando) return <p>Cargando...</p>;         // early return: la forma más clara para casos excluyentes

  return (
    <div>
      {error && <p className="error">{error}</p>}   {/* && : renderiza el JSX solo si error es "truthy" (JS lo trata como equivalente a true en un condicional) */}
      <p>Contenido normal</p>
    </div>
  );
}
```

| Patrón | Cuándo usarlo |
|---|---|
| `if (...) return <X />;` al principio del componente | Casos que se excluyen entre sí (cargando / con error / con datos) |
| `{condicion && <X />}` | Mostrar algo solo si una condición se cumple, sin alternativa |
| `{condicion ? <X /> : <Y />}` | Elegir entre dos JSX según una condición |

> **Concepto clave**: `{ cargando, error }: { cargando: boolean; error: string | null }` tipa las props igual que la `interface XxxProps` de la Clase 1, pero escribiendo la forma del objeto **inline**, sin ponerle nombre — sirve cuando ese tipo no se va a reutilizar en ningún otro lado. Es azúcar sintáctica (una forma más corta de escribir lo mismo, ver Clase 1 — "JSX"), no un mecanismo distinto: `{ cargando: boolean; error: string | null }` acá cumple exactamente el mismo rol que declarar antes `interface EstadoProps { cargando: boolean; error: string | null }` y usar `(props: EstadoProps)`.

> **Cuidado**: `{cantidad && <p>Hay {cantidad}</p>}` con `cantidad = 0` renderiza literalmente `0` en la pantalla (`0` es "truthy" para el `&&` de JS solo si es distinto de cero, pero acá es exactamente cero, que es falsy — el problema es que en vez de "nada" React igual imprime el `0` en lugar de omitir el JSX). Con valores numéricos que pueden ser `0`, conviene `cantidad > 0 && <p>...</p>` o el ternario.

## Listas y `key`

> **Concepto clave**: `array.map(fn)` (método nativo de los arrays de JS/TS, no de React) devuelve un **array nuevo** con el resultado de aplicar `fn` a cada elemento — igual que `.stream().map(fn)` en Java, pero sin necesidad de `.collect(...)` al final. Es la forma habitual de convertir un array de datos en un array de JSX, uno por elemento.

```tsx
interface Libro { id: string; titulo: string; }

function ListaLibros({ libros }: { libros: Libro[] }) {
  return (
    <ul>
      {libros.map((libro) => (
        <li key={libro.id}>{libro.titulo}</li>
      ))}
    </ul>
  );
}
```

`key` no es una prop más — es información privada que usa React para el algoritmo de reconciliación (decidir qué elemento de la lista es "el mismo" entre un render y el siguiente, en vez de recrear toda la lista). Tiene que ser **estable** (el mismo id en cada render) y **única entre hermanos**; usar el índice del array como `key` funciona solo si la lista nunca reordena ni inserta/borra en el medio — en el resto de los casos produce bugs de UI difíciles de rastrear (inputs que "recuerdan" el valor del elemento equivocado, por ejemplo).

## Tablas con varias columnas, responsive

El mismo patrón `array.map(fn)` con `key` de arriba sirve para generar filas de una tabla (`<tr>`), no solo `<li>`. Lo nuevo acá no es React — es que una `<table>` con muchas columnas no se achica sola en una pantalla angosta, y hay que resolverlo con CSS:

```tsx
interface Receta {
  id: string;
  nombre: string;
  categoria: string;
  tiempoPreparacionMinutos: number;
  porciones: number;
}

function TablaRecetas({ recetas }: { recetas: Receta[] }) {
  return (
    <div className="tabla-wrapper">
      <table>
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Categoría</th>
            <th>Tiempo (min)</th>
            <th>Porciones</th>
          </tr>
        </thead>
        <tbody>
          {recetas.map((receta) => (
            <tr key={receta.id}>
              <td>{receta.nombre}</td>
              <td>{receta.categoria}</td>
              <td>{receta.tiempoPreparacionMinutos}</td>
              <td>{receta.porciones}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

```css
.tabla-wrapper {
  overflow-x: auto;               /* scroll horizontal solo si la tabla no entra */
  -webkit-overflow-scrolling: touch;
}

table {
  width: 100%;
  min-width: 480px;               /* evita que las columnas se aplasten */
  border-collapse: collapse;
}

th, td {
  padding: 8px 10px;
  text-align: left;
  border-bottom: 1px solid #8884;
}
```

> **Concepto clave**: el `<div className="tabla-wrapper">` que envuelve a la `<table>` es lo que hace el trabajo — `overflow-x: auto` en ese contenedor le permite scrollear horizontalmente por dentro sin romper el layout del resto de la página, mientras que `min-width` en la tabla evita que el navegador comprima las columnas hasta hacerlas ilegibles. Es la técnica más usada en la práctica: no cambia el HTML semántico de la tabla (accesible para lectores de pantalla) y no hace falta ninguna librería.

Otras técnicas existen para cuando ni el scroll horizontal es aceptable en mobile — no se desarrollan en esta unidad, pero vale saber que existen:

| Técnica | Idea |
|---|---|
| Scroll horizontal en wrapper (la de arriba) | La tabla entera se ve igual en cualquier pantalla; en mobile se desliza |
| "Tarjetas" apiladas por fila (media query + `data-label` en cada `<td>`) | En mobile cada fila se muestra como bloque vertical en vez de fila de tabla |
| Ocultar columnas secundarias (`display: none` en un `<th>`/`<td>` bajo una media query) | Se pierde información en mobile, pero sin scroll ni reflow |


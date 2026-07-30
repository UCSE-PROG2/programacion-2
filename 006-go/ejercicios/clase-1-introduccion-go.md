# Ejercicios — Clase 1: Introducción a Go

Ejercicios de evaluación para la [Clase 1](../README.md#clase-1--introducción-a-go). Usan solo lo visto hasta esa clase: variables, tipos, control de flujo, funciones con múltiples retornos, slices, maps y errores como valores.

**No usar** `struct`, `interface` ni punteros (`&`/`*`) — eso es contenido de la Clase 2. Todo el código va en un único `package main`.

---

## Ejercicio 1 — Planilla de calificaciones

Un docente carga las notas de un curso como un `[]float64` y necesita un pequeño análisis antes de cerrar el acta.

**Requerimientos:**

1. `Promedio(notas []float64) (float64, error)` — devuelve el promedio; si el slice está vacío, devuelve un error (no un `0` silencioso ni un `panic` por división por cero).
2. `NotaExtrema(notas []float64, buscarMaxima bool) (valor float64, posicion int, err error)` — según el flag, devuelve la nota más alta o más baja **y su posición** en el slice original. Reutilizar la misma función para ambos casos (sin duplicar la lógica de recorrido).
3. `Clasificar(nota float64) string` — usando `switch`, devuelve `"Insuficiente"` (`<4`), `"Regular"` (`4-6`), `"Bueno"` (`6-8`) o `"Excelente"` (`>8`).
4. En `main()`: cargar al menos 6 notas, mostrar promedio, máxima y mínima con su posición, y la clasificación de cada nota individual.

**Evalúa:** múltiples valores de retorno, retorno con nombre, errores como valores en un caso borde real (slice vacío), `switch` con rangos.

**Checklist:**
- [ ] `Promedio` de un slice vacío devuelve `error`, no `NaN` ni `panic`
- [ ] `NotaExtrema` no duplica lógica entre el caso máximo y mínimo
- [ ] El programa compila y corre con `go run` sin `go vet` quejándose

---

## Ejercicio 2 — Control de stock con mapas

Un `map[string]int` representa el stock de un depósito (`nombre del producto → cantidad`).

**Requerimientos:**

1. `Vender(stock map[string]int, producto string, cantidad int) error` — descuenta `cantidad` del stock. Debe devolver un error **distinto y descriptivo** (con `fmt.Errorf`) para cada uno de estos tres casos: el producto no existe, la cantidad pedida es `<= 0`, o no hay stock suficiente.
2. `Reponer(stock map[string]int, producto string, cantidad int)` — si el producto no existe todavía en el mapa, lo crea; si existe, suma la cantidad.
3. `ProductosBajoStock(stock map[string]int, umbral int) []string` — devuelve los nombres de los productos con stock estrictamente menor al umbral (no hace falta ordenarlos).
4. En `main()`: simular una secuencia de al menos 5 operaciones de venta/reposición sobre 4 productos, imprimiendo el error cuando `Vender` falle **sin frenar el programa** (seguir procesando las siguientes operaciones).

**Evalúa:** maps, comma-ok idiom, errores diferenciados por causa, mutación de un map recibido por parámetro (los maps son tipos de referencia en Go, a diferencia de los structs que se copian).

**Checklist:**
- [ ] `Vender` devuelve 3 mensajes de error distinguibles según la causa
- [ ] El stock se actualiza correctamente sin usar punteros (el map se muta directamente)
- [ ] Un error de `Vender` no interrumpe el resto de las operaciones simuladas en `main`

---

## Ejercicio 3 — Validador de formulario de registro

Antes de dar de alta un usuario, hay que validar los datos que llegaron de un formulario (todavía como variables sueltas, no como struct).

**Requerimientos:**

1. `ValidarEdad(edad int) error` — válida entre 0 y 120 inclusive.
2. `ValidarNombre(nombre string) error` — no vacío y sin espacios en blanco al inicio/final (usar `strings.TrimSpace` para detectarlo).
3. `ValidarEmail(email string) error` — validación simple: debe contener exactamente un `@` y al menos un `.` después del `@` (alcanza con `strings.Contains` / `strings.Index`, no hace falta una regex).
4. `ValidarFormulario(nombre, email string, edad int) []error` — corre las tres validaciones y devuelve **todos** los errores encontrados en un slice (no cortar en el primer error: un formulario puede fallar por varias razones a la vez).
5. En `main()`: probar con al menos un formulario 100% válido (el slice de errores debe quedar vacío) y uno con múltiples campos inválidos a la vez.

**Evalúa:** composición de funciones que devuelven `error`, acumulación de errores en un slice (patrón distinto al típico `if err != nil { return }` que corta en el primero), uso básico de `strings`.

**Checklist:**
- [ ] Un formulario con nombre vacío Y edad negativa devuelve **2** errores en el slice, no solo 1
- [ ] `ValidarFormulario` de datos válidos devuelve un slice vacío (`len(errores) == 0`), no `nil` usado de forma inconsistente
- [ ] Ninguna validación usa `panic`

---

## Ejercicio 4 — Simulador de cajero automático

Una cuenta arranca en `0` y recibe una secuencia de operaciones (`[]float64`, positivas = depósito, negativas = extracción) que hay que aplicar **en orden**.

**Requerimientos:**

1. `ProcesarOperaciones(saldoInicial float64, operaciones []float64) (saldoFinal float64, indiceFallida int, err error)` — aplica las operaciones una por una. Si una extracción dejaría el saldo en negativo, la función **corta ahí** (no sigue procesando las siguientes) y devuelve el saldo hasta ese punto, el índice de la operación que falló, y un error. Si todas se aplican con éxito, `indiceFallida` debe valer `-1` y `err` debe ser `nil`.
2. `ResumenOperaciones(operaciones []float64) (depositos int, extracciones int, montoTotal float64)` — cuenta cuántas operaciones son depósitos vs. extracciones (sin aplicarlas a ningún saldo) y suma los montos absolutos de todas.
3. En `main()`: probar una secuencia que se procesa completa con éxito, y otra que falla a mitad de camino — imprimiendo en ambos casos el saldo final y, si corresponde, en qué operación falló.

**Evalúa:** `for range` con corte temprano (`break` o `return` dentro del loop), retorno con nombre para comunicar tres resultados relacionados, distinción entre "procesar con efectos" y "solo analizar" sobre el mismo slice de entrada.

**Checklist:**
- [ ] Una secuencia que se queda sin saldo a mitad de camino no sigue aplicando las operaciones siguientes
- [ ] `indiceFallida` es `-1` únicamente cuando `err` es `nil`
- [ ] `ResumenOperaciones` no modifica ni depende del resultado de `ProcesarOperaciones`

---

## Ejercicio 5 — Analizador de frecuencia de palabras

Dado un slice de strings (por ejemplo, líneas de un chat de soporte), hay que analizar qué palabras se repiten más.

**Requerimientos:**

1. `ContarPalabras(lineas []string) map[string]int` — separa cada línea por espacios (`strings.Fields` es más robusto que `strings.Split` para esto — investigarlo) y arma un mapa de frecuencia. Ignorar mayúsculas/minúsculas (normalizar con `strings.ToLower` antes de contar).
2. `PalabraMasFrecuente(frecuencias map[string]int) (palabra string, cantidad int, err error)` — recorre el mapa **una sola vez** (sin ordenar todo el mapa) y devuelve la palabra de mayor frecuencia. Si el mapa está vacío, devuelve un error.
3. `PalabrasQueSuperan(frecuencias map[string]int, minimo int) []string` — devuelve las palabras cuya frecuencia es mayor o igual al mínimo dado.
4. En `main()`: cargar al menos 5 líneas de texto con palabras repetidas a propósito, e imprimir el resultado de las tres funciones.

**Evalúa:** transformar datos de un slice a un map, recorrido de maps con `range`, encontrar un máximo sin usar librerías externas, manejo del caso "mapa vacío" como error explícito.

**Checklist:**
- [ ] `ContarPalabras` no distingue mayúsculas de minúsculas al contar (`"Hola"` y `"hola"` suman a la misma clave)
- [ ] `PalabraMasFrecuente` de un mapa vacío devuelve error, no la palabra `""` con cantidad `0` como si fuera válida
- [ ] El programa completo pasa `go vet ./...` sin advertencias

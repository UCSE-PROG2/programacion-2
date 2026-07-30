# Ejercicios — Clase 2: Módulos, paquetes, structs, interfaces y punteros

Ejercicios de evaluación para la [Clase 2](../README.md#clase-2--módulos-paquetes-structs-interfaces-y-punteros). Suman a lo de la Clase 1: `go mod`, paquetes y exportación, `struct`, métodos (value vs. pointer receiver), punteros, interfaces con satisfacción implícita, y testing con `go test`.

**No usar** todavía Gin, `net/http` ni JSON tags — eso empieza en la Clase 3.

---

## Ejercicio 1 — Catálogo de figuras con interfaz doble

Extender el ejemplo de `Figura` visto en clase, agregando una segunda operación y un constructor que valida.

**Requerimientos:**

1. Interfaz `Figura` con **dos** métodos: `Area() float64` y `Perimetro() float64`.
2. Structs `Rectangulo` (`Ancho`, `Alto`) y `Circulo` (`Radio`) que satisfagan `Figura`.
3. Struct `Triangulo` (tres lados `A`, `B`, `C`) que también la satisfaga, calculando el área con la **fórmula de Herón**. Agregar una función constructora `NuevoTriangulo(a, b, c float64) (*Triangulo, error)` que devuelva error si los tres lados no forman un triángulo válido (la suma de cualquier par de lados debe ser mayor al tercero).
4. `FiguraMasGrande(figuras []Figura) Figura` — recorre un slice mixto de figuras y devuelve la de mayor área, sin usar un `switch`/`if` por tipo concreto (debe funcionar solo llamando al método de la interfaz).
5. Al menos 3 tests con `go test`: uno que verifique que `NuevoTriangulo` rechaza lados inválidos (ej: `1, 1, 10`), uno que verifique el área de un triángulo válido conocido (ej: 3-4-5), y uno que verifique que `FiguraMasGrande` elige correctamente entre una mezcla de las tres figuras.

**Evalúa:** interfaz con más de un método, satisfacción implícita con múltiples tipos, constructor que valida y devuelve error + puntero, función que opera solo contra la interfaz (polimorfismo sin `type switch`).

**Checklist:**
- [ ] `FiguraMasGrande` no usa `switch tipo := f.(type)` ni ningún `type assertion` — solo llama a `Area()`
- [ ] `NuevoTriangulo(1, 1, 10)` devuelve error, no un triángulo con área `NaN` o negativa
- [ ] `go test ./...` corre los 3+ tests en verde

---

## Ejercicio 2 — Cuenta bancaria con encapsulamiento real

A diferencia de Java, Go no tiene `private` — la exportación por mayúscula/minúscula es la única herramienta, y acá se usa a propósito.

**Requerimientos:**

1. Crear un paquete `banco` (carpeta propia) con un struct `CuentaBancaria` cuyo campo de saldo (`saldo float64`) sea **privado al paquete** (minúscula) — no debe poder leerse ni escribirse directamente desde otro paquete.
2. Constructor `NuevaCuenta(titular string, saldoInicial float64) (*CuentaBancaria, error)` — rechaza `saldoInicial` negativo.
3. Método `(c *CuentaBancaria) Depositar(monto float64) error` (pointer receiver) — rechaza montos `<= 0`.
4. Método `(c *CuentaBancaria) Retirar(monto float64) error` (pointer receiver) — rechaza montos `<= 0` o mayores al saldo disponible.
5. Método `(c CuentaBancaria) Saldo() float64` (value receiver, de solo lectura) — es la **única** forma en que otro paquete puede conocer el saldo.
6. Desde `main` (otro paquete), probar que **no compila** si se intenta acceder a `cuenta.saldo` directamente (dejar el intento comentado con una nota explicando por qué no compila), y sí funciona a través de `Saldo()`.
7. Al menos 2 tests que verifiquen: que `Retirar` con pointer receiver efectivamente reduce el saldo del original (no de una copia), y que un `Retirar` que excede el saldo disponible **no** modifica el saldo original.

**Evalúa:** exportación a nivel de paquete como mecanismo de encapsulamiento, por qué `Depositar`/`Retirar` necesitan pointer receiver y `Saldo` no, verificación explícita de que un pointer receiver muta el original y un intento fallido no.

**Checklist:**
- [ ] `saldo` es un campo privado del paquete `banco`, no accesible desde `main`
- [ ] Los tests confirman que un `Retirar` fallido deja el saldo exactamente igual que antes (comparación exacta, no aproximada)
- [ ] `Depositar`/`Retirar` usan pointer receiver; `Saldo` usa value receiver

---

## Ejercicio 3 — Sistema de notificaciones con fallas simuladas

Distintos canales de notificación, cada uno con su propia forma de fallar.

**Requerimientos:**

1. Interfaz `Notificador` con un método `Enviar(mensaje string) error`.
2. `NotificadorEmail` — falla (devuelve error) si el mensaje está vacío.
3. `NotificadorSMS` — falla si el mensaje supera los 160 caracteres (simular el límite real de un SMS).
4. `NotificadorPush` — nunca falla por contenido, pero simula estar "desconectado" con un campo `bool` en el struct; si está desconectado, `Enviar` devuelve error.
5. `EnviarATodos(notificadores []Notificador, mensaje string) []error` — intenta enviar por todos los canales de la lista y devuelve **solo** los errores que ocurrieron (si todos tienen éxito, el slice debe quedar vacío, no lleno de `nil`).
6. Tests que prueben, para cada uno de los tres notificadores, tanto el caso de éxito como el de falla.

**Evalúa:** interfaz con implementaciones que fallan por razones distintas y específicas de cada tipo, filtrado de errores (no todos los intentos fallan, hay que quedarse solo con los que sí), testing por tipo concreto.

**Checklist:**
- [ ] `EnviarATodos` con 3 notificadores donde solo 1 falla devuelve un slice de longitud 1, no de longitud 3 con `nil`s
- [ ] Cada notificador tiene al menos un test de éxito y uno de falla
- [ ] Ningún notificador usa `panic` para señalar la falla

---

## Ejercicio 4 — Contador: value receiver vs. pointer receiver, a propósito

Ejercicio dirigido específicamente a demostrar (con un test, no solo con un comentario) la diferencia entre ambos receivers.

**Requerimientos:**

1. Struct `Contador` con un campo `valor int`.
2. Método `(c *Contador) Incrementar()` — pointer receiver, suma 1 al campo del struct original.
3. Método `(c Contador) IncrementarCopia() Contador` — value receiver, devuelve una **nueva** `Contador` con el valor incrementado, sin tocar el original.
4. Función `IncrementarNVeces(c *Contador, n int)` que llame a `Incrementar()` `n` veces sobre el mismo puntero.
5. Un test que demuestre que, tras llamar `IncrementarCopia()` sobre una variable, el `valor` de la variable original **no cambió**.
6. Un test que demuestre que, tras llamar `IncrementarNVeces(&c, 5)`, el `valor` de `c` es exactamente 5 más que el inicial.

**Evalúa:** la diferencia observable (no solo teórica) entre mutar el original y devolver una copia modificada, paso de punteros a funciones (no solo a métodos).

**Checklist:**
- [ ] El test de `IncrementarCopia` falla si, por error, se cambia el receiver a puntero (para verificar que realmente están testeando la diferencia)
- [ ] `IncrementarNVeces` recibe `*Contador`, no `Contador`
- [ ] Ambos tests corren en verde con `go test -v ./...`

---

## Ejercicio 5 — Paquete de validaciones componibles

Un paquete reutilizable de validaciones de string, pensado para combinarse.

**Requerimientos:**

1. Crear un módulo nuevo (`go mod init`) con un paquete `validaciones` (carpeta propia).
2. Interfaz `Validador` con un método `EsValido(valor string) bool`.
3. `ValidadorLongitud` (struct con `Minimo`, `Maximo int`) que valida el largo del string.
4. `ValidadorNoVacio` (struct sin campos, o con un campo de configuración si se prefiere) que valida que el string no sea `""` después de `strings.TrimSpace`.
5. `ValidadorSoloNumeros` que valida que todos los caracteres sean dígitos (se puede recorrer el string con `for _, r := range valor` y `unicode.IsDigit(r)`).
6. `ValidarTodos(vs []Validador, valor string) bool` — aplica todos los validadores de la lista con **AND lógico** (todos deben pasar).
7. Al menos 5 tests: casos válidos e inválidos para cada uno de los 3 validadores por separado, más al menos un test de `ValidarTodos` combinando varios donde uno falla.

**Evalúa:** diseño de una interfaz pensada para componerse (no una sola implementación "de juguete"), organización en un paquete propio con `go mod init`, cobertura de tests por caso límite (string vacío, justo en el límite de longitud).

**Checklist:**
- [ ] `go mod init` generó un `go.mod` válido para este paquete
- [ ] Los 3 validadores satisfacen `Validador` sin ninguna declaración de "implements"
- [ ] `ValidarTodos` con un solo validador que falla entre varios que pasan devuelve `false`
- [ ] `go test ./...` corre 5+ tests, todos en verde

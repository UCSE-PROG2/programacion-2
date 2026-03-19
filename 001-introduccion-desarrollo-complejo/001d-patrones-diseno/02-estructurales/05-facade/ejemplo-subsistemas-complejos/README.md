# Ejemplo: Subsistemas complejos (Facade)

**Facade** ofrece una interfaz simplificada a un conjunto de clases o subsistemas complejos. Aquí: un sistema con tres subsistemas (como en el ejercicio de la PPT); la fachada expone una operación sencilla que orquesta los tres.

## Archivos que deberían ir en esta carpeta

### Subsistema A
- `SubsistemaA.java` — Clase con métodos específicos (ej. `validarEntrada(String)`, `preprocesar(String)`). API más detallada o técnica.

### Subsistema B
- `SubsistemaB.java` — Otra responsabilidad (ej. `calcular(String)`, `aplicarReglas(String)`).

### Subsistema C
- `SubsistemaC.java` — Otra más (ej. `persistir(String)`, `notificar(String)`).

### Fachada
- `FachadaSistema.java` — Tiene referencias a los tres subsistemas (creados en el constructor o inyectados). Método(s) de alto nivel, por ejemplo `ejecutarOperacionPrincipal(String entrada)` que internamente llama en orden: `subsistemaA.validarEntrada(entrada)`, `subsistemaB.calcular(...)`, `subsistemaC.persistir(...)`, y devuelve un resultado simple (o void). Oculta la complejidad y el orden de llamadas.

### Cliente
- `Main.java` — Solo conoce `FachadaSistema`; llama a `ejecutarOperacionPrincipal("dato")` sin usar directamente `SubsistemaA`, `B` ni `C`.

## Cómo comprobar que está bien aplicado

- El cliente no importa ni instancia los subsistemas; solo la fachada.
- Si cambia la lógica interna (orden, nuevos pasos), se modifica solo la fachada; el cliente no cambia.

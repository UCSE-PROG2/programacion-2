# Ejemplo: Listado de tareas (MVVM - Modelo-Vista-ViewModel)

Este ejemplo aplica **MVVM** a una app de listado de tareas (estilo “todo list”). El **ViewModel** expone datos y comandos; la **Vista** se enlaza a ellos (binding) y no contiene lógica de negocio. Muy usado en aplicaciones móviles y escritorio moderno.

## Estructura

- **modelo/** — Entidad Tarea y repositorio que las persiste/lista.
- **viewmodel/** — Propiedades observables y comandos; actualiza el modelo y refresca lo que la vista observa.
- **vista/** — Pantalla que se enlaza al ViewModel; no llama al modelo directamente.

## Punto de entrada

- **Main.java** o **Application** (Android/JavaFX) — Crea repositorio, ViewModel y vista; asigna el ViewModel a la vista. Puede estar en la raíz del ejemplo.

## Cómo comprobar que está bien aplicado

- La vista no tiene imports del modelo ni del repositorio; solo del ViewModel (o de tipos DTO que expone el ViewModel).
- El ViewModel es fácil de testear con un repositorio mock; la vista puede probarse con un ViewModel fake.
- Si cambias la fuente de datos (archivo, API), solo tocas el modelo/repositorio y quizá el ViewModel, no la vista.

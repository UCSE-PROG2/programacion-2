# Ejemplo: Listado de tareas (MVVM - Modelo-Vista-ViewModel)

Este ejemplo aplica **MVVM** a una app de listado de tareas (estilo “todo list”). El **ViewModel** expone datos y comandos; la **Vista** se enlaza a ellos (binding) y no contiene lógica de negocio. Muy usado en aplicaciones móviles y escritorio moderno.

## Papel de cada componente

- **Modelo**: entidad Tarea (id, texto, completada) y repositorio o servicio que las persiste/lista.
- **Vista**: pantalla que muestra la lista y el formulario; se enlaza a propiedades y comandos del ViewModel (no llama al modelo directamente).
- **ViewModel**: expone lista observable de tareas, texto del ítem nuevo, comando “agregar”, comando “marcar completada”; actualiza el modelo y refresca las propiedades que la vista observa.

## Archivos que deberían ir en esta carpeta

### Modelo (`modelo/` o `model/`)
- `Tarea.java` — id, texto, completada (boolean).
- `TareaRepository.java` — `listar()`, `guardar(Tarea)`, `marcarCompletada(id)` (o equivalente).

### ViewModel (`viewmodel/`)
- `ListadoTareasViewModel.java` — Propiedades observables (o getters que notifiquen): `List<Tarea> getTareas()`, `String getNuevoTexto()`, `setNuevoTexto(String)`. Comandos: `agregarTarea()`, `marcarCompletada(int id)`. Internamente usa `TareaRepository` y actualiza la lista que la vista observa.

### Vista (`vista/` o `view/`)
- En Android: `ListadoTareasActivity.java` o Fragment + layout XML; enlaza el RecyclerView y el EditText/botón a los datos y comandos del ViewModel (LiveData/Flow + binding o callbacks).
- En JavaFX: `ListadoTareasView.fxml` + controlador que bindea tabla y campos al ViewModel.
- La vista solo asigna el ViewModel y reacciona a cambios; no contiene reglas de negocio ni acceso a repositorio.

### Punto de entrada
- `Main.java` o `Application` (Android/JavaFX) — Crea repositorio, ViewModel y vista; asigna el ViewModel a la vista.

## Cómo comprobar que está bien aplicado

- La vista no tiene imports del modelo ni del repositorio; solo del ViewModel (o de tipos DTO que expone el ViewModel).
- El ViewModel es fácil de testear con un repositorio mock; la vista puede probarse con un ViewModel fake.
- Si cambias la fuente de datos (archivo, API), solo tocas el modelo/repositorio y quizá el ViewModel, no la vista.

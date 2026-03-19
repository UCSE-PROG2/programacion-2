# ViewModel (`viewmodel/`)

Expone lista observable de tareas, texto del ítem nuevo, comando “agregar”, comando “marcar completada”; actualiza el modelo y refresca las propiedades que la vista observa.

## Archivos que deberían ir en esta carpeta

- **ListadoTareasViewModel.java** — Propiedades observables (o getters que notifiquen): `List<Tarea> getTareas()`, `String getNuevoTexto()`, `setNuevoTexto(String)`. Comandos: `agregarTarea()`, `marcarCompletada(int id)`. Internamente usa `TareaRepository` y actualiza la lista que la vista observa.

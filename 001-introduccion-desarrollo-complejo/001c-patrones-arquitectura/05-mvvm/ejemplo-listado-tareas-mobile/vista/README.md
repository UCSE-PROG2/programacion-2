# Vista (`vista/` o `view/`)

Pantalla que muestra la lista y el formulario; se enlaza a propiedades y comandos del ViewModel (no llama al modelo directamente).

## Archivos que deberían ir en esta carpeta

- En Android: **ListadoTareasActivity.java** o Fragment + layout XML — Enlaza el RecyclerView y el EditText/botón a los datos y comandos del ViewModel (LiveData/Flow + binding o callbacks).
- En JavaFX: **ListadoTareasView.fxml** + controlador que bindea tabla y campos al ViewModel.
- La vista solo asigna el ViewModel y reacciona a cambios; no contiene reglas de negocio ni acceso a repositorio.

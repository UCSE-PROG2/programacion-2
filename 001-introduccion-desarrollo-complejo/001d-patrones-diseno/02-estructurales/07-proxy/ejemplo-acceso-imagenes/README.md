# Ejemplo: Acceso a imágenes con proxy (Proxy)

**Proxy** actúa como sustituto de otro objeto y controla el acceso (lazy loading, caché, control de permisos). Aquí: servicio de imágenes; el proxy carga la imagen solo cuando se pide por primera vez (o la cachea).

## Archivos que deberían ir en esta carpeta

### Sujeto (interfaz)
- `Imagen.java` — Interfaz: `mostrar()`, `byte[] getDatos()` (o `String getRuta()`). Es lo que tanto el objeto real como el proxy implementan.

### Objeto real
- `ImagenReal.java` — Implementa `Imagen`; en el constructor o en `mostrar()` carga el archivo desde disco o red (costoso). Almacena los datos una vez cargados.

### Proxy
- `ProxyImagen.java` — Implementa `Imagen`; tiene referencia a `ImagenReal` (inicialmente null o referencia a un objeto no cargado). Tiene el nombre/ruta del archivo. Al llamar `mostrar()` o `getDatos()`, si aún no ha cargado, instancia `ImagenReal` con esa ruta y delega; si ya está cargada, solo delega. Así el coste de carga ocurre solo cuando se accede.

### Cliente
- `VisorGaleria.java` o `Main.java` — Trabaja con la interfaz `Imagen`; puede recibir un `ProxyImagen`. Al llamar `mostrar()` la primera vez se carga; las siguientes usan la ya cargada (caché implícita en el objeto real dentro del proxy).

## Cómo comprobar que está bien aplicado

- El cliente no sabe si está usando proxy o imagen real; ambos implementan la misma interfaz.
- La creación o carga del objeto costoso ocurre solo cuando el cliente accede al proxy, no al construir el proxy.

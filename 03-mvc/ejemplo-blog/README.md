# Ejemplo: Blog (MVC - Modelo-Vista-Controlador)

Este ejemplo aplica **MVC** a un blog: el **Modelo** son los datos y la lógica de negocio, la **Vista** es la interfaz (HTML/consola), y el **Controlador** actúa de intermediario entre ambos.

## Papel de cada componente

- **Modelo**: entidades (Post, Usuario, Comentario), reglas de negocio y acceso a datos.
- **Vista**: muestra datos y envía acciones del usuario (clic en “publicar”, “editar”) al controlador.
- **Controlador**: recibe la acción, llama al modelo y decide qué vista mostrar o qué dato devolver.

## Archivos que deberían ir en esta carpeta

### Modelo (`modelo/` o `model/`)
- `Post.java` — Entidad: id, título, contenido, autor, fecha.
- `Comentario.java` — Entidad asociada a un post.
- `PostRepository.java` o `PostDAO.java` — Guardar, listar, buscar por id.
- `PostService.java` — Lógica: crear post, validar, listar publicados (si aplica).

### Vista (`vista/` o `view/`)
- Si es web: `posts.html`, `detalle-post.html`, `formulario-post.html` (templates que reciben datos del controlador).
- Si es consola: `VistaBlog.java` — Imprime listado de posts y menú (crear, ver, salir); lee entrada por teclado y notifica al controlador (por callbacks o devolviendo opción elegida).

### Controlador (`controlador/` o `controller/`)
- `PostController.java` — Métodos: `listarPosts()`, `verPost(id)`, `crearPost(titulo, contenido)`. Usa el modelo y actualiza o elige la vista.
- Si es web: `BlogServlet.java` o controlador Spring que mapee rutas (`/posts`, `/posts/{id}`) y devuelva vista o JSON.

### Punto de entrada
- `Main.java` o configuración de la app web — Crea modelo, vista y controlador y los conecta; en web, el contenedor hace esta conexión vía rutas.

## Flujo típico

1. Usuario elige “ver post” en la vista.
2. Vista notifica al controlador (o el controlador recibe la petición HTTP).
3. Controlador pide al modelo el post con ese id.
4. Modelo devuelve datos; controlador los pasa a la vista.
5. Vista muestra el post.

## Cómo comprobar que está bien aplicado

- El modelo no tiene referencias a vistas ni controladores.
- La vista no accede directamente a base de datos; solo recibe datos ya preparados del controlador.
- El controlador no contiene HTML ni lógica de persistencia compleja; solo orquesta.

# Ejemplo: Portal modular (Arquitectura basada en componentes)

Este ejemplo aplica **arquitectura basada en componentes**: la aplicación se construye a partir de **componentes** reutilizables y autónomos, definidos por sus **interfaces** (qué ofrecen y qué necesitan). Cada componente es una unidad independiente que se ensambla en un portal (inicio, noticias, clima, usuario).

## Papel de cada parte

- **Componentes**: módulos con interfaz pública (ej. “proporcionar fragmento HTML y datos”, “consumir servicio de noticias”). Implementación interna oculta.
- **Contenedor/Portal**: ensambla los componentes, les inyecta dependencias (servicios, config) y los coloca en la página o layout.

## Archivos que deberían ir en esta carpeta

### Definición de interfaces (`api/` o `interfaces/`)
- `ComponentePortal.java` — Interfaz: `String getId()`, `String getTitulo()`, `Fragmento renderizar(ContextoPortal ctx)` (o `Map<String,Object> getDatos()` para que el portal renderice). Define el contrato que todo componente debe cumplir.
- `ContextoPortal.java` — Objeto que el portal pasa a cada componente (usuario actual, preferencias, servicios compartidos) para que puedan obtener datos.

### Componentes concretos (`componentes/`)
- `ComponenteNoticias.java` — Implementa `ComponentePortal`; internamente usa un `ServicioNoticias` (inyectado) para obtener últimas noticias y devuelve título + datos o fragmento.
- `ComponenteClima.java` — Implementa `ComponentePortal`; usa `ServicioClima` y devuelve datos para mostrar clima actual.
- `ComponenteUsuario.java` — Muestra datos del usuario logueado; usa `ContextoPortal` para obtener el usuario.

### Servicios compartidos (`servicios/`)
- `ServicioNoticias.java` — Lista de noticias (mock o API).
- `ServicioClima.java` — Datos de clima (mock o API).
- Estos son los “requisitos” que algunos componentes necesitan; el contenedor los inyecta.

### Contenedor/Portal (`portal/`)
- `Portal.java` — Lista o registro de componentes (`ComponenteNoticias`, `ComponenteClima`, `ComponenteUsuario`). Método `renderizarPagina()` que recorre los componentes, les pasa el contexto y ensambla el resultado (HTML, JSON o estructura para una vista).
- `ConfiguracionPortal.java` — Define qué componentes se cargan y en qué orden (archivo de config o código).

### Punto de entrada
- `Main.java` — Crea servicios, instancia componentes, los registra en el portal y ejecuta `renderizarPagina()` o arranca un servidor que sirve la página del portal.

## Cómo comprobar que está bien aplicado

- Cada componente está en su propio paquete o módulo; no depende de otros componentes, solo de la interfaz `ComponentePortal` y del contexto/servicios inyectados.
- Añadir un componente nuevo (ej. “últimos comentarios”) implica implementar la interfaz y registrarlo en el portal; no modificar los demás componentes.
- Las dependencias entre componentes y servicios están explícitas (constructor o setter) para facilitar tests con mocks.

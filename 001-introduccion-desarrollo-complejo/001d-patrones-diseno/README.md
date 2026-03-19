# Patrones de diseño

Patrones de diseño (design patterns): soluciones habituales a problemas comunes. Cada carpeta corresponde a un patrón de la presentación, con un ejemplo distinto y un `README.md` que indica qué archivos implementar.

## Clasificación

### Creacionales
Mecanismos de creación de objetos que aumentan flexibilidad y reutilización.

| # | Patrón | Ejemplo |
|---|--------|---------|
| 01 | Factory Method | Fabricación de vehículos |
| 02 | Abstract Factory | UI con temas (claro/oscuro) |
| 03 | Builder | Construcción de casa |
| 04 | Prototype | Documentos clonables |
| 05 | Singleton | Conexión a base de datos |

### Estructurales
Ensamblar objetos y clases en estructuras más grandes manteniendo flexibilidad.

| # | Patrón | Ejemplo |
|---|--------|---------|
| 01 | Adapter | Adaptador entre dos tipos de bases de datos |
| 02 | Bridge | Reproductores y dispositivos de salida |
| 03 | Composite | Archivos y carpetas (árbol) |
| 04 | Decorator | Bebidas con agregados |
| 05 | Facade | Subsistemas complejos (tres subsistemas) |
| 06 | Flyweight | Árbol con texturas compartidas |
| 07 | Proxy | Acceso a imágenes (caché/lazy) |

### Comportamiento
Comunicación y asignación de responsabilidades entre objetos.

| # | Patrón | Ejemplo |
|---|--------|---------|
| 01 | Chain of Responsibility | Validación de login (cadena de validadores) |
| 02 | Command | Control remoto (encender, apagar, cambiar canal) |
| 03 | Iterator | Colección de personas (recorrido sin exponer estructura) |
| 04 | Mediator | Chat en sala (mediador entre usuarios) |
| 05 | Memento | Editor con undo (guardar/restaurar estado) |
| 06 | Observer | Cotizaciones de bolsa (suscriptores) |
| 07 | State | Máquina expendedora (estados) |
| 08 | Strategy | Rutas de transporte (coche, bici, andando) |
| 09 | Template Method | Procesamiento de informes (esqueleto común) |
| 10 | Visitor | Exportar a distintos formatos (PDF, JSON) |

Entrad en cada subcarpeta y leed el `README.md` del ejemplo para saber qué archivos implementar.

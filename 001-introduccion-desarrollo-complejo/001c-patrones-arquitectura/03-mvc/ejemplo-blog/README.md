# Ejemplo: Blog (MVC - Modelo-Vista-Controlador)

Este ejemplo aplica **MVC** a un blog: el **Modelo** son los datos y la lógica de negocio, la **Vista** es la interfaz (HTML/consola), y el **Controlador** actúa de intermediario entre ambos.

## Estructura

- **modelo/** — Entidades, repositorios y lógica de negocio.
- **vista/** — Templates o vista de consola que muestran datos.
- **controlador/** — Orquesta modelo y vista según las acciones del usuario.

## Flujo típico

1. Usuario elige “ver post” en la vista.
2. Vista notifica al controlador (o el controlador recibe la petición HTTP).
3. Controlador pide al modelo el post con ese id.
4. Modelo devuelve datos; controlador los pasa a la vista.
5. Vista muestra el post.

## Punto de entrada

- **Main.java** o configuración de la app web — Crea modelo, vista y controlador y los conecta; en web, el contenedor hace esta conexión vía rutas. Puede estar en la raíz del ejemplo.

## Cómo comprobar que está bien aplicado

- El modelo no tiene referencias a vistas ni controladores.
- La vista no accede directamente a base de datos; solo recibe datos ya preparados del controlador.
- El controlador no contiene HTML ni lógica de persistencia compleja; solo orquesta.

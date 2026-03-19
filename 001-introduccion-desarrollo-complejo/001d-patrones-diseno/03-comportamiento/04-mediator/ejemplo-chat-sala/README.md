# Ejemplo: Chat en sala (Mediator)

**Mediator** centraliza la comunicación entre objetos; los participantes no se referencian entre sí, solo al mediador. Aquí: sala de chat donde varios usuarios envían mensajes; el mediador reparte cada mensaje al resto.

## Archivos que deberían ir en esta carpeta

### Mediador
- `SalaChat.java` — Interfaz o clase: método `enviarMensaje(String de, String mensaje)` (o `enviarMensaje(Usuario de, String mensaje)`). Mantiene la lista de usuarios registrados. Al recibir un mensaje, lo reenvía a todos los demás (o a un subconjunto); no deja que los usuarios se envíen mensajes directamente entre sí.

### Colega (participante)
- `Usuario.java` — Tiene nombre; tiene referencia al mediador `SalaChat`. Método `enviar(String mensaje)` que llama a `sala.enviarMensaje(this.nombre, mensaje)`. Método `recibir(String de, String mensaje)` que el mediador invocará para entregar mensajes (aquí el usuario puede imprimir o mostrar en UI). Al registrarse en la sala, se añade a la lista del mediador.

### Cliente
- `Main.java` — Crea una instancia de la sala (mediador), varios `Usuario` pasándoles la sala; cada usuario se registra en la sala. Un usuario envía mensaje; el mediador notifica a los otros; ninguno tiene referencia directa a los demás, solo a la sala.

## Cómo comprobar que está bien aplicado

- Añadir un nuevo usuario no requiere que los existentes lo conozcan; solo se registra en el mediador.
- Si cambia la lógica de distribución (ej. mensajes privados), solo se modifica el mediador.

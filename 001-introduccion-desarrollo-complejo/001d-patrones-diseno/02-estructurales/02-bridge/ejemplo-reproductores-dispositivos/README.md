# Ejemplo: Reproductores y dispositivos (Bridge)

**Bridge** separa la abstracción de la implementación en dos jerarquías independientes. Aquí: reproductor (reproductor de música, de video) como abstracción, y dispositivo de salida (altavoz, auriculares, TV) como implementación; se combinan sin multiplicar clases.

## Archivos que deberían ir en esta carpeta

### Implementación (dispositivo de salida)
- `DispositivoSalida.java` — Interfaz: `reproducirAudio(String)`, `reproducirVideo(String)` (o un método genérico `emitir(String tipo, String datos)`).
- `Altavoz.java`, `Auriculares.java`, `Television.java` — Implementan `DispositivoSalida`.

### Abstracción (reproductor)
- `Reproductor.java` — Clase abstracta que tiene una referencia a `DispositivoSalida`. Constructor recibe el dispositivo. Método `reproducir(Media m)` que delega en el dispositivo (quizá transformando el contenido antes).
- `ReproductorMusica.java` — Extiende `Reproductor`; lógica específica para música (metadata, lista).
- `ReproductorVideo.java` — Extiende `Reproductor`; lógica específica para video.

### Cliente
- `Main.java` — Crea un dispositivo (ej. `new Auriculares()`) y un reproductor (ej. `new ReproductorMusica(dispositivo)`); llama a `reproducir()`. Puede combinar cualquier reproductor con cualquier dispositivo sin nuevas clases.

## Cómo comprobar que está bien aplicado

- Cambiar el dispositivo no requiere subclases nuevas de reproductor; se inyecta otra implementación de `DispositivoSalida`.
- Las dos jerarquías (reproductor vs dispositivo) evolucionan por separado.

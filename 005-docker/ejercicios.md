# Ejercicios — Contenedores con Docker

Ejercicios prácticos de la [Unidad 4](README.md) para practicar `docker run` y el manejo básico de contenedores. Todos usan **imágenes oficiales y conocidas de Docker Hub** — no hace falta escribir ningún Dockerfile.

Antes de empezar, verificar que Docker está corriendo:

```bash
docker --version
docker run hello-world
```

---

## Ejercicio 1 — Primer contenedor: `hello-world`

**Consigna:**

1. Descargar y correr la imagen `hello-world`.
2. Leer el mensaje que imprime — explica en sus propias palabras qué acaba de pasar (de dónde bajó la imagen, qué hizo el contenedor, por qué terminó solo).
3. Listar **todos** los contenedores (incluidos los detenidos) y ubicar el que se acaba de crear.

**Comandos de referencia:** `docker pull`, `docker run`, `docker ps -a`

**Verificar:**
- [ ] `docker images` muestra `hello-world` descargada localmente
- [ ] `docker ps -a` muestra el contenedor con estado `Exited (0)`
- [ ] Se entiende por qué el contenedor no queda corriendo (el proceso principal terminó)

---

## Ejercicio 2 — Servidor web con `nginx`

**Consigna:**

1. Correr un contenedor de la imagen `nginx` en segundo plano, con nombre `mi-nginx`, publicando el puerto 8080 del host contra el puerto 80 del contenedor.
2. Abrir `http://localhost:8080` en el navegador (o `curl`) y confirmar que se ve la página de bienvenida de nginx.
3. Ver los logs del contenedor mientras se hacen un par de refrescos del navegador.

**Comandos de referencia:** `docker run -d -p -–name`, `docker logs -f`

**Verificar:**
- [ ] `docker ps` muestra `mi-nginx` con estado `Up` y el mapeo `0.0.0.0:8080->80/tcp`
- [ ] El navegador/`curl` a `localhost:8080` devuelve la página de nginx
- [ ] Cada request nuevo aparece como una línea en `docker logs`

---

## Ejercicio 3 — Terminal interactiva con `ubuntu`

**Consigna:**

1. Correr un contenedor de `ubuntu` en modo interactivo, abriendo una shell `bash`.
2. Dentro del contenedor: verificar la versión del sistema (`cat /etc/os-release`), crear un archivo de texto en `/tmp`, y salir con `exit`.
3. Volver a correr el mismo `docker run` desde cero y comprobar que el archivo creado **no** está — cada `docker run` crea un contenedor nuevo.

**Comandos de referencia:** `docker run -it`

**Verificar:**
- [ ] Se puede entrar y salir de la shell del contenedor sin errores
- [ ] Se entiende la diferencia entre `-i`, `-t` y la combinación `-it`
- [ ] Se confirma que el segundo `docker run` arranca "limpio" (el archivo del paso 2 no persiste)

---

## Ejercicio 4 — Ciclo de vida de un contenedor

**Consigna:**

Usando la imagen `nginx` con nombre `ciclo-nginx`:

1. Crearlo y arrancarlo en background.
2. Detenerlo (`stop`) y confirmar en `docker ps -a` que quedó `Exited`.
3. Volver a arrancarlo (`start`, sin crear uno nuevo) y confirmar que vuelve a responder en su puerto.
4. Reiniciarlo (`restart`) y explicar en qué se diferencia de hacer `stop` + `start` por separado.
5. Eliminarlo.

**Comandos de referencia:** `docker stop`, `docker start`, `docker restart`, `docker rm`

**Verificar:**
- [ ] El mismo contenedor (mismo `CONTAINER ID`) pasa por los tres estados: corriendo → detenido → corriendo de nuevo
- [ ] `docker rm ciclo-nginx` falla si el contenedor sigue corriendo (hay que detenerlo antes, o usar `-f`)
- [ ] Al final, `docker ps -a` ya no lo lista

---

## Ejercicio 5 — Variables de entorno con `postgres`

**Consigna:**

1. Correr un contenedor de la imagen oficial `postgres`, definiendo la variable de entorno `POSTGRES_PASSWORD` (obligatoria para que la imagen arranque).
2. Ver los logs y ubicar la línea que confirma que el servidor de base de datos quedó listo para aceptar conexiones.
3. Sin definir `POSTGRES_PASSWORD`, volver a intentar correr la imagen y observar qué pasa.

**Comandos de referencia:** `docker run -e`, `docker logs`

**Verificar:**
- [ ] Con la variable definida, los logs muestran `database system is ready to accept connections`
- [ ] Sin la variable, el contenedor termina inmediatamente con un error explicando qué falta
- [ ] Se entiende que cada imagen documenta sus propias variables de entorno soportadas en su página de Docker Hub

---

## Ejercicio 6 — Explorar un contenedor corriendo con `exec`

**Consigna:**

Con el contenedor `mi-nginx` del ejercicio 2 todavía corriendo (si no, volver a crearlo):

1. Abrir una shell dentro del contenedor **sin detenerlo**.
2. Ubicar el archivo `index.html` que sirve nginx (pista: `/usr/share/nginx/html/`) y modificar su contenido con algún editor disponible o con `echo`.
3. Salir de la shell y refrescar `http://localhost:8080` — confirmar que el cambio se ve reflejado, sin haber reiniciado el contenedor.

**Comandos de referencia:** `docker exec -it`

**Verificar:**
- [ ] Se pudo entrar a la shell sin interrumpir el servicio (`docker ps` lo sigue mostrando `Up` durante todo el ejercicio)
- [ ] El cambio hecho adentro se refleja inmediatamente al recargar la página
- [ ] Se entiende que ese cambio se pierde si el contenedor se elimina (no hay volumen todavía — ver ejercicio 8)

---

## Ejercicio 7 — Inspeccionar y medir un contenedor

**Consigna:**

Con `mi-nginx` corriendo:

1. Ver su metadata completa en JSON y ubicar su dirección IP interna.
2. Ver el consumo de CPU/memoria en vivo durante unos segundos.
3. Buscar, dentro del JSON de `inspect`, el mapeo de puertos configurado y la política de reinicio (`RestartPolicy`).

**Comandos de referencia:** `docker inspect`, `docker stats`

**Verificar:**
- [ ] Se identifica la IP interna del contenedor (típicamente `172.17.0.x`)
- [ ] Se identifica cuánta memoria y CPU está usando el contenedor en este momento
- [ ] Se entiende que esa IP interna es distinta de `localhost` y solo es alcanzable desde otros contenedores en la misma red, o desde el host en algunos casos

---

## Ejercicio 8 — Persistencia con un volumen (`redis`)

**Consigna:**

1. Crear un volumen con nombre `datos-redis`.
2. Correr un contenedor `redis` en background, montando ese volumen en el directorio donde Redis guarda sus datos (`/data`), y publicando el puerto 6379.
3. Conectarse al contenedor (`docker exec -it <nombre> redis-cli`) y guardar un valor: `SET curso "programacion2"`.
4. Forzar que Redis guarde en disco (`SAVE` desde `redis-cli`, o esperar), eliminar el contenedor **sin borrar el volumen**, y crear uno nuevo montando el mismo volumen.
5. Verificar con `GET curso` que el valor sigue estando.

**Comandos de referencia:** `docker volume create`, `docker run -v`, `docker exec`

**Verificar:**
- [ ] `docker volume ls` muestra `datos-redis`
- [ ] Después de eliminar y recrear el contenedor, `GET curso` devuelve el valor guardado
- [ ] Se entiende que el dato "vive" en el volumen, no en el contenedor

---

## Ejercicio 9 — Bind mount con contenido propio (`nginx`)

**Consigna:**

1. Crear una carpeta local `sitio/` con un `index.html` propio (cualquier HTML simple).
2. Correr un contenedor `nginx` montando esa carpeta local contra `/usr/share/nginx/html` dentro del contenedor (bind mount, no volumen nombrado), publicando algún puerto.
3. Verificar en el navegador que se sirve el HTML propio.
4. Sin reiniciar el contenedor, modificar el `index.html` local y refrescar el navegador.

**Comandos de referencia:** `docker run -v $(pwd)/sitio:/usr/share/nginx/html`

**Verificar:**
- [ ] El navegador muestra el HTML propio, no la página default de nginx
- [ ] Los cambios hechos en el archivo **local** (fuera del contenedor) se reflejan al instante en el navegador
- [ ] Se explica la diferencia entre este ejercicio y el volumen nombrado del ejercicio 8 (bind mount = carpeta específica del host; volumen = gestionado por Docker)

---

## Ejercicio 10 — Dos contenedores en la misma red

**Consigna:**

1. Crear una red Docker propia llamada `red-practica`.
2. Correr un contenedor `redis` en esa red, con nombre `cache`.
3. Correr un contenedor `alpine` **interactivo** en la misma red (necesita el paquete `redis-cli`; alcanza con `docker run -it --network red-practica redis redis-cli -h cache` usando la propia imagen `redis` como cliente).
4. Desde ese segundo contenedor, conectarse a `cache` **usando el nombre del contenedor como hostname** (no una IP) y ejecutar `PING`.

**Comandos de referencia:** `docker network create`, `docker run --network`

**Verificar:**
- [ ] `docker network inspect red-practica` lista ambos contenedores conectados
- [ ] El segundo contenedor resuelve `cache` por nombre y `PING` responde `PONG`
- [ ] Se explica por qué esto no funcionaría si los contenedores estuvieran en redes `bridge` distintas (o sin red compartida)

---

## Limpieza final

Al terminar los 10 ejercicios, dejar el entorno limpio:

```bash
docker ps -a                  # revisar qué quedó
docker stop $(docker ps -q)   # detener todo lo que siga corriendo
docker system prune -a        # eliminar contenedores parados, imágenes sin uso, redes y caches
docker volume prune           # eliminar volúmenes sin uso (¡revisar antes! borra datos)
```

**Verificar:**
- [ ] `docker ps -a` no muestra contenedores de los ejercicios
- [ ] `docker volume ls` y `docker network ls` solo muestran lo que corresponde por defecto

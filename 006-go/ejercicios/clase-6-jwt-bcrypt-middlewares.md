# Ejercicios — Clase 6: JWT, bcrypt, middlewares y logging

Ejercicios de evaluación para la [Clase 6](../README.md#clase-6--jwt-bcrypt-middlewares-y-logging), la última de la Unidad 6. Parten de `POST /login` y `AuthMiddleware()` ya armados en la práctica de esa clase.

---

## Ejercicio 1 — Roles y autorización (no solo autenticación)

`AuthMiddleware` de la clase solo verifica **quién es** el usuario (autenticación). Este ejercicio agrega **qué puede hacer** (autorización).

**Requerimientos:**

1. Al generar el token en `GenerarToken`, agregar un claim adicional `rol` (`"admin"` o `"cliente"`), tomado del `Usuario` que hace login (agregar el campo `Rol` al struct `Usuario`).
2. Crear `RequireRol(rolRequerido string) gin.HandlerFunc` — una **fábrica** de middleware (a diferencia de `AuthMiddleware()`, que no recibe parámetros, esta función recibe un argumento y devuelve el middleware ya configurado).
3. `RequireRol` debe validar el JWT igual que `AuthMiddleware` (o asumir que corre **después** de él en la cadena y leer el rol ya guardado con `c.Get(...)`) y, si el rol del token no coincide con el requerido, responder `403 Forbidden` (no 401 — ya está autenticado, simplemente no tiene permiso).
4. Aplicar `RequireRol("admin")` únicamente a `DELETE /productos/:id`, dejando `POST`/`PUT` accesibles para cualquier usuario autenticado (cualquier rol).
5. Probar con un usuario `cliente` intentando borrar (403) y un usuario `admin` haciéndolo (204).

**Evalúa:** diferencia entre 401 y 403, middleware parametrizado (factory), diseño de una cadena de middlewares donde uno depende de datos que dejó otro anterior (`c.Get`).

**Checklist:**
- [ ] Un token válido de un usuario `cliente` en `DELETE /productos/:id` responde 403, no 401 ni 500
- [ ] Un token de `admin` en la misma ruta responde 204 (o el código de éxito elegido)
- [ ] `RequireRol` es reutilizable: se podría aplicar con otro rol distinto sin duplicar código

---

## Ejercicio 2 — Expiración de JWT probada de verdad

No basta con confiar en que `exp` funciona — hay que demostrarlo con un test.

**Requerimientos:**

1. Modificar (o agregar una variante de) `GenerarToken` que reciba la duración de expiración como parámetro, en vez de tenerla fija en 2 horas — para poder generar tokens de vida muy corta en los tests sin tocar el código de producción.
2. Escribir un test que genere un token con una expiración de, por ejemplo, 1 segundo, espere un poco más que eso (`time.Sleep`), y verifique que `ValidarToken` devuelve un error específico de expiración.
3. Escribir un segundo test que genere un token con expiración larga y verifique que `ValidarToken` lo acepta sin esperar nada (para confirmar que el primer test falla por expiración y no por otra razón).
4. **Extra:** en vez de `time.Sleep` en el test (lento y un poco frágil), investigar cómo construir directamente un `jwt.MapClaims` con un `exp` ya vencido en el pasado y firmarlo a mano, sin esperar tiempo real.

**Evalúa:** testing de comportamiento dependiente del tiempo, separar el parámetro de configuración (duración) del código para poder testearlo con valores extremos, distinguir "token inválido" de "token expirado" como causas de error distintas.

**Checklist:**
- [ ] El test de expiración no depende de esperar 2 horas reales
- [ ] Existe un test de control que confirma que un token no vencido sí es válido
- [ ] `go test -run TestExpiracion -v` corre en menos de unos pocos segundos

---

## Ejercicio 3 — Rate limiting simple por IP

Un middleware con estado compartido entre requests — reutiliza la idea del `sync.Mutex` visto en la Clase 4 para el repository in-memory.

**Requerimientos:**

1. Crear `RateLimiter(maxRequests int, ventana time.Duration) gin.HandlerFunc` — otra fábrica de middleware.
2. Internamente, mantener un `map[string][]time.Time` (o una estructura equivalente) que registre, por IP (`c.ClientIP()`), los timestamps de sus requests recientes, protegido con `sync.Mutex` porque Gin atiende requests concurrentes.
3. En cada request, limpiar del registro los timestamps más viejos que `ventana`, y si la cantidad de requests recientes de esa IP ya alcanzó `maxRequests`, responder `429 Too Many Requests` y abortar la cadena.
4. Aplicarlo de forma **global** (`router.Use`), con un límite bajo para poder probarlo fácilmente (ej: 5 requests cada 10 segundos).
5. Probar disparando más requests que el límite en poco tiempo (un loop simple con `curl` o un script) y confirmar que las primeras pasan y las siguientes devuelven 429.

**Evalúa:** middleware con estado propio (no solo validación sin memoria, como `AuthMiddleware`), sincronización con `sync.Mutex` en un contexto de requests HTTP concurrentes, un código de estado HTTP específico para este caso (429, no 403 ni 401).

**Checklist:**
- [ ] El límite se aplica por IP, no de forma global a todas las requests juntas
- [ ] El acceso al mapa compartido está protegido con `sync.Mutex` (o una estructura concurrente equivalente)
- [ ] Pasada la ventana de tiempo, la misma IP puede volver a hacer requests

---

## Ejercicio 4 — Logging estructurado que conoce al usuario autenticado

Extender `LoggerMiddleware` para que, cuando la request esté autenticada, el log lo refleje.

**Requerimientos:**

1. Modificar `LoggerMiddleware` para que, después de `c.Next()`, intente leer `c.Get("usuario")` (el valor que deja `AuthMiddleware` con `c.Set`).
2. Si el valor existe, incluirlo en la línea de log (ej: `usuario=juan@email.com`); si no existe (ruta pública, sin autenticación), loguear `usuario=anonimo` o similar, sin que el middleware falle ni haga `panic` por el valor ausente.
3. Pensar y justificar en un comentario **el orden correcto** de registro entre `LoggerMiddleware` y `AuthMiddleware` para que esto funcione — probar qué pasa si se registran en el orden incorrecto y documentar la diferencia observada.
4. Probar con una request a una ruta pública (`GET /productos`) y una autenticada (`POST /productos` con token válido), confirmando que el log distingue ambos casos.

**Evalúa:** que el orden de los middlewares en la cadena importa y por qué (leer un valor que otro middleware "más adentro" todavía no dejó), manejo seguro de un valor opcional del contexto de Gin (`c.Get` devuelve un segundo valor booleano, análogo al comma-ok idiom de los maps visto en la Clase 1).

**Checklist:**
- [ ] El log de una ruta pública no muestra un `nil` crudo ni hace `panic` por `c.MustGet`
- [ ] El log de una ruta autenticada muestra el email correcto del usuario del token usado
- [ ] El comentario explica por qué el orden de registro de los middlewares afecta si el dato está disponible o no

---

## Ejercicio 5 — Registro de usuarios con contraseña validada antes de hashear

Un endpoint nuevo, `POST /registro`, que nunca debería hashear (ni guardar) una contraseña débil.

**Requerimientos:**

1. `POST /registro` recibe `email` y `password` en el body, validando el formato de `email` con `binding:"required,email"`.
2. Antes de llamar a `bcrypt.GenerateFromPassword`, agregar una validación manual de complejidad (no expresable con un tag simple): longitud mínima de 8 caracteres **y** al menos un dígito. Si no cumple, responder `400 Bad Request` con un mensaje que indique qué regla falló, sin haber hasheado nada todavía.
3. Si pasa la validación, verificar que el `email` no esté ya registrado (`409 Conflict` si lo está) antes de hashear — evitar el trabajo costoso de bcrypt si la request va a fallar de todas formas.
4. Si todo es válido, hashear con bcrypt, guardar el `Usuario` en el store, y responder `201 Created` **sin** incluir el hash ni la contraseña en la respuesta.
5. Probar: registro exitoso, contraseña corta (400), contraseña sin dígitos (400), email ya registrado (409).

**Evalúa:** orden de validaciones pensado por costo (las validaciones baratas y sin efectos secundarios van primero; bcrypt es deliberadamente lento, no conviene ejecutarlo si la request ya va a fallar por otra razón), que ninguna respuesta filtre el hash ni la contraseña en texto plano.

**Checklist:**
- [ ] Una contraseña de 5 caracteres nunca llega a `bcrypt.GenerateFromPassword` (se puede verificar con un log o un breakpoint que no se dispara)
- [ ] La respuesta de un registro exitoso no incluye `password` ni `passwordHash` en ningún campo del JSON
- [ ] Los 4 casos de prueba devuelven exactamente los códigos de estado esperados (201, 400, 400, 409)

# Site Reliability Engineering (SRE) — Que los sistemas no se caigan (y qué hacer cuando se caen)

Material de apoyo para la **Unidad 8** de **Programación 2** — Ingeniería en Computación (UCSE). Es la segunda parte de la clase; la primera parte es Cloud Computing.

---

## Índice

1. [Por qué existe SRE](#1-por-qué-existe-sre)
2. [SRE, DevOps y sysadmin](#2-sre-devops-y-sysadmin)
3. [La confiabilidad es una característica](#3-la-confiabilidad-es-una-característica)
4. [SLI, SLO y SLA](#4-sli-slo-y-sla)
5. [Los nueves de disponibilidad](#5-los-nueves-de-disponibilidad)
6. [Error budget: el presupuesto de errores](#6-error-budget-el-presupuesto-de-errores)
7. [Monitoreo y observabilidad](#7-monitoreo-y-observabilidad)
8. [Los cuatro golden signals](#8-los-cuatro-golden-signals)
9. [Alertas útiles vs ruido](#9-alertas-útiles-vs-ruido)
10. [Toil y automatización](#10-toil-y-automatización)
11. [Gestión de incidentes](#11-gestión-de-incidentes)
12. [Postmortems sin culpa](#12-postmortems-sin-culpa)
13. [Despliegues seguros](#13-despliegues-seguros)
14. [Diseñar para que las fallas no manden](#14-diseñar-para-que-las-fallas-no-manden)
15. [Chaos engineering](#15-chaos-engineering)
16. [SRE y la nube](#16-sre-y-la-nube)
17. [Ejercicio de aula](#17-ejercicio-de-aula)
18. [Recursos recomendados](#18-recursos-recomendados)

---

## 1. Por qué existe SRE

### Un problema que conocés

Imaginá que el colegio vende las entradas del acto de fin de año por internet. Se abre la venta a las 20:00. A las 20:01 entran 3.000 personas juntas. A las 20:03 la página deja de responder. Nadie puede comprar.

Las preguntas que aparecen son: ¿quién se dio cuenta primero?, ¿quién lo arregla y a qué hora?, ¿cómo evitamos que pase de nuevo?

Responder esas tres preguntas de manera ordenada es, en esencia, el trabajo de **SRE**.

> **SRE (Site Reliability Engineering, "ingeniería de confiabilidad de sitios")**: una disciplina que aplica técnicas de ingeniería de software para mantener los sistemas funcionando de forma confiable. Ejemplo cotidiano: es como tener un equipo de mecánicos que, en lugar de arreglar el auto cada vez que se rompe, diseña el auto para que se rompa menos y tenga sensores que avisan antes.

### De dónde viene

- Se originó en **Google**, alrededor de **2003**. Ben Treynor Sloss, entonces un ingeniero de software, recibió la tarea de dirigir un equipo de siete personas de "producción" (el equipo que mantiene los servicios andando).
- Su idea fue simple: en vez de contratar administradores de sistemas que hagan todo a mano, formar un equipo de **ingenieros de software** que escriba programas para operar los sistemas.
- Su frase más citada, tomada del libro oficial de Google:

> "SRE es lo que pasa cuando le pedís a un ingeniero de software que diseñe un equipo de operaciones." (Ben Treynor Sloss, en *Site Reliability Engineering*, Google, 2016; cita traducida.)

Otra frase típica del mundo SRE, que aparece en el mismo libro: **"la esperanza no es una estrategia"**. Es decir, "ojalá no se caiga" no es un plan.

### El conflicto que SRE viene a resolver

Dentro de una empresa hay dos grupos con objetivos que chocan:

| Grupo | Qué le piden | Qué quiere |
|---|---|---|
| **Desarrollo (Dev)** | Sacar funcionalidades nuevas rápido | Cambiar mucho, lanzar seguido |
| **Operaciones (Ops)** | Que el sistema no se caiga | Cambiar poco, porque **cada cambio es un riesgo** |

Resultado clásico: Dev "tira el código por arriba del muro" y Ops lo frena por miedo. Se pelean, se echan la culpa y el producto sufre.

```
   Desarrollo                         Operaciones
   "¡Lancemos                         "¡No toquen
    cosas nuevas!"      ──MURO──       nada, funciona!"
```

SRE resuelve el choque con **datos y reglas acordadas**, no con discusiones. La herramienta central es el *error budget* (sección 6): un número que dice cuánto riesgo se puede correr, y que Dev y Ops aceptan de antemano.

---

## 2. SRE, DevOps y sysadmin

Tres palabras que se confunden. Vamos de a una.

> **Sysadmin (administrador de sistemas)**: persona que instala, configura y arregla servidores, mayormente a mano. Analogía: el plomero del edificio que responde cuando alguien avisa que hay una pérdida.

> **DevOps**: una **cultura y conjunto de prácticas** para que desarrollo y operaciones trabajen juntos, con automatización y entrega frecuente. Analogía: en vez de que el arquitecto y el constructor se pasen los planos por el buzón, trabajan en la misma obra.

> **SRE**: una **forma concreta de hacerlo**, con roles, métricas y reglas definidas. Analogía: si DevOps es "hay que comer sano", SRE es el plan de comidas con calorías y horarios.

La documentación de Google lo resume con una metáfora de programación que ustedes entienden bien:

```java
// La idea, en código (metáfora, no código real)
interface DevOps { /* filosofía: colaborar, medir, automatizar */ }

class SRE implements DevOps { /* prácticas concretas: SLO, error budget, postmortems... */ }
```

Esa frase, *"class SRE implements interface DevOps"*, aparece en el SRE Workbook de Google.

---

## 3. La confiabilidad es una característica

Cuando pensamos en "features" de una app, pensamos en botones, pantallas y funciones nuevas. Pero hay una característica que los usuarios notan más que cualquier otra: **que la app funcione cuando la necesitan**.

> **Confiabilidad**: la probabilidad de que un sistema haga lo que tiene que hacer, de forma correcta, durante el tiempo que se lo necesita.

Ejemplos:

- Una app de home banking con un diseño hermoso, pero que se cae el día 10 (cuando todos cobran), **no sirve**.
- Un sistema de inscripción a exámenes que anda perfecto todo el año, pero se cuelga la noche en que abre la inscripción, **falló justo cuando importaba**.

Tres ideas para llevarse:

1. **Nadie usa una app que no anda.** Una feature nueva no vale nada si el sistema está caído.
2. **100% no existe** (ni conviene). Cada "9" extra cuesta mucho más que el anterior, y el usuario muchas veces ni lo nota: su celular, su wifi y su proveedor de internet también fallan.
3. **La confiabilidad se decide y se mide**, no se espera. Por eso las próximas secciones hablan de números: SLI, SLO y error budget.

---

## 4. SLI, SLO y SLA

Tres siglas parecidas que **no significan lo mismo**. Definiciones del libro de Google, con analogías:

| Sigla | Nombre | Qué es | Analogía (delivery de pizza) |
|---|---|---|---|
| **SLI** | Service Level **Indicator** | Una **medición**: qué tan bien anda algo | "Cuánto tardó cada pizza en llegar" |
| **SLO** | Service Level **Objective** | Una **meta** para ese SLI | "El 95% de las pizzas llegan en menos de 40 minutos" |
| **SLA** | Service Level **Agreement** | Un **contrato** con consecuencias si no se cumple | "Si tarda más de 40 minutos, la pizza es gratis" |

La pregunta que distingue un SLO de un SLA es: **"¿qué pasa si no se cumple?"**. Si no hay consecuencia formal (devolución de plata, multa), es un SLO.

```
Se mide  ──►  SLI   (dato real: "hoy el 99,7% de las requests salieron bien")
Se apunta ──► SLO   (meta interna: "queremos 99,9%")
Se promete ─► SLA   (contrato con el cliente: "si baja de 99,5%, te devolvemos plata")
```

> **Regla práctica**: el SLO interno debe ser **más exigente** que el SLA. Así tenés margen para reaccionar antes de pagar una multa.

### Buenos SLIs: pensá como el usuario

Un buen SLI mide lo que el **usuario siente**, no lo que le pasa al servidor. Que la CPU esté al 80% no le importa a nadie; que la página tarde 10 segundos, sí.

Fórmula general de un SLI: `eventos buenos / eventos válidos x 100`.

### Ejemplo: tu API de Go de la Unidad 6

Recordá esa API: Go, JWT para autenticación, MongoDB para los datos, corriendo en un contenedor de la Unidad 4. Estos podrían ser SLIs y SLOs razonables:

| Qué le importa al usuario | SLI (cómo lo medimos) | SLO (la meta) |
|---|---|---|
| "La API responde" | Requests que **no** devuelven 5xx / total de requests | 99,9% en 30 días |
| "Responde rápido" | Requests `GET` que responden en menos de 300 ms / total | 95% en 30 días |
| "Puedo iniciar sesión" | Logins exitosos (o rechazados por credenciales mal, que no es falla nuestra) / logins válidos | 99,5% en 30 días |

> **Cuidado**: un `401` por password incorrecta **no es una falla del servicio**; es el sistema funcionando bien. Un `500` sí lo es. Elegir qué cuenta como "bueno" es parte del diseño del SLI.

### Ejemplo de SLO escrito como configuración (ilustrativo)

No existe un formato único; esto es solo para que veas que un SLO se puede escribir de forma precisa. **Es un ejemplo ilustrativo, no un archivo de una herramienta real.**

```yaml
# ILUSTRATIVO: no corresponde a ninguna herramienta concreta
servicio: api-go
slo:
  nombre: disponibilidad
  objetivo: 99.9          # porcentaje
  ventana: 30d            # se evalúa sobre los últimos 30 días
  sli:
    buenos:  "requests con status < 500"
    validos: "todas las requests (excepto health checks)"
```

### Por qué no usar el promedio para la latencia

Si 99 requests tardan 100 ms y 1 tarda 10 segundos, el promedio da unos 200 ms ("parece bien"), pero un usuario de cada cien la pasó pésimo. Por eso se usan **percentiles**: el **p95** es el valor por debajo del cual está el 95% de las mediciones. Analogía: en un curso de 40 alumnos, el p95 de altura deja afuera a los 2 más altos.

---

## 5. Los nueves de disponibilidad

> **Disponibilidad**: el porcentaje del tiempo (o de las requests) en que el servicio funciona correctamente. Se suele nombrar por "cantidad de nueves": 99,9% son "tres nueves".

La tabla que todo SRE tiene en la cabeza. Calculada sobre un año de 365 días, un mes de 30 días y una semana:

| Disponibilidad | "Nueves" | Caída permitida por año | Por mes (30 días) | Por semana |
|---|---|---|---|---|
| 99% | dos | 3,65 días (5.256 min) | 7,2 horas (432 min) | 1,68 horas (100,8 min) |
| 99,5% | dos y medio | 1,83 días (2.628 min) | 3,6 horas (216 min) | 50,4 min |
| 99,9% | tres | 8,76 horas (525,6 min) | 43,2 min | 10,08 min |
| 99,95% | tres y medio | 4,38 horas (262,8 min) | 21,6 min | 5,04 min |
| 99,99% | cuatro | 52,56 min | 4,32 min | 1,01 min |
| 99,999% | cinco | 5,26 min | 26 segundos | 6 segundos |

### Cómo se calcula (para que lo puedas verificar)

```
minutos permitidos de caída = (1 - disponibilidad) x minutos del período

Ejemplo: 99,9% en 30 días
  minutos del período = 30 x 24 x 60 = 43.200
  (1 - 0,999) x 43.200 = 0,001 x 43.200 = 43,2 minutos
```

### Disponibilidad de sistemas encadenados

Si tu API depende de otros componentes en serie, las disponibilidades se **multiplican**:

```
API (99,9%) x Base de datos (99,9%) x Proveedor de login externo (99,9%)
= 0,999 x 0,999 x 0,999 = 0,997  → 99,7%
```

Tres componentes de "tres nueves" dan menos de "tres nueves". Por eso importa tanto la sección 14 (redundancia y degradación elegante).

---

## 6. Error budget: el presupuesto de errores

> **Error budget (presupuesto de errores)**: la cantidad de fallas que el servicio **puede** tener sin romper su SLO. Analogía: tu mesada. Si tenés $10.000 al mes, podés gastarlos en lo que quieras, pero cuando se acaban, se acabó hasta el mes que viene.

Es simplemente la resta:

```
error budget = 100% - SLO
```

- SLO de 99,9% → presupuesto de **0,1%** de fallas.
- En 30 días son **43,2 minutos** de caída (si se mide por tiempo).
- Por requests: con 1.000.000 de requests en el mes, podés fallar **1.000** y seguir cumpliendo.

### La idea central: la falla deja de ser "el enemigo"

El presupuesto convierte una discusión emocional en una regla objetiva:

| Estado del presupuesto | Qué se hace |
|---|---|
| **Sobra** presupuesto | Se lanzan features nuevas, se hacen experimentos, se toman riesgos |
| **Se está acabando** | Se frena o se ralentiza el lanzamiento de cosas nuevas |
| **Se agotó** | Se congelan los lanzamientos (salvo arreglos de confiabilidad y de seguridad) hasta que se recupere |

Así se resuelve el conflicto de la sección 1: Dev y Ops **acuerdan la regla antes** y después la regla decide. Nadie tiene que "ganar la pelea".

---

## 7. Monitoreo y observabilidad

### Monitoreo

> **Monitoreo**: recolectar, procesar y mostrar datos sobre un sistema en tiempo real para saber si está bien. Analogía: el tablero del auto (velocidad, temperatura, nafta). Te avisa que **algo** anda mal.

### Observabilidad

> **Observabilidad**: la capacidad de entender **por qué** un sistema hace lo que hace mirando lo que emite hacia afuera. Analogía: el tablero te dice que el motor se recalienta; la observabilidad es poder abrir el capó y ver qué pieza falla sin desarmar todo el auto.

En resumen: el monitoreo responde "¿anda bien?", la observabilidad responde "¿por qué no anda bien?", incluso frente a problemas que no anticipaste.

### Los tres pilares: métricas, logs y trazas

| Señal | Qué es | Analogía | Ejemplo en tu API |
|---|---|---|---|
| **Métricas** | Números medidos a lo largo del tiempo | La temperatura registrada cada minuto | Requests por segundo, latencia p95 |
| **Logs** | Registro de eventos, uno por uno, con texto | El diario del sistema | `"error conectando a MongoDB: timeout"` |
| **Trazas (traces)** | El recorrido completo de **una** request por todos los componentes | El seguimiento de un paquete de correo, punto por punto | Request → API Go → validación JWT → MongoDB (mostrando cuánto tardó cada tramo) |

Cada una responde algo distinto:

- **Métricas**: *¿hay un problema y qué tan grave?* (baratas y agregadas)
- **Trazas**: *¿en qué parte del recorrido está el problema?*
- **Logs**: *¿qué pasó exactamente en ese momento?*

### Herramientas (nivel conceptual)

| Herramienta | Para qué sirve | Analogía |
|---|---|---|
| **Prometheus** | Base de datos de métricas: cada cierto tiempo consulta ("scrapea") tus servicios y guarda los números. Tiene el lenguaje **PromQL** para consultarlos | El que anota la temperatura cada minuto |
| **Grafana** | Dibuja **tableros** (gráficos) a partir de esas métricas (y de logs y trazas) | El tablero del auto |
| **OpenTelemetry (OTel)** | Estándar abierto para que tu código **emita** métricas, logs y trazas de forma uniforme, sin atarte a un proveedor | El enchufe universal: cualquier aparato entra |
| **Loki / Tempo** (del ecosistema Grafana) | Almacenar y consultar logs (Loki) y trazas (Tempo) | Archivo de diarios y de paquetes |

### Ejemplo de consultas PromQL (ilustrativas)

Los nombres de las métricas dependen de cómo instrumentes tu API; los de abajo son convenciones habituales, **no algo que tu API ya expone**.

```promql
# Requests por segundo (tráfico), promedio de los últimos 5 minutos
sum(rate(http_requests_total[5m]))

# Proporción de errores 5xx sobre el total
sum(rate(http_requests_total{status=~"5.."}[5m]))
  /
sum(rate(http_requests_total[5m]))

# Latencia p95 a partir de un histograma
histogram_quantile(0.95,
  sum by (le) (rate(http_request_duration_seconds_bucket[5m])))
```

`rate(...[5m])` calcula cuánto crece por segundo un contador en los últimos 5 minutos. `histogram_quantile(0.95, ...)` estima el percentil 95.

```
┌───────────┐   scrape    ┌────────────┐   consulta   ┌─────────┐
│ API en Go │ ──────────► │ Prometheus │ ───────────► │ Grafana │
│ /metrics  │  cada 15 s  │ (métricas) │   (PromQL)   │(tablero)│
└───────────┘             └────────────┘              └─────────┘
```

---

## 8. Los cuatro golden signals

Si solo pudieras medir cuatro cosas de un servicio, el libro de Google dice cuáles: los **cuatro golden signals** ("señales de oro").

| Señal | Pregunta que responde | Ejemplo en la API de la materia |
|---|---|---|
| **Latencia** | ¿Cuánto tarda en responder? | p95 del tiempo de `GET /recetas` |
| **Tráfico** | ¿Cuánta demanda recibe? | Requests por segundo |
| **Errores** | ¿Qué proporción de requests falla? | % de respuestas 5xx |
| **Saturación** | ¿Qué tan "llena" está la parte más limitada? | Memoria del contenedor, conexiones a MongoDB usadas |

### Detalles importantes (del libro de Google)

- **Latencia**: separar la de las requests **exitosas** y la de las **fallidas**. "Un error lento es peor que un error rápido": si un error tarda 30 segundos, el usuario espera para nada.
- **Errores**: no solo los `500`. También cuentan los errores "silenciosos": una respuesta `200` con el contenido equivocado, o una respuesta que tarda más de lo prometido.
- **Saturación**: se mira el recurso más limitado (memoria, disco, conexiones). Muchos sistemas se **degradan antes** de llegar al 100%, así que conviene alertar antes.

---

## 9. Alertas útiles vs ruido

> **Alerta**: un aviso automático (mensaje, llamada, notificación) que le dice a una persona "hay algo que necesita tu atención".

### El problema: la fatiga de alertas

Imaginá un celular que suena 200 veces por día. En pocos días dejás de mirarlo. Con las alertas pasa lo mismo:

> **Fatiga de alertas**: cuando hay tantas alertas (muchas falsas o irrelevantes) que las personas dejan de prestarles atención, y se pierde la alerta importante entre el ruido.


### Regla de oro: alertar por síntomas, no por causas

Según el libro de Google, una alerta que despierta a alguien (un *page*) debe indicar una situación **urgente, accionable y con impacto en usuarios (actual o inminente)**.

| Alerta mala (causa) | Alerta buena (síntoma) |
|---|---|
| "La CPU pasó del 90%" | "El 5% de las requests están fallando" |
| "Un pod se reinició" | "La latencia p95 supera 1 segundo hace 10 minutos" |
| "El disco está al 70%" | "El disco se llena en menos de 4 horas" (predictivo y urgente) |

Si la CPU está al 90% pero los usuarios están felices, **no hay nada que hacer**: despertar a alguien a las 3 de la mañana sería un desperdicio.

### Alertas basadas en el error budget

En lugar de "hay un error", se alerta por **qué tan rápido se está gastando el presupuesto** (burn rate). El SRE Workbook de Google recomienda, para un SLO de 99,9% en 30 días:

| Severidad | Ventana larga | Ventana corta | Burn rate | Presupuesto consumido |
|---|---|---|---|---|
| Page | 1 hora | 5 min | 14,4 | 2% |
| Page | 6 horas | 30 min | 6 | 5% |
| Ticket | 3 días | 6 horas | 1 | 10% |

La alerta se dispara solo si **ambas** ventanas superan el umbral. La ventana corta evita que la alerta siga sonando cuando el problema ya se solucionó.

Verificación del primer renglón: un burn rate de 14,4 durante 1 hora consume `14,4 x 1 h / 720 h = 2%` del presupuesto de 30 días (720 horas).

### On-call (guardia)

> **On-call (guardia)**: turno en el que una persona está disponible para responder si suena una alerta urgente. Analogía: el médico de guardia o la farmacia de turno.

Buenas prácticas:

- **Rotación**: nadie está de guardia siempre; se turnan.
- **Runbooks**: cada alerta enlaza a una guía con qué hacer (ver abajo).

### Runbook mínimo (ejemplo)

> **Runbook**: guía paso a paso para resolver un problema conocido. Analogía: las instrucciones que vienen pegadas en el matafuegos.

```
ALERTA: api-go-alta-tasa-de-errores-5xx

Qué significa:
  Más del 5% de las requests devuelven 5xx durante 5 minutos.

Impacto:
  Usuarios no pueden ver ni crear recetas.

Pasos:
  1. Mirar el tablero "API Go" y confirmar el % de errores.
  2. ¿Hubo un deploy en la última hora? Si sí -> hacer rollback
     (kubectl rollout undo deployment/api-go).
  3. Revisar logs: buscar "mongo" o "timeout".
  4. Si MongoDB no responde -> ver estado del contenedor/servicio de la base.
  5. Si no se resuelve en 15 minutos -> escalar a la persona de segunda línea.

```

Es un ejemplo mínimo. Un buen runbook es corto, específico, y se prueba.

---

## 10. Toil y automatización

> **Toil**: trabajo operativo manual, repetitivo, automatizable, sin valor duradero, que crece a medida que crece el servicio. Analogía: lavar los platos a mano todos los días teniendo un lavavajillas en la cocina.

El libro de Google lo define con varias características. Una tarea es *toil* si es:

- **Manual** (alguien tiene que hacerlo a mano).
- **Repetitiva** (una y otra vez).
- **Automatizable** (una máquina podría hacerlo).
- **Táctica** (reactiva, sin mejora duradera).
- **Crece linealmente** con el tamaño del servicio (el doble de usuarios, el doble de trabajo).

### La regla del 50%

Google pone un **tope del 50%** al trabajo de "operaciones" (tickets, guardias, tareas manuales) de los SRE. El resto del tiempo se dedica a **ingeniería**: automatizar, mejorar, diseñar. Si el toil supera ese tope, se devuelve trabajo al equipo de desarrollo o se frenan tareas hasta automatizar.

> **Concepto**: el objetivo de SRE es **quedarse sin trabajo repetitivo**: cada vez que hacés algo a mano por segunda vez, pensá si un script lo puede hacer.

---

## 11. Gestión de incidentes

> **Incidente**: un evento que causa (o puede causar) una degradación o interrupción de un servicio. Analogía: un incendio en el edificio. No se resuelve "improvisando": hay protocolos, roles y salidas de emergencia.

### Severidades

No todos los incidentes son iguales. Cada empresa define su escala; esta es un ejemplo típico (**ilustrativa**, no una norma):

| Severidad | Impacto | Ejemplo con la API de la materia | Respuesta |
|---|---|---|---|
| **SEV1** | Caída total o pérdida de datos | La API no responde para nadie | Todos los que hagan falta, ya, a cualquier hora |
| **SEV2** | Función importante rota, o degradación fuerte | El login falla para la mitad de los usuarios | Respuesta inmediata en horario extendido |
| **SEV3** | Problema menor, hay alternativa | La búsqueda es lenta | Se resuelve en horario laboral |

### Roles

En un incidente grande, si todos hacen todo, nadie coordina. Por eso se **asignan roles** (Atlassian y PagerDuty describen esquemas muy parecidos):

| Rol | Qué hace | Analogía |
|---|---|---|
| **Comandante del incidente** (Incident Commander) | Coordina, decide, delega. **No** arregla el problema | El director técnico |
| **Responsables técnicos** (ops / resolvers) | Investigan y arreglan | Los jugadores en la cancha |
| **Comunicaciones** | Informa a usuarios y a la empresa | El vocero del club |
| **Escriba** (scribe) | Anota en tiempo real qué se hizo y cuándo | El que lleva el acta |

> **Por qué separar**: quien está concentrado depurando código no puede, además, contestar mails de clientes ni coordinar a otras diez personas.

### Métricas de incidentes

Cuatro siglas que vas a ver seguido:

| Sigla | Significa | Pregunta que responde |
|---|---|---|
| **MTTD** | Mean Time To Detect: tiempo medio de **detección** | ¿Cuánto tardamos en darnos cuenta? |
| **MTTR** | Mean Time To Recover/Repair/Respond/Resolve (¡la R tiene varios significados!) | ¿Cuánto tardamos en arreglarlo? |
| **MTBF** | Mean Time Between Failures: tiempo medio **entre** fallas | ¿Cada cuánto se rompe? |
| **MTTA** | Mean Time To Acknowledge: tiempo medio hasta que alguien **atiende** la alerta | ¿Cuánto tardó alguien en decir "yo me encargo"? |

> **Cuidado con MTTR**: la "R" puede significar recuperar, reparar, responder o resolver, y cada una mide algo distinto. Al usar la sigla, aclará cuál usás. Atlassian lo explica en su guía de métricas (ver Recursos).

Ejemplo: en un mes hubo 3 incidentes que duraron 10, 30 y 20 minutos.

```
MTTR = (10 + 30 + 20) / 3 = 20 minutos
MTBF = (tiempo total en funcionamiento) / (cantidad de fallas)
     = (43.200 min - 60 min de caída) / 3 = 14.380 minutos (unos 10 días)
```

### Tres incidentes reales (documentados públicamente)

Estos casos tienen relatos oficiales publicados por las propias empresas.

| Incidente | Qué pasó | Qué enseña |
|---|---|---|
| **Amazon S3, 28 de febrero de 2017** | Durante una tarea de mantenimiento, un integrante del equipo ejecutó un comando para quitar capacidad y **escribió mal un parámetro**: se sacaron más servidores de los previstos. Afectó subsistemas críticos de S3. Duró aproximadamente 4 horas y 17 minutos. Amazon cambió la herramienta para que quite capacidad más despacio y no pueda bajar de un mínimo | Las herramientas deben tener **barandas de seguridad**. El error humano es inevitable; el sistema tiene que resistirlo |
| **GitLab, 31 de enero de 2017** | Un ingeniero, durante una emergencia, borró datos en el **servidor equivocado** (la base primaria en lugar de la secundaria): unos 300 GB eliminados antes de que lo frenara. Se perdieron los cambios entre las 17:20 y las 00:00 UTC (unos 5.000 proyectos, 5.000 comentarios y 700 usuarios nuevos). Además, **cuatro de las cinco técnicas de backup fallaron** o no estaban disponibles | **Un backup que nunca probaste restaurar no es un backup.** GitLab publicó un postmortem transparente y sin buscar culpables |
| **Facebook (Meta), 4 de octubre de 2021** | Un comando de mantenimiento, pensado para evaluar la capacidad de la red troncal, desconectó por error **todas** las conexiones de la red troncal. Un fallo en la herramienta de auditoría no lo frenó. Al quedar desconectados, los servidores DNS retiraron sus anuncios de red (BGP) y dejaron de ser alcanzables desde internet. Las herramientas internas también dependían de esa red, así que **la recuperación fue lenta**: hasta hubo que acceder físicamente a los centros de datos | **Evitá que las herramientas de emergencia dependan de lo que se rompió.** Tener un "plan B" para entrar cuando todo falla |

Fuentes en Recursos. Fijate que en los tres casos el punto de partida fue un **cambio de rutina**, no un ataque ni un desastre natural. Por eso los despliegues seguros (sección 13) son tan importantes.

---

## 12. Postmortems sin culpa

> **Postmortem (análisis posterior al incidente)**: documento escrito después de un incidente que cuenta qué pasó, por qué, cómo se resolvió y qué se va a hacer para que no se repita. Analogía: la investigación de un accidente aéreo: no busca al culpable, busca por qué el sistema permitió que pasara.

### Sin culpa (blameless)

> **Blameless (sin culpa)**: cultura en la que se analiza **qué falló en el sistema y en el proceso**, no quién se equivocó. Analogía: si un alumno aprueba cuatro veces mal el mismo tema, la pregunta útil es "¿qué falla en cómo lo explicamos?", no "¿qué le pasa a este alumno?".

¿Por qué? Porque si castigás a quien se equivoca:

- La gente **esconde** los errores y los incidentes.
- Nadie cuenta la verdad completa.
- Los problemas de fondo se repiten.

En cambio, si se considera que las personas actúan de buena fe con la información que tenían, se descubren los problemas reales: falta de barandas, cansancio, documentación confusa, alertas engañosas.


### Plantilla de postmortem

```markdown
# Postmortem: API Go no disponible durante venta de entradas

Fecha: 2026-03-14        Severidad: SEV2      Duración: 47 minutos
Autores: [nombres]       Estado: Revisado

## Resumen
Durante 47 minutos, el 38% de los pedidos a la API devolvieron error 500.

## Impacto
- ~1.200 usuarios no pudieron comprar entradas.
- Se consumieron 47 min del error budget mensual (43,2 min): presupuesto agotado.

## Línea de tiempo (hora local)
20:00  Empieza la venta. Sube el tráfico 10 veces.
20:03  Comienzan errores 500. (Alerta dispara a las 20:08.)
20:10  Persona de guardia confirma; abre el incidente.
20:25  Se detecta que MongoDB llegó al límite de conexiones.
20:40  Se sube el límite y se reinicia la API.
20:47  Errores normalizados.

## Causa raíz
El pool de conexiones a MongoDB tenía un tope de 20, calculado para
tráfico normal. Con 10 veces más tráfico, se agotó y las requests fallaron.

## Qué salió mal
- La alerta tardó 5 minutos en dispararse (MTTD alto).
- Nadie había hecho una prueba de carga.

## Acciones (con responsable y fecha)
| Acción | Responsable | Fecha |
|---|---|---|
| Alertar por saturación del pool a 80% | A. | 2026-03-21 |
| Prueba de carga antes de cada venta | B. | 2026-03-28 |
| Escalar automáticamente la API según tráfico | C. | 2026-04-15 |

```

Este ejemplo es **inventado con fines didácticos**, no es un incidente real.

---

## 13. Despliegues seguros

Muchos incidentes empiezan con un cambio (los tres casos de la sección 11). Por eso SRE se obsesiona con **cómo se lanzan los cambios**.

> **Despliegue (deploy)**: poner una nueva versión del software en producción, a disposición de los usuarios.

### CI/CD

> **CI/CD (Integración y Entrega Continuas)**: automatizar los pasos que van desde que escribís código hasta que llega a producción: compilar, correr tests, empaquetar en un contenedor (como en la Unidad 4), desplegar. Analogía: una cinta transportadora en una fábrica, en lugar de llevar cada pieza a mano.

Flujo típico: `git push` → tests → build de la imagen Docker → deploy a testing → deploy a producción. Si un paso falla, la cadena se corta (y en producción, se hace rollback automático).

Estudios de la investigación **DORA** (ver Recursos) encontraron que los equipos que despliegan **más seguido con cambios chicos** tienen **menos** fallas por cambio, no más. Lo chico es más fácil de entender y de revertir.

### Estrategias de despliegue

| Estrategia | Cómo funciona | Analogía | Ventaja | Costo |
|---|---|---|---|---|
| **Canary (canario)** | La versión nueva recibe primero un **pequeño porcentaje** del tráfico (1%, 5%...). Si las métricas están bien, se sube de a poco | El canario en la mina de carbón: si le pasa algo, salvás al resto | Un error afecta a pocos usuarios | Más complejo, hay dos versiones a la vez |
| **Blue-green** | Hay **dos entornos idénticos**: "azul" (actual) y "verde" (nueva). Se prepara el verde y se **cambia todo el tráfico** de golpe. Si algo sale mal, se vuelve al azul | Tener dos escenarios listos y cambiar de uno al otro | Rollback instantáneo | Necesitás el doble de infraestructura |
| **Rolling update** | Se reemplazan las instancias de a una o de a grupos | Cambiar las ruedas del auto una por una | Sin doble infraestructura | Convivencia temporal de versiones |

```
CANARY
Usuarios ──► Balanceador ──► 95% ──► Versión 1 (estable)
                         └─► 5%  ──► Versión 2 (canario)  ← se vigilan errores y latencia
```

Kubernetes (Unidad 4) hace **rolling updates** por defecto con los `Deployment`, y permite volver atrás con `kubectl rollout undo`.

### Rollback

> **Rollback**: volver a la versión anterior que funcionaba. Analogía: el botón "deshacer" (Ctrl+Z).

Reglas SRE:

- **El rollback debe ser rápido y estar practicado**, no algo que se prueba por primera vez en medio de una crisis.
- Ante la duda, **revertir primero, investigar después**.

### Feature flags

> **Feature flag (interruptor de funcionalidad)**: una condición en el código que enciende o apaga una función sin volver a desplegar. Analogía: la llave de luz de una habitación que ya está cableada.

Ilustrativo en Go: `if flags.Enabled("busqueda-nueva", usuarioID) { return buscarConIndiceNuevo(q) }` y, si no, se usa `buscarClasico(q)`.

Beneficios:

- **Separar desplegar de lanzar**: el código llega a producción apagado y se enciende cuando se decide.
- Encendido gradual: 1% de usuarios, luego 10%, luego todos.
- **Apagado de emergencia** en segundos, sin nuevo despliegue.

Costo: si nunca se limpian, los flags se acumulan y el código se vuelve difícil de entender (ver el artículo de Martin Fowler en Recursos).

---

## 14. Diseñar para que las fallas no manden

En sistemas grandes, **algo siempre está fallando**: un disco, una red, un servicio de terceros. La pregunta no es "¿va a fallar?" sino "¿qué pasa cuando falle?".

### Capacidad, escalado y redundancia

> **Escalar**: aumentar la capacidad. **Verticalmente** = un servidor más grande. **Horizontalmente** = más servidores en paralelo (lo que hace Kubernetes con las réplicas). Con el **autoescalado**, la plataforma agrega instancias sola cuando sube la demanda.

> **Redundancia**: tener más de una copia de lo crítico, para que si una falla, otra tome su lugar. Analogía: la rueda de auxilio.

- En vez de **una** instancia de la API, dos o tres detrás de un balanceador (en Kubernetes: `replicas: 3`).
- MongoDB usa **replica sets**: varias copias de los datos.
- Planificá la capacidad **antes** de eventos conocidos (la venta de entradas, el cierre de mes) y probala con **pruebas de carga**.

### Degradación elegante

> **Degradación elegante (graceful degradation)**: cuando algo falla, el sistema sigue funcionando con menos funciones, en lugar de caerse entero. Analogía: si se corta el ascensor, el edificio sigue abierto y se usa la escalera.

Ejemplos:

- El servicio de recomendaciones no responde → la página muestra una lista genérica, pero se puede comprar igual.
- Falla la base de datos secundaria → la app funciona en "solo lectura".
- Demasiadas requests → se rechazan algunas rápido (con `429` o `503`) en lugar de atender todas lentamente y colapsar (esto se llama *load shedding*).

### Timeouts

> **Timeout (tiempo límite)**: cuánto se espera una respuesta antes de rendirse. Analogía: si el mozo no viene en 10 minutos, te vas del restaurante.

Sin timeout, una llamada lenta puede **quedarse esperando para siempre**, acumulando conexiones y hundiendo al servicio. En Go, `http.Client` **no tiene timeout por defecto**, así que hay que ponerlo explícitamente.

### Reintentos con backoff

> **Retry (reintento)**: volver a intentar una operación que falló, porque muchas fallas son transitorias.

Problema: si todos reintentan **al instante y a la vez**, empeoran el problema. Un servicio que está sufriendo recibe una avalancha de reintentos (una "estampida").

Solución:

> **Backoff exponencial con jitter**: esperar cada vez **más** entre reintentos (1 s, 2 s, 4 s, 8 s...) y sumar un **poco de azar (jitter)** para que no reintenten todos exactamente al mismo tiempo. Analogía: si el teléfono da ocupado, no llamás cada medio segundo: esperás un rato, y cada vez más.

```go
// Ilustrativo: reintento con timeout, backoff exponencial y jitter
func llamarConReintentos(url string) (*http.Response, error) {
	cliente := &http.Client{Timeout: 2 * time.Second} // timeout siempre
	espera := 200 * time.Millisecond

	var ultimoError error
	for intento := 1; intento <= 4; intento++ {
		resp, err := cliente.Get(url)
		if err == nil && resp.StatusCode < 500 {
			return resp, nil // éxito (o error del cliente: reintentar no ayuda)
		}
		if err == nil {
			resp.Body.Close()
			err = fmt.Errorf("status %d", resp.StatusCode)
		}
		ultimoError = err

		jitter := time.Duration(rand.Int63n(int64(espera))) // azar entre 0 y espera
		time.Sleep(espera + jitter)
		espera *= 2 // 200 ms, 400 ms, 800 ms...
	}
	return nil, fmt.Errorf("falló tras 4 intentos: %w", ultimoError)
}
```

Advertencias importantes:

- Solo reintentar operaciones **seguras de repetir** (*idempotentes*): repetir un `GET` no hace daño; repetir un "cobrar" podría cobrar dos veces.
- Poner un **máximo** de reintentos: reintentar infinitamente es otra forma de caerse.

### Circuit breaker (cortacircuitos)

> **Circuit breaker**: un mecanismo que **deja de llamar** a un servicio que está fallando, para no gastar recursos ni empeorar la situación, y lo vuelve a probar de vez en cuando. Analogía: la llave térmica de tu casa: si hay un cortocircuito, corta la corriente para que no se prenda fuego, y después la volvés a subir para probar.

Tiene tres estados:

```
            demasiadas fallas
   CERRADO ───────────────────► ABIERTO
 (deja pasar)                 (rechaza al instante)
      ▲                             │
      │ prueba exitosa              │ pasa un tiempo
      │                             ▼
      └───────────────────── SEMI-ABIERTO
                          (deja pasar una prueba)
```

```go
// Ilustrativo y simplificado (sin concurrencia; en serio, usar una librería probada)
type Breaker struct {
	fallas, limite int
	espera         time.Duration
	abiertoHasta   time.Time
}

var ErrAbierto = errors.New("circuit breaker abierto")

func (b *Breaker) Ejecutar(f func() error) error {
	if time.Now().Before(b.abiertoHasta) {
		return ErrAbierto // falla rápido, sin llamar al servicio
	}
	if err := f(); err != nil {
		if b.fallas++; b.fallas >= b.limite {
			b.abiertoHasta = time.Now().Add(b.espera)
			b.fallas = 0
		}
		return err
	}
	b.fallas = 0 // éxito: se resetea
	return nil
}
```

Cuando el tiempo de espera termina, la próxima llamada vuelve a pasar como "prueba". En la práctica se usan librerías (por ejemplo `sony/gobreaker` en Go, o Resilience4j en Java). Martin Fowler tiene una explicación clásica del patrón (ver Recursos).

---

## 15. Chaos engineering

> **Chaos engineering (ingeniería del caos)**: la práctica de **provocar fallas a propósito, de forma controlada**, para descubrir debilidades antes de que las descubran los usuarios. Analogía: el simulacro de incendio en el colegio: se hace cuando no hay fuego, para saber si las salidas funcionan.

### Ideas clave

- Nació en **Netflix** con **Chaos Monkey**, una herramienta que apaga servidores al azar en producción para verificar que el servicio sigue funcionando.
- Los principios están resumidos en *Principles of Chaos Engineering* (ver Recursos): definir el estado "normal" medible, plantear una **hipótesis** ("si cae un servidor, los usuarios no lo notan"), provocar la falla en un experimento, y comparar.
- Empezar **chico** y controlado, con un plan para frenar el experimento.
- Ejemplo con tu API: hipótesis "si borro un pod de la API, Kubernetes levanta otro y nadie nota nada" (`kubectl delete pod api-go-xxxxx` en un entorno de pruebas). Si aparecen errores, descubriste un problema (faltan réplicas, falta un health check) antes que un usuario.

---

## 16. SRE y la nube

En la primera parte de la clase viste qué es la nube. Acá la conexión:

| En la nube... | Por qué SRE importa más |
|---|---|
| Hay **muchísimos componentes** (servidores virtuales, bases, redes) | Más piezas, más lugares donde puede fallar algo |
| La infraestructura es **de otro** (el proveedor) | No podés arreglar el hardware; solo diseñar para que soporte fallas |
| **Todo es efímero**: las instancias nacen y mueren | La automatización deja de ser opcional |
| **Se paga por uso** | Cada "9" cuesta plata (redundancia, más zonas) |
| Los servicios tienen **SLAs** de los proveedores | Tu disponibilidad depende de ellos (recordá la multiplicación de la sección 5) |

### Modelo de responsabilidad compartida

En la nube el proveedor se ocupa de la infraestructura física, y **vos** de tu aplicación, tu configuración, tus datos y tu diseño de tolerancia a fallas. Que el proveedor sea confiable **no alcanza**: una zona entera puede caerse, y el S3 de 2017 (sección 11) mostró que hasta los servicios más grandes tienen incidentes. Contenedores y Kubernetes (Unidad 4) ya traen `replicas`, health checks, rolling updates y rollbacks: las mismas ideas de este capítulo, incorporadas a la plataforma.

---

## 17. Ejercicio de aula

Trabajen en grupos de 3 o 4 personas. Usen la **API de Go de la Unidad 6** (JWT + MongoDB) y el cliente React de la Unidad 7 como sistema de ejemplo. Duración sugerida: 20 minutos.

### Parte A — Definir SLIs y SLOs

Completen esta tabla para **tres** funciones de la API (por ejemplo: ver recetas, crear receta, iniciar sesión):

| Función | ¿Qué le importa al usuario? | SLI (fórmula) | SLO (meta y ventana) |
|---|---|---|---|
| | | | |

Pistas: pensá como usuario, no como servidor. Decidí qué respuestas cuentan como "buenas" (¿un 401 por password incorrecta es una falla?). Usá percentiles para latencia.

### Parte B — Calcular el error budget

Tomá el SLO de disponibilidad que elegiste y respondé:

1. ¿Cuántos minutos de caída permite en 30 días? (Fórmula: `(1 - SLO) x 43.200`)
2. Si hubo dos incidentes de 12 y 25 minutos, ¿cuánto presupuesto queda con un SLO de 99,9%? ¿Se puede lanzar una función nueva?
3. Si el tráfico es de 500.000 requests por mes, ¿cuántas pueden fallar sin romper un SLO de 99,9%?

Resultados para verificar: 1) con 99,9%: 43,2 min; 2) 43,2 - 37 = 6,2 min restantes: se puede lanzar, pero con canary y mucho cuidado; 3) 0,001 x 500.000 = 500 requests.

### Parte C — Analizar un incidente

Leé este caso (inventado para el ejercicio):

> Un viernes a las 18:00 se despliega una nueva versión de la API. A las 18:20 empiezan a llegar quejas: la lista de recetas devuelve error. Nadie se enteró antes porque no hay alertas configuradas. A las 19:10, la persona de guardia (que no sabía que estaba de guardia) ve el mensaje. Tarda 40 minutos en encontrar el problema: la nueva versión usa un campo de MongoDB que no existe en los datos viejos. Recién a las 19:50 hace rollback. Pierde tiempo porque no hay una guía de cómo hacerlo. El lunes el jefe pregunta quién fue el culpable del deploy.

Respondan:

1. ¿Cuál fue el MTTD? ¿Y el tiempo total del incidente? (Pista: hay un inicio real de la falla, un momento de detección y un momento de recuperación.)
2. ¿Qué **tres** cosas del proceso fallaron (no personas)?
3. ¿Qué alerta escribirían? ¿Sobre qué señal (golden signal)?
4. ¿Qué estrategia de despliegue habría reducido el impacto?
5. Escriban dos acciones concretas para el postmortem, con responsable y fecha. Y reformulen la pregunta del jefe para que sea **sin culpa**.

Respuestas de referencia: 1) MTTD = 50 min (de 18:20 a 19:10); duración total = 90 min (18:20 a 19:50). 2) Sin alertas, guardia mal comunicada, sin runbook (y sin canary). 3) Errores: proporción de respuestas 5xx. 4) Canary. 5) "¿Qué en nuestro proceso permitió que llegara a producción sin detectarse?"

---

## 18. Recursos recomendados

Todos los enlaces fueron verificados antes de incluirlos.

### Libros de Google (gratis, online)

- [Site Reliability Engineering (SRE Book)](https://sre.google/sre-book/table-of-contents/): el libro original de Google, con capítulos sobre SLOs, error budgets, monitoreo, incidentes y postmortems.
- [The Site Reliability Workbook](https://sre.google/workbook/table-of-contents/): la versión práctica, con ejemplos de cómo implementar SLOs y alertas en la vida real.
- [Service Level Objectives](https://sre.google/sre-book/service-level-objectives/): definiciones precisas de SLI, SLO y SLA con ejemplos.
- [Embracing Risk](https://sre.google/sre-book/embracing-risk/): por qué 100% no es la meta y cómo se usa el error budget.
- [Monitoring Distributed Systems](https://sre.google/sre-book/monitoring-distributed-systems/): los cuatro golden signals y cómo alertar por síntomas.
- [Alerting on SLOs](https://sre.google/workbook/alerting-on-slos/): alertas de múltiples ventanas y velocidad de consumo (burn rate).
- [Error Budget Policy](https://sre.google/workbook/error-budget-policy/): un ejemplo de política de error budget.
- [Canarying Releases](https://sre.google/workbook/canarying-releases/): despliegues canary bien hechos.
- [How SRE relates to DevOps](https://sre.google/workbook/how-sre-relates/): la relación entre SRE y DevOps ("class SRE implements interface DevOps").
- [Supercharge your DevOps practice with SRE principles](https://cloud.google.com/blog/products/devops-sre/supercharge-your-devops-practice-with-sre-principles): cómo aplicar principios SRE en una práctica DevOps.

### Gestión de incidentes y postmortems

- [Atlassian Incident Management Handbook](https://www.atlassian.com/incident-management/handbook): guía práctica de severidades, roles y comunicación.
- [Atlassian: métricas comunes de incidentes](https://www.atlassian.com/incident-management/kpis/common-metrics): qué son MTTR, MTBF, MTTA y sus variantes.
- [Atlassian: postmortems sin culpa](https://www.atlassian.com/incident-management/postmortem/blameless): cómo hacer análisis posteriores sin buscar culpables.
- [PagerDuty Incident Response](https://response.pagerduty.com/): documentación abierta del proceso de respuesta a incidentes de PagerDuty, con roles, guardias y entrenamiento.
- [Etsy Debriefing Facilitation Guide](https://extfiles.etsy.com/DebriefingFacilitationGuide.pdf): guía de Etsy para conducir reuniones de análisis posterior al incidente.
- [Colección de postmortems públicos (danluu/post-mortems)](https://github.com/danluu/post-mortems): lista enorme de postmortems reales de empresas, ideal para aprender de casos.
- [Resumen del incidente de Amazon S3 (2017)](https://aws.amazon.com/message/41926/): el relato oficial de Amazon sobre el comando mal escrito.
- [Postmortem de GitLab (31/1/2017)](https://about.gitlab.com/blog/postmortem-of-database-outage-of-january-31/): relato transparente de la pérdida de datos y los backups que fallaron.
- [Detalles de la caída de Facebook (2021)](https://engineering.fb.com/2021/10/05/networking-traffic/outage-details/): explicación oficial de la caída del 4 de octubre de 2021.

### Monitoreo y observabilidad

- [Prometheus: introducción](https://prometheus.io/docs/introduction/overview/): qué es Prometheus, cómo funciona y para qué sirve.
- [Prometheus: consultas (PromQL)](https://prometheus.io/docs/prometheus/latest/querying/basics/): fundamentos del lenguaje de consultas.
- [OpenTelemetry](https://opentelemetry.io/docs/): estándar abierto para métricas, logs y trazas.
- [Documentación de Grafana](https://grafana.com/docs/grafana/latest/): tableros y visualización.

### Despliegues, resiliencia y caos

- [Martin Fowler: Canary Release](https://martinfowler.com/bliki/CanaryRelease.html): explicación breve y clara del despliegue canario.
- [Martin Fowler: Feature Toggles](https://martinfowler.com/articles/feature-toggles.html): tipos de feature flags y cómo manejarlos sin generar desorden.
- [Martin Fowler: Circuit Breaker](https://martinfowler.com/bliki/CircuitBreaker.html): el patrón cortacircuitos explicado.
- [Amazon Builders' Library: timeouts, reintentos y backoff con jitter](https://aws.amazon.com/builders-library/timeouts-retries-and-backoff-with-jitter/): cómo Amazon configura reintentos sin empeorar las cosas.
- [Principles of Chaos Engineering](https://principlesofchaos.org/): el manifiesto de la ingeniería del caos.
- [Netflix Chaos Monkey](https://github.com/Netflix/chaosmonkey): la herramienta original que apaga instancias al azar.

### Cultura, métricas y carrera

- [DORA](https://dora.dev/): la investigación sobre rendimiento de equipos de software (base del libro *Accelerate*), con las métricas de frecuencia de despliegue, tiempo de entrega, tasa de fallos y tiempo de recuperación.
- [roadmap.sh: DevOps](https://roadmap.sh/devops): mapa visual de qué aprender para trabajar en DevOps y afines.
- [SREcon (USENIX)](https://www.usenix.org/conference/srecon): la conferencia de ingeniería de confiabilidad, con charlas y videos públicos.

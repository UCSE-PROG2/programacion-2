# Unidad 8 — Cloud Computing y SRE

Material de apoyo para la **Unidad 8** de **Programación 2** — Ingeniería en Computación (UCSE).

Hasta acá aprendieron a **construir** software: una API en Java o Go, un cliente en React, contenedores con Docker. Esta unidad responde la pregunta que viene después: **¿dónde corre eso cuando lo usa gente de verdad, y cómo hacemos para que no se caiga?**

Son dos temas que van de la mano:

- **Cloud Computing**: *dónde* corre tu software (computadoras que se alquilan por internet).
- **SRE (Site Reliability Engineering)**: *cómo* lo mantenemos funcionando (medir, alertar, aprender de los errores).

---

## Índice

1. [Introducción: de tu laptop a millones de usuarios](#1-introducción-de-tu-laptop-a-millones-de-usuarios)
2. [Cloud Computing en pocas palabras](#2-cloud-computing-en-pocas-palabras)
3. [SRE en pocas palabras](#3-sre-en-pocas-palabras)
4. [Cómo se conectan los dos temas](#4-cómo-se-conectan-los-dos-temas)
5. [Material de la unidad](#5-material-de-la-unidad)
6. [Plan sugerido para la clase de 2 horas](#6-plan-sugerido-para-la-clase-de-2-horas)
7. [Recursos recomendados](#7-recursos-recomendados)

---

## 1. Introducción: de tu laptop a millones de usuarios

Imaginá que armás una app para vender entradas del recital de fin de año de tu colegio. En tu laptop anda perfecto. Ahora pasan tres cosas:

1. **Tiene que estar prendida las 24 horas.** Tu laptop se apaga, se queda sin batería o se va el wifi.
2. **De golpe entran 5.000 personas en el mismo minuto** (cuando abre la venta). Tu laptop no aguanta.
3. **Algo falla a las 3 de la mañana.** ¿Quién se entera? ¿Cómo lo arreglan? ¿Cómo evitan que vuelva a pasar?

Cada problema tiene un nombre:

| Problema | Tema que lo resuelve |
|----------|----------------------|
| Necesito computadoras siempre prendidas, y más cuando hay mucha gente | **Cloud Computing** |
| Necesito saber si mi sistema funciona bien y qué hacer cuando falla | **SRE** |

> **Idea central**: escribir el código es solo una parte del trabajo. La otra parte es que ese código **corra, escale y se mantenga funcionando** en el mundo real.

---

## 2. Cloud Computing en pocas palabras

> **Cloud Computing (computación en la nube)**: usar computadoras, almacenamiento y servicios de otra empresa a través de internet, pagando solo por lo que usás, sin comprar ni mantener los equipos.

**Analogía**: en vez de construir una usina para tener electricidad, te conectás a la red eléctrica y pagás por kilowatt consumido. La "nube" no es magia ni algo flotando: son **centros de datos** (edificios llenos de servidores) de empresas como Amazon, Microsoft o Google.

**Ejemplo**: el contenedor Docker que armaste en la Unidad 4 puede correr en tu laptop, o en un servidor de Amazon que alquilás por hora. El contenedor es el mismo; cambia *dónde* corre.

Lo que vas a ver en [`cloud-computing.md`](cloud-computing.md):

- Por qué existe la nube (on-premise vs cloud).
- La definición oficial del NIST y sus 5 características.
- IaaS, PaaS, SaaS y serverless (quién se encarga de qué).
- Regiones y zonas de disponibilidad.
- Cómputo, almacenamiento, bases de datos y redes.
- Escalar, seguridad, costos e infraestructura como código.
- AWS, Azure y Google Cloud, y cuándo *no* conviene la nube.

---

## 3. SRE en pocas palabras

> **SRE (Site Reliability Engineering, Ingeniería de Confiabilidad de Sitios)**: una disciplina que nació en Google y que aplica la **ingeniería de software** a los problemas de operar sistemas, para que sean confiables.

**Analogía**: un hospital no espera a que un paciente se muera para actuar. Mide signos vitales (pulso, presión), tiene alarmas, protocolos de emergencia y, cuando algo sale mal, analiza qué pasó **sin buscar culpables** para que no se repita. SRE hace lo mismo con el software.

**Ejemplo**: tu API de Go de la Unidad 6 podría tener una regla como "el 99,9% de los pedidos debe responder bien en menos de medio segundo". Si se cumple, se siguen lanzando features. Si no, se frena y se arregla primero.

Lo que vas a ver en [`sre.md`](sre.md):

- De dónde viene SRE y en qué se diferencia de DevOps.
- SLI, SLO y SLA, y los "nueves" de disponibilidad.
- Error budget: cuántos errores nos podemos permitir.
- Monitoreo, observabilidad y los cuatro golden signals.
- Alertas, on-call, gestión de incidentes y postmortems sin culpa.
- Despliegues seguros y patrones de resiliencia (timeouts, retries, circuit breaker).

---

## 4. Cómo se conectan los dos temas

```
        Tu código (Java / Go / React)
                    │
                    ▼
        Contenedor (Docker, Unidad 4)
                    │
                    ▼
   ┌────────────────────────────────────┐
   │  CLOUD: dónde corre                │
   │  servidores, redes, bases de datos │
   │  que se alquilan y escalan         │
   └────────────────────────────────────┘
                    │
                    ▼
   ┌────────────────────────────────────┐
   │  SRE: cómo se mantiene funcionando │
   │  medir, alertar, responder,        │
   │  aprender de las fallas            │
   └────────────────────────────────────┘
                    │
                    ▼
            Usuarios contentos
```

- La nube te da **poder** (podés tener 1.000 servidores en minutos), pero también **más piezas que pueden fallar**.
- SRE te da el **método** para saber si todo eso anda bien y decidir qué hacer cuando no.
- Sin nube, SRE igual sirve. Sin SRE, la nube es una forma cara de tener un sistema que se cae.

---

## 5. Material de la unidad

| Archivo | Tema | Tiempo sugerido |
|---------|------|-----------------|
| [cloud-computing.md](cloud-computing.md) | Cloud Computing: modelos, servicios, escalado, seguridad, costos, IaC | ~55 min |
| [sre.md](sre.md) | SRE: SLI/SLO/SLA, error budget, observabilidad, incidentes, postmortems | ~55 min |

Los dos archivos terminan con una sección de **recursos recomendados** con enlaces a documentación oficial, libros y cursos gratuitos para seguir profundizando, y con un **ejercicio de aula**.

---

## 6. Plan sugerido para la clase de 2 horas

| Bloque | Duración | Contenido |
|--------|----------|-----------|
| Introducción | 10 min | Esta página: el problema de pasar de la laptop a usuarios reales |
| Cloud Computing | 50 min | Secciones 1 a 9 de `cloud-computing.md`; ejercicio de arquitectura al final |
| Pausa | 5 min | |
| SRE | 50 min | Secciones 1 a 12 de `sre.md`; ejercicio de SLIs/SLOs para la API de la materia |
| Cierre | 5 min | Cómo se conectan los dos temas y qué leer para seguir |

Las secciones restantes de cada archivo (IaC, cloud native, chaos engineering, patrones de resiliencia, etc.) quedan como lectura complementaria.

---

## 7. Recursos recomendados

Para profundizar, empezá por estos. Cada archivo de la unidad tiene una lista más completa.

**Cloud Computing**

- [NIST SP 800-145 — The NIST Definition of Cloud Computing](https://csrc.nist.gov/pubs/sp/800/145/final) — la definición oficial de nube, de una página.
- [AWS — What is Cloud Computing?](https://aws.amazon.com/what-is-cloud-computing/) — introducción del proveedor más usado.
- [The Twelve-Factor App](https://12factor.net/) — buenas prácticas para apps que corren en la nube.

**SRE**

- [Google SRE Book](https://sre.google/sre-book/table-of-contents/) — el libro original, gratis y online.
- [Google SRE Workbook](https://sre.google/workbook/table-of-contents/) — la versión práctica, con ejemplos.

**Más recursos**

- Ver la sección final "Recursos recomendados" de [cloud-computing.md](cloud-computing.md) y de [sre.md](sre.md).

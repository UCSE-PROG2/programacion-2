# Cloud Computing: qué es la nube y cómo se usa

> Material de teoría para la primera mitad de la clase (unos 50-60 minutos). La segunda mitad trata de SRE.

## Índice

1. [Por qué existe la nube](#1-por-qué-existe-la-nube)
2. [La definición oficial (NIST)](#2-la-definición-oficial-nist)
3. [Modelos de servicio: IaaS, PaaS, SaaS y serverless](#3-modelos-de-servicio-iaas-paas-saas-y-serverless)
4. [Modelos de despliegue: pública, privada, híbrida, multi-cloud](#4-modelos-de-despliegue-pública-privada-híbrida-multi-cloud)
5. [Regiones, zonas de disponibilidad y edge](#5-regiones-zonas-de-disponibilidad-y-edge)
6. [Los bloques básicos: cómputo, almacenamiento, bases de datos y redes](#6-los-bloques-básicos-cómputo-almacenamiento-bases-de-datos-y-redes)
7. [Escalar: vertical, horizontal, elasticidad y alta disponibilidad](#7-escalar-vertical-horizontal-elasticidad-y-alta-disponibilidad)
8. [Seguridad: responsabilidad compartida, IAM y secretos](#8-seguridad-responsabilidad-compartida-iam-y-secretos)
9. [Costos: pagar por uso y no llevarse sorpresas](#9-costos-pagar-por-uso-y-no-llevarse-sorpresas)
10. [Infraestructura como código](#10-infraestructura-como-código)
11. [Los grandes proveedores: AWS, Azure y Google Cloud](#11-los-grandes-proveedores-aws-azure-y-google-cloud)
12. [Cloud native y las 12 factores](#12-cloud-native-y-las-12-factores)
13. [Cuándo NO conviene la nube](#13-cuándo-no-conviene-la-nube)
14. [Ejercicio y preguntas para el aula](#14-ejercicio-y-preguntas-para-el-aula)
15. [Recursos recomendados](#recursos-recomendados)

---

## 1. Por qué existe la nube

### El problema: tener tus propios servidores

Imaginá que tu colegio vende entradas online para la fiesta de fin de año. Para tener una página web necesitás un **servidor**: una computadora que está siempre prendida y responde pedidos de otras computadoras.

Si no usás la nube, tenés que comprar esa computadora y ponerla en un cuarto del colegio. A eso se le llama **on-premise** ("en las instalaciones propias"). Es como tener tu propio pozo de agua en casa.

Tener tu propio servidor implica:

- Comprar el equipo (mucha plata al principio).
- Ponerle aire acondicionado, electricidad y un lugar con llave.
- Contratar a alguien que lo arregle cuando se rompe.
- Pagar de más para el peor día, aunque el resto del año esté casi vacío.

### La solución: alquilar en vez de comprar

La **nube** (cloud computing) es alquilar computadoras, almacenamiento y programas de otra empresa, a través de internet, y pagar solo lo que usás. Es como pasar del pozo propio a la canilla: abrís, usás, y te llega la factura del consumo.

> **Concepto**: la nube no es "algo en el aire". Son **centros de datos** (edificios llenos de servidores) que pertenecen a una empresa como Amazon, Microsoft o Google. Vos usás una parte de esos servidores, por internet.

### CAPEX vs OPEX

Dos palabras de finanzas que se usan todo el tiempo:

| Término | Significado | Analogía | En informática |
|---|---|---|---|
| **CAPEX** (gasto de capital) | Plata que ponés de una vez para comprar algo que dura | Comprar un auto | Comprar servidores |
| **OPEX** (gasto operativo) | Plata que pagás de forma continua por usar algo | Usar Uber | Pagar la nube mes a mes |

On-premise es mayormente CAPEX. La nube es mayormente OPEX.

### El caso de la venta de entradas

Tu colegio vende entradas. Casi todo el año hay 5 visitas por día. El día que salen a la venta hay miles de personas conectadas a la vez.

| | On-premise | Nube |
|---|---|---|
| Antes de empezar | Comprás servidores para el pico | No comprás nada |
| Día de la venta | Alcanzan (si calculaste bien) o se cae | Se agregan máquinas automáticamente |
| Los otros 364 días | Los servidores están casi parados y los seguís pagando | Pagás muy poco |
| Si se rompe un equipo | Lo arreglás vos | Lo resuelve el proveedor |

> **Ojo**: esto no significa que la nube siempre sea más barata. Significa que es más **flexible**. Lo vemos en la sección 13.

### Un poco de historia

Amazon empezó a ofrecer alquiler de infraestructura como servicio público en 2006 (con S3 y EC2). Desde entonces Microsoft (Azure) y Google (Google Cloud) fueron sumando servicios similares.

---

## 2. La definición oficial (NIST)

El NIST (Instituto Nacional de Estándares y Tecnología de Estados Unidos) publicó en septiembre de 2011 el documento **SP 800-145**, que define qué es la nube. Todavía se usa como referencia en el mundo entero.

> **Concepto**: según el NIST, la computación en la nube es un modelo para acceder, cuando se necesita y por red, a un conjunto compartido de recursos configurables (redes, servidores, almacenamiento, aplicaciones y servicios) que se pueden asignar y liberar rápidamente con muy poco esfuerzo de gestión o de interacción con el proveedor.

Suena a mucho. Se entiende mejor con las **5 características esenciales**. Para que algo sea "nube" de verdad, tiene que cumplirlas.

### Las 5 características esenciales

| # | Característica | Qué significa en criollo | Ejemplo |
|---|---|---|---|
| 1 | **On-demand self-service** (autoservicio a demanda) | Pedís recursos vos mismo, sin llamar a nadie ni esperar a una persona | Creás un servidor desde la web en un minuto, un sábado a la noche |
| 2 | **Broad network access** (acceso amplio por red) | Se usa desde cualquier dispositivo con internet | Editás un documento desde el celular y después desde la compu |
| 3 | **Resource pooling** (recursos agrupados) | El proveedor tiene un "pozo" grande de recursos y se los reparte entre muchos clientes | Tu app y la de otra empresa comparten la misma máquina física, sin verse entre sí |
| 4 | **Rapid elasticity** (elasticidad rápida) | Los recursos crecen y se achican rápido, a veces solos | Una app de delivery en el Mundial sube de 2 a 20 servidores durante el partido y vuelve a 2 después |
| 5 | **Measured service** (servicio medido) | Se mide lo que usás y se cobra por eso | La factura dice "usaste X horas de servidor y X GB de almacenamiento" |

### Cómo se ve en la práctica

Ejemplo de la característica 1 (comando **ilustrativo**, la sintaxis exacta depende del proveedor y de la versión):

```bash
# Ilustrativo: pedir un servidor virtual en AWS desde la terminal
aws ec2 run-instances \
  --image-id ami-xxxxxxxx \
  --instance-type t3.micro \
  --count 1
```

Un minuto después tenés una computadora. Sin comprar, sin cables, sin esperar. Esa es la esencia.

> **Para entender**: pensá en Netflix. No tiene un servidor para "los que miran series los viernes". Usa nube justamente por las características 4 y 5: crece cuando hay mucha gente y paga según lo que usa.

---

## 3. Modelos de servicio: IaaS, PaaS, SaaS y serverless

### La palabra "servicio"

En la nube casi todo termina en "as a Service" (**aaS**): "como servicio". Significa "en vez de comprarlo, lo usás como un servicio y alguien más lo mantiene".

La pregunta clave es: **¿cuánto trabajo te queda a vos y cuánto hace el proveedor?**

### La analogía de la pizza

Imaginá 4 formas de comer pizza:

| Forma | Qué hacés vos | Modelo cloud |
|---|---|---|
| **Hacerla en tu casa** | Todo: masa, salsa, horno, mesa, bebida | On-premise |
| **Comprar la pizza congelada y cocinarla en tu casa** | Cocinar, poner la mesa | IaaS |
| **Pedir por delivery** | Poner la mesa y las bebidas | PaaS |
| **Ir a una pizzería** | Solo comer | SaaS |

Cuanto más "servicio", menos control tenés, pero menos laburo.

### IaaS: Infraestructura como servicio

> **Concepto**: **IaaS** (Infrastructure as a Service) te alquila lo más básico: máquinas virtuales, discos y redes. Es como alquilar un terreno con luz y agua: la casa la construís vos.

Vos elegís el sistema operativo, instalás lo que quieras y lo mantenés actualizado.

- **Ejemplos**: Amazon EC2, Azure Virtual Machines, Google Compute Engine.
- **Cuándo se usa**: cuando necesitás control total, o para migrar un sistema viejo tal cual está.

### PaaS: Plataforma como servicio

> **Concepto**: **PaaS** (Platform as a Service) te da una plataforma lista para ejecutar tu código. Vos subís la aplicación y el proveedor se ocupa del sistema operativo, los parches y muchas veces del escalado. Es como alquilar un departamento amueblado.

- **Ejemplos**: AWS Elastic Beanstalk, Azure App Service, Google App Engine, Heroku.
- **Cuándo se usa**: cuando querés concentrarte en el código, no en los servidores.

Ejemplo: tu API de Go de la Unidad 6 se podría subir a un servicio de este tipo. Vos das el código o la imagen, y el servicio te da una URL pública.

### SaaS: Software como servicio

> **Concepto**: **SaaS** (Software as a Service) es un programa terminado que usás desde el navegador o una app. No instalás ni administrás nada.

- **Ejemplos**: Gmail, Google Drive, Netflix, Spotify, Microsoft 365, Zoom, Mercado Pago.
- **Cuándo se usa**: cuando el programa ya existe y solo querés usarlo.

Vos ya usás SaaS todos los días sin pensarlo.

### FaaS / serverless: funciones que se prenden solo cuando hacen falta

> **Concepto**: **serverless** ("sin servidor") no significa que no haya servidores. Significa que **vos no los ves ni los administrás**. La variante más famosa es **FaaS** (Function as a Service): subís una función chica y el proveedor la ejecuta solo cuando llega un evento. Es como un taxi: no tenés auto propio, pagás solo el viaje.

- **Ejemplos**: AWS Lambda, Azure Functions, Google Cloud Functions / Cloud Run functions.
- **Se cobra** por ejecución y por tiempo de uso, no por "tener el servidor prendido".
- **Ejemplo típico**: cada vez que alguien sube una foto de perfil, se dispara una función que la achica. Si nadie sube fotos en toda la noche, no gastás nada.

Ejemplo de función serverless en Go (**ilustrativo**, con la librería de AWS Lambda):

```go
package main

import (
	"context"
	"github.com/aws/aws-lambda-go/lambda"
)

type Evento struct {
	Nombre string `json:"nombre"`
}

func manejar(ctx context.Context, e Evento) (string, error) {
	return "Hola " + e.Nombre, nil
}

func main() {
	lambda.Start(manejar)
}
```

Es como un handler de tu API de Go, pero sin `http.ListenAndServe`: no hay servidor que levantar.

> **Cuidado**: serverless tiene límites (por ejemplo, un tiempo máximo de ejecución por invocación, y un "arranque en frío" cuando la función estuvo un rato sin usarse). Se elige para tareas cortas y basadas en eventos.

### Tabla: quién gestiona qué

"Vos" es quien contrata el servicio. "Proveedor" es la empresa de la nube.

| Capa | On-premise | IaaS | PaaS | SaaS |
|---|---|---|---|---|
| Aplicación y datos propios | Vos | Vos | Vos | Proveedor (vos cargás tus datos) |
| Lenguaje / runtime | Vos | Vos | Proveedor | Proveedor |
| Sistema operativo | Vos | Vos | Proveedor | Proveedor |
| Máquina virtual | Vos | Vos | Proveedor | Proveedor |
| Servidores físicos | Vos | Proveedor | Proveedor | Proveedor |
| Almacenamiento y red física | Vos | Proveedor | Proveedor | Proveedor |
| Edificio, luz, refrigeración | Vos | Proveedor | Proveedor | Proveedor |

Regla simple: **cuanto más a la derecha, más cosas hace el proveedor y menos control tenés.**

En la práctica los límites entre modelos no son tan tajantes. Servicios como los contenedores administrados quedan entre IaaS y PaaS. La tabla sirve para ordenar ideas, no como ley.

---

## 4. Modelos de despliegue: pública, privada, híbrida, multi-cloud

Los modelos de servicio (sección 3) responden "¿qué me dan?". Los de despliegue responden "**¿dónde está y de quién es?**". El NIST define cuatro: pública, privada, comunitaria e híbrida. Hoy se suma un quinto término muy usado: multi-cloud.

| Modelo | Qué es | Analogía | Ejemplo |
|---|---|---|---|
| **Nube pública** | La infraestructura es de un proveedor y la comparten muchos clientes | Un colectivo: compartido, pagás el pasaje | AWS, Azure, Google Cloud |
| **Nube privada** | La infraestructura es de una sola organización | Auto propio | Un banco con su propio centro de datos con tecnología de nube |
| **Nube comunitaria** | La comparten varias organizaciones con necesidades parecidas | Un consorcio de edificio | Universidades o entidades públicas que comparten infraestructura |
| **Nube híbrida** | Mezcla de privada (o on-premise) y pública, conectadas | Casa propia + alquiler de verano | Un hospital guarda historias clínicas en su servidor y usa la nube pública para la página web |
| **Multi-cloud** | Se usan **dos o más proveedores** públicos a la vez | Tener cuenta en dos bancos | Una empresa usa AWS para el backend y Google Cloud para análisis de datos |

### Ejemplo de por qué alguien elegiría híbrida

Un banco argentino tiene reglas que le exigen guardar ciertos datos de clientes bajo control estricto. Los deja en su centro de datos propio. Pero la app móvil de consultas tiene picos de uso y la corre en la nube pública. Las dos partes se comunican por una conexión segura.

### Multi-cloud: ventajas y costos

| Ventaja | Costo |
|---|---|
| No depender de un solo proveedor (evitar el **vendor lock-in**, "quedar atado") | Hay que dominar dos o tres plataformas distintas |
| Usar lo mejor de cada una | Más complejidad y más puntos que pueden fallar |
| Cumplir regulaciones locales | Los servicios no son idénticos: migrar cuesta trabajo |

> **Concepto**: **vendor lock-in** es quedar tan atado a un proveedor (por usar sus servicios exclusivos) que irse después sale carísimo. Es como haber armado toda tu casa con enchufes de una marca que solo vende esa empresa.

---

## 5. Regiones, zonas de disponibilidad y edge

### Región

> **Concepto**: una **región** es una zona geográfica donde el proveedor tiene centros de datos. Por ejemplo "São Paulo", "Virginia del Norte" o "Frankfurt". Es como una sucursal de una cadena en una ciudad.

Por qué importa elegir bien la región:

- **Latencia**: cuanto más cerca del usuario, más rápido responde. Un usuario en Santiago del Estero que use un servidor en Tokio esperará más que si usa uno en São Paulo.
- **Leyes**: algunas normas exigen que ciertos datos no salgan de un país o de una región.
- **Precio**: el mismo servicio puede costar distinto según la región.
- **Servicios disponibles**: no todos los servicios están en todas las regiones.

### Zona de disponibilidad (AZ)

> **Concepto**: una **zona de disponibilidad** es uno o más centros de datos **separados físicamente** dentro de una región, con su propia electricidad, refrigeración y red, pero conectados entre sí con redes rápidas. Es como tener dos depósitos en barrios distintos de la misma ciudad: si uno se inunda, el otro sigue funcionando.

Una región tiene normalmente varias zonas. En AWS, cada región se compone de varias AZ (consultá la página oficial para el detalle actual, porque los números cambian). Azure y Google Cloud usan un esquema parecido.

```
Región: sa-east-1 (São Paulo)
┌────────────────────────────────────────────────┐
│  Zona A            Zona B            Zona C    │
│ ┌────────┐        ┌────────┐        ┌────────┐ │
│ │ Centro │ <----> │ Centro │ <----> │ Centro │ │
│ │ datos  │  red   │ datos  │  red   │ datos  │ │
│ └────────┘ rápida └────────┘ rápida └────────┘ │
└────────────────────────────────────────────────┘
   Cada zona: luz, refrigeración y red propias
```

**Ejemplo**: tu API corre en dos máquinas, una en la zona A y otra en la zona B. Si se corta la luz en el centro de datos de la zona A, la zona B sigue atendiendo. Los usuarios no se enteran.

### Edge y CDN

> **Concepto**: **edge** ("borde") son puntos de presencia del proveedor repartidos por el mundo, mucho más numerosos y cercanos a los usuarios que las regiones. Una **CDN** (Content Delivery Network, red de distribución de contenido) usa esos puntos para guardar copias de archivos que se piden mucho. Es como tener kioscos en cada barrio con las figuritas más pedidas, en vez de que todos vayan a la fábrica.

Ejemplo:

1. Tu app React (Unidad 6) tiene archivos estáticos: HTML, JS, CSS e imágenes.
2. Los subís a un servicio de almacenamiento y los servís a través de una CDN (Amazon CloudFront, Azure Front Door / CDN, Google Cloud CDN, o proveedores independientes como Cloudflare).
3. Un alumno en Santiago del Estero recibe los archivos desde un punto cercano, no desde el servidor original.

Resultado: la página carga más rápido y el servidor principal recibe menos pedidos.

> **Para entender**: las CDN sirven muy bien para contenido que **no cambia** entre usuarios (imágenes, JS, CSS, video). El listado de "tus pedidos" es distinto para cada persona y no se puede copiar así.

---

## 6. Los bloques básicos: cómputo, almacenamiento, bases de datos y redes

Casi todo lo que hace una aplicación en la nube se arma con cuatro bloques: dónde corre el código, dónde guarda archivos, dónde guarda datos y cómo se conecta.

### 6.1 Cómputo: dónde corre tu código

| Opción | Qué es | Analogía | Ejemplo |
|---|---|---|---|
| **Máquina virtual (VM)** | Una computadora simulada por software, con su propio sistema operativo | Un departamento completo dentro de un edificio | EC2, Azure VM, Compute Engine |
| **Contenedor** | Un paquete con tu app y lo que necesita, que comparte el sistema operativo del anfitrión | Una habitación de hotel: más liviana que un departamento | Docker (Unidad 4) |
| **Orquestador de contenedores** | Programa que arranca, reparte y repara contenedores | El encargado del hotel | Kubernetes (Unidad 5) |
| **Serverless** | Solo dejás el código, sin pensar en servidores | Taxi | Lambda, Azure Functions |

Servicios administrados de Kubernetes: Amazon EKS, Azure AKS y Google GKE. Son el mismo Kubernetes que viste en la Unidad 5, pero el proveedor administra la parte compleja (el "plano de control").

Servicios "corré mi contenedor y listo": AWS Fargate / App Runner, Azure Container Apps, Google Cloud Run. Ejemplo: le das la imagen Docker de tu API de Go y te devuelven una URL.

Ejemplo **ilustrativo** con Google Cloud:

```bash
# Ilustrativo: desplegar una imagen de contenedor en Cloud Run
gcloud run deploy mi-api \
  --image us-docker.pkg.dev/mi-proyecto/repo/mi-api:1.0 \
  --region southamerica-east1 \
  --allow-unauthenticated
```

> **Conexión con la materia**: la imagen que armaste con un `Dockerfile` en la Unidad 4 es exactamente lo que se sube a estos servicios. Eso es lo lindo de los contenedores: funcionan igual en tu laptop y en cualquier nube.

**Una VM y un contenedor, comparados:**

```
   Máquina virtual                      Contenedor
┌─────┐ ┌─────┐ ┌─────┐            ┌─────┐ ┌─────┐ ┌─────┐
│ App │ │ App │ │ App │            │ App │ │ App │ │ App │
│ SO  │ │ SO  │ │ SO  │            └─────┘ └─────┘ └─────┘
└─────┘ └─────┘ └─────┘            ┌─────────────────────┐
┌─────────────────────┐            │  Motor de contenedores│
│    Hipervisor       │            │  Sistema operativo    │
│  Hardware físico    │            │  Hardware físico      │
└─────────────────────┘            └─────────────────────┘
 Cada VM trae su propio SO          Comparten el SO del anfitrión
```

### 6.2 Almacenamiento: dónde guardar archivos

Hay tres tipos. Se confunden seguido.

| Tipo | Cómo se accede | Analogía | Uso típico | Ejemplos |
|---|---|---|---|---|
| **Objetos** | Por HTTP, cada archivo tiene un nombre (clave) dentro de un "bucket" | Un guardamuebles: dejás cajas con etiqueta | Fotos, videos, backups, sitios estáticos | Amazon S3, Azure Blob Storage, Google Cloud Storage |
| **Bloques** | Como un disco duro conectado a una VM | El disco de tu compu | Discos de una VM, bases de datos | Amazon EBS, Azure Managed Disks, Google Persistent Disk |
| **Archivos** | Como una carpeta compartida en red | Una carpeta compartida del colegio | Varios servidores leyendo los mismos archivos | Amazon EFS, Azure Files, Google Filestore |

Ejemplo **ilustrativo**: subir la foto de perfil de un usuario a almacenamiento de objetos.

```bash
# Ilustrativo: subir un archivo a un bucket de S3
aws s3 cp foto.jpg s3://mi-bucket-colegio/perfiles/usuario-42.jpg
```

> **Cuidado**: los contenedores de tu API deberían ser **descartables**. Si guardás las fotos dentro del contenedor, cuando se reinicia se pierden. Por eso se guardan en almacenamiento de objetos, no en el disco del contenedor.

### 6.3 Bases de datos administradas

> **Concepto**: una **base de datos administrada** es una base de datos que el proveedor instala, actualiza, respalda y monitorea por vos. Es como contratar un servicio de mantenimiento en vez de arreglar la heladera vos mismo.

Vos usás la base igual que siempre (mismas consultas), pero no te ocupás de los parches, los backups ni de reemplazar discos rotos.

| Tipo | Ejemplos de motor | Servicios administrados |
|---|---|---|
| Relacional (SQL) | PostgreSQL, MySQL | Amazon RDS / Aurora, Azure SQL Database, Google Cloud SQL |
| Documentos (NoSQL) | MongoDB | MongoDB Atlas (corre sobre AWS, Azure y GCP), Amazon DocumentDB, Azure Cosmos DB (con API compatible con MongoDB) |
| Clave-valor / cache | Redis | Amazon ElastiCache, Azure Cache for Redis, Google Memorystore |

**Ejemplo con la materia**: tu API con MongoDB de la Unidad 6 usa una base que levantaste en Docker. En la nube, cambiarías la cadena de conexión para que apunte a un MongoDB administrado, por ejemplo Atlas. El código Go casi no cambia:

```go
// La URI viene de una variable de entorno, no está escrita en el código
uri := os.Getenv("MONGODB_URI")
cliente, err := mongo.Connect(ctx, options.Client().ApplyURI(uri))
```

### 6.4 Redes: cómo se conecta todo

> **Concepto**: una **VPC** (Virtual Private Cloud, red privada virtual) es tu propia red aislada dentro de la nube. Es como el barrio cerrado con seguridad en la entrada: adentro tus servicios se ven entre sí, y afuera nadie entra si vos no lo permitís.

Dentro de una VPC se arma:

- **Subredes**: partes de la red. Una **pública** puede recibir tráfico de internet. Una **privada** no.
- **Reglas de firewall** (en AWS se llaman *security groups*): dicen qué tráfico se permite y desde dónde.
- **Balanceador de carga (load balancer)**: reparte los pedidos entre varias copias de tu app. Es la persona de la entrada del banco que dice "usted a la caja 3, usted a la caja 5".

```
Internet
   │
   ▼
┌───────────────┐
│ Load balancer │  (subred pública)
└───────┬───────┘
   ┌────┴─────┐
   ▼          ▼
┌──────┐   ┌──────┐
│ API  │   │ API  │   (subred privada, dos zonas)
│ zona A│  │ zona B│
└──┬───┘   └───┬──┘
   └─────┬─────┘
         ▼
   ┌────────────┐
   │  MongoDB   │   (subred privada, sin acceso desde internet)
   └────────────┘
```

**Buena práctica**: la base de datos **nunca** debe estar accesible directamente desde internet. Solo tu API puede hablar con ella.

Esto es lo mismo que hacía Kubernetes con un `Service` frente a varios `Pod` (Unidad 5), pero a nivel de infraestructura del proveedor.

---

## 7. Escalar: vertical, horizontal, elasticidad y alta disponibilidad

### Escalabilidad

> **Concepto**: **escalar** es hacer que un sistema pueda atender más trabajo. Hay dos maneras.

| | Escalado vertical (scale up) | Escalado horizontal (scale out) |
|---|---|---|
| Qué hacés | Poner una máquina **más potente** | Poner **más máquinas** iguales |
| Analogía | Contratar un cajero superrápido | Abrir más cajas con cajeros normales |
| Ventaja | Simple: no hay que cambiar la app | Casi sin techo, y si una cae siguen las demás |
| Desventaja | Hay un límite físico y sale caro; si esa máquina cae, se cae todo | La app tiene que estar pensada para correr en varias copias |
| Ejemplo | Pasar de 2 a 16 GB de RAM | Pasar de 1 a 10 contenedores de la API |

### Qué se necesita para escalar horizontalmente

Para que 10 copias de tu API funcionen bien, cada copia tiene que ser **sin estado (stateless)**: no puede guardar nada importante en su propia memoria o disco.

- Tu API con JWT (Unidad 6) ya está bien encaminada: el token viaja con cada pedido y cualquier copia puede validarlo, sin memoria compartida.
- Si guardaras la "sesión" del usuario en la memoria de una copia, otra copia no la conocería y el usuario tendría que volver a iniciar sesión.

### Elasticidad

> **Concepto**: **elasticidad** es escalar **automáticamente** hacia arriba y hacia abajo según la demanda. Escalabilidad es "poder crecer". Elasticidad es "crecer y achicarse solo, en el momento justo". Es como un elástico: se estira cuando lo necesitás y vuelve a su tamaño.

**Ejemplo, app de delivery en el Mundial**: cuando empieza un partido, en el entretiempo miles de personas piden comida al mismo tiempo. Se define una regla:

```yaml
# Ilustrativo: autoescalado horizontal en Kubernetes (HPA, Unidad 5)
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: api-pedidos
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: api-pedidos
  minReplicas: 2
  maxReplicas: 20
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

Traducción: "mantené entre 2 y 20 copias; si el uso de CPU pasa de 70%, agregá más". Terminado el partido, vuelve a 2. Pagás por las 20 copias solo durante ese rato.

### Alta disponibilidad

> **Concepto**: **alta disponibilidad** (high availability, HA) es diseñar el sistema para que **siga funcionando aunque algo falle**. Se logra con **redundancia**: tener más de una copia de cada pieza importante. Es como llevar una rueda de auxilio.

Ingredientes típicos:

1. Varias copias de la app, repartidas en **más de una zona de disponibilidad**.
2. Un balanceador de carga que deja de mandar tráfico a las copias que no responden (**health checks**).
3. Base de datos con réplica en otra zona.
4. Backups probados (un backup que nunca probaste restaurar no es un backup).

La disponibilidad se suele expresar en porcentajes ("tres nueves", "cuatro nueves"). Cada nueve más reduce mucho el tiempo permitido de caída y hace la solución mucho más cara. De eso trata la segunda mitad de la clase (SRE).

> **Diferencia clave**: escalabilidad = atender **más** trabajo. Alta disponibilidad = seguir funcionando **cuando algo falla**. Son cosas distintas, aunque muchas veces se resuelven con las mismas herramientas.

---

## 8. Seguridad: responsabilidad compartida, IAM y secretos

### Modelo de responsabilidad compartida

Cuando alquilás un departamento, el dueño se ocupa de la estructura del edificio, y vos de cerrar la puerta con llave. Si te robaron porque dejaste la puerta abierta, no es culpa del dueño.

> **Concepto**: en el **modelo de responsabilidad compartida**, el proveedor y el cliente se reparten la seguridad. El proveedor es responsable de la seguridad **DE** la nube (edificios, servidores, red física). El cliente es responsable de la seguridad **EN** la nube (sus datos, sus usuarios, sus configuraciones).

| Quién | Responsable de |
|---|---|
| Proveedor (seguridad **de** la nube) | Centros de datos, hardware, red física, software base de sus servicios |
| Cliente (seguridad **en** la nube) | Datos que carga, quién puede acceder, contraseñas, configuración correcta de los servicios, parches de lo que instaló |

El reparto **cambia según el modelo de servicio**. En IaaS el cliente es responsable de más cosas (por ejemplo, del sistema operativo de la VM). En SaaS, de mucho menos (sobre todo de sus datos y de quién accede).

**Ejemplo real y frecuente**: un bucket de almacenamiento configurado como "público" sin querer, con datos de clientes adentro. El servicio del proveedor funcionó bien. El error fue de configuración del cliente. Estos incidentes se repiten seguido en la industria.

### IAM: quién puede hacer qué

> **Concepto**: **IAM** (Identity and Access Management, gestión de identidades y accesos) es el sistema que responde a "¿quién sos y qué tenés permitido hacer?". Es como las llaves de un edificio: la del portero abre más puertas que la de un visitante.

Piezas típicas:

| Pieza | Qué es | Ejemplo |
|---|---|---|
| **Usuario** | Una persona | Ana, desarrolladora |
| **Rol** | Un conjunto de permisos que se le puede dar a una persona o a un servicio | "Solo lectura de un bucket" |
| **Política (policy)** | Documento que describe qué acciones se permiten sobre qué recursos | Ver ejemplo abajo |

Ejemplo **ilustrativo** de una política de AWS: permite solo **leer** objetos de un bucket.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:GetObject"],
      "Resource": "arn:aws:s3:::mi-bucket-colegio/*"
    }
  ]
}
```

### Principio de mínimo privilegio

> **Concepto**: **mínimo privilegio** significa dar a cada persona o servicio **solo los permisos que necesita para su tarea, y nada más**. Es como darle al plomero la llave del baño, no la de toda la casa.

Ejemplos:

- Tu API de recetas solo necesita leer y escribir en su propia base. No necesita permiso para borrar servidores.
- Un becario que revisa facturas puede tener acceso de lectura a la facturación, no de administrador.

Si roban esas credenciales, el daño queda limitado. La recomendación oficial de AWS también incluye proteger la cuenta principal (root) y activar **MFA** (segundo factor de autenticación, como el código del celular).

### Secretos: nunca en git

Un **secreto** es todo dato que no debe conocer cualquiera: contraseñas, claves de API, cadenas de conexión, la clave con la que firmás los JWT.

> **Regla de oro**: **nunca subas secretos a git.** Ni a un repositorio privado. Una vez que algo está en el historial de git, considerá que quedó expuesto. Existen bots que rastrean GitHub permanentemente buscando claves de nube filtradas, y las usan en cuestión de minutos para crear servidores y minar criptomonedas a tu costo.

Mal:

```go
var claveJWT = "mi-clave-super-secreta-123" // ¡nunca!
```

Bien:

```go
claveJWT := os.Getenv("JWT_SECRET") // viene del entorno, no del código
```

Y en el despliegue, el valor real se inyecta desde un **gestor de secretos** (AWS Secrets Manager, Azure Key Vault, Google Secret Manager) o, en Kubernetes, desde un `Secret` (Unidad 5). También conviene tener un `.gitignore` con los archivos `.env`:

```
# .gitignore
.env
*.pem
```

**Si se te escapó una clave**: no alcanza con borrarla del repositorio. Hay que **revocarla o rotarla** (generar una nueva y anular la vieja).

### Otras prácticas básicas

- Activar MFA en la cuenta.
- Cifrar los datos guardados y en tránsito (HTTPS).
- No dejar puertos abiertos "a todo internet" sin necesidad (por ejemplo, el puerto de la base de datos).
- Mantener actualizado lo que instalaste vos.
- Registrar y revisar quién hizo qué (logs de auditoría).

---

## 9. Costos: pagar por uso y no llevarse sorpresas

### Pago por uso

Ya vimos que la nube es "servicio medido": pagás por lo que usás. Lo que se mide depende del servicio:

| Servicio | Se suele cobrar por |
|---|---|
| Máquina virtual | Tiempo encendida (por hora o por segundo, según el proveedor) y tamaño |
| Almacenamiento de objetos | GB guardados por mes, más cantidad de pedidos |
| Función serverless | Cantidad de ejecuciones y tiempo de cada una |
| Base de datos administrada | Tamaño de instancia, almacenamiento y backups |
| **Transferencia de datos** | GB que **salen** de la nube hacia internet (o entre regiones) |

> **Ojo con la transferencia de datos**: meter datos a la nube suele ser barato o gratis, pero **sacarlos** suele costar. Se le llama costo de **egress**. Es de las sorpresas más comunes en la primera factura.

Los precios y las tarifas cambian con el tiempo y según la región. **No memorices números**: usá siempre la calculadora oficial del proveedor.

### Modelos de precio

| Modelo | Idea | Analogía |
|---|---|---|
| **On-demand** (a demanda) | Pagás lo que usás, sin compromiso | Taxi |
| **Compromiso de uso** (reserved / savings plans / committed use) | Te comprometés a usar cierta cantidad durante un tiempo, a cambio de descuento | Abono mensual del gimnasio |
| **Instancias interrumpibles** (spot / preemptible) | Usás capacidad sobrante muy barata, pero el proveedor te la puede quitar | Pasaje de último minuto sin garantía |

### Free tier

Los tres grandes proveedores ofrecen una capa gratuita para aprender. Suele combinar tres cosas: servicios siempre gratuitos hasta cierto límite, pruebas por tiempo limitado y créditos iniciales. **Las condiciones cambian**, así que leé la página oficial de cada uno antes de usarla (ver Recursos).

### Sorpresas de facturación

Casos típicos donde llega una factura enorme:

1. **Servidor que se olvidó prendido**: creaste una VM para practicar y la dejaste un mes.
2. **Credenciales filtradas en git**: alguien las usa para minar criptomonedas a tu costo.
3. **Bucle infinito en una función serverless** que se llama a sí misma.
4. **Transferencia de datos** muy grande hacia internet.
5. **Base de datos grande** encendida de noche y fines de semana sin necesidad.

Cómo protegerte:

- Crear **alertas de presupuesto** (budgets) desde el primer día.
- Poner **etiquetas (tags)** en cada recurso ("proyecto: programacion2") para saber qué gasta qué.
- Apagar o borrar lo que no usás al terminar una práctica.
- Revisar el panel de costos de forma regular.
- Configurar las cuentas de práctica con límites y con MFA.

### FinOps: introducción

> **Concepto**: **FinOps** (Financial Operations) es la práctica de que ingenieros, finanzas y negocio trabajen juntos para decidir **cuánto vale la pena gastar en la nube**. No busca gastar lo menos posible, sino gastar bien: que cada peso rinda. Es como el presupuesto familiar: no es dejar de comer, es saber en qué se va la plata.

Ideas básicas:

- **Visibilidad**: saber quién gasta qué (con etiquetas y reportes).
- **Optimización**: ajustar el tamaño de los recursos ("right-sizing"), apagar lo que sobra, usar compromisos si el uso es estable.
- **Responsabilidad**: cada equipo se hace cargo de lo que gasta.

**Ejemplo**: la base de datos de pruebas no se usa de noche ni los fines de semana. Apagarla en esos horarios puede recortar una gran parte de su costo, sin perder nada.

---

## 10. Infraestructura como código

### El problema de hacer clic

Armar un servidor, una red y una base de datos con clics en la consola web funciona para aprender. En un trabajo real, trae problemas:

- Nadie recuerda exactamente qué clics se hicieron.
- Repetirlo en otro entorno (pruebas, producción) sale distinto cada vez.
- Si algo se rompe, no hay forma simple de reconstruirlo.
- No queda registro de quién cambió qué.

### La solución: escribir la infraestructura en archivos

> **Concepto**: la **infraestructura como código** (IaC, Infrastructure as Code) consiste en describir la infraestructura (servidores, redes, bases) en **archivos de texto** que se guardan en git, se revisan y se ejecutan de forma automática. Es como pasar de explicar "a ojo" una receta a escribirla: cualquiera la cocina igual.

Ventajas:

| Ventaja | Explicación |
|---|---|
| **Repetible** | El mismo archivo crea el mismo resultado, tantas veces como quieras |
| **Versionada** | Está en git: se ve el historial y se puede volver atrás |
| **Revisable** | Un compañero la revisa en un pull request antes de aplicar cambios |
| **Documentada** | El archivo mismo describe cómo está armada la infraestructura |
| **Rápida** | Levantar un entorno completo lleva minutos |

Con IaC podés destruir y recrear un entorno entero de pruebas en minutos. Lo mismo que hacías con `docker compose up` en la Unidad 4, pero para la infraestructura completa.

### Declarativo: decís el "qué", no el "cómo"

Las herramientas más usadas son **declarativas**: describís el resultado que querés, y la herramienta calcula qué pasos dar. Es como pedir "quiero una pizza de muzzarella" en lugar de dictar los pasos.

**Terraform** (de HashiCorp) es la más conocida. Funciona con muchos proveedores. Ejemplo **ilustrativo**:

```hcl
# main.tf: un bucket de almacenamiento en AWS
provider "aws" {
  region = "sa-east-1"
}

resource "aws_s3_bucket" "fotos" {
  bucket = "mi-bucket-colegio-fotos"
}
```

Flujo de trabajo:

```bash
terraform init      # descarga los plugins del proveedor
terraform plan      # muestra QUÉ cambiaría, sin tocar nada
terraform apply     # crea o modifica los recursos
terraform destroy   # borra todo lo que creó
```

> **Para entender**: `terraform plan` es como el resumen del carrito antes de pagar. Podés revisar todo antes de confirmar.

Terraform guarda un **archivo de estado** con lo que creó, para saber qué cambió la próxima vez. Ese archivo puede contener información sensible, así que no se sube a git sin cuidado.

### Otras herramientas

| Herramienta | Nota |
|---|---|
| **OpenTofu** | Bifurcación (fork) de código abierto de Terraform, mantenida por la Linux Foundation |
| **Pulumi** | Permite escribir IaC en lenguajes como TypeScript, Python o Go |
| **AWS CloudFormation** | IaC propia de AWS |
| **Azure Bicep / ARM** | IaC propia de Azure |
| **Ansible** | Más orientada a configurar servidores ya creados |
| **Manifiestos de Kubernetes** | Los YAML de la Unidad 5 son también una forma de "infraestructura declarativa" |

---

## 11. Los grandes proveedores: AWS, Azure y Google Cloud

Los tres grandes proveedores públicos son:

| Proveedor | Empresa | Se lo asocia con |
|---|---|---|
| **AWS** (Amazon Web Services) | Amazon | Fue el primero en masificarse. Catálogo muy amplio |
| **Microsoft Azure** | Microsoft | Fuerte en empresas que ya usan productos de Microsoft |
| **Google Cloud (GCP)** | Google | Fuerte en datos, análisis, Kubernetes e inteligencia artificial |

Existen también otros (Oracle Cloud, IBM Cloud, DigitalOcean, Cloudflare, etc.), pero estos tres son la referencia.

### Tabla de equivalencias

Los tres ofrecen casi lo mismo con **nombres distintos**. Aprender a traducir es medio trabajo.

| Necesidad | AWS | Azure | Google Cloud |
|---|---|---|---|
| Máquina virtual | EC2 | Virtual Machines | Compute Engine |
| Kubernetes administrado | EKS | AKS | GKE |
| Contenedor sin administrar servidores | Fargate / App Runner | Container Apps | Cloud Run |
| PaaS para aplicaciones | Elastic Beanstalk | App Service | App Engine |
| Funciones serverless | Lambda | Functions | Cloud Run functions (antes Cloud Functions) |
| Almacenamiento de objetos | S3 | Blob Storage | Cloud Storage |
| Disco de bloques | EBS | Managed Disks | Persistent Disk |
| Base relacional administrada | RDS / Aurora | Azure SQL Database / Azure Database for PostgreSQL | Cloud SQL |
| Base NoSQL de documentos / clave-valor | DynamoDB | Cosmos DB | Firestore |
| Red privada virtual | VPC | Virtual Network (VNet) | VPC |
| Balanceador de carga | Elastic Load Balancing | Load Balancer / Application Gateway | Cloud Load Balancing |
| CDN | CloudFront | Front Door / CDN | Cloud CDN |
| Gestión de identidades y permisos | IAM | Microsoft Entra ID + Azure RBAC | IAM |
| Gestor de secretos | Secrets Manager | Key Vault | Secret Manager |
| Monitoreo y logs | CloudWatch | Azure Monitor | Cloud Monitoring / Cloud Logging |
| Infraestructura como código propia | CloudFormation | Bicep / ARM | Deployment Manager (en la práctica Google recomienda Terraform) |

Los nombres pueden cambiar (Google renombró varios servicios). Antes de decidir, verificá en la documentación actual. Las comparativas oficiales están en Recursos.

### Cómo se elige

No hay un "mejor" proveedor. Se suele decidir por:

- Qué **conocimientos** ya tiene el equipo.
- Qué **servicios específicos** se necesitan.
- **Costos** para el caso concreto (se calcula con la calculadora de cada uno).
- **Regiones** disponibles cerca de los usuarios.
- Convenios o **créditos** existentes en la empresa.
- Regulaciones o requisitos de los clientes.

**Consejo para estudiantes**: lo conceptual (regiones, IAM, VPC, escalado, contenedores) es igual en los tres. Aprendé bien uno y el paso al otro es traducir nombres.

---

## 12. Cloud native y las 12 factores

### Cloud native

> **Concepto**: una aplicación **cloud native** está diseñada desde el principio para **aprovechar la nube**: usa contenedores, se divide en partes chicas que se pueden actualizar por separado, escala de forma horizontal y se administra con automatización. La **CNCF** (Cloud Native Computing Foundation), que cuida proyectos como Kubernetes, la define en torno a estas ideas.

No es lo mismo que "una app que corre en la nube". Es la diferencia entre **mudar** tus muebles viejos a otra casa (lift and shift) y **diseñar** muebles pensados para esa casa.

Piezas que ya conocés:

| Pieza | Dónde la viste |
|---|---|
| Contenedores | Docker, Unidad 4 |
| Orquestación | Kubernetes, Unidad 5 |
| APIs que se comunican por red | Tu API en Go, Unidad 6 |
| Automatización de despliegues | CI/CD (siguiente etapa del camino) |
| Infraestructura como código | Sección 10 |
| Observabilidad (métricas, logs, trazas) | Segunda mitad de la clase (SRE) |

### La metodología de las 12 factores

**The Twelve-Factor App** (12factor.net) es una lista de 12 buenas prácticas para construir aplicaciones que funcionen bien como servicio en la nube. La escribieron ingenieros de Heroku. Ya aparece en la lectura de la Unidad 4.

Los 12 factores, con su traducción y un ejemplo:

| # | Factor | Idea | Ejemplo con tu API |
|---|---|---|---|
| 1 | **Base de código** | Un repositorio por aplicación, muchos despliegues | El mismo repo se despliega en pruebas y en producción |
| 2 | **Dependencias** | Declarar explícitamente todo lo que se necesita | `go.mod` y `package.json` |
| 3 | **Configuración** | Guardar la configuración en variables de entorno, no en el código | `os.Getenv("MONGODB_URI")` |
| 4 | **Servicios de apoyo** | Tratar la base de datos y otros servicios como recursos que se conectan por URL | Cambiar de Mongo local a Atlas cambia solo la URI |
| 5 | **Build, release, run** | Separar construir, combinar con config y ejecutar | `docker build` y luego `docker run` con las variables |
| 6 | **Procesos** | Procesos sin estado; lo que hay que guardar va a un servicio externo | JWT en vez de sesiones en memoria |
| 7 | **Asignación de puertos** | La app se ofrece por un puerto propio | El contenedor expone el puerto 8080 |
| 8 | **Concurrencia** | Escalar sumando procesos | Más réplicas en Kubernetes |
| 9 | **Descartabilidad** | Arrancar rápido y apagarse sin drama | Un Pod que se reinicia y nadie lo nota |
| 10 | **Paridad dev/prod** | Entornos lo más parecidos posible | Docker Compose local parecido a producción |
| 11 | **Logs** | Tratar los logs como un flujo de eventos escrito a la salida estándar | `log.Println` va a stdout y la plataforma lo recolecta |
| 12 | **Procesos administrativos** | Tareas puntuales (migraciones) se ejecutan como procesos aparte | Un script de carga inicial que corre una vez |

**Ejemplo concreto del factor 3 (configuración)**: en un `docker run` cambiás el comportamiento sin tocar el código.

```bash
docker run -p 8080:8080 \
  -e MONGODB_URI="mongodb://mongo:27017/recetas" \
  -e JWT_SECRET="valor-de-prueba" \
  mi-api-go:1.0
```

Con esto, la **misma imagen** corre en tu laptop, en pruebas y en producción. Solo cambian las variables.

> **Conexión con la materia**: si tu API cumple estos factores (config por entorno, sin estado, logs a stdout, contenedor descartable), se puede mover entre proveedores con muy pocos cambios. Eso reduce el vendor lock-in de la sección 4.

---

## 13. Cuándo NO conviene la nube

La nube es una herramienta, no una religión. Hay casos donde no es la mejor opción.

| Situación | Por qué puede convenir otra cosa |
|---|---|
| **Carga muy estable y predecible, las 24 horas** | Si un servidor va a usarse al máximo siempre, alquilarlo a demanda puede salir más caro que comprarlo. La flexibilidad de la nube no te aporta nada |
| **Transferencia de datos enorme** | El costo de sacar datos (egress) puede dominar la factura |
| **Regulaciones estrictas** | Algunas normas exigen que los datos estén en un lugar concreto o bajo control propio |
| **Latencia ultra baja o hardware especial** | Sistemas industriales, control de maquinaria o equipos que deben responder en tiempos muy cortos suelen ser locales |
| **Conectividad mala o inestable** | Si depende de internet y la conexión es floja, es un riesgo |
| **Aplicación pequeña sin necesidad de escalar** | Un sitio de un comercio de barrio puede vivir bien en un hosting básico |
| **Falta de conocimiento del equipo** | Una nube mal configurada puede ser más cara e insegura que un servidor simple |
| **Dependencia de un solo proveedor no deseada** | Si el lock-in es un riesgo grave para el negocio |

Hay casos conocidos de empresas que movieron parte de su carga **de la nube a infraestructura propia** para bajar costos, cuando su uso era grande y estable. No quiere decir que la nube sea mala, sino que la decisión hay que **calcularla** para cada caso.

> **Regla práctica**: la nube brilla cuando la demanda **varía**, cuando querés **empezar rápido** y cuando no tenés equipo para operar hardware. Puede no convenir cuando la demanda es plana, enorme y conocida.

---

## 14. Ejercicio y preguntas para el aula

### Mini-ejercicio: decidí la arquitectura

**Situación**: el centro de estudiantes de la UCSE quiere una app para **vender entradas a la fiesta de fin de año**. Usa una API en Go con JWT, un front en React y MongoDB (como en la materia).

Datos del problema:

- Durante meses casi nadie entra.
- Cuando salen a la venta, hay muchísimos usuarios durante unas horas.
- Se guardan datos personales (nombre, DNI, mail) y se procesan pagos a través de un servicio externo.
- No hay plata para pagar de más. El equipo son 3 estudiantes.

**En grupos de 3 o 4 personas, respondan:**

1. **Modelo de servicio**: ¿usarían IaaS, PaaS o serverless para la API? ¿Y para el front? Justifiquen.
2. **Dónde va cada cosa**: ¿dónde guardarían las imágenes del evento? ¿La base de datos? ¿Cómo entregarían el front (React)?
3. **Región**: ¿qué región elegirían y por qué?
4. **Escalado**: ¿escalado vertical u horizontal? ¿Qué regla de autoescalado pondrían?
5. **Seguridad**: ¿dónde guardarían la clave del JWT y las credenciales de la base? ¿Qué permisos mínimos necesita la API?
6. **Costos**: nombren dos maneras en que la factura podría dispararse y cómo la protegerían (alertas, límites).

**Guía para el docente (una posible respuesta razonable, no la única):**

| Decisión | Una opción razonable | Por qué |
|---|---|---|
| API | Contenedor en un servicio como Cloud Run / Fargate / Container Apps, con autoescalado a partir de 0 o 1 réplica | Ya tienen la imagen Docker, escala con la demanda y cuesta poco en los meses de calma |
| Front | Archivos estáticos en almacenamiento de objetos + CDN | Barato y rápido; no necesita servidor |
| Imágenes | Almacenamiento de objetos | Es su uso típico |
| Base de datos | MongoDB administrado (por ejemplo Atlas) | Evita operar backups y parches con un equipo de 3 |
| Secretos | Gestor de secretos o variables de entorno inyectadas, nunca en git | Regla de oro |
| Región | La más cercana a los usuarios con los servicios necesarios (por ejemplo São Paulo) | Menor latencia |
| Costos | Alertas de presupuesto, apagar el entorno de pruebas, etiquetas | Evitar sorpresas |
| Pagos | No guardar datos de tarjeta propios; usar un procesador de pagos externo | Reduce la responsabilidad y el riesgo |

### Preguntas de discusión

1. Una empresa dice: "La nube es siempre más barata". ¿Qué le responderían? Den un caso donde sí y uno donde no.
2. ¿Qué diferencia hay entre **escalabilidad** y **alta disponibilidad**? Den un ejemplo de un sistema que sea muy escalable pero no muy disponible.
3. Un compañero subió por error la clave de acceso de AWS a un repositorio público de GitHub. ¿Qué pasos deberían seguir, en orden?
4. Si alguien te dice "serverless significa que no hay servidores", ¿es cierto? ¿Qué es lo que realmente cambia?
5. ¿Qué cosas de tu API de la Unidad 6 habría que cambiar para que sea "cloud native" según las 12 factores?

---

## Recursos recomendados

### Definiciones y conceptos generales

| Recurso | Qué se aprende ahí |
|---|---|
| [NIST SP 800-145: The NIST Definition of Cloud Computing](https://csrc.nist.gov/pubs/sp/800/145/final) | La definición oficial de 2011, con las 5 características, 3 modelos de servicio y 4 de despliegue. Corta y muy citada |
| [AWS: What is Cloud Computing?](https://aws.amazon.com/what-is-cloud-computing/) | Introducción clara de AWS: tipos de nube, beneficios y casos de uso |
| [Google Cloud: What is cloud computing?](https://cloud.google.com/learn/what-is-cloud-computing) | La misma pregunta desde la mirada de Google, con ejemplos y comparaciones |
| [Microsoft Azure: What is cloud computing?](https://azure.microsoft.com/en-us/resources/cloud-computing-dictionary/what-is-cloud-computing) | Definiciones y glosario de conceptos cloud en el diccionario de Azure |
| [Red Hat: IaaS vs PaaS vs SaaS](https://www.redhat.com/en/topics/cloud-computing/iaas-vs-paas-vs-saas) | Comparación de los modelos de servicio, sin depender de un proveedor |
| [AWS: What is IaaS?](https://aws.amazon.com/what-is/iaas/) | Explicación de infraestructura como servicio con casos de uso |
| [AWS: What is serverless computing?](https://aws.amazon.com/what-is/serverless-computing/) | Qué significa serverless y cómo funciona |
| [AWS: What is a CDN?](https://aws.amazon.com/what-is/cdn/) | Cómo funcionan las redes de distribución de contenido |

### Cursos gratuitos para empezar

| Recurso | Qué se aprende ahí |
|---|---|
| [AWS Cloud Practitioner Essentials](https://aws.amazon.com/training/digital/aws-cloud-practitioner-essentials/) | Curso digital de AWS que cubre los conceptos básicos de nube, seguridad, precios y servicios principales |
| [Azure Fundamentals (AZ-900) en Microsoft Learn](https://learn.microsoft.com/en-us/credentials/certifications/azure-fundamentals/) | Certificación inicial de Azure, con rutas de estudio gratuitas |
| [Microsoft Learn: Describe cloud concepts](https://learn.microsoft.com/en-us/training/paths/microsoft-azure-fundamentals-describe-cloud-concepts/) | Ruta de aprendizaje sobre conceptos de nube, modelos de servicio y responsabilidad compartida |
| [Microsoft Learn: Training](https://learn.microsoft.com/en-us/training/azure/) | Catálogo de rutas y módulos interactivos sobre Azure |
| [Google Cloud: Training](https://cloud.google.com/learn/training) | Catálogo de cursos, laboratorios y certificaciones de Google Cloud |

### Regiones, seguridad y costos

| Recurso | Qué se aprende ahí |
|---|---|
| [AWS: Global Infrastructure (regiones y zonas de disponibilidad)](https://aws.amazon.com/about-aws/global-infrastructure/regions_az/) | Mapa y explicación de regiones y AZ, con los números actualizados |
| [AWS: Modelo de responsabilidad compartida](https://aws.amazon.com/compliance/shared-responsibility-model/) | Qué le toca al proveedor y qué al cliente |
| [AWS IAM: Security best practices](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html) | Buenas prácticas de permisos: mínimo privilegio, MFA, credenciales temporales |
| [AWS Free Tier](https://aws.amazon.com/free/) | Qué se puede usar gratis y bajo qué condiciones |
| [Google Cloud Free Program](https://cloud.google.com/free) | Servicios gratuitos y créditos para empezar en Google Cloud |
| [Azure Free Account](https://azure.microsoft.com/en-us/pricing/purchase-options/azure-account) | Servicios gratuitos y créditos iniciales de Azure |
| [FinOps Foundation: What is FinOps?](https://www.finops.org/introduction/what-is-finops/) | Introducción a FinOps, sus principios y su marco de trabajo |
| [FinOps Foundation](https://www.finops.org/) | Comunidad y recursos para gestionar costos en la nube |

### Arquitectura y buenas prácticas

| Recurso | Qué se aprende ahí |
|---|---|
| [AWS Well-Architected](https://aws.amazon.com/architecture/well-architected/) | Marco de buenas prácticas de AWS para diseñar sistemas seguros, confiables, eficientes y económicos |
| [AWS Well-Architected Framework (documentación)](https://docs.aws.amazon.com/wellarchitected/latest/framework/welcome.html) | Los pilares del marco explicados en detalle |
| [Google Cloud Architecture Center](https://cloud.google.com/architecture) | Diagramas de referencia, guías y patrones de arquitectura |
| [Google Cloud Architecture Framework](https://cloud.google.com/architecture/framework) | Principios de diseño y operación de sistemas en Google Cloud |

### Comparar proveedores

| Recurso | Qué se aprende ahí |
|---|---|
| [Google Cloud: AWS, Azure y GCP service comparison](https://cloud.google.com/docs/get-started/aws-azure-gcp-service-comparison) | Tabla oficial de equivalencias de servicios entre los tres proveedores |
| [Microsoft: Azure para profesionales de AWS](https://learn.microsoft.com/en-us/azure/architecture/aws-professional/services) | Comparación de servicios AWS y Azure |

### Cloud native e infraestructura como código

| Recurso | Qué se aprende ahí |
|---|---|
| [The Twelve-Factor App](https://12factor.net/es/) | Las 12 buenas prácticas para aplicaciones en la nube, con versión en español |
| [12 factores: Configuración](https://12factor.net/es/config) | El factor 3 en detalle: por qué la configuración va en variables de entorno |
| [CNCF (Cloud Native Computing Foundation)](https://www.cncf.io/) | La fundación detrás de Kubernetes: proyectos, definición de cloud native y comunidad |
| [CNCF Cloud Native Landscape](https://landscape.cncf.io/) | Mapa interactivo de las herramientas del ecosistema cloud native |
| [Terraform: Introducción](https://developer.hashicorp.com/terraform/intro) | Qué es Terraform y cómo se usa la infraestructura como código |
| [Terraform: Documentación](https://developer.hashicorp.com/terraform/docs) | Documentación oficial: lenguaje HCL, proveedores y comandos |

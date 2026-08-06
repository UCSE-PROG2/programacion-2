# Recuperatorio 1 — Sistema de examen teórico de manejo

> **Importante:** No se considera válido código que no haya sido dado en clase. No es necesario ejecutar el `main` de la aplicación para la corrección; se utilizarán los tests unitarios para eso. La aplicación debe ser 100% funcional.

---

## Descripción del sistema

Un centro de emisión de licencias de conducir necesita una API REST desarrollada con **Spring Boot, Spring Data JPA y MySQL**, tal como se vio en clase, que permita registrar las respuestas de aspirantes a un examen teórico de manejo de opción múltiple y consultar los resultados obtenidos.

Cada pregunta tiene cuatro opciones posibles, de las cuales solo una es correcta, y vale 10 puntos. El puntaje máximo posible es de 100 puntos.

---

## Base de datos

Crear el esquema `manejo` en MySQL y ejecutar el siguiente script:

```sql
CREATE SCHEMA IF NOT EXISTS manejo;

CREATE TABLE manejo.pregunta (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    enunciado VARCHAR(300) NOT NULL,
    puntaje   INT          NOT NULL DEFAULT 10
);

CREATE TABLE manejo.opcion (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    pregunta_id BIGINT       NOT NULL,
    texto       VARCHAR(200) NOT NULL,
    correcta    BOOLEAN      NOT NULL DEFAULT FALSE,
    FOREIGN KEY (pregunta_id) REFERENCES manejo.pregunta(id)
);

CREATE TABLE manejo.examen (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    dni_aspirante VARCHAR(100) NOT NULL,
    fecha         DATE         NOT NULL,
    puntaje_total INT
);

CREATE TABLE manejo.detalle_examen (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    examen_id    BIGINT NOT NULL,
    pregunta_id  BIGINT NOT NULL,
    opcion_id    BIGINT NOT NULL,
    puntaje      INT    NOT NULL DEFAULT 0,
    FOREIGN KEY (examen_id)    REFERENCES manejo.examen(id),
    FOREIGN KEY (pregunta_id)  REFERENCES manejo.pregunta(id),
    FOREIGN KEY (opcion_id)    REFERENCES manejo.opcion(id)
);

INSERT INTO manejo.pregunta (enunciado, puntaje) VALUES
('¿Qué color de semáforo indica que se debe detener el vehículo?',                 10),
('¿Qué forma tiene la señal de PARE?',                                              10),
('¿Qué elemento de seguridad es obligatorio para conductor y acompañantes?',        10),
('¿Qué documento acredita la habilitación para conducir?',                         10),
('¿Qué indica una línea amarilla continua en el centro de la calzada?',            10),
('¿De qué color son las señales de advertencia o peligro?',                        10),
('¿Qué se debe hacer ante una señal de PARE?',                                     10),
('¿Qué indica una señal circular con borde rojo?',                                 10),
('¿Qué elemento debe llevar todo vehículo para señalizar una emergencia vial?',    10),
('¿Por qué lado de la calzada se circula habitualmente en Argentina?',             10);

INSERT INTO manejo.opcion (pregunta_id, texto, correcta) VALUES
(1,  'Amarillo',                    FALSE),
(1,  'Rojo',                        TRUE),
(1,  'Verde',                       FALSE),
(1,  'Azul',                        FALSE),
(2,  'Círculo',                     FALSE),
(2,  'Triángulo',                   FALSE),
(2,  'Octágono',                    TRUE),
(2,  'Rectángulo',                  FALSE),
(3,  'Apoyacabezas',                FALSE),
(3,  'Cinturón de seguridad',       TRUE),
(3,  'Guantes',                     FALSE),
(3,  'Chaleco reflectante',         FALSE),
(4,  'Cédula verde',                FALSE),
(4,  'Licencia de conducir',        TRUE),
(4,  'Seguro obligatorio',          FALSE),
(4,  'DNI',                         FALSE),
(5,  'Prohibido adelantar',         TRUE),
(5,  'Permitido estacionar',        FALSE),
(5,  'Fin de la ruta',              FALSE),
(5,  'Doble mano',                  FALSE),
(6,  'Rojo',                        FALSE),
(6,  'Amarillo',                    TRUE),
(6,  'Verde',                       FALSE),
(6,  'Blanco',                      FALSE),
(7,  'Reducir la velocidad sin detenerse',  FALSE),
(7,  'Detener totalmente el vehículo',      TRUE),
(7,  'Tocar bocina y continuar',            FALSE),
(7,  'Ceder el paso sin frenar',            FALSE),
(8,  'Obligación',                  FALSE),
(8,  'Información',                 FALSE),
(8,  'Prohibición',                 TRUE),
(8,  'Advertencia',                 FALSE),
(9,  'Matafuegos',                  FALSE),
(9,  'Triángulo de emergencia',     TRUE),
(9,  'Botiquín',                    FALSE),
(9,  'Rueda de auxilio',            FALSE),
(10, 'Izquierda',                   FALSE),
(10, 'Derecha',                     TRUE),
(10, 'Centro',                      FALSE),
(10, 'Indistinto',                  FALSE);
```

---

## Funcionalidades del sistema

**Registrar examen**: Al recibir las respuestas de un aspirante (identificado por su DNI), el sistema debe almacenar la fecha del sistema y registrar la opción elegida para cada pregunta. Por cada opción elegida, la verificación de si es correcta debe realizarse buscando esa opción de forma individual en ese momento, **sin traer el listado completo de opciones**. Si la opción es correcta, se deben registrar 10 puntos para esa pregunta; de lo contrario, 0 puntos. Una vez procesadas todas las opciones, si el aspirante respondió la totalidad de las preguntas, el sistema debe calcular la suma de los puntos obtenidos y almacenar el puntaje total.

**Buscar exámenes**: Permite consultar el historial de exámenes aplicando filtros opcionales: fecha desde, fecha hasta, DNI del aspirante (coincidencia parcial) y puntaje mínimo. Si se informa el filtro de puntaje, se deben retornar únicamente los exámenes cuyo puntaje total sea mayor o igual al valor recibido. Los filtros son combinables entre sí; si no se informa ninguno, se retornan todos los exámenes.

---

## Endpoints esperados

| Método | URL | Descripción |
|--------|-----|-------------|
| POST | `/examenes` | Registrar el examen de un aspirante |
| GET | `/examenes` | Buscar exámenes con filtros opcionales |

El cuerpo del `POST /examenes` debe tener la siguiente estructura:

```json
{
  "dni": "30123456",
  "respuestas": [
    { "preguntaId": 1, "opcionId": 2 },
    { "preguntaId": 2, "opcionId": 7 }
  ]
}
```

Los cuatro filtros del `GET /examenes` son combinables entre sí: si se envían varios, se aplican todos simultáneamente.

---

## Consideraciones técnicas

- El endpoint de registro debe devolver **HTTP 201 Created** con el resultado en el cuerpo de la respuesta.
- El endpoint de búsqueda debe devolver **HTTP 200 OK** con la lista de exámenes.
- Si una opción referenciada no existe en la base de datos, la respuesta debe ser **HTTP 404 Not Found** con un mensaje descriptivo.
- Los errores deben manejarse de forma centralizada y devolver el código y mensaje apropiados.
- Validar que el DNI no esté vacío y que la lista de respuestas no esté vacía. Si la validación falla, la respuesta debe ser **HTTP 400 Bad Request**.
- La búsqueda con filtros opcionales debe implementarse usando **CriteriaBuilder**.

---

## Tests unitarios

Escribir tests unitarios únicamente para la capa de servicio. La cobertura de líneas del servicio debe ser **igual o superior al 80%**.

Verificar tanto los caminos exitosos como los casos de error. Como mínimo, cubrir los siguientes escenarios:

- Registrar un examen donde todas las opciones elegidas son correctas y verificar que el puntaje total resultante sea 100.
- Registrar un examen donde ninguna opción es correcta y verificar que el puntaje total resultante sea 0.
- Registrar un examen con algunas opciones correctas y verificar que el puntaje parcial sea el esperado.
- Intentar registrar un examen con un ID de opción inexistente y verificar que se produzca el error correspondiente.
- Buscar exámenes utilizando distintos filtros y verificar que los resultados sean los esperados.

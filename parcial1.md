# Parcial 1 — Sistema de trivia con preguntas y respuestas

> **Importante:** No se considera válido código que no haya sido dado en clase. No es necesario ejecutar el `main` de la aplicación para la corrección; se utilizarán los tests unitarios para eso. La aplicación debe ser 100% funcional.

---

## Descripción del sistema

Una plataforma de trivia necesita una API REST desarrollada con **Spring Boot, Spring Data JPA y MySQL**, tal como se vio en clase, que permita registrar las respuestas de usuarios a un cuestionario de opción múltiple y consultar los resultados obtenidos.

Cada pregunta tiene cuatro opciones posibles, de las cuales solo una es correcta, y vale 10 puntos. El puntaje máximo posible es de 100 puntos.

---

## Base de datos

Crear el esquema `trivia` en MySQL y ejecutar el siguiente script:

```sql
CREATE SCHEMA IF NOT EXISTS trivia;

CREATE TABLE trivia.pregunta (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    enunciado VARCHAR(300) NOT NULL,
    puntaje   INT          NOT NULL DEFAULT 10
);

CREATE TABLE trivia.opcion (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    pregunta_id BIGINT       NOT NULL,
    texto       VARCHAR(200) NOT NULL,
    correcta    BOOLEAN      NOT NULL DEFAULT FALSE,
    FOREIGN KEY (pregunta_id) REFERENCES trivia.pregunta(id)
);

CREATE TABLE trivia.respuesta (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    mail_usuario  VARCHAR(100) NOT NULL,
    fecha         DATE         NOT NULL,
    puntaje_total INT
);

CREATE TABLE trivia.detalle_respuesta (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    respuesta_id BIGINT NOT NULL,
    pregunta_id  BIGINT NOT NULL,
    opcion_id    BIGINT NOT NULL,
    puntaje      INT    NOT NULL DEFAULT 0,
    FOREIGN KEY (respuesta_id) REFERENCES trivia.respuesta(id),
    FOREIGN KEY (pregunta_id)  REFERENCES trivia.pregunta(id),
    FOREIGN KEY (opcion_id)    REFERENCES trivia.opcion(id)
);

INSERT INTO trivia.pregunta (enunciado, puntaje) VALUES
('¿Cuál es el planeta más grande del sistema solar?',        10),
('¿En qué año llegó el hombre a la Luna?',                  10),
('¿Cuál es el continente más grande del mundo?',             10),
('¿Cuántos huesos tiene el cuerpo humano adulto?',           10),
('¿Cuál es el elemento químico cuyo símbolo es Fe?',         10),
('¿Cuántos lados tiene un hexágono?',                        10),
('¿Cuál es la capital de Australia?',                        10),
('¿Quién escribió "Don Quijote de la Mancha"?',             10),
('¿A qué velocidad viaja la luz en el vacío (aprox.)?',     10),
('¿Cuál es el océano más grande del mundo?',                 10);

INSERT INTO trivia.opcion (pregunta_id, texto, correcta) VALUES
(1,  'Saturno',                FALSE),
(1,  'Júpiter',                TRUE),
(1,  'Marte',                  FALSE),
(1,  'Urano',                  FALSE),
(2,  '1965',                   FALSE),
(2,  '1967',                   FALSE),
(2,  '1969',                   TRUE),
(2,  '1971',                   FALSE),
(3,  'África',                 FALSE),
(3,  'Asia',                   TRUE),
(3,  'Europa',                 FALSE),
(3,  'América',                FALSE),
(4,  '196',                    FALSE),
(4,  '206',                    TRUE),
(4,  '213',                    FALSE),
(4,  '220',                    FALSE),
(5,  'Flúor',                  FALSE),
(5,  'Fósforo',                FALSE),
(5,  'Hierro',                 TRUE),
(5,  'Francio',                FALSE),
(6,  '5',                      FALSE),
(6,  '6',                      TRUE),
(6,  '7',                      FALSE),
(6,  '8',                      FALSE),
(7,  'Sídney',                 FALSE),
(7,  'Melbourne',              FALSE),
(7,  'Canberra',               TRUE),
(7,  'Brisbane',               FALSE),
(8,  'William Shakespeare',    FALSE),
(8,  'Miguel de Cervantes',    TRUE),
(8,  'Gabriel García Márquez', FALSE),
(8,  'Lope de Vega',           FALSE),
(9,  '200.000 km/s',           FALSE),
(9,  '250.000 km/s',           FALSE),
(9,  '300.000 km/s',           TRUE),
(9,  '350.000 km/s',           FALSE),
(10, 'Atlántico',              FALSE),
(10, 'Índico',                 FALSE),
(10, 'Ártico',                 FALSE),
(10, 'Pacífico',               TRUE);
```

---

## Funcionalidades del sistema

**Registrar respuestas de un usuario**: Al recibir las respuestas de un usuario (identificado por su mail), el sistema debe almacenar la fecha del sistema y registrar la opción elegida para cada pregunta. Por cada opción elegida, la verificación de si es correcta debe realizarse buscando esa opción de forma individual en ese momento, **sin traer el listado completo de opciones**. Si la opción es correcta, se deben registrar 10 puntos para esa pregunta; de lo contrario, 0 puntos. Una vez procesadas todas las opciones, si el usuario respondió la totalidad de las preguntas, el sistema debe calcular la suma de los puntos obtenidos y almacenar el puntaje total.

**Buscar respuestas**: Permite consultar el historial de respuestas aplicando filtros opcionales: fecha desde, fecha hasta, mail del usuario (coincidencia parcial) y puntaje mínimo. Si se informa el filtro de puntaje, se deben retornar únicamente las respuestas cuyo puntaje total sea mayor o igual al valor recibido. Los filtros son combinables entre sí; si no se informa ninguno, se retornan todas las respuestas.

---

## Endpoints esperados

| Método | URL | Descripción |
|--------|-----|-------------|
| POST | `/respuestas` | Registrar las respuestas de un usuario |
| GET | `/respuestas` | Buscar respuestas con filtros opcionales |

El cuerpo del `POST /respuestas` debe tener la siguiente estructura:

```json
{
  "mail": "usuario@mail.com",
  "respuestas": [
    { "preguntaId": 1, "opcionId": 2 },
    { "preguntaId": 2, "opcionId": 7 }
  ]
}
```

Los cuatro filtros del `GET /respuestas` son combinables entre sí: si se envían varios, se aplican todos simultáneamente.

---

## Consideraciones técnicas

- El endpoint de registro debe devolver **HTTP 201 Created** con el resultado en el cuerpo de la respuesta.
- El endpoint de búsqueda debe devolver **HTTP 200 OK** con la lista de respuestas.
- Si una opción referenciada no existe en la base de datos, la respuesta debe ser **HTTP 404 Not Found** con un mensaje descriptivo.
- Los errores deben manejarse de forma centralizada y devolver el código y mensaje apropiados.
- Validar que el mail no esté vacío y que la lista de respuestas no esté vacía. Si la validación falla, la respuesta debe ser **HTTP 400 Bad Request**.
- La búsqueda con filtros opcionales debe implementarse usando **CriteriaBuilder**.

---

## Tests unitarios

Escribir tests unitarios únicamente para la capa de servicio. La cobertura de líneas del servicio debe ser **igual o superior al 80%**.

Verificar tanto los caminos exitosos como los casos de error. Como mínimo, cubrir los siguientes escenarios:

- Registrar una respuesta donde todas las opciones elegidas son correctas y verificar que el puntaje total resultante sea 100.
- Registrar una respuesta donde ninguna opción es correcta y verificar que el puntaje total resultante sea 0.
- Registrar una respuesta con algunas opciones correctas y verificar que el puntaje parcial sea el esperado.
- Intentar registrar una respuesta con un ID de opción inexistente y verificar que se produzca el error correspondiente.
- Buscar respuestas utilizando distintos filtros y verificar que los resultados sean los esperados.

-- ============================================================
-- Schema: ejemplo_orm_avanzado
--
-- Modelo de un sistema académico universitario.
-- Refleja el esquema que Hibernate genera automáticamente
-- a partir de las anotaciones @Entity del proyecto Java.
--
-- Relaciones implementadas:
--   1 a 1 : alumnos      ──── perfiles
--   1 a N : carreras     ──── materias
--   N a N : alumnos      ──── materias  (tabla: inscripciones)
--
-- Uso: ejecutar este script en MySQL Workbench o HeidiSQL
-- para levantar la base de datos con datos de prueba.
-- ============================================================

CREATE DATABASE IF NOT EXISTS ejemplo_orm_avanzado
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ejemplo_orm_avanzado;


-- ── Tablas ──────────────────────────────────────────────────────────────────

-- Carrera universitaria (lado "uno" de la relación 1 a N con materias)
CREATE TABLE IF NOT EXISTS carreras (
    id     INT          NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

-- Perfil de contacto de un alumno (lado "inverso" de la relación 1 a 1)
-- La FK vive en alumnos.perfil_id, no aquí.
CREATE TABLE IF NOT EXISTS perfiles (
    id       INT          NOT NULL AUTO_INCREMENT,
    email    VARCHAR(100),
    telefono VARCHAR(20),
    PRIMARY KEY (id)
);

-- Alumno (propietario de las relaciones 1 a 1 y N a N)
-- perfil_id puede ser NULL: no todos los alumnos tienen datos de contacto.
CREATE TABLE IF NOT EXISTS alumnos (
    id        INT          NOT NULL AUTO_INCREMENT,
    nombre    VARCHAR(100) NOT NULL,
    apellido  VARCHAR(100) NOT NULL,
    dni       VARCHAR(10)  NOT NULL,
    activo    BIT(1)       NOT NULL,
    perfil_id INT,
    PRIMARY KEY (id),
    UNIQUE  KEY uq_dni           (dni),
    CONSTRAINT fk_alumno_perfil  FOREIGN KEY (perfil_id) REFERENCES perfiles (id)
);

-- Materia del plan de estudios (lado "muchos" de la relación 1 a N con carreras)
-- carrera_id indica a qué carrera pertenece.
-- anio: año de la carrera en que se cursa (1 a 5).
-- obligatoria: 1 = materia del plan; 0 = optativa/electiva.
CREATE TABLE IF NOT EXISTS materias (
    id          INT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(100) NOT NULL,
    anio        INT          NOT NULL,
    obligatoria BIT(1)       NOT NULL,
    carrera_id  INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_materia_carrera FOREIGN KEY (carrera_id) REFERENCES carreras (id)
);

-- Tabla intermedia de la relación N a N entre alumnos y materias.
-- Representa la inscripción de un alumno a una materia.
-- Hibernate la genera automáticamente a partir de @JoinTable en Alumno.java.
CREATE TABLE IF NOT EXISTS inscripciones (
    alumno_id  INT NOT NULL,
    materia_id INT NOT NULL,
    PRIMARY KEY (alumno_id, materia_id),
    CONSTRAINT fk_insc_alumno  FOREIGN KEY (alumno_id)  REFERENCES alumnos  (id),
    CONSTRAINT fk_insc_materia FOREIGN KEY (materia_id) REFERENCES materias (id)
);


-- ── Inserts ─────────────────────────────────────────────────────────────────
-- Orden obligatorio por las FK:
--   1. carreras y perfiles (sin dependencias)
--   2. alumnos (depende de perfiles) y materias (depende de carreras)
--   3. inscripciones (depende de alumnos y materias)


-- ── carreras (20 filas) ────────────────────────────────────────────────────
INSERT INTO carreras (nombre) VALUES
    ('Ingeniería en Sistemas de Información'),    -- id = 1
    ('Licenciatura en Sistemas de Información'),  -- id = 2
    ('Ingeniería Industrial'),                    -- id = 3
    ('Contaduría Pública'),                       -- id = 4
    ('Administración de Empresas'),               -- id = 5
    ('Ingeniería Civil'),                         -- id = 6
    ('Ingeniería Electrónica'),                   -- id = 7
    ('Ingeniería Química'),                       -- id = 8
    ('Arquitectura'),                             -- id = 9
    ('Diseño Gráfico'),                           -- id = 10
    ('Ingeniería en Telecomunicaciones'),         -- id = 11
    ('Medicina'),                                 -- id = 12
    ('Abogacía'),                                 -- id = 13
    ('Psicología'),                               -- id = 14
    ('Nutrición y Dietética'),                    -- id = 15
    ('Ingeniería Mecánica'),                      -- id = 16
    ('Ciencias de la Educación'),                 -- id = 17
    ('Trabajo Social'),                           -- id = 18
    ('Ingeniería Agronómica'),                    -- id = 19
    ('Ingeniería Ambiental');                     -- id = 20


-- ── perfiles (20 filas) ────────────────────────────────────────────────────
INSERT INTO perfiles (email, telefono) VALUES
    ('garcia.ana@mail.com',        '3512111001'),  -- id = 1
    ('lopez.carlos@mail.com',      '3513222002'),  -- id = 2
    ('martinez.laura@mail.com',    '3514333003'),  -- id = 3
    ('fernandez.diego@mail.com',   '3515444004'),  -- id = 4
    ('torres.sofia@mail.com',      '3516555005'),  -- id = 5
    ('rodriguez.pablo@mail.com',   '3517666006'),  -- id = 6
    ('gomez.valentina@mail.com',   '3518777007'),  -- id = 7
    ('sanchez.nicolas@mail.com',   '3519888008'),  -- id = 8
    ('diaz.camila@mail.com',       '3511999009'),  -- id = 9
    ('alvarez.martin@mail.com',    '3512000010'),  -- id = 10
    ('perez.lucia@mail.com',       '3513111011'),  -- id = 11
    ('ramirez.andres@mail.com',    '3514222012'),  -- id = 12
    ('silva.florencia@mail.com',   '3515333013'),  -- id = 13
    ('morales.juan@mail.com',      '3516444014'),  -- id = 14
    ('herrera.belen@mail.com',     '3517555015'),  -- id = 15
    ('castro.ignacio@mail.com',    '3518666016'),  -- id = 16
    ('rojas.martina@mail.com',     '3519777017'),  -- id = 17
    ('ruiz.sebastian@mail.com',    '3511888018'),  -- id = 18
    ('vargas.julieta@mail.com',    '3512999019'),  -- id = 19
    ('medina.agustin@mail.com',    '3513000020');  -- id = 20


-- ── alumnos (20 filas) ─────────────────────────────────────────────────────
-- 14 alumnos con perfil_id, 6 sin (NULL → no tienen datos de contacto).
-- 3 alumnos inactivos (activo = 0): Diego (id=4), Martín (id=10), Sebastián (id=18).
INSERT INTO alumnos (nombre, apellido, dni, activo, perfil_id) VALUES
    ('Ana',       'García',      '30111001', 1,  1),
    ('Carlos',    'López',       '31222002', 1,  2),
    ('Laura',     'Martínez',    '32333003', 1,  3),
    ('Diego',     'Fernández',   '33444004', 0,  4),  -- inactivo
    ('Sofía',     'Torres',      '34555005', 1,  5),
    ('Pablo',     'Rodríguez',   '35666006', 1,  6),
    ('Valentina', 'Gómez',       '36777007', 1,  7),
    ('Nicolás',   'Sánchez',     '37888008', 1,  8),
    ('Camila',    'Díaz',        '38999009', 1,  9),
    ('Martín',    'Álvarez',     '39000010', 0, 10),  -- inactivo
    ('Lucía',     'Pérez',       '40111011', 1, 11),
    ('Andrés',    'Ramírez',     '41222012', 1, 12),
    ('Florencia', 'Silva',       '42333013', 1, 13),
    ('Juan',      'Morales',     '43444014', 1, 14),
    ('Belén',     'Herrera',     '44555015', 1, NULL), -- sin perfil
    ('Ignacio',   'Castro',      '45666016', 1, NULL), -- sin perfil
    ('Martina',   'Rojas',       '30777017', 1, NULL), -- sin perfil
    ('Sebastián', 'Ruiz',        '31888018', 0, NULL), -- sin perfil, inactivo
    ('Julieta',   'Vargas',      '32999019', 1, NULL), -- sin perfil
    ('Agustín',   'Medina',      '33000020', 1, NULL); -- sin perfil


-- ── materias (20 filas) ────────────────────────────────────────────────────
-- Distribuidas en las 5 primeras carreras.
-- anio: año del plan de estudios (1 al 5).
-- obligatoria: 1 = troncal del plan, 0 = optativa/electiva.
INSERT INTO materias (nombre, anio, obligatoria, carrera_id) VALUES
    -- Ingeniería en Sistemas (carrera_id = 1)
    ('Programación 1',                     1, 1, 1),  -- id = 1
    ('Programación 2',                     2, 1, 1),  -- id = 2
    ('Bases de Datos',                     2, 1, 1),  -- id = 3
    ('Algoritmos y Estructuras de Datos',  2, 1, 1),  -- id = 4
    ('Redes de Computadoras',              3, 1, 1),  -- id = 5
    ('Sistemas Operativos',                3, 1, 1),  -- id = 6
    ('Ingeniería de Software',             4, 0, 1),  -- id = 7  (optativa)
    ('Arquitectura de Software',           4, 0, 1),  -- id = 8  (optativa)
    -- Licenciatura en Sistemas (carrera_id = 2)
    ('Análisis y Diseño de Sistemas',      2, 1, 2),  -- id = 9
    ('Gestión de Proyectos de Software',   3, 1, 2),  -- id = 10
    ('Business Intelligence',              4, 0, 2),  -- id = 11 (optativa)
    ('Seguridad Informática',              4, 0, 2),  -- id = 12 (optativa)
    -- Ingeniería Industrial (carrera_id = 3)
    ('Física General',                     1, 1, 3),  -- id = 13
    ('Matemática Aplicada',                1, 1, 3),  -- id = 14
    ('Gestión de la Producción',           3, 1, 3),  -- id = 15
    -- Contaduría Pública (carrera_id = 4)
    ('Contabilidad General',               1, 1, 4),  -- id = 16
    ('Contabilidad de Costos',             2, 1, 4),  -- id = 17
    ('Impuestos y Tributación',            3, 1, 4),  -- id = 18
    -- Administración de Empresas (carrera_id = 5)
    ('Administración General',             1, 1, 5),  -- id = 19
    ('Marketing Estratégico',              2, 0, 5);  -- id = 20 (optativa)


-- ── inscripciones (20 filas — tabla N a N) ────────────────────────────────
-- Cada fila representa que un alumno está inscripto en una materia.
-- La PK compuesta (alumno_id, materia_id) garantiza que no haya duplicados.
INSERT INTO inscripciones (alumno_id, materia_id) VALUES
    (1,  1),   -- Ana       → Programación 1
    (1,  2),   -- Ana       → Programación 2
    (1,  3),   -- Ana       → Bases de Datos
    (2,  1),   -- Carlos    → Programación 1
    (2,  4),   -- Carlos    → Algoritmos y Estructuras de Datos
    (3,  1),   -- Laura     → Programación 1
    (3,  9),   -- Laura     → Análisis y Diseño de Sistemas
    (5,  5),   -- Sofía     → Redes de Computadoras
    (5,  6),   -- Sofía     → Sistemas Operativos
    (6,  7),   -- Pablo     → Ingeniería de Software
    (6,  8),   -- Pablo     → Arquitectura de Software
    (7, 13),   -- Valentina → Física General
    (7, 14),   -- Valentina → Matemática Aplicada
    (8, 16),   -- Nicolás   → Contabilidad General
    (8, 17),   -- Nicolás   → Contabilidad de Costos
    (9, 19),   -- Camila    → Administración General
    (9, 20),   -- Camila    → Marketing Estratégico
    (11, 1),   -- Lucía     → Programación 1
    (11, 3),   -- Lucía     → Bases de Datos
    (12, 15);  -- Andrés    → Gestión de la Producción

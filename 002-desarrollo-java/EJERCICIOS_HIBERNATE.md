# Ejercicios de práctica — Hibernate con JPA

**Dentro del mismo proyecto** (`002b-ejercicios-orm`).
Se puede reutilizar `HibernateUtil` y el archivo `hibernate.cfg.xml`; solo hay que
agregar cada nueva entidad al XML:

```xml
<mapping class="com.example.orm.model.NombreEntidad"/>
```

Esquema SQL compartido: **`002b-ejercicios`**

---

## Ejercicio 1 — Gestión de productos

### Objetivo
Registrar productos y consultar cuáles tienen bajo stock o están discontinuados.

Implementar:
- `Producto.java` — entidad con campos: `nombre` (String), `precio` (Double), `stock` (Integer), `activo` (Boolean)
- `ProductoRepository.java` — Singleton con los siguientes métodos:
  - `guardar` — INSERT: persiste un nuevo producto en la base de datos
  - `listarTodos` — SELECT sin filtros: trae todos los registros de la tabla
  - `listarConBajoStock(int limite)` — SELECT con filtro `<=` sobre el campo `stock`
  - `listarActivos` — SELECT con filtro de igualdad (`=`) sobre el campo booleano `activo`
  - `buscarActivosConPrecioMenorA(double precioMax)` — SELECT con dos filtros combinados con **AND**: `activo = true` AND `precio <= precioMax`
  - `buscarBaratosOSinStock(double precioMax, int stockMin)` — SELECT con dos filtros combinados con **OR**: `precio <= precioMax` OR `stock <= stockMin`
- `ProductoService.java` — con los siguientes métodos:
  - `registrar` — llama al repository para insertar; el campo `activo` siempre arranca en `true`
  - `reporteBajoStock(int limite)` — delega al repository la búsqueda con `<=` y muestra el resultado por pantalla
  - `buscarOfertasOCriticos(double precioMax, int stockMin)` — llama al método OR del repository y lista los resultados

### Script SQL

```sql
CREATE SCHEMA IF NOT EXISTS `002b-ejercicios`;

CREATE TABLE `002b-ejercicios`.producto (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre   VARCHAR(100) NOT NULL,
    precio   DOUBLE       NOT NULL,
    stock    INT          NOT NULL,
    activo   BOOLEAN      NOT NULL DEFAULT TRUE
);
```

---

## Ejercicio 2 — Turnos médicos

### Objetivo
Registrar turnos de pacientes y consultar disponibilidad por médico o por fecha.

Implementar:
- `Turno.java` — entidad con campos: `paciente` (String), `medico` (String), `fecha` (LocalDate), `confirmado` (Boolean)
- `TurnoRepository.java` — Singleton con los siguientes métodos:
  - `guardar` — INSERT: persiste un nuevo turno; `confirmado` arranca en `false`
  - `buscarPorMedico(String medico)` — SELECT con filtro de igualdad (`=`) sobre el campo `medico`
  - `buscarPorFecha(LocalDate fecha)` — SELECT con filtro de igualdad (`=`) sobre el campo `fecha`
  - `buscarConfirmadosDeMedico(String medico)` — SELECT con dos filtros combinados con **AND**: `medico = X` AND `confirmado = true`
  - `buscarPorMedicoOFecha(String medico, LocalDate fecha)` — SELECT con dos filtros combinados con **OR**: `medico = X` OR `fecha = Y`
- `TurnoService.java` — con los siguientes métodos:
  - `reservar` — llama al repository para insertar el turno
  - `mostrarConfirmadosDe(String medico)` — usa el método AND del repository y lista los turnos confirmados de ese médico
  - `mostrarTurnosDeMedicoOFecha(String medico, LocalDate fecha)` — usa el método OR del repository y lista los resultados

### Script SQL

```sql
CREATE TABLE `002b-ejercicios`.turno (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente   VARCHAR(100) NOT NULL,
    medico     VARCHAR(100) NOT NULL,
    fecha      DATE         NOT NULL,
    confirmado BOOLEAN      NOT NULL DEFAULT FALSE
);
```

---

## Ejercicio 3 — Registro de empleados

### Objetivo
Administrar empleados y calcular métricas salariales.

Implementar:
- `Empleado.java` — entidad con campos: `nombre` (String), `apellido` (String), `salario` (Double), `fechaIngreso` (LocalDate) — columna `fecha_ingreso`
- `EmpleadoRepository.java` — Singleton con los siguientes métodos:
  - `guardar` — INSERT: persiste un nuevo empleado; `fechaIngreso` se setea con la fecha actual
  - `listarTodos` — SELECT sin filtros: trae todos los empleados
  - `listarConSalarioMayorA(double minimo)` — SELECT con filtro `>` sobre el campo `salario`, ordenado de mayor a menor
  - `buscarPorRangoDeSalario(double min, double max)` — SELECT con dos filtros combinados con **AND**: `salario >= min` AND `salario <= max`
  - `buscarIngresadosRecientesOConAltoSalario(LocalDate desde, double salarioMin)` — SELECT con dos filtros combinados con **OR**: `fechaIngreso >= desde` OR `salario >= salarioMin`
- `EmpleadoService.java` — con los siguientes métodos:
  - `contratar` — llama al repository para insertar el empleado
  - `buscarEnBandaSalarial(double min, double max)` — usa el método AND del repository y lista los empleados dentro del rango
  - `buscarDestacadosORecientes(LocalDate desde, double salarioMin)` — usa el método OR del repository y lista los resultados

### Script SQL

```sql
CREATE TABLE `002b-ejercicios`.empleado (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(80)  NOT NULL,
    apellido       VARCHAR(80)  NOT NULL,
    salario        DOUBLE       NOT NULL,
    fecha_ingreso  DATE         NOT NULL
);
```

---

## Ejercicio 4 — Catálogo de libros

### Objetivo
Gestionar el catálogo de una biblioteca y verificar disponibilidad para préstamo.

Implementar:
- `Libro.java` — entidad con campos: `titulo` (String), `autor` (String), `anioPublicacion` (Integer) — columna `anio_publicacion`, `disponible` (Boolean)
- `LibroRepository.java` — Singleton con los siguientes métodos:
  - `guardar` — INSERT: persiste un nuevo libro; `disponible` arranca en `true`
  - `buscarPorAutor(String autor)` — SELECT con filtro de igualdad (`=`) sobre `autor`, ordenado por `anioPublicacion ASC`
  - `listarDisponibles` — SELECT con filtro de igualdad (`=`) sobre el campo booleano `disponible`
  - `buscarPorId(Long id)` — SELECT por clave primaria usando `session.get()`; devuelve `null` si no existe
  - `actualizarDisponibilidad(Long id, boolean disponible)` — UPDATE: busca el libro por id dentro de la misma transacción y modifica el campo `disponible`, luego hace `merge`
  - `buscarDisponiblesDeAutor(String autor)` — SELECT con dos filtros combinados con **AND**: `disponible = true` AND `autor = X`
  - `buscarDisponiblesODeAutor(String autor)` — SELECT con dos filtros combinados con **OR**: `disponible = true` OR `autor = X`
- `LibroService.java` — con los siguientes métodos:
  - `agregar` — llama al repository para insertar el libro
  - `prestar(Long id)` — primero busca por id; si no existe o ya está prestado, muestra un mensaje y corta; si está disponible, llama a `actualizarDisponibilidad` con `false`
  - `buscarDisponiblesDeAutor(String autor)` — usa el método AND del repository (libros disponibles de ese autor puntualmente)
  - `buscarCatalogoDeAutor(String autor)` — usa el método OR del repository (libros disponibles en general más todos los del autor, estén o no prestados)

### Script SQL

```sql
CREATE TABLE `002b-ejercicios`.libro (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo            VARCHAR(150) NOT NULL,
    autor             VARCHAR(100) NOT NULL,
    anio_publicacion  INT          NOT NULL,
    disponible        BOOLEAN      NOT NULL DEFAULT TRUE
);
```

---

## Ejercicio 5 — Registro de ventas

### Objetivo
Registrar ventas diarias y analizar el rendimiento por producto y por día.

Implementar:
- `Venta.java` — entidad con campos: `producto` (String), `cantidad` (Integer), `total` (Double), `fecha` (LocalDate)
- `VentaRepository.java` — Singleton con los siguientes métodos:
  - `guardar` — INSERT: persiste una nueva venta; `fecha` se setea con la fecha actual
  - `buscarPorFecha(LocalDate fecha)` — SELECT con filtro de igualdad (`=`) sobre el campo `fecha`
  - `buscarPorFechaYProducto(LocalDate fecha, String producto)` — SELECT con dos filtros combinados con **AND**: `fecha = X` AND `producto = Y`
  - `buscarVentasGrandesORecientes(double totalMin, LocalDate desde)` — SELECT con dos filtros combinados con **OR**: `total >= totalMin` OR `fecha >= desde`
- `VentaService.java` — con los siguientes métodos:
  - `registrar` — calcula `total = cantidad * precioUnitario` en el servicio antes de insertar; el repository no recibe el precio unitario, solo la entidad ya armada
  - `buscarVentasDelDiaDe(String producto)` — usa el método AND del repository con la fecha de hoy y el producto recibido
  - `buscarVentasDestacadas(double totalMin, LocalDate desde)` — usa el método OR del repository y lista los resultados

### Script SQL

```sql
CREATE TABLE `002b-ejercicios`.venta (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto  VARCHAR(100) NOT NULL,
    cantidad  INT          NOT NULL,
    total     DOUBLE       NOT NULL,
    fecha     DATE         NOT NULL
);
```

---

## Main — `Main.java`

Archivo único que integra los 5 ejercicios mediante un menú.

```java
package com.example.orm;

import com.example.orm.service.*;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            System.out.println("\n=== Menú de ejercicios ===");
            System.out.println("1. Gestión de productos");
            System.out.println("2. Turnos médicos");
            System.out.println("3. Registro de empleados");
            System.out.println("4. Catálogo de libros");
            System.out.println("5. Registro de ventas");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            int opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1 -> {
                    ProductoService productos = new ProductoService();
                    System.out.print("Nombre del producto: ");
                    String nombre = sc.nextLine();
                    System.out.print("Precio: ");
                    double precio = sc.nextDouble();
                    System.out.print("Stock inicial: ");
                    int stock = sc.nextInt();
                    sc.nextLine();
                    productos.registrar(nombre, precio, stock);
                    productos.reporteBajoStock(5);
                    productos.buscarOfertasOCriticos(500.0, 3);
                }
                case 2 -> {
                    TurnoService turnos = new TurnoService();
                    System.out.print("Nombre del paciente: ");
                    String paciente = sc.nextLine();
                    System.out.print("Nombre del médico: ");
                    String medico = sc.nextLine();
                    System.out.print("Fecha del turno (YYYY-MM-DD): ");
                    LocalDate fecha = LocalDate.parse(sc.nextLine());
                    turnos.reservar(paciente, medico, fecha);
                    turnos.mostrarConfirmadosDe(medico);
                    turnos.mostrarTurnosDeMedicoOFecha(medico, LocalDate.now());
                }
                case 3 -> {
                    EmpleadoService empleados = new EmpleadoService();
                    System.out.print("Nombre: ");
                    String nombre = sc.nextLine();
                    System.out.print("Apellido: ");
                    String apellido = sc.nextLine();
                    System.out.print("Salario: ");
                    double salario = sc.nextDouble();
                    sc.nextLine();
                    empleados.contratar(nombre, apellido, salario);
                    empleados.buscarEnBandaSalarial(50000, 150000);
                    empleados.buscarDestacadosORecientes(LocalDate.now().minusMonths(6), 120000);
                }
                case 4 -> {
                    LibroService libros = new LibroService();
                    System.out.print("Título: ");
                    String titulo = sc.nextLine();
                    System.out.print("Autor: ");
                    String autor = sc.nextLine();
                    System.out.print("Año de publicación: ");
                    int anio = Integer.parseInt(sc.nextLine());
                    libros.agregar(titulo, autor, anio);
                    libros.buscarDisponiblesDeAutor(autor);
                    libros.buscarCatalogoDeAutor(autor);
                }
                case 5 -> {
                    VentaService ventas = new VentaService();
                    System.out.print("Producto vendido: ");
                    String producto = sc.nextLine();
                    System.out.print("Cantidad: ");
                    int cantidad = sc.nextInt();
                    System.out.print("Precio unitario: ");
                    double precioU = sc.nextDouble();
                    sc.nextLine();
                    ventas.registrar(producto, cantidad, precioU);
                    ventas.buscarVentasDelDiaDe(producto);
                    ventas.buscarVentasDestacadas(1000.0, LocalDate.now().minusDays(7));
                }
                case 0 -> salir = true;
                default -> System.out.println("Opción inválida.");
            }
        }
    }
}
```

---

## Recordatorio: agregar entidades al XML

En `hibernate.cfg.xml`, dentro de `<session-factory>`, agregar una línea por cada entidad:

```xml
<mapping class="com.example.orm.model.Producto"/>
<mapping class="com.example.orm.model.Turno"/>
<mapping class="com.example.orm.model.Empleado"/>
<mapping class="com.example.orm.model.Libro"/>
<mapping class="com.example.orm.model.Venta"/>
```

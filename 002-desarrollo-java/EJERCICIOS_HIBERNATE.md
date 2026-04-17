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
- `ProductoRepository.java` — Singleton con métodos: `guardar`, `listarTodos`, `listarConBajoStock(int limite)`, `listarActivos`
- `ProductoService.java` — con métodos: `registrar`, `calcularValorTotalInventario`, `reporteBajoStock(int limite)`

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
- `TurnoRepository.java` — Singleton con métodos: `guardar`, `buscarPorMedico`, `buscarPorFecha`, `contarConfirmadosPorMedico`
- `TurnoService.java` — con métodos: `reservar`, `mostrarTurnosDel(String medico)`, `mostrarTurnosDeFecha(LocalDate fecha)`

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
- `EmpleadoRepository.java` — Singleton con métodos: `guardar`, `listarTodos`, `listarConSalarioMayorA(Double minimo)`, `calcularPromedioDeSalarios`
- `EmpleadoService.java` — con métodos: `contratar`, `reporteSalarial` (muestra promedio y empleados sobre el promedio)

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
- `LibroRepository.java` — Singleton con métodos: `guardar`, `buscarPorAutor`, `listarDisponibles`, `buscarPorId`, `actualizarDisponibilidad`
- `LibroService.java` — con métodos: `agregar`, `prestar(Long id)` (valida que esté disponible), `mostrarDisponibles`, `buscarPorAutor`

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
- `VentaRepository.java` — Singleton con métodos: `guardar`, `buscarPorFecha`, `totalRecaudadoEnFecha`, `rankingDeProductos` (GROUP BY con SUM)
- `VentaService.java` — con métodos: `registrar` (calcula el total a partir de precio unitario), `resumenDelDia`, `rankingProductos`

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
                    System.out.printf("Valor total del inventario: $%.2f%n",
                        productos.calcularValorTotalInventario());
                    productos.reporteBajoStock(5);
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
                    turnos.mostrarTurnosDel(medico);
                    turnos.mostrarTurnosDeFecha(LocalDate.now());
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
                    empleados.reporteSalarial();
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
                    libros.mostrarDisponibles();
                    libros.buscarPorAutor(autor);
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
                    ventas.resumenDelDia();
                    ventas.rankingProductos();
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

# Ejercicios Java - Programación 2

## Configuración inicial

1. Crear un repositorio en GitHub (ej: `prog2-ejercicios-java`) — puede ser público o privado
2. Copiar la URL del repositorio y hacer **git clone** en una carpeta de tu computadora:
   ```bash
   git clone https://github.com/tu-usuario/prog2-ejercicios-java.git
   ```
3. Abrir IntelliJ y crear el proyecto Java **dentro de la carpeta que se acaba de clonar**:
   `File → New Project` → seleccionar la carpeta del repositorio como ubicación → en el campo **JDK** elegir `Java 21 (Zulu)`. Si no aparece, hacer clic en `Add JDK` y apuntar a la instalación de Zulu 21.

   > Si no tenés instalado Zulu 21, descargarlo desde [azul.com/downloads](https://www.azul.com/downloads/) seleccionando **Zulu** y **Java 21**.

   > **Importante:** el proyecto Java debe quedar dentro de la carpeta del repositorio. Si el proyecto se crea en otro lado, los cambios no van a estar versionados.

## Estructura del proyecto

Cada ejercicio va en su propio paquete dentro de `src/`:

```
src/
├── Main.java                        ← único main, acá se prueban todos los ejercicios
├── ejercicio01_pedidos/
│   ├── Producto.java
│   ├── Pedido.java
│   └── GestionPedidos.java
├── ejercicio02_cursos/
│   ├── Curso.java
│   ├── Estudiante.java
│   └── AdministracionCursos.java
└── ...
```

> `Main.java` es el único punto de entrada del programa. Cada paquete contiene solo las clases del ejercicio, sin `main`. Para probar un ejercicio, instanciar las clases desde `Main.java`.

---

## Ejemplo de referencia

Antes de arrancar con los ejercicios, tomá este ejemplo como modelo de cómo estructurar el código y las pruebas.

```java
// Libro.java
public class Libro {
    String titulo;
    String autor;
    boolean disponible;

    public Libro(String titulo, String autor) {
        this.titulo = titulo;
        this.autor = autor;
        this.disponible = true;
    }
}
```

```java
// Usuario.java
public class Usuario {
    String nombre;
    String dni;
    Libro libroActual;

    public Usuario(String nombre, String dni) {
        this.nombre = nombre;
        this.dni = dni;
    }
}
```

```java
// ejercicio00_biblioteca/Biblioteca.java
import java.util.ArrayList;
import java.util.List;

public class Biblioteca {

    List<Libro> libros = new ArrayList<>();
    List<Usuario> usuarios = new ArrayList<>();

    public boolean prestarLibro(Libro libro, Usuario usuario) {
        if (libro.disponible) {
            libro.disponible = false;
            usuario.libroActual = libro;
            return true;
        }
        return false;
    }

    public Libro devolverLibro(Libro libro, Usuario usuario) {
        libro.disponible = true;
        usuario.libroActual = null;
        return libro;
    }

    public boolean consultarDisponibilidad(Libro libro) {
        return libro.disponible;
    }
}
```

```java
// Main.java
public class Main {
    public static void main(String[] args) {
        ejercicioBiblioteca();
        ejercicioPedidos();
        ejercicioCursos();
    }

    static void ejercicioBiblioteca() {
        // Pedir datos por pantalla con Scanner (titulo, autor, nombre de usuario, dni)
        // Crear objetos Libro y Usuario con esos datos
        // Crear instancia de Biblioteca y agregar los objetos a sus listas
        // Invocar prestarLibro(), consultarDisponibilidad(), devolverLibro()
        // Imprimir los resultados retornados por cada método
    }

    static void ejercicioPedidos() {
        // Pedir datos por pantalla (nombre y precio de productos, cantidad)
        // Crear objetos Producto y un Pedido vacío
        // Invocar agregarProducto() por cada producto ingresado
        // Invocar calcularTotal() e imprimir el resultado
        // Pedir qué producto quitar, invocar quitarProducto() y mostrar el nuevo total
    }

    static void ejercicioCursos() {
        // Pedir datos del curso (nombre, cupo) y de varios estudiantes (nombre, legajo)
        // Crear instancia de Curso y lista de Estudiante
        // Invocar inscribirEstudiante() para cada uno e imprimir el resultado
        // Invocar verificarCupo() e imprimir si hay lugares disponibles
        // Invocar darBajaEstudiante() para alguno y mostrar el resultado
    }
}
```


---

## Ejercicios

### 1. Gestión de Pedidos

**Clases:**
- `Producto`: `nombre: String`, `precio: double`
- `Pedido`: `productos: List<Producto>`, `total: double`

**Métodos a implementar:**
- `agregarProducto(Pedido, Producto)` → agrega el producto a la lista y suma su precio al total
- `quitarProducto(Pedido, Producto)` → elimina el producto de la lista y resta su precio del total
- `calcularTotal(Pedido)` → recalcula y retorna el total sumando los precios de todos los productos

---

### 2. Administración de Cursos

**Clases:**
- `Curso`: `nombre: String`, `cupo: int`, `inscriptos: List<Estudiante>`
- `Estudiante`: `nombre: String`, `legajo: String`

**Métodos a implementar:**
- `inscribirEstudiante(Curso, Estudiante)` → agrega al estudiante si la cantidad de inscriptos es menor al cupo
- `darBajaEstudiante(Curso, Estudiante)` → elimina al estudiante de la lista
- `verificarCupo(Curso)` → retorna `true` si `inscriptos.size() < cupo`

---

### 3. Gestión de Cuentas Bancarias

**Clases:**
- `Cuenta`: `numero: String`, `saldo: double`
- `Cliente`: `nombre: String`, `dni: String`

**Métodos a implementar:**
- `depositar(Cuenta, double monto)` → suma el monto al saldo
- `retirar(Cuenta, double monto)` → resta el monto si el saldo es suficiente; si no, informar error
- `consultarSaldo(Cuenta)` → retorna el saldo actual

---

### 4. Sistema de Reservas de Hotel

**Clases:**
- `Habitacion`: `numero: int`, `disponible: boolean`
- `Huesped`: `nombre: String`, `dni: String`

**Métodos a implementar:**
- `reservarHabitacion(Habitacion, Huesped)` → asigna la habitación al huésped si está disponible y la marca como ocupada
- `cancelarReserva(Habitacion, Huesped)` → desvincula al huésped y la marca como disponible
- `verificarDisponibilidad(Habitacion)` → retorna `true` si está libre

---

### 5. Gestión de Inventario

**Clases:**
- `Producto`: `nombre: String`, `stock: int`
- `Proveedor`: `nombre: String`, `cuit: String`

**Métodos a implementar:**
- `reponerStock(Producto, int cantidad)` → suma la cantidad al stock
- `descontarStock(Producto, int cantidad)` → resta la cantidad si hay stock suficiente; si no, informar error
- `verificarStock(Producto)` → retorna el stock actual

---

### 6. Gestión de Empleados

**Clases:**
- `Empleado`: `nombre: String`, `salario: double`
- `Departamento`: `nombre: String`, `empleados: List<Empleado>`

**Métodos a implementar:**
- `agregarEmpleado(Departamento, Empleado)` → agrega el empleado a la lista
- `removerEmpleado(Departamento, Empleado)` → elimina el empleado de la lista
- `calcularNomina(Departamento)` → retorna la suma de todos los salarios del departamento

---

### 7. Sistema de Ventas

**Clases:**
- `Cliente`: `nombre: String`, `dni: String`, `historial: List<Compra>`
- `Compra`: `productos: List<Producto>`, `total: double`

**Métodos a implementar:**
- `realizarCompra(Cliente, Compra)` → agrega la compra al historial del cliente
- `devolverCompra(Cliente, Compra)` → elimina la compra del historial
- `consultarHistorial(Cliente)` → retorna la lista de compras del cliente

---

### 8. Gestión de Alquiler de Autos

**Clases:**
- `Auto`: `patente: String`, `disponible: boolean`
- `Cliente`: `nombre: String`, `licencia: String`

**Métodos a implementar:**
- `alquilarAuto(Auto, Cliente)` → asigna el auto al cliente si está disponible y lo marca como no disponible
- `devolverAuto(Auto, Cliente)` → desvincula al cliente y marca el auto como disponible
- `verificarDisponibilidad(Auto)` → retorna `true` si está libre

---

### 9. Sistema de Consultas Médicas

**Clases:**
- `Paciente`: `nombre: String`, `dni: String`, `consultas: List<Consulta>`
- `Consulta`: `fecha: String`, `especialidad: String`

**Métodos a implementar:**
- `agendarConsulta(Paciente, Consulta)` → agrega la consulta a la lista del paciente
- `cancelarConsulta(Paciente, Consulta)` → elimina la consulta de la lista
- `listarConsultas(Paciente)` → retorna la lista de consultas programadas

---

### 10. Gestión de Proyectos

**Clases:**
- `Proyecto`: `nombre: String`, `presupuesto: double`, `empleados: List<Empleado>`
- `Empleado`: `nombre: String`, `puesto: String`, `salario: double`

**Métodos a implementar:**
- `asignarEmpleado(Proyecto, Empleado)` → agrega al empleado al proyecto
- `removerEmpleado(Proyecto, Empleado)` → elimina al empleado del proyecto
- `calcularGasto(Proyecto)` → retorna la suma de salarios de todos los empleados asignados

---

### 11. Sistema de Vuelos

**Clases:**
- `Vuelo`: `codigo: String`, `capacidad: int`, `pasajeros: List<Pasajero>`
- `Pasajero`: `nombre: String`, `pasaporte: String`

**Métodos a implementar:**
- `reservarAsiento(Vuelo, Pasajero)` → agrega al pasajero si hay capacidad disponible (`pasajeros.size() < capacidad`)
- `cancelarReserva(Vuelo, Pasajero)` → elimina al pasajero del vuelo
- `verificarCapacidad(Vuelo)` → retorna la cantidad de asientos disponibles (`capacidad - pasajeros.size()`)

---

### 12. Gestión de Alquiler de Equipos

**Clases:**
- `Equipo`: `nombre: String`, `disponible: boolean`
- `Usuario`: `nombre: String`, `id: String`

**Métodos a implementar:**
- `alquilarEquipo(Equipo, Usuario)` → asigna el equipo al usuario si está disponible y lo marca como no disponible
- `devolverEquipo(Equipo, Usuario)` → desvincula al usuario y marca el equipo como disponible
- `verificarDisponibilidad(Equipo)` → retorna `true` si el equipo está libre

---

### 13. Sistema de Notas Escolares

**Clases:**
- `Estudiante`: `nombre: String`, `legajo: String`, `notas: Map<String, Double>` (clave: nombre de la materia)
- `Materia`: `nombre: String`

**Métodos a implementar:**
- `asignarNota(Estudiante, Materia, double nota)` → registra la nota para esa materia en el mapa del estudiante
- `modificarNota(Estudiante, Materia, double nota)` → reemplaza la nota existente de esa materia
- `calcularPromedio(Estudiante)` → retorna el promedio de todos los valores del mapa de notas

---

### 14. Sistema de Turnos Médicos

**Clases:**
- `Medico`: `nombre: String`, `especialidad: String`, `turnos: List<Turno>`
- `Turno`: `fecha: String`, `paciente: String`

**Métodos a implementar:**
- `asignarTurno(Medico, Turno)` → agrega el turno a la agenda del médico
- `cancelarTurno(Medico, Turno)` → elimina el turno de la agenda
- `consultarAgenda(Medico)` → retorna la lista de turnos del médico

---

### 15. Gestión de Torneo Deportivo

**Clases:**
- `Equipo`: `nombre: String`, `puntos: int`
- `Partido`: `equipoLocal: Equipo`, `equipoVisitante: Equipo`, `jugado: boolean`

**Métodos a implementar:**
- `registrarResultado(Partido, int golesLocal, int golesVisitante)` → suma 3 puntos al ganador o 1 a cada equipo si hay empate, y marca el partido como jugado
- `obtenerPuntos(Equipo)` → retorna los puntos actuales del equipo
- `partidoJugado(Partido)` → retorna `true` si el partido ya fue jugado

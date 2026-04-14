package org.example;

import org.example.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String URL  = "jdbc:mysql://localhost:3306/ejemplo_app?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "123456";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            crearTabla(conn);

            // INSERT
            System.out.println("=== INSERT ===");
            insertar(conn, "Ana García",     true);
            insertar(conn, "Carlos López",   false);
            insertar(conn, "Laura Martínez", true);

            // SELECT
            System.out.println("\n=== SELECT todos ===");
            listar(conn).forEach(System.out::println);

            // UPDATE
            System.out.println("\n=== UPDATE (desactivar id=1) ===");
            actualizar(conn, 1, false);
            listar(conn).forEach(System.out::println);

            // DELETE
            System.out.println("\n=== DELETE (eliminar id=2) ===");
            eliminar(conn, 2);
            listar(conn).forEach(System.out::println);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void crearTabla(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id     INT PRIMARY KEY AUTO_INCREMENT,
                name   VARCHAR(45) NOT NULL,
                active BIT NOT NULL
            )
            """;
        conn.createStatement().executeUpdate(sql);
    }

    static void insertar(Connection conn, String name, boolean active) throws SQLException {
        String sql = "INSERT INTO users (name, active) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.setBoolean(2, active);
        int filas = stmt.executeUpdate();
        System.out.println(filas + " fila insertada: " + name);
    }

    static void actualizar(Connection conn, int id, boolean active) throws SQLException {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setBoolean(1, active);
        stmt.setInt(2, id);
        int filas = stmt.executeUpdate();
        System.out.println(filas + " fila actualizada");
    }

    static void eliminar(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, id);
        int filas = stmt.executeUpdate();
        System.out.println(filas + " fila eliminada");
    }

    static List<User> listar(Connection conn) throws SQLException {
        List<User> usuarios = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM users");
        while (rs.next()) {
            usuarios.add(new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getBoolean("active")
            ));
        }
        return usuarios;
    }
}

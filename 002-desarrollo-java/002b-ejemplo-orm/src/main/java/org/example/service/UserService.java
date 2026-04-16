package org.example.service;

import org.example.data.Repository;
import org.example.data.User;

import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// CAPA DE LÓGICA — Service
//
// El Service contiene las reglas de negocio de la aplicación.
// Su función es decidir QUÉ hacer y bajo QUÉ condiciones, antes de
// delegar el acceso a la BD al Repository.
//
// Separar esta capa del Repository tiene una ventaja clave: si las reglas
// cambian (por ej. agregar un límite de usuarios o un log de auditoría),
// se modifica el Service sin tocar el Repository ni la presentación.
// ─────────────────────────────────────────────────────────────────────────────
public class UserService {

    // El Service conoce al Repository, pero no al revés.
    // Esta dirección de dependencia (Service → Repository) es intencional:
    // cada capa solo conoce a la capa inmediatamente inferior.
    private final Repository repository = Repository.getInstance();

    // Registrar un usuario: valida los datos antes de persistir.
    // El Repository no valida nada; esa responsabilidad es del Service.
    public void registrar(String name, boolean active) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        repository.saveUser(new User(name, active));
        System.out.println("Usuario registrado: " + name);
    }

    // Desactivar requiere primero buscar si el usuario existe.
    // La decisión de "qué hacer si no existe" es lógica de negocio,
    // no responsabilidad del Repository.
    public void desactivar(Integer id) {
        User user = repository.findUserById(id);
        if (user == null) {
            System.out.println("No se encontró usuario con id=" + id);
            return;
        }
        user.setActive(false);
        repository.updateUser(user);
        System.out.println("Usuario desactivado: " + user.getName());
    }

    public void eliminar(Integer id) {
        repository.deleteUser(id);
        System.out.println("Usuario con id=" + id + " eliminado");
    }

    // La capa de presentación (Main) recibe la lista y decide cómo mostrarla.
    // El Service solo provee los datos sin formatearlos.
    public List<User> listarTodos() {
        return repository.findAllUsers();
    }
}

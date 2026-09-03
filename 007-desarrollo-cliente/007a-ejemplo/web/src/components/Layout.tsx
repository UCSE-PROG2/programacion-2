import type { ReactNode } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

// Layout envuelve las páginas que ya tienen sesión iniciada (Home, Recetas)
// con un header persistente: marca + navegación + usuario/logout. Las
// páginas sin sesión (Login, Registro) NO pasan por acá — se arman su
// propia tarjeta centrada, sin header, porque todavía no hay nada que
// navegar ni ninguna cuenta para mostrar arriba.
export function Layout({ children }: { children: ReactNode }) {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();

  function manejarLogout() {
    logout();
    // navigate(), no <Link>: esto corre desde un manejador de evento, no en
    // respuesta a un click sobre un elemento navegable (ver Clase 4 —
    // "React Router: navegación sin recargar la página").
    navigate("/login");
  }

  return (
    <div className="layout">
      <header className="header">
        <span className="header-marca">🍳 Recetario</span>

        {/* NavLink (no Link): además de navegar, le agrega automáticamente
            una clase cuando la ruta coincide con la actual — así el link
            activo queda resaltado sin manejar ese estado a mano. "end" en
            el de "/" evita que quede marcado como activo en CUALQUIER ruta
            (por default NavLink matchea por prefijo, y "/" es prefijo de
            todo). */}
        <nav className="header-nav">
          <NavLink to="/" end className={({ isActive }) => (isActive ? "activo" : undefined)}>
            Inicio
          </NavLink>
          <NavLink
            to="/recetas"
            className={({ isActive }) => (isActive ? "activo" : undefined)}
          >
            Recetas
          </NavLink>
        </nav>

        <div className="header-usuario">
          {/* usuario siempre está definido acá: Layout solo se usa dentro
              de RutaProtegida (ver App.tsx), que ya garantiza sesión
              iniciada antes de llegar a este punto. */}
          {usuario && <span className="header-email">{usuario.email}</span>}
          <button type="button" onClick={manejarLogout}>
            Cerrar sesión
          </button>
        </div>
      </header>

      {/* El contenido de cada página (HomePage, RecetasPage, ...) se
          renderiza acá adentro, como children — Layout no sabe nada del
          contenido, solo le da un marco común. */}
      <main className="contenido">{children}</main>
    </div>
  );
}

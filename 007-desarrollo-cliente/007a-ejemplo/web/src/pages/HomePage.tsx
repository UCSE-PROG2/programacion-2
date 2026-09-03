import { useAuth } from "../auth/AuthContext";

export function HomePage() {
  const { usuario } = useAuth();

  // RutaProtegida garantiza que HomePage nunca se monta con usuario === null.
  if (!usuario) return null;

  return (
    <div className="tarjeta">
      <h1>Hola, {usuario.email}</h1>
      <div className="dato">
        <span>ID</span>
        <span>{usuario.id}</span>
      </div>
      <div className="dato">
        <span>Email</span>
        <span>{usuario.email}</span>
      </div>
      {/* "Cerrar sesión" ahora vive en el header (Layout, ver App.tsx) — un
          solo botón de logout, visible en cualquier página autenticada, en
          vez de repetirlo página por página. */}
    </div>
  );
}

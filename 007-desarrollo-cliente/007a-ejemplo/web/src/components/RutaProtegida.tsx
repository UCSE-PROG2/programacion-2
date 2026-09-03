import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

// Envuelve una página que exige sesión iniciada. "cargando" evita un falso
// negativo mientras AuthProvider todavía está resolviendo el token guardado
// contra GET /usuarios/me (ver Unidad 7, Clase 4).
export function RutaProtegida({ children }: { children: ReactNode }) {
  const { usuario, cargando } = useAuth();

  if (cargando) return <p>Cargando...</p>;
  if (!usuario) return <Navigate to="/login" replace />;
  return children;
}

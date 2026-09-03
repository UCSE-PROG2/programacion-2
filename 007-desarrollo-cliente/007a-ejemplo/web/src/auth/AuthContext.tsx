import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import * as api from "../api/usuarios";
import type { UsuarioDTO } from "../api/types";

const CLAVE_TOKEN = "recetario_token";

interface AuthContextValue {
  usuario: UsuarioDTO | null;
  // token: el JWT crudo, expuesto además de "usuario" — lo necesita
  // cualquier página que llame a un endpoint protegido por su cuenta (ver
  // pages/RecetasPage.tsx) en vez de pasar solo por las funciones ya
  // armadas de api/usuarios.ts.
  token: string | null;
  cargando: boolean;
  login: (email: string, password: string) => Promise<void>;
  registrar: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

// AuthProvider guarda el token en localStorage (sobrevive a un F5, ver
// Unidad 7 Clase 4) y expone el usuario ya resuelto contra GET /usuarios/me
// — ningún componente de "pages/" necesita saber que existe un JWT.
export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioDTO | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    const tokenGuardado = localStorage.getItem(CLAVE_TOKEN);
    if (!tokenGuardado) {
      setCargando(false);
      return;
    }
    api
      .obtenerPerfil(tokenGuardado)
      .then((u) => {
        setUsuario(u);
        setToken(tokenGuardado);
      })
      .catch(() => localStorage.removeItem(CLAVE_TOKEN))
      .finally(() => setCargando(false));
  }, []);

  async function login(email: string, password: string) {
    const { token: nuevoToken } = await api.login({ email, password });
    localStorage.setItem(CLAVE_TOKEN, nuevoToken);
    setToken(nuevoToken);
    setUsuario(await api.obtenerPerfil(nuevoToken));
  }

  async function registrar(email: string, password: string) {
    await api.registrar({ email, password });
    // El registro no devuelve token (ver dto.go de la Unidad 6) — se
    // encadena un login con las mismas credenciales para dejar a la
    // persona ya autenticada en vez de mandarla de vuelta al login.
    await login(email, password);
  }

  function logout() {
    localStorage.removeItem(CLAVE_TOKEN);
    setToken(null);
    setUsuario(null);
  }

  return (
    <AuthContext.Provider value={{ usuario, token, cargando, login, registrar, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const contexto = useContext(AuthContext);
  if (!contexto) throw new Error("useAuth debe usarse dentro de un AuthProvider");
  return contexto;
}

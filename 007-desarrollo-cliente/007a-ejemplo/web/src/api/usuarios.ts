import type { LoginDTO, LoginResponse, RegistroDTO, UsuarioDTO } from "./types";

// La URL de la API sale de una variable de entorno con el prefijo VITE_ (ver
// Unidad 7, Clase 3 — "Variables de entorno con Vite"), con un default para
// correr sin configurar nada extra.
const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

// ErrorAPI envuelve el mensaje que devuelve el backend ({"error": "..."})
// para poder mostrarlo directo en los formularios de login/registro.
export class ErrorAPI extends Error {}

async function manejarRespuesta<T>(respuesta: Response): Promise<T> {
  if (!respuesta.ok) {
    const cuerpo = await respuesta.json().catch(() => null);
    throw new ErrorAPI(cuerpo?.error ?? `Error ${respuesta.status}`);
  }
  return respuesta.json();
}

export async function registrar(dto: RegistroDTO): Promise<UsuarioDTO> {
  const respuesta = await fetch(`${API_URL}/usuarios/registro`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto),
  });
  return manejarRespuesta<UsuarioDTO>(respuesta);
}

export async function login(dto: LoginDTO): Promise<LoginResponse> {
  const respuesta = await fetch(`${API_URL}/usuarios/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto),
  });
  return manejarRespuesta<LoginResponse>(respuesta);
}

export async function obtenerPerfil(token: string): Promise<UsuarioDTO> {
  const respuesta = await fetch(`${API_URL}/usuarios/me`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return manejarRespuesta<UsuarioDTO>(respuesta);
}

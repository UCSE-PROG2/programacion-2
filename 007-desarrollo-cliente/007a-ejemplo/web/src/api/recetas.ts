import type { RecetaDTO } from "./types";

// Mismo patrón que api/usuarios.ts (Unidad 7, Clase 3): la URL de la API
// sale de una variable de entorno con el prefijo VITE_, con un default para
// correr sin configurar nada extra.
const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

// ErrorAPI envuelve el mensaje que devuelve el backend ({"error": "..."})
// para poder mostrarlo directo en el formulario de RecetasPage.
export class ErrorAPI extends Error {}

// manejarRespuesta asume que, si la respuesta es OK, el body es JSON — no
// sirve para DELETE, que responde 204 sin body (ver eliminarReceta, más
// abajo, que maneja ese caso aparte).
async function manejarRespuesta<T>(respuesta: Response): Promise<T> {
  if (!respuesta.ok) {
    const cuerpo = await respuesta.json().catch(() => null);
    throw new ErrorAPI(cuerpo?.error ?? `Error ${respuesta.status}`);
  }
  return respuesta.json();
}

// Todas las rutas de /recetas exigen sesión (ver AuthMiddleware en el
// backend, api/internal/middleware/auth_middleware.go) — por eso cada
// función de acá abajo recibe el token y lo manda en el header
// Authorization, igual que obtenerPerfil en usuarios.ts.
function headersAutenticados(token: string): HeadersInit {
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };
}

// listarRecetas: GET /recetas — devuelve todas las recetas visibles para
// cualquier usuario logueado (no solo las que creó esa cuenta).
export async function listarRecetas(token: string): Promise<RecetaDTO[]> {
  const respuesta = await fetch(`${API_URL}/recetas`, {
    headers: headersAutenticados(token),
  });
  return manejarRespuesta<RecetaDTO[]>(respuesta);
}

// crearReceta: POST /recetas. El parámetro es RecetaDTO SIN "id" (Omit<...>)
// porque una receta nueva todavía no tiene id — lo asigna Mongo recién al
// insertarla, y viene en la respuesta. El backend además sella la receta con
// el id del usuario autenticado y la fecha actual (campos de auditoría); el
// cliente nunca los manda ni los recibe de vuelta.
export async function crearReceta(
  token: string,
  dto: Omit<RecetaDTO, "id">,
): Promise<RecetaDTO> {
  const respuesta = await fetch(`${API_URL}/recetas`, {
    method: "POST",
    headers: headersAutenticados(token),
    body: JSON.stringify(dto),
  });
  return manejarRespuesta<RecetaDTO>(respuesta);
}

// actualizarReceta: PUT /recetas/:id — reemplazo completo de los campos
// editables (no un patch parcial). El backend además actualiza quién y
// cuándo la modificó por última vez, sin tocar quién la creó originalmente.
export async function actualizarReceta(
  token: string,
  id: string,
  dto: Omit<RecetaDTO, "id">,
): Promise<RecetaDTO> {
  const respuesta = await fetch(`${API_URL}/recetas/${id}`, {
    method: "PUT",
    headers: headersAutenticados(token),
    body: JSON.stringify(dto),
  });
  return manejarRespuesta<RecetaDTO>(respuesta);
}

// eliminarReceta: DELETE /recetas/:id — la API responde 204 (sin body) si
// sale bien, así que NO se puede reusar manejarRespuesta acá (llamaría a
// .json() sobre una respuesta vacía y rompería).
export async function eliminarReceta(token: string, id: string): Promise<void> {
  const respuesta = await fetch(`${API_URL}/recetas/${id}`, {
    method: "DELETE",
    headers: headersAutenticados(token),
  });
  if (!respuesta.ok) {
    const cuerpo = await respuesta.json().catch(() => null);
    throw new ErrorAPI(cuerpo?.error ?? `Error ${respuesta.status}`);
  }
}

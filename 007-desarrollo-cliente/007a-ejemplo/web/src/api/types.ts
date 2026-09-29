// Contratos de la API propia de este proyecto (007a-ejemplo/api/internal/).
// Cada interface refleja exactamente los campos JSON de su DTO en Go — ver
// Unidad 7, Clase 3 ("Tipando los contratos del backend").

export interface RegistroDTO {
  email: string;
  password: string;
}

export interface LoginDTO {
  email: string;
  password: string;
}

export interface UsuarioDTO {
  id: string;
  email: string;
}

export interface LoginResponse {
  token: string;
}

// Refleja RecetaDTO (api/internal/receta/dto.go). A propósito no tiene
// campos de auditoría (quién la creó/actualizó, fechas) — el backend nunca
// los incluye en la respuesta, así que tampoco existen acá.
export interface RecetaDTO {
  id: string;
  nombre: string;
  categoria: string;
  tiempoPreparacionMinutos: number;
  porciones: number;
  vegetariana: boolean;
}

// Refleja PaginaRecetasDTO (api/internal/receta/dto.go): respuesta de
// GET /recetas?pagina=N. "total" es la cantidad de recetas de toda la
// colección, no solo de esta página.
export interface PaginaRecetasDTO {
  items: RecetaDTO[];
  total: number;
  pagina: number;
  tamanioPagina: number;
}

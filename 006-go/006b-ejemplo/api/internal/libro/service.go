package libro

import (
	"context"
	"time"
)

// Service contiene la lógica de negocio del dominio "libro" — reglas que no
// son ni HTTP ni persistencia. El campo "repo" es la INTERFAZ Repository, no
// *MongoRepository: el service depende únicamente del contrato, nunca de la
// implementación concreta (ver Clase 1 — "satisfacción implícita").
//
// Por ahora Service solo delega en el repository, sin reglas propias todavía
// — el mismo punto en el que queda el TP al final de la Clase 2. La Clase 6
// agrega ahí validaciones reales (ej. no permitir dos libros con el mismo
// ISBN), que es donde un service empieza a ganar su lugar frente a delegar
// directo del handler al repository.
type Service struct {
	repo Repository
}

// NewService recibe la interfaz, no la implementación: cualquier tipo que
// cumpla Repository sirve acá (la MongoRepository real, o un repository de
// prueba en un test).
func NewService(repo Repository) *Service {
	return &Service{repo: repo}
}

func (s *Service) ListarTodos(ctx context.Context) ([]Libro, error) {
	return s.repo.FindAll(ctx)
}

func (s *Service) BuscarPorID(ctx context.Context, id string) (Libro, error) {
	return s.repo.FindByID(ctx, id)
}

func (s *Service) Buscar(ctx context.Context, filtro BusquedaLibros) ([]Libro, error) {
	return s.repo.Buscar(ctx, filtro)
}

func (s *Service) Crear(ctx context.Context, l Libro) (Libro, error) {
	// Completar campos automáticos (que el cliente no manda) es
	// responsabilidad del service, no del handler ni del repository (ver
	// Clase 6 — "Auditoría: campos comunes a todo documento"): si no vino
	// fecha_ingreso en el DTO, se asume que el libro ingresa HOY.
	if l.FechaIngreso.IsZero() {
		l.FechaIngreso = time.Now()
	}
	return s.repo.Create(ctx, l)
}

func (s *Service) Actualizar(ctx context.Context, id string, l Libro) (Libro, error) {
	return s.repo.Update(ctx, id, l)
}

func (s *Service) Eliminar(ctx context.Context, id string) error {
	return s.repo.Delete(ctx, id)
}

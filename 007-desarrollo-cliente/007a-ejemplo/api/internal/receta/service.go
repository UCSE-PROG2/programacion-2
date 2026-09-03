package receta

import (
	"context"
	"time"
)

// Service contiene la lógica de negocio del dominio "receta" — reglas que no
// son ni HTTP ni persistencia. El campo "repo" es la INTERFAZ Repository, no
// *MongoRepository: el service depende únicamente del contrato, nunca de la
// implementación concreta.
type Service struct {
	repo Repository
}

// NewService recibe la interfaz, no la implementación: cualquier tipo que
// cumpla Repository sirve acá (la MongoRepository real, o un repository de
// prueba en un test).
func NewService(repo Repository) *Service {
	return &Service{repo: repo}
}

func (s *Service) ListarTodas(ctx context.Context) ([]Receta, error) {
	return s.repo.FindAll(ctx)
}

func (s *Service) BuscarPorID(ctx context.Context, id string) (Receta, error) {
	return s.repo.FindByID(ctx, id)
}

// Crear da de alta una receta nueva, sellada con auditoría de creación:
// usuarioID llega desde el JWT ya validado por AuthMiddleware (nunca del
// body — el cliente no puede elegir a nombre de quién queda registrada la
// receta). Al momento de crearla, "quien la creó" y "quien la actualizó por
// última vez" son la misma persona, así que se completan los cuatro campos
// de auditoría con el mismo usuario y el mismo instante.
func (s *Service) Crear(ctx context.Context, r Receta, usuarioID string) (Receta, error) {
	ahora := time.Now()
	r.UsuarioCreadorID = usuarioID
	r.UsuarioActualizadorID = usuarioID
	r.FechaCreacion = ahora
	r.FechaActualizacion = ahora
	return s.repo.Create(ctx, r)
}

// Actualizar reemplaza los datos editables de una receta existente y
// refresca SOLO la auditoría de modificación (quién y cuándo la actualizó
// por última vez) — UsuarioCreadorID y FechaCreacion no se tocan acá: son
// del alta original y MongoRepository.Update (repository.go) los deja
// intactos en la base a propósito, sin incluirlos en el $set.
func (s *Service) Actualizar(ctx context.Context, id string, r Receta, usuarioID string) (Receta, error) {
	r.UsuarioActualizadorID = usuarioID
	r.FechaActualizacion = time.Now()
	return s.repo.Update(ctx, id, r)
}

func (s *Service) Eliminar(ctx context.Context, id string) error {
	return s.repo.Delete(ctx, id)
}

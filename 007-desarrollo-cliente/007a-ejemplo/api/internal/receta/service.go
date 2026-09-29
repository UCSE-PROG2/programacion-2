package receta

import (
	"context"
	"time"
)

// Service contiene la lógica de negocio del dominio "receta" — reglas que no
// son ni HTTP ni persistencia. El campo "repo" es la INTERFAZ Repository, no
// *MongoRepository: el service depende únicamente del contrato, nunca de la
// implementación concreta.
//
// Service es la ÚNICA pieza del dominio que ve tanto RecetaDTO como Receta:
// recibe el DTO que ya validó el Handler, lo convierte a Receta con
// dto.ToModel() antes de llamar al Repository, y convierte la Receta que
// devuelve el Repository de vuelta a RecetaDTO con r.ToDTO() antes de
// retornar. El Handler nunca ve Receta, y el Repository nunca ve RecetaDTO.
type Service struct {
	repo Repository
}

// NewService recibe la interfaz, no la implementación: cualquier tipo que
// cumpla Repository sirve acá (la MongoRepository real, o un repository de
// prueba en un test).
func NewService(repo Repository) *Service {
	return &Service{repo: repo}
}

// TamanioPagina es fijo: el cliente no puede elegir cuántas filas quiere.
const TamanioPagina = 10

// ListarPagina devuelve la página "pagina" (base 1) de recetas junto con el
// total de la colección, convertido a PaginaRecetasDTO.
func (s *Service) ListarPagina(ctx context.Context, pagina int) (PaginaRecetasDTO, error) {
	recetas, total, err := s.repo.FindPage(ctx, pagina, TamanioPagina)
	if err != nil {
		return PaginaRecetasDTO{}, err
	}

	dtos := make([]RecetaDTO, 0, len(recetas))
	for _, r := range recetas {
		dtos = append(dtos, r.ToDTO())
	}
	return PaginaRecetasDTO{
		Items:         dtos,
		Total:         total,
		Pagina:        pagina,
		TamanioPagina: TamanioPagina,
	}, nil
}

func (s *Service) BuscarPorID(ctx context.Context, id string) (RecetaDTO, error) {
	r, err := s.repo.FindByID(ctx, id)
	if err != nil {
		return RecetaDTO{}, err
	}
	return r.ToDTO(), nil
}

// Crear da de alta una receta nueva, sellada con auditoría de creación:
// usuarioID llega desde el JWT ya validado por AuthMiddleware (nunca del
// body — el cliente no puede elegir a nombre de quién queda registrada la
// receta). Al momento de crearla, "quien la creó" y "quien la actualizó por
// última vez" son la misma persona, así que se completan los cuatro campos
// de auditoría con el mismo usuario y el mismo instante.
func (s *Service) Crear(ctx context.Context, dto RecetaDTO, usuarioID string) (RecetaDTO, error) {
	r, err := dto.ToModel()
	if err != nil {
		return RecetaDTO{}, err
	}

	ahora := time.Now()
	r.UsuarioCreadorID = usuarioID
	r.UsuarioActualizadorID = usuarioID
	r.FechaCreacion = ahora
	r.FechaActualizacion = ahora

	creada, err := s.repo.Create(ctx, r)
	if err != nil {
		return RecetaDTO{}, err
	}
	return creada.ToDTO(), nil
}

// Actualizar reemplaza los datos editables de una receta existente y
// refresca SOLO la auditoría de modificación (quién y cuándo la actualizó
// por última vez) — UsuarioCreadorID y FechaCreacion no se tocan acá: son
// del alta original y MongoRepository.Update (repository.go) los deja
// intactos en la base a propósito, sin incluirlos en el $set.
func (s *Service) Actualizar(ctx context.Context, id string, dto RecetaDTO, usuarioID string) (RecetaDTO, error) {
	r, err := dto.ToModel()
	if err != nil {
		return RecetaDTO{}, err
	}

	r.UsuarioActualizadorID = usuarioID
	r.FechaActualizacion = time.Now()

	actualizada, err := s.repo.Update(ctx, id, r)
	if err != nil {
		return RecetaDTO{}, err
	}
	return actualizada.ToDTO(), nil
}

func (s *Service) Eliminar(ctx context.Context, id string) error {
	return s.repo.Delete(ctx, id)
}

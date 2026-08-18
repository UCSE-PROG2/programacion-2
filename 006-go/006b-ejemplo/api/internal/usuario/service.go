package usuario

import (
	"context"
	"errors"

	"biblioteca/api/internal/auth"
)

// Errores de negocio propios del dominio, distinguibles con errors.Is desde
// el handler — así el handler puede mapear cada uno al código HTTP correcto
// sin comparar strings ni arriesgarse a confundirlos con un error genérico
// del driver de Mongo (ver Clase 3 — "el mensaje de error es el mismo tanto
// si el email no existe como si la contraseña es incorrecta", aplicado acá
// con ErrCredencialesInvalidas cubriendo ambos casos).
var (
	ErrEmailYaRegistrado      = errors.New("ya existe un usuario registrado con ese email")
	ErrCredencialesInvalidas  = errors.New("credenciales inválidas")
	ErrPasswordActualInvalida = errors.New("la contraseña actual no es correcta")
)

// Service contiene la lógica de negocio del dominio "usuario". El campo
// "repo" es la interfaz Repository, no *MongoRepository (ver Clase 1 —
// "satisfacción implícita").
type Service struct {
	repo Repository
}

func NewService(repo Repository) *Service {
	return &Service{repo: repo}
}

// Registrar crea un usuario nuevo. El orden de las validaciones importa: se
// chequea el email duplicado (una consulta liviana) ANTES de hashear la
// contraseña (bcrypt es deliberadamente lento) — no tiene sentido pagar ese
// costo si la request ya va a fallar por otra razón.
func (s *Service) Registrar(ctx context.Context, dto RegistroDTO) (Usuario, error) {
	_, err := s.repo.FindByEmail(ctx, dto.Email)
	if err == nil {
		// FindByEmail sin error significa que SÍ encontró un usuario con
		// ese email — es el caso de conflicto.
		return Usuario{}, ErrEmailYaRegistrado
	}

	hash, err := auth.HashPassword(dto.Password)
	if err != nil {
		return Usuario{}, err
	}

	nuevo := Usuario{Email: dto.Email, PasswordHash: hash}
	return s.repo.Create(ctx, nuevo)
}

// Login verifica email + contraseña y, si son correctos, emite un JWT nuevo.
// Devuelve el mismo error (ErrCredencialesInvalidas) tanto si el email no
// existe como si la contraseña no coincide — distinguir ambos casos le
// regalaría a un atacante información sobre qué emails están registrados
// (ver Clase 3 — "Login completo").
func (s *Service) Login(ctx context.Context, dto LoginDTO) (string, error) {
	u, err := s.repo.FindByEmail(ctx, dto.Email)
	if err != nil {
		return "", ErrCredencialesInvalidas
	}

	if err := auth.CheckPassword(u.PasswordHash, dto.Password); err != nil {
		return "", ErrCredencialesInvalidas
	}

	return auth.GenerarToken(u.ID.Hex())
}

// CambiarPassword implementa el "reset pass" autenticado: el usuario ya
// logueado manda su contraseña actual y la nueva. usuarioID llega desde el
// JWT ya validado por AuthMiddleware (ver internal/middleware), NUNCA desde
// un campo del body — así un usuario autenticado solo puede cambiar SU
// PROPIA contraseña, nunca la de otra cuenta.
func (s *Service) CambiarPassword(ctx context.Context, usuarioID string, dto CambiarPasswordDTO) error {
	u, err := s.repo.FindByID(ctx, usuarioID)
	if err != nil {
		return err
	}

	if err := auth.CheckPassword(u.PasswordHash, dto.PasswordActual); err != nil {
		return ErrPasswordActualInvalida
	}

	nuevoHash, err := auth.HashPassword(dto.PasswordNueva)
	if err != nil {
		return err
	}

	return s.repo.ActualizarPasswordHash(ctx, usuarioID, nuevoHash)
}

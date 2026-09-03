package usuario

// RegistroDTO es el body esperado por POST /usuarios/registro. binding:"email"
// valida el formato en el mismo paso que ShouldBindJSON (ver Clase 5 —
// "Binding y validación con c.ShouldBindJSON"); min=8 es la única regla de
// complejidad que se puede expresar como tag — cualquier regla que necesite
// leer el estado de la base (ej. "el email ya existe") se valida a mano en el
// service, no acá.
type RegistroDTO struct {
	Email    string `json:"email" binding:"required,email"`
	Password string `json:"password" binding:"required,min=8"`
}

// LoginDTO es el body esperado por POST /usuarios/login.
type LoginDTO struct {
	Email    string `json:"email" binding:"required,email"`
	Password string `json:"password" binding:"required"`
}

// CambiarPasswordDTO es el body esperado por PUT /usuarios/password. A
// propósito NO lleva ni el ID ni el email del usuario: esta ruta va detrás de
// AuthMiddleware, así que el usuario objetivo se obtiene del JWT (ver
// handler.go), nunca de un campo que el cliente podría manipular para
// intentar cambiar la contraseña de otra cuenta.
type CambiarPasswordDTO struct {
	PasswordActual string `json:"passwordActual" binding:"required"`
	PasswordNueva  string `json:"passwordNueva" binding:"required,min=8"`
}

// UsuarioDTO es lo que efectivamente viaja en las respuestas JSON que
// devuelven un usuario. No tiene un campo para el hash de la contraseña —ni
// siquiera con `json:"-"` para "excluirlo": directamente no existe en este
// struct, así que es imposible filtrarlo por accidente en una respuesta (ver
// Clase 3 — "Login completo", donde el hash se excluye con json:"-" sobre el
// mismo struct del modelo; acá se logra lo mismo separando model de DTO,
// igual que Producto/ProductoDTO desde la Clase 2).
type UsuarioDTO struct {
	ID    string `json:"id"`
	Email string `json:"email"`
}

// ToDTO convierte un Usuario (modelo de Mongo, con el hash adentro) en un
// UsuarioDTO (JSON de salida, sin el hash). Value receiver: solo lee el
// struct (ver Clase 1 — "Métodos con receiver: value vs. pointer").
func (u Usuario) ToDTO() UsuarioDTO {
	return UsuarioDTO{
		ID:    u.ID.Hex(),
		Email: u.Email,
	}
}

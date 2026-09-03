// Package usuario agrupa el modelo, DTOs, repository, service y handler del
// dominio "Usuario" — registro, login y cambio de contraseña (mismo flujo de
// JWT + bcrypt que la Unidad 6, Clase 3). Misma organización por dominio que
// "receta" (dto.go, handler.go, model.go, repository.go, service.go).
package usuario

import "go.mongodb.org/mongo-driver/v2/bson"

// Usuario es el struct que representa EXACTAMENTE un documento de la
// colección "usuarios" en MongoDB. El campo PasswordHash guarda el resultado
// de bcrypt, NUNCA la contraseña en texto plano (ver Clase 3 — "Por qué nunca
// se guarda una contraseña en texto plano").
//
// A propósito ningún campo usa tags `json:"..."`: este struct nunca se
// serializa directamente a JSON — de eso se encargan los DTOs (dto.go), y
// UsuarioDTO en particular ni siquiera tiene un campo para el hash.
type Usuario struct {
	ID           bson.ObjectID `bson:"_id,omitempty"`
	Email        string        `bson:"email"`
	PasswordHash string        `bson:"password_hash"`
}

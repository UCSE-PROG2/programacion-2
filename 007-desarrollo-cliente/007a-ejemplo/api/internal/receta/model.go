// Package receta agrupa todo lo que necesita el dominio "Receta": el modelo
// que se persiste en Mongo, el DTO que viaja por HTTP, el repository, el
// service y el handler. Misma organización por dominio que "usuario"
// (dto.go, handler.go, model.go, repository.go, service.go).
package receta

import (
	"time"

	"go.mongodb.org/mongo-driver/v2/bson"
)

// Receta es el struct que representa EXACTAMENTE un documento de la
// colección "recetas" en MongoDB. Los tags `bson:"..."` controlan cómo se
// serializa cada campo al hablar con el driver.
//
// A propósito, ningún campo usa tags `json:"..."`: este struct nunca se
// serializa directamente a JSON — de eso se encarga RecetaDTO (dto.go).
type Receta struct {
	ID                       bson.ObjectID `bson:"_id,omitempty"`
	Nombre                   string        `bson:"nombre"`
	Categoria                string        `bson:"categoria"`
	TiempoPreparacionMinutos int           `bson:"tiempo_preparacion_minutos"`
	Porciones                int           `bson:"porciones"`
	Vegetariana              bool          `bson:"vegetariana"`

	// Campos de auditoría: quién creó/modificó la receta y cuándo. Los
	// completa exclusivamente el Service (ver service.go) a partir del
	// usuario autenticado que ya dejó AuthMiddleware en el contexto — el
	// cliente HTTP nunca los manda ni los recibe de vuelta, por eso
	// RecetaDTO (dto.go) no tiene un campo `json:"..."` para ninguno de
	// estos cuatro.
	UsuarioCreadorID      string    `bson:"usuario_creador_id"`
	UsuarioActualizadorID string    `bson:"usuario_actualizador_id"`
	FechaCreacion         time.Time `bson:"fecha_creacion"`
	FechaActualizacion    time.Time `bson:"fecha_actualizacion"`
}

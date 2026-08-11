// Package libro agrupa todo lo que necesita el dominio "Libro": el modelo que se
// persiste en Mongo, el DTO que viaja por HTTP, el repository, el service y el
// handler. Es la misma organización "por dominio" que usa el TP de Gestock con
// el paquete "producto" (ver Clase 2 del readme de la unidad).
package libro

import "go.mongodb.org/mongo-driver/v2/bson"

// Libro es el struct que representa EXACTAMENTE un documento de la colección
// "libros" en MongoDB. Los tags `bson:"..."` controlan cómo se serializa cada
// campo al hablar con el driver (ver Clase 5 para el detalle de estos tags).
//
// A propósito, ningún campo usa tags `json:"..."`: este struct nunca se
// serializa directamente a JSON — de eso se encarga LibroDTO (dto.go).
type Libro struct {
	ID          bson.ObjectID `bson:"_id,omitempty"`
	Titulo      string        `bson:"titulo"`
	Autor       string        `bson:"autor"`
	ISBN        string        `bson:"isbn"`
	AnioEdicion int           `bson:"anio_edicion"`
	Disponible  bool          `bson:"disponible"`
}

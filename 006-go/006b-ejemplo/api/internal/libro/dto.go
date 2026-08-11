package libro

import "go.mongodb.org/mongo-driver/v2/bson"

// LibroDTO es el struct que efectivamente viaja en el JSON de entrada y salida
// de la API. Es un tipo DISTINTO de Libro a propósito (ver Clase 2 — "El DTO:
// qué viaja por HTTP"):
//   - El cliente HTTP nunca debería ver un bson.ObjectID: es un tipo binario
//     propio del driver de Mongo, no algo que un frontend sepa interpretar.
//   - Los tags `binding:"..."` (validación de Gin) solo tienen sentido acá,
//     nunca en el modelo de Mongo.
type LibroDTO struct {
	ID          string `json:"id,omitempty"`
	Titulo      string `json:"titulo" binding:"required"`
	Autor       string `json:"autor" binding:"required"`
	ISBN        string `json:"isbn" binding:"required"`
	AnioEdicion int    `json:"anio_edicion" binding:"required,gt=0"`
	Disponible  bool   `json:"disponible"`
}

// ToDTO convierte un Libro (modelo de Mongo) en un LibroDTO (JSON de salida).
// Es un value receiver: solo lee el struct, no lo modifica (ver Clase 1 —
// "Métodos con receiver: value vs. pointer").
func (l Libro) ToDTO() LibroDTO {
	return LibroDTO{
		ID:          l.ID.Hex(),
		Titulo:      l.Titulo,
		Autor:       l.Autor,
		ISBN:        l.ISBN,
		AnioEdicion: l.AnioEdicion,
		Disponible:  l.Disponible,
	}
}

// ToModel convierte un LibroDTO (JSON de entrada) en un Libro (lo que persiste
// el repository). Retorna (Libro, error) porque la conversión del ID puede
// fallar (ver Clase 1 — "Manejo de errores como valores").
//
// dto.ID puede venir vacío: en un alta (POST) todavía no existe id, así que
// solo se intenta convertir si hay algo que convertir.
func (dto LibroDTO) ToModel() (Libro, error) {
	l := Libro{
		Titulo:      dto.Titulo,
		Autor:       dto.Autor,
		ISBN:        dto.ISBN,
		AnioEdicion: dto.AnioEdicion,
		Disponible:  dto.Disponible,
	}

	if dto.ID == "" {
		return l, nil
	}

	oid, err := bson.ObjectIDFromHex(dto.ID)
	if err != nil {
		return Libro{}, err
	}
	l.ID = oid
	return l, nil
}

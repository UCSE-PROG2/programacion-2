package receta

import "go.mongodb.org/mongo-driver/v2/bson"

// RecetaDTO es el struct que efectivamente viaja en el JSON de entrada y
// salida de la API. Es un tipo DISTINTO de Receta a propósito:
//   - El cliente HTTP nunca debería ver un bson.ObjectID: es un tipo binario
//     propio del driver de Mongo, no algo que un frontend sepa interpretar.
//   - Los tags `binding:"..."` (validación de Gin) solo tienen sentido acá,
//     nunca en el modelo de Mongo.
//   - No tiene NINGÚN campo de auditoría (usuario creador/actualizador,
//     fechas): esos los completa el servidor, nunca el cliente — al no
//     existir en este struct, es imposible que viajen por error en una
//     respuesta ni que alguien intente pisarlos desde el body de un
//     request (mismo criterio que UsuarioDTO excluyendo el hash de la
//     contraseña, ver internal/usuario/dto.go).
type RecetaDTO struct {
	ID                       string `json:"id,omitempty"`
	Nombre                   string `json:"nombre" binding:"required"`
	Categoria                string `json:"categoria" binding:"required"`
	TiempoPreparacionMinutos int    `json:"tiempoPreparacionMinutos" binding:"required,gt=0"`
	Porciones                int    `json:"porciones" binding:"required,gt=0"`
	Vegetariana              bool   `json:"vegetariana"`
}

// ToDTO convierte una Receta (modelo de Mongo, con los campos de auditoría
// adentro) en un RecetaDTO (JSON de salida, sin ellos). Value receiver: solo
// lee el struct, no lo modifica.
func (r Receta) ToDTO() RecetaDTO {
	return RecetaDTO{
		ID:                       r.ID.Hex(),
		Nombre:                   r.Nombre,
		Categoria:                r.Categoria,
		TiempoPreparacionMinutos: r.TiempoPreparacionMinutos,
		Porciones:                r.Porciones,
		Vegetariana:              r.Vegetariana,
	}
}

// ToModel convierte un RecetaDTO (JSON de entrada) en una Receta (lo que
// persiste el repository). Retorna (Receta, error) porque la conversión del
// ID puede fallar. Los campos de auditoría quedan en su valor cero acá — los
// completa el Service (Crear/Actualizar), que es quien conoce al usuario
// autenticado; ToModel no tiene forma de saber quién hizo el request.
//
// dto.ID puede venir vacío: en un alta (POST) todavía no existe id, así que
// solo se intenta convertir si hay algo que convertir.
func (dto RecetaDTO) ToModel() (Receta, error) {
	r := Receta{
		Nombre:                   dto.Nombre,
		Categoria:                dto.Categoria,
		TiempoPreparacionMinutos: dto.TiempoPreparacionMinutos,
		Porciones:                dto.Porciones,
		Vegetariana:              dto.Vegetariana,
	}

	if dto.ID == "" {
		return r, nil
	}

	oid, err := bson.ObjectIDFromHex(dto.ID)
	if err != nil {
		return Receta{}, err
	}
	r.ID = oid
	return r, nil
}

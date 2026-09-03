package receta

import (
	"context"
	"errors"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
)

// Repository declara el contrato de acceso a datos del dominio "receta",
// ANTES de escribir ninguna implementación (satisfacción implícita de
// interfaces en Go). Opera siempre sobre Receta (el modelo de Mongo), nunca
// sobre el DTO — el repository no tiene por qué saber que existe HTTP.
type Repository interface {
	FindAll(ctx context.Context) ([]Receta, error)
	FindByID(ctx context.Context, id string) (Receta, error)
	Create(ctx context.Context, r Receta) (Receta, error)
	Update(ctx context.Context, id string, r Receta) (Receta, error)
	Delete(ctx context.Context, id string) error
}

// MongoRepository es la única implementación de Repository: guarda recetas
// en una colección de MongoDB. El campo "coll" es privado porque nadie fuera
// de este paquete necesita tocarlo directamente.
type MongoRepository struct {
	coll *mongo.Collection
}

// NewMongoRepository es el constructor de facto de MongoRepository.
func NewMongoRepository(coll *mongo.Collection) *MongoRepository {
	return &MongoRepository{coll: coll}
}

// var _ Repository = (*MongoRepository)(nil) fuerza en tiempo de compilación
// que MongoRepository cumple la interfaz Repository.
var _ Repository = (*MongoRepository)(nil)

func (r *MongoRepository) FindAll(ctx context.Context) ([]Receta, error) {
	cursor, err := r.coll.Find(ctx, bson.M{})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	recetas := make([]Receta, 0)
	if err := cursor.All(ctx, &recetas); err != nil {
		return nil, err
	}
	return recetas, nil
}

func (r *MongoRepository) FindByID(ctx context.Context, id string) (Receta, error) {
	oid, err := bson.ObjectIDFromHex(id)
	if err != nil {
		return Receta{}, err
	}

	var receta Receta
	if err := r.coll.FindOne(ctx, bson.M{"_id": oid}).Decode(&receta); err != nil {
		if errors.Is(err, mongo.ErrNoDocuments) {
			return Receta{}, errors.New("receta no encontrada")
		}
		return Receta{}, err
	}
	return receta, nil
}

func (r *MongoRepository) Create(ctx context.Context, receta Receta) (Receta, error) {
	result, err := r.coll.InsertOne(ctx, receta)
	if err != nil {
		return Receta{}, err
	}

	receta.ID = result.InsertedID.(bson.ObjectID)
	return receta, nil
}

func (r *MongoRepository) Update(ctx context.Context, id string, receta Receta) (Receta, error) {
	oid, err := bson.ObjectIDFromHex(id)
	if err != nil {
		return Receta{}, err
	}

	// A propósito el $set NO incluye usuario_creador_id ni fecha_creacion:
	// esos son del alta original y quedan intactos en el documento existente.
	// Solo se pisan los campos editables y la auditoría de modificación.
	update := bson.M{"$set": bson.M{
		"nombre":                     receta.Nombre,
		"categoria":                  receta.Categoria,
		"tiempo_preparacion_minutos": receta.TiempoPreparacionMinutos,
		"porciones":                  receta.Porciones,
		"vegetariana":                receta.Vegetariana,
		"usuario_actualizador_id":    receta.UsuarioActualizadorID,
		"fecha_actualizacion":        receta.FechaActualizacion,
	}}

	result, err := r.coll.UpdateOne(ctx, bson.M{"_id": oid}, update)
	if err != nil {
		return Receta{}, err
	}
	if result.MatchedCount == 0 {
		return Receta{}, errors.New("receta no encontrada")
	}

	receta.ID = oid
	return receta, nil
}

func (r *MongoRepository) Delete(ctx context.Context, id string) error {
	oid, err := bson.ObjectIDFromHex(id)
	if err != nil {
		return err
	}

	result, err := r.coll.DeleteOne(ctx, bson.M{"_id": oid})
	if err != nil {
		return err
	}
	if result.DeletedCount == 0 {
		return errors.New("receta no encontrada")
	}
	return nil
}

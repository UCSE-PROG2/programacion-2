package usuario

import (
	"context"
	"errors"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
)

// Repository declara el contrato de acceso a datos del dominio "usuario",
// antes de escribir ninguna implementación (ver Clase 1 — "Interfaces:
// satisfacción implícita"). Opera siempre sobre Usuario (el modelo de Mongo,
// con el hash adentro), nunca sobre un DTO.
type Repository interface {
	FindByEmail(ctx context.Context, email string) (Usuario, error)
	FindByID(ctx context.Context, id string) (Usuario, error)
	Create(ctx context.Context, u Usuario) (Usuario, error)
	ActualizarPasswordHash(ctx context.Context, id string, nuevoHash string) error
}

// MongoRepository es la única implementación de Repository: guarda usuarios
// en una colección de MongoDB.
type MongoRepository struct {
	coll *mongo.Collection
}

func NewMongoRepository(coll *mongo.Collection) *MongoRepository {
	return &MongoRepository{coll: coll}
}

// var _ Repository = (*MongoRepository)(nil) fuerza en tiempo de compilación
// que MongoRepository cumple la interfaz Repository (ver Clase 1).
var _ Repository = (*MongoRepository)(nil)

// FindByEmail es la consulta que usa el login: el email es el identificador
// que ingresa el usuario, no el _id interno de Mongo.
func (r *MongoRepository) FindByEmail(ctx context.Context, email string) (Usuario, error) {
	var u Usuario
	if err := r.coll.FindOne(ctx, bson.M{"email": email}).Decode(&u); err != nil {
		if errors.Is(err, mongo.ErrNoDocuments) {
			return Usuario{}, errors.New("usuario no encontrado")
		}
		return Usuario{}, err
	}
	return u, nil
}

// FindByID es la consulta que usa el cambio de contraseña: el ID viaja en el
// claim "sub" del JWT ya validado por AuthMiddleware, no en el body.
func (r *MongoRepository) FindByID(ctx context.Context, id string) (Usuario, error) {
	oid, err := bson.ObjectIDFromHex(id)
	if err != nil {
		return Usuario{}, err
	}

	var u Usuario
	if err := r.coll.FindOne(ctx, bson.M{"_id": oid}).Decode(&u); err != nil {
		if errors.Is(err, mongo.ErrNoDocuments) {
			return Usuario{}, errors.New("usuario no encontrado")
		}
		return Usuario{}, err
	}
	return u, nil
}

// Create inserta un usuario nuevo. Quien llama (Service.Registrar) ya dejó
// PasswordHash con el resultado de bcrypt — el repository nunca hashea nada,
// solo persiste lo que recibe.
func (r *MongoRepository) Create(ctx context.Context, u Usuario) (Usuario, error) {
	result, err := r.coll.InsertOne(ctx, u)
	if err != nil {
		return Usuario{}, err
	}

	u.ID = result.InsertedID.(bson.ObjectID)
	return u, nil
}

// ActualizarPasswordHash sobreescribe SOLO el campo password_hash — nunca
// se toca el email ni el _id desde este método, para no correr el riesgo de
// pisar otro campo del documento por accidente.
func (r *MongoRepository) ActualizarPasswordHash(ctx context.Context, id string, nuevoHash string) error {
	oid, err := bson.ObjectIDFromHex(id)
	if err != nil {
		return err
	}

	update := bson.M{"$set": bson.M{"password_hash": nuevoHash}}
	result, err := r.coll.UpdateOne(ctx, bson.M{"_id": oid}, update)
	if err != nil {
		return err
	}
	if result.MatchedCount == 0 {
		return errors.New("usuario no encontrado")
	}
	return nil
}

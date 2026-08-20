package libro

import (
	"context"
	"errors"
	"time"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
)

// Repository declara el contrato de acceso a datos del dominio "libro", ANTES
// de escribir ninguna implementación (ver Clase 1 — "Interfaces: satisfacción
// implícita"). Opera siempre sobre Libro (el modelo de Mongo), nunca sobre el
// DTO — el repository no tiene por qué saber que existe HTTP.
//
// Se llama "Repository" y no "LibroRepository": ya está dentro del paquete
// "libro", así que agregar "Libro" al nombre sería redundante (quien la usa
// desde afuera la ve como libro.Repository, que ya deja claro el dominio).
type Repository interface {
	FindAll(ctx context.Context) ([]Libro, error)
	FindByID(ctx context.Context, id string) (Libro, error)
	Buscar(ctx context.Context, filtro BusquedaLibros) ([]Libro, error)
	Create(ctx context.Context, l Libro) (Libro, error)
	Update(ctx context.Context, id string, l Libro) (Libro, error)
	Delete(ctx context.Context, id string) error
}

// BusquedaLibros agrupa todos los filtros opcionales de una búsqueda de
// libros. Cada campo en su valor cero (string vacío, puntero nil, int 0,
// time.Time{}) significa "no filtrar por esto" — es responsabilidad de
// MongoRepository.Buscar armar el bson.M agregando SOLO las condiciones de
// los campos que efectivamente vinieron cargados (ver Clase 4 — "Filtros
// opcionales: armar el bson.M a partir de query params"). Vive acá, junto a
// la interfaz, porque repository y service lo comparten sin que ninguno de
// los dos sepa nada de HTTP — el handler es el único que traduce query
// params a este struct.
type BusquedaLibros struct {
	Titulo     string    // aproximación de texto sobre el título ($regex, case-insensitive)
	Autor      string    // igualdad exacta
	Disponible *bool     // puntero: nil = no filtrar; valor = filtrar por ese booleano exacto
	AnioDesde  int       // $gte sobre anio_edicion; 0 = sin límite inferior
	AnioHasta  int       // $lte sobre anio_edicion; 0 = sin límite superior
	FechaDesde time.Time // $gte sobre fecha_ingreso; time.Time{} = sin límite inferior
	FechaHasta time.Time // $lte sobre fecha_ingreso; time.Time{} = sin límite superior
}

// MongoRepository es la única implementación de Repository: guarda libros en
// una colección de MongoDB. El campo "coll" es privado (minúscula, ver Clase 1
// — "primera letra del identificador") porque nadie fuera de este paquete
// necesita tocarlo directamente.
type MongoRepository struct {
	coll *mongo.Collection
}

// NewMongoRepository es el constructor de facto de MongoRepository (Go no
// tiene "new Clase(...)", ver Clase 1 — "Structs: declaración e
// instanciación").
func NewMongoRepository(coll *mongo.Collection) *MongoRepository {
	return &MongoRepository{coll: coll}
}

// var _ Repository = (*MongoRepository)(nil) fuerza en tiempo de compilación
// que MongoRepository cumple la interfaz Repository. Si algún método faltara
// o tuviera una firma distinta, esta línea deja de compilar acá mismo, en vez
// de fallar recién al cablear main.go (ver Clase 1).
var _ Repository = (*MongoRepository)(nil)

// Todos los métodos de MongoRepository usan pointer receiver (*MongoRepository)
// por consistencia: aunque ninguno modifica el struct en sí (solo usan el
// *mongo.Collection que ya tienen), la convención de Go es que si un tipo va a
// necesitar pointer receiver en algún método, todos lo usen (ver Clase 1 —
// "Regla práctica").

func (r *MongoRepository) FindAll(ctx context.Context) ([]Libro, error) {
	cursor, err := r.coll.Find(ctx, bson.M{})
	//agregar un filtro para obtener unicamente los docs del usuarioID
	//where (x.id_usuario_creado = user.UsuarioID)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var libros []Libro
	if err := cursor.All(ctx, &libros); err != nil {
		return nil, err
	}
	return libros, nil
}

func (r *MongoRepository) FindByID(ctx context.Context, id string) (Libro, error) {
	oid, err := bson.ObjectIDFromHex(id)
	if err != nil {
		return Libro{}, err
	}

	var l Libro
	if err := r.coll.FindOne(ctx, bson.M{"_id": oid}).Decode(&l); err != nil {
		if errors.Is(err, mongo.ErrNoDocuments) {
			return Libro{}, errors.New("libro no encontrado")
		}
		return Libro{}, err
	}
	return l, nil
}

// Buscar arma un bson.M agregando SOLO las condiciones de los campos de
// filtro que vinieron cargados en BusquedaLibros — el patrón de "filtros
// opcionales" de la Clase 4. A diferencia de Buscar en versiones anteriores
// de este handler (que traían TODO con FindAll y filtraban acá, en memoria,
// con "if"), el filtrado completo lo hace Mongo: nunca se trae a Go un
// documento que no matchea.
func (r *MongoRepository) Buscar(ctx context.Context, filtro BusquedaLibros) ([]Libro, error) {
	query := bson.M{}

	if filtro.Titulo != "" {
		// $regex + $options: "i" = aproximación de texto case-insensitive
		// (busca "contiene", no exige coincidencia exacta como {titulo: x}).
		query["titulo"] = bson.M{"$regex": filtro.Titulo, "$options": "i"}
	}
	if filtro.Autor != "" {
		query["autor"] = filtro.Autor
	}
	if filtro.Disponible != nil {
		query["disponible"] = *filtro.Disponible
	}

	if filtro.AnioDesde != 0 || filtro.AnioHasta != 0 {
		anio := bson.M{}
		if filtro.AnioDesde != 0 {
			anio["$gte"] = filtro.AnioDesde
		}
		if filtro.AnioHasta != 0 {
			anio["$lte"] = filtro.AnioHasta
		}
		query["anio_edicion"] = anio
	}

	if !filtro.FechaDesde.IsZero() || !filtro.FechaHasta.IsZero() {
		fecha := bson.M{}
		if !filtro.FechaDesde.IsZero() {
			fecha["$gte"] = filtro.FechaDesde
		}
		if !filtro.FechaHasta.IsZero() {
			fecha["$lte"] = filtro.FechaHasta
		}
		query["fecha_ingreso"] = fecha
	}

	cursor, err := r.coll.Find(ctx, query)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	libros := make([]Libro, 0)
	if err := cursor.All(ctx, &libros); err != nil {
		return nil, err
	}
	return libros, nil
}

func (r *MongoRepository) Create(ctx context.Context, l Libro) (Libro, error) {
	result, err := r.coll.InsertOne(ctx, l)
	if err != nil {
		return Libro{}, err
	}

	l.ID = result.InsertedID.(bson.ObjectID)
	return l, nil
}

func (r *MongoRepository) Update(ctx context.Context, id string, l Libro) (Libro, error) {
	oid, err := bson.ObjectIDFromHex(id)
	if err != nil {
		return Libro{}, err
	}

	update := bson.M{"$set": bson.M{
		"titulo":       l.Titulo,
		"autor":        l.Autor,
		"isbn":         l.ISBN,
		"anio_edicion": l.AnioEdicion,
		"disponible":   l.Disponible,
	}}

	result, err := r.coll.UpdateOne(ctx, bson.M{"_id": oid}, update)
	if err != nil {
		return Libro{}, err
	}
	if result.MatchedCount == 0 {
		return Libro{}, errors.New("libro no encontrado")
	}

	l.ID = oid
	return l, nil
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
		return errors.New("libro no encontrado")
	}
	return nil
}

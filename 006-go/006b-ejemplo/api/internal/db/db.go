// Package db centraliza la conexión a MongoDB. Es un paquete propio, separado
// de cualquier dominio en particular (ver Clase 2 — "La implementación contra
// MongoDB"): si la Biblioteca tuviera más entidades además de "libro" (por
// ejemplo "socio" o "prestamo"), todas usarían este mismo Conectar.
package db

import (
	"context"
	"time"

	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
)

// Conectar abre una conexión contra MongoDB y hace un ping con timeout para
// confirmar que el servidor efectivamente responde antes de devolver el
// cliente — si Mongo no está disponible, main.go se entera altiro con
// log.Fatal en vez de fallar recién en el primer request.
func Conectar(uri string) (*mongo.Client, error) {
	client, err := mongo.Connect(options.Client().ApplyURI(uri))
	if err != nil {
		return nil, err
	}

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := client.Ping(ctx, nil); err != nil {
		return nil, err
	}
	return client, nil
}

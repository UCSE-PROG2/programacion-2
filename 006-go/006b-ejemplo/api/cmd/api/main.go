// Comando "api": punto de entrada de la API de la Biblioteca.
//
// Arma el grafo de dependencias a mano (sin ningún framework de inyección de
// dependencias, ver Clase 2 — "main.go — el cableado final"):
//
//	MongoRepository → Service → Handler → rutas de Gin
//
// # Cómo correr esto en LOCAL, sin Docker
//
// Se necesita un Mongo escuchando en localhost:27017. La forma más rápida es
// un único contenedor (visto en la Unidad 4 y retomado en la Clase 2 del
// readme de esta unidad):
//
//	docker run -d --name mongo-biblioteca -p 27017:27017 -v datos-biblioteca:/data/db mongo:7
//
// Con Mongo arriba, desde la carpeta api/:
//
//	go mod tidy   # descarga gin, el driver de Mongo, golang-jwt/jwt y x/crypto/bcrypt, escribe go.sum
//	go run ./cmd/api
//
// La API queda escuchando en http://localhost:8080.
//
// # Cómo armar la imagen de Docker de la API (build manual, sin compose)
//
// El Dockerfile (api/Dockerfile) es multi-stage: una etapa "builder" con la
// imagen de golang compila el binario, y la imagen final solo copia ese
// binario ya compilado — el resultado es una imagen final liviana, que no
// necesita el toolchain de Go instalado para correr (ver Clase 1 — "un
// binario de go build es autocontenido").
//
// Parado en la carpeta api/ (donde está el Dockerfile):
//
//	docker build -t biblioteca-api .
//
// Para correr ESA imagen sola (sin compose), conectándola al Mongo de arriba
// -- ojo que dentro de un contenedor "localhost" es el contenedor mismo, no
// el host, por eso se apunta a host.docker.internal en Mac/Windows:
//
//	docker run -d --name biblioteca-api -p 8080:8080 \
//	  -e MONGO_URI="mongodb://host.docker.internal:27017" \
//	  biblioteca-api
//
// # Cómo levantar TODO el stack con docker-compose (forma recomendada)
//
// docker-compose.yml (en la raíz de 006b-ejemplo/) ya declara los dos
// servicios (mongo + api) con la red y las variables de entorno resueltas
// entre sí, igual que en el TP (ver Clase 2 — "Sumando la API al
// docker-compose.yml"). Parado en 006b-ejemplo/:
//
//	docker compose up --build
//
// Esto: (1) construye la imagen de la API con el mismo Dockerfile de arriba,
// (2) levanta Mongo con un volumen para persistencia, y (3) deja la API
// escuchando en http://localhost:8080, ya conectada a Mongo (dentro de la red
// de compose, el host se llama "mongo", no "localhost" — por eso
// MONGO_URI usa mongodb://mongo:27017 en ese caso, resuelto vía variable de
// entorno más abajo).
//
// Para parar todo y borrar los contenedores (el volumen de datos queda,
// salvo que se agregue "-v"):
//
//	docker compose down
//
// # Cómo probar la API
//
// Importar la colección de Postman en postman/Biblioteca-API.postman_collection.json
// (ver el README.md de esta carpeta para el detalle de cada request), o con
// curl:
//
//	curl -X POST http://localhost:8080/libros \
//	  -H "Content-Type: application/json" \
//	  -d '{"titulo":"Cien años de soledad","autor":"Gabriel García Márquez","isbn":"978-0307474728","anio_edicion":1967,"disponible":true}'
//
//	curl http://localhost:8080/libros
package main

import (
	"log"
	"os"

	"github.com/gin-gonic/gin"

	"biblioteca/api/internal/db"
	"biblioteca/api/internal/libro"
	"biblioteca/api/internal/middleware"
	"biblioteca/api/internal/usuario"
)

func main() {
	// mongoURI se lee de una variable de entorno con un default para correr
	// en local sin Docker. docker-compose.yml sobreescribe MONGO_URI a
	// "mongodb://mongo:27017" -- dentro de la red de compose cada servicio
	// resuelve el nombre de los demás como si fuera un hostname de red (ver
	// Clase 2).
	mongoURI := os.Getenv("MONGO_URI")
	if mongoURI == "" {
		mongoURI = "mongodb://localhost:27017"
	}

	client, err := db.Conectar(mongoURI)
	if err != nil {
		log.Fatal(err)
	}
	database := client.Database("biblioteca")
	libroColl := database.Collection("libros")
	// "usuarios" es una colección propia, separada de "libros" — cada
	// dominio tiene su propia colección, igual que tiene su propio paquete
	// Go (ver Clase 2).
	usuarioColl := database.Collection("usuarios")

	// Cableado manual de dependencias: cada capa recibe la anterior por
	// parámetro (inyección de dependencias explícita, sin ningún framework
	// — ver Clase 6 para el porqué de este orden).
	libroRepo := libro.NewMongoRepository(libroColl)
	libroService := libro.NewService(libroRepo)
	libroHandler := libro.NewHandler(libroService)

	usuarioRepo := usuario.NewMongoRepository(usuarioColl)
	usuarioService := usuario.NewService(usuarioRepo)
	usuarioHandler := usuario.NewHandler(usuarioService)

	// authMiddleware se construye una sola vez acá y se pasa por parámetro a
	// quien lo necesite (ver Clase 3 — "Middleware de autenticación") — así
	// "usuario" no depende de una instancia global de "middleware".
	authMiddleware := middleware.AuthMiddleware()

	router := gin.Default()
	libro.RegisterRoutes(router, libroHandler)
	usuario.RegisterRoutes(router, usuarioHandler, authMiddleware)

	router.Run(":8080")
}

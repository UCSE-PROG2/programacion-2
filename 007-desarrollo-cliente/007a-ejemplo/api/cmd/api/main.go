// Comando "api": punto de entrada de la API del Recetario, el backend del
// ejemplo de la Unidad 7. Copia adaptada de la API de la Biblioteca de la
// Unidad 6 (006-go/006b-ejemplo): mismo stack (Gin + MongoDB + JWT + bcrypt)
// y misma arquitectura en capas, pero con su propia base de datos ("recetario",
// separada de "biblioteca") y el dominio "receta" en vez de "libro". El
// dominio "usuario" (registro, login, /me) es el mismo flujo que en la
// Unidad 6, corriendo acá contra su propia colección — una cuenta creada acá
// no es la misma cuenta que una creada contra la Biblioteca.
//
// Arma el grafo de dependencias a mano (sin ningún framework de inyección de
// dependencias):
//
//	MongoRepository → Service → Handler → rutas de Gin
//
// # Cómo correr esto en LOCAL, sin Docker
//
// Se necesita un Mongo escuchando en localhost:27017:
//
//	docker run -d --name mongo-recetario -p 27017:27017 -v datos-recetario:/data/db mongo:7
//
// Con Mongo arriba, desde la carpeta api/:
//
//	go mod tidy   # descarga gin, el driver de Mongo, golang-jwt/jwt y x/crypto/bcrypt, escribe go.sum
//	go run ./cmd/api
//
// La API queda escuchando en http://localhost:8080.
//
// # Cómo levantar TODO el stack con docker-compose (forma recomendada)
//
// docker-compose.yml (en la raíz de 007a-ejemplo/) declara los dos servicios
// (mongo + api). Parado en 007a-ejemplo/:
//
//	docker compose up --build
//
// Si ya tenés el docker-compose de 006-go/006b-ejemplo corriendo, bajalo
// primero (docker compose down desde esa carpeta) — ambos usan los mismos
// puertos (8080 y 27017) y no pueden correr al mismo tiempo.
//
//	docker compose down    # para y borra los contenedores (el volumen queda)
//
// # Cómo probar la API
//
//	curl -X POST http://localhost:8080/usuarios/registro \
//	  -H "Content-Type: application/json" \
//	  -d '{"email":"ana@test.com","password":"12345678"}'
//
//	curl http://localhost:8080/recetas \
//	  -H "Authorization: Bearer <token>"
package main

import (
	"log"
	"os"

	"github.com/gin-gonic/gin"

	"recetario/api/internal/db"
	"recetario/api/internal/middleware"
	"recetario/api/internal/receta"
	"recetario/api/internal/usuario"
)

func main() {
	// mongoURI se lee de una variable de entorno con un default para correr
	// en local sin Docker. docker-compose.yml sobreescribe MONGO_URI a
	// "mongodb://mongo:27017" — dentro de la red de compose cada servicio
	// resuelve el nombre de los demás como si fuera un hostname de red.
	mongoURI := os.Getenv("MONGO_URI")
	if mongoURI == "" {
		mongoURI = "mongodb://localhost:27017"
	}

	client, err := db.Conectar(mongoURI)
	if err != nil {
		log.Fatal(err)
	}
	// "recetario" es una base de datos propia, separada de "biblioteca"
	// (Unidad 6): las cuentas de usuario de este ejemplo no se comparten con
	// las de la Biblioteca, aunque el código del dominio "usuario" sea el
	// mismo.
	database := client.Database("recetario")
	recetaColl := database.Collection("recetas")
	usuarioColl := database.Collection("usuarios")

	// Cableado manual de dependencias: cada capa recibe la anterior por
	// parámetro (inyección de dependencias explícita, sin ningún framework).
	recetaRepo := receta.NewMongoRepository(recetaColl)
	recetaService := receta.NewService(recetaRepo)
	recetaHandler := receta.NewHandler(recetaService)

	usuarioRepo := usuario.NewMongoRepository(usuarioColl)
	usuarioService := usuario.NewService(usuarioRepo)
	usuarioHandler := usuario.NewHandler(usuarioService)

	// authMiddleware se construye una sola vez acá y se pasa por parámetro a
	// quien lo necesite — así "usuario" y "receta" no dependen de una
	// instancia global de "middleware".
	authMiddleware := middleware.AuthMiddleware()

	router := gin.Default()
	// CORS va antes que cualquier ruta: un cliente en otro origen (la app de
	// React servida por Vite en localhost:5173) necesita este header en
	// TODAS las respuestas, incluidas las que fallan con 401/404.
	router.Use(middleware.CORSMiddleware())
	receta.RegisterRoutes(router, recetaHandler, authMiddleware)
	usuario.RegisterRoutes(router, usuarioHandler, authMiddleware)

	router.Run(":8080")
}

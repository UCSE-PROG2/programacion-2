package middleware

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

// CORSMiddleware habilita a un cliente servido desde otro origen (por
// ejemplo, la app de React de la Unidad 7 corriendo con Vite en
// http://localhost:5173) a llamar esta API, que escucha en otro puerto
// (http://localhost:8080). Sin esto, el navegador bloquea la respuesta por la
// política de mismo origen antes de que el código JS de la página la vea (ver
// Unidad 7 — "CORS: por qué el navegador bloquea la llamada").
func CORSMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Header("Access-Control-Allow-Origin", "*")
		c.Header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		c.Header("Access-Control-Allow-Headers", "Content-Type, Authorization")

		// El navegador manda un preflight OPTIONS antes de cualquier request
		// "no simple" (ej. con header Authorization) para confirmar que el
		// servidor permite ese método/headers antes de mandar la request real.
		// Gin no tiene una ruta registrada para OPTIONS, así que hay que
		// cortarla acá mismo con 204 en vez de dejarla caer en un 404.
		if c.Request.Method == http.MethodOptions {
			c.AbortWithStatus(http.StatusNoContent)
			return
		}

		c.Next()
	}
}

// Package middleware agrupa los gin.HandlerFunc que corren antes de que la
// request llegue al handler final (ver Clase 3 — "Middlewares en Gin"). Es un
// paquete transversal, igual que "auth" y "db": ningún dominio de negocio
// debería reimplementar su propia validación de JWT.
package middleware

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"

	"biblioteca/api/internal/auth"
)

// AuthMiddleware valida el JWT del header "Authorization: Bearer <token>" y,
// si es válido, deja disponible en el contexto de la request un
// UsuarioContexto con el ID del usuario autenticado (ver ObtenerUsuarioContexto
// en dto.go). Cualquier handler registrado DESPUÉS de este middleware puede
// asumir que, si se ejecutó, hay un usuario autenticado — no necesita volver
// a chequear el token.
func AuthMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		// Paso 1: el header tiene que estar presente. Sin él no hay forma de
		// saber quién es el cliente.
		header := c.GetHeader("Authorization")
		if header == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "falta el header Authorization"})
			return
		}

		// Paso 2: el formato esperado es EXACTAMENTE "Bearer <token>" — dos
		// partes separadas por un espacio. strings.SplitN con límite 2 evita
		// problemas si el token en sí tuviera un espacio (no debería, pero no
		// cuesta nada ser explícito con el límite).
		partes := strings.SplitN(header, " ", 2)
		if len(partes) != 2 || partes[0] != "Bearer" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "formato esperado: Bearer <token>"})
			return
		}

		// Paso 3: decodificar y validar la firma y expiración del JWT. Un
		// token alterado, firmado con otra clave, o vencido, cae en este
		// mismo error — el cliente no necesita (ni debería) distinguir cuál
		// de esos tres pasó.
		claims, err := auth.ValidarToken(partes[1])
		if err != nil {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "token inválido o expirado"})
			return
		}

		// Paso 4: el claim "sub" es el ID del usuario (ver auth.GenerarToken).
		// Viene como interface{} porque jwt.MapClaims es un map[string]any —
		// hay que confirmar que efectivamente es un string antes de usarlo.
		usuarioID, ok := claims["sub"].(string)
		if !ok || usuarioID == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "token inválido o expirado"})
			return
		}

		// Paso 5: armar el DTO (solo el ID, nada más del claim) y trasladarlo
		// al resto de la cadena a través del *gin.Context de esta request.
		setUsuarioContexto(c, UsuarioContexto{UsuarioID: usuarioID})

		// Paso 6: recién acá se le cede el control al siguiente eslabón
		// (otro middleware, o el handler final). Si alguno de los checks de
		// arriba hubiera fallado, c.Abort...() ya cortó la cadena y este
		// c.Next() nunca se alcanza.
		c.Next()
	}
}

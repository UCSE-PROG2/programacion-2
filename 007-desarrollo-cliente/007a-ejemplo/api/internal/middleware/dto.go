package middleware

import "github.com/gin-gonic/gin"

// UsuarioContexto es el DTO que AuthMiddleware arma a partir del JWT y deja
// disponible para el resto de la request. A propósito lleva SOLO el ID del
// usuario (nunca el hash de la contraseña, ni nada más del claim) — es
// justo el dato mínimo que un handler protegido necesita para saber "quién
// está haciendo esta request", sin volver a decodificar el token él mismo.
type UsuarioContexto struct {
	UsuarioID string
}

// claveUsuarioContexto es la clave con la que UsuarioContexto viaja dentro
// del *gin.Context de la request. Es una constante privada del paquete para
// que nadie fuera de "middleware" pueda pisarla por accidente con un
// c.Set("usuarioContexto", ...) usando un string suelto.
const claveUsuarioContexto = "usuarioContexto"

// setUsuarioContexto guarda el DTO en el contexto de ESTA request particular
// (c.Set, no una variable global — cada request de Gin tiene su propio
// *gin.Context, así que no hay condición de carrera entre requests
// concurrentes).
func setUsuarioContexto(c *gin.Context, u UsuarioContexto) {
	c.Set(claveUsuarioContexto, u)
}

// ObtenerUsuarioContexto recupera el DTO que dejó AuthMiddleware. El segundo
// valor de retorno sigue el mismo "comma-ok idiom" que c.Get y que leer un
// map — false si la ruta no pasó por AuthMiddleware (por ejemplo, un handler
// mal cableado que se olvidó de aplicar el middleware).
func ObtenerUsuarioContexto(c *gin.Context) (UsuarioContexto, bool) {
	valor, existe := c.Get(claveUsuarioContexto)
	if !existe {
		return UsuarioContexto{}, false
	}

	usuario, ok := valor.(UsuarioContexto)
	return usuario, ok
}

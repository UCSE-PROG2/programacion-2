package auth

import (
	"errors"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

// secretKey firma y verifica los JWT. En un proyecto real viaja por variable
// de entorno (os.Getenv("JWT_SECRET")), nunca hardcodeada — se deja fija acá
// solo porque este es un ejemplo de la unidad (ver Clase 3 — "Generar y
// validar un JWT en Go").
var secretKey = []byte("clave-secreta-del-servidor-biblioteca")

// duracionToken es cuánto tiempo queda vigente un token desde que se emite.
const duracionToken = 2 * time.Hour

// GenerarToken emite un JWT cuyo único claim de negocio es "sub" (subject):
// el ID (hex de bson.ObjectID) del usuario autenticado. A propósito NO viaja
// nada más sensible en el payload — el JWT solo está firmado, no encriptado,
// así que cualquiera que lo intercepte puede leerlo en base64 (ver Clase 3 —
// "Qué es un JWT").
func GenerarToken(usuarioID string) (string, error) {
	ahora := time.Now()
	claims := jwt.MapClaims{
		"sub": usuarioID,
		"iat": ahora.Unix(),
		"exp": ahora.Add(duracionToken).Unix(),
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(secretKey)
}

// ValidarToken decodifica el token, verifica la firma contra secretKey y
// chequea la expiración (todo eso lo hace jwt.ParseWithClaims por debajo). Si
// el token fue alterado, está mal formado, o está vencido, devuelve error.
func ValidarToken(tokenString string) (jwt.MapClaims, error) {
	token, err := jwt.ParseWithClaims(tokenString, jwt.MapClaims{}, func(t *jwt.Token) (any, error) {
		return secretKey, nil
	})
	if err != nil {
		return nil, err
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok || !token.Valid {
		return nil, errors.New("token inválido")
	}
	return claims, nil
}

// Package auth agrupa todo lo que no es específico del dominio "usuario" pero
// hace falta para autenticarlo: hashing de contraseñas con bcrypt y emisión /
// validación de JWT (ver Clase 3 — "JWT, bcrypt, middlewares y autorización
// por rol"). Es un paquete transversal, igual que "db": ningún dominio de
// negocio debería reimplementar esto por su cuenta.
package auth

import "golang.org/x/crypto/bcrypt"

// HashPassword aplica bcrypt sobre una contraseña en texto plano. Se usa UNA
// SOLA VEZ, al registrar el usuario — nunca se guarda la contraseña tal cual
// la escribió, solo este hash irreversible (ver Clase 3 — "Por qué nunca se
// guarda una contraseña en texto plano").
//
// bcrypt.DefaultCost (10) es el costo recomendado: suficientemente lento para
// dificultar fuerza bruta, sin volverse impracticable en un login normal.
func HashPassword(password string) (string, error) {
	hash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return "", err
	}
	return string(hash), nil
}

// CheckPassword compara una contraseña en texto plano contra un hash ya
// guardado. Devuelve nil si coinciden, o un error si no — nunca se
// "desencripta" el hash para comparar, se vuelve a hashear el intento de
// login y se comparan los dos hashes (ver Clase 3).
func CheckPassword(hash, password string) error {
	return bcrypt.CompareHashAndPassword([]byte(hash), []byte(password))
}

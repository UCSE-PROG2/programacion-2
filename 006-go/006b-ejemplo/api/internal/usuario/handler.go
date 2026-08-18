package usuario

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"

	"biblioteca/api/internal/middleware"
)

// Handler es la única pieza del dominio que ve los DTOs de entrada/salida
// (RegistroDTO, LoginDTO, CambiarPasswordDTO, UsuarioDTO). Service nunca los
// ve — para él el único tipo que existe es Usuario (ver Clase 2).
type Handler struct {
	service *Service
}

func NewHandler(service *Service) *Handler {
	return &Handler{service: service}
}

// Registro responde POST /usuarios/registro.
func (h *Handler) Registro(c *gin.Context) {
	// Paso 1: parsear y validar el body con los tags `binding:"..."` de
	// RegistroDTO (formato de email, longitud mínima de la contraseña).
	var dto RegistroDTO
	if err := c.ShouldBindJSON(&dto); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// Paso 2: el service valida el email duplicado y recién ahí hashea con
	// bcrypt (ver Service.Registrar).
	creado, err := h.service.Registrar(c.Request.Context(), dto)
	if err != nil {
		if errors.Is(err, ErrEmailYaRegistrado) {
			c.JSON(http.StatusConflict, gin.H{"error": err.Error()})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	// Paso 3: responder con UsuarioDTO — nunca con el hash de la contraseña.
	c.JSON(http.StatusCreated, creado.ToDTO())
}

// Login responde POST /usuarios/login.
func (h *Handler) Login(c *gin.Context) {
	var dto LoginDTO
	if err := c.ShouldBindJSON(&dto); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	token, err := h.service.Login(c.Request.Context(), dto)
	if err != nil {
		// Tanto "email no existe" como "contraseña incorrecta" llegan acá
		// como ErrCredencialesInvalidas — el mismo 401 y el mismo mensaje
		// para los dos casos (ver Service.Login).
		c.JSON(http.StatusUnauthorized, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"token": token})
}

// CambiarPassword responde PUT /usuarios/password. Esta ruta va DETRÁS de
// middleware.AuthMiddleware() (ver RegisterRoutes) — para cuando este handler
// corre, ya hay un UsuarioContexto disponible con el ID del usuario
// autenticado.
func (h *Handler) CambiarPassword(c *gin.Context) {
	// Paso 1: recuperar el usuario autenticado que dejó AuthMiddleware. Si no
	// existe (la ruta se registró sin el middleware, por error de cableado),
	// no hay quién sea "el usuario actual" — 401 en vez de un panic más
	// adelante tratando de usar un ID vacío.
	usuarioCtx, ok := middleware.ObtenerUsuarioContexto(c)
	if !ok {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "no autenticado"})
		return
	}

	// Paso 2: parsear y validar el body (passwordActual + passwordNueva).
	var dto CambiarPasswordDTO
	if err := c.ShouldBindJSON(&dto); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// Paso 3: el service verifica passwordActual contra el hash guardado y,
	// si coincide, guarda el hash de passwordNueva. El ID viene del JWT
	// (usuarioCtx), nunca del body — así nadie puede pedir cambiar la
	// contraseña de otra cuenta.
	err := h.service.CambiarPassword(c.Request.Context(), usuarioCtx.UsuarioID, dto)
	if err != nil {
		switch {
		case errors.Is(err, ErrPasswordActualInvalida):
			c.JSON(http.StatusUnauthorized, gin.H{"error": err.Error()})
		default:
			c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		}
		return
	}

	c.Status(http.StatusNoContent)
}

// RegisterRoutes agrupa las rutas del dominio bajo /usuarios (ver Clase 5 —
// "router.Group() — organizar rutas por dominio"). authMiddleware se recibe
// por parámetro en vez de importarlo acá adentro: quién cablea las rutas
// (main.go) decide qué middleware aplicar, el paquete "usuario" no depende
// de una instancia global de "middleware".
func RegisterRoutes(router *gin.Engine, h *Handler, authMiddleware gin.HandlerFunc) {
	usuarios := router.Group("/usuarios")
	{
		usuarios.POST("/registro", h.Registro)
		usuarios.POST("/login", h.Login)
		// Solo esta ruta necesita saber quién está autenticado — registro y
		// login son, por definición, accesibles sin token todavía.
		usuarios.PUT("/password", authMiddleware, h.CambiarPassword)
	}
}

package receta

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"

	"recetario/api/internal/middleware"
)

// Handler solo habla en RecetaDTO: parsea el body a RecetaDTO con
// c.ShouldBindJSON y responde con lo que le devuelve el Service, que también
// es siempre RecetaDTO. La conversión a/desde Receta (el modelo de Mongo)
// vive en el Service (ver service.go) — el Handler nunca ve una Receta.
type Handler struct {
	service *Service
}

func NewHandler(service *Service) *Handler {
	return &Handler{service: service}
}

// List responde GET /recetas?pagina=N — devuelve una página de recetas (el
// tamaño es fijo, ver TamanioPagina) y el total. Sin "pagina" asume la 1.
func (h *Handler) List(c *gin.Context) {
	pagina, err := strconv.Atoi(c.DefaultQuery("pagina", "1"))
	if err != nil || pagina < 1 {
		c.JSON(http.StatusBadRequest, gin.H{"error": "pagina debe ser un entero >= 1"})
		return
	}

	resultado, err := h.service.ListarPagina(c.Request.Context(), pagina)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, resultado)
}

// GetByID responde GET /recetas/:id.
func (h *Handler) GetByID(c *gin.Context) {
	id := c.Param("id")

	dto, err := h.service.BuscarPorID(c.Request.Context(), id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, dto)
}

// Create responde POST /recetas. c.ShouldBindJSON parsea el body y valida
// los tags `binding:"..."` de RecetaDTO al mismo tiempo. AuthMiddleware ya
// corrió antes (ver RegisterRoutes), así que ObtenerUsuarioContexto siempre
// encuentra un usuario acá — el chequeo `!ok` es defensivo, por si algún día
// esta ruta se registrara sin el middleware por error.
func (h *Handler) Create(c *gin.Context) {
	usuarioCtx, ok := middleware.ObtenerUsuarioContexto(c)
	if !ok {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "no autenticado"})
		return
	}

	var dto RecetaDTO
	if err := c.ShouldBindJSON(&dto); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// usuarioCtx.UsuarioID es quien queda registrado como creador — nunca un
	// campo del body, que el cliente podría manipular para crear una receta
	// "a nombre de" otra persona.
	creada, err := h.service.Crear(c.Request.Context(), dto, usuarioCtx.UsuarioID)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, creada)
}

// Update responde PUT /recetas/:id. Mismo criterio que Create: el usuario
// que queda registrado como último actualizador sale del JWT, no del body.
func (h *Handler) Update(c *gin.Context) {
	usuarioCtx, ok := middleware.ObtenerUsuarioContexto(c)
	if !ok {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "no autenticado"})
		return
	}

	id := c.Param("id")

	var dto RecetaDTO
	if err := c.ShouldBindJSON(&dto); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	actualizada, err := h.service.Actualizar(c.Request.Context(), id, dto, usuarioCtx.UsuarioID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, actualizada)
}

// Delete responde DELETE /recetas/:id.
func (h *Handler) Delete(c *gin.Context) {
	id := c.Param("id")
	if err := h.service.Eliminar(c.Request.Context(), id); err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}
	c.Status(http.StatusNoContent)
}

// RegisterRoutes agrupa todas las rutas del dominio bajo /recetas. Todas
// requieren sesión (authMiddleware) — igual criterio que /libros en la
// Unidad 6.
func RegisterRoutes(router *gin.Engine, h *Handler, authMiddleware gin.HandlerFunc) {
	recetas := router.Group("/recetas")
	{
		recetas.GET("", authMiddleware, h.List)
		recetas.GET("/:id", authMiddleware, h.GetByID)
		recetas.POST("", authMiddleware, h.Create)
		recetas.PUT("/:id", authMiddleware, h.Update)
		recetas.DELETE("/:id", authMiddleware, h.Delete)
	}
}

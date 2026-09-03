package receta

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"recetario/api/internal/middleware"
)

// Handler es la ÚNICA pieza del dominio que ve RecetaDTO: convierte a Receta
// apenas recibe un request (dto.ToModel()) y convierte de vuelta apenas arma
// la response (r.ToDTO()). Service y Repository nunca ven RecetaDTO — para
// ellos el único tipo que existe es Receta.
type Handler struct {
	service *Service
}

func NewHandler(service *Service) *Handler {
	return &Handler{service: service}
}

// List responde GET /recetas — devuelve todas las recetas.
func (h *Handler) List(c *gin.Context) {
	recetas, err := h.service.ListarTodas(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	dtos := make([]RecetaDTO, 0, len(recetas))
	for _, r := range recetas {
		dtos = append(dtos, r.ToDTO())
	}
	c.JSON(http.StatusOK, dtos)
}

// GetByID responde GET /recetas/:id.
func (h *Handler) GetByID(c *gin.Context) {
	id := c.Param("id")

	r, err := h.service.BuscarPorID(c.Request.Context(), id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, r.ToDTO())
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

	r, err := dto.ToModel()
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// usuarioCtx.UsuarioID es quien queda registrado como creador — nunca un
	// campo del body, que el cliente podría manipular para crear una receta
	// "a nombre de" otra persona.
	creada, err := h.service.Crear(c.Request.Context(), r, usuarioCtx.UsuarioID)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, creada.ToDTO())
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

	r, err := dto.ToModel()
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	actualizada, err := h.service.Actualizar(c.Request.Context(), id, r, usuarioCtx.UsuarioID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, actualizada.ToDTO())
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

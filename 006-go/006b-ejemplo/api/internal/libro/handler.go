package libro

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

// Handler es la ÚNICA pieza del dominio que ve LibroDTO: convierte a Libro
// apenas recibe un request (dto.ToModel()) y convierte de vuelta apenas arma
// la response (l.ToDTO()). Service y Repository nunca ven LibroDTO — para
// ellos el único tipo que existe es Libro (ver Clase 2).
type Handler struct {
	service *Service
}

func NewHandler(service *Service) *Handler {
	return &Handler{service: service}
}

// List responde GET /libros — devuelve todos los libros.
func (h *Handler) List(c *gin.Context) {
	libros, err := h.service.ListarTodos(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	dtos := make([]LibroDTO, 0, len(libros))
	for _, l := range libros {
		dtos = append(dtos, l.ToDTO())
	}
	c.JSON(http.StatusOK, dtos)
}

// GetByID responde GET /libros/:id — c.Param("id") extrae el segmento de la
// ruta declarado como ":id" (ver Clase 5 — "Parámetros de path vs. query
// string").
func (h *Handler) GetByID(c *gin.Context) {
	id := c.Param("id")

	l, err := h.service.BuscarPorID(c.Request.Context(), id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, l.ToDTO())
}

// Buscar responde GET /libros/buscar?autor=... — a diferencia de GetByID, acá
// el filtro llega por query string, no por path (ver Clase 5). Es un ejemplo
// simple de c.Query junto con c.DefaultQuery.
func (h *Handler) Buscar(c *gin.Context) {
	autor := c.Query("autor")
	soloDisponibles := c.DefaultQuery("disponible", "false")

	libros, err := h.service.ListarTodos(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	dtos := make([]LibroDTO, 0)
	for _, l := range libros {
		if autor != "" && l.Autor != autor {
			continue
		}
		if soloDisponibles == "true" && !l.Disponible {
			continue
		}
		dtos = append(dtos, l.ToDTO())
	}
	c.JSON(http.StatusOK, dtos)
}

// Create responde POST /libros. c.ShouldBindJSON parsea el body y valida los
// tags `binding:"..."` de LibroDTO al mismo tiempo (ver Clase 5 — "Binding y
// validación con c.ShouldBindJSON").
func (h *Handler) Create(c *gin.Context) {
	var dto LibroDTO
	if err := c.ShouldBindJSON(&dto); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	l, err := dto.ToModel()
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	creado, err := h.service.Crear(c.Request.Context(), l)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, creado.ToDTO())
}

// Update responde PUT /libros/:id.
func (h *Handler) Update(c *gin.Context) {
	id := c.Param("id")

	var dto LibroDTO
	if err := c.ShouldBindJSON(&dto); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	l, err := dto.ToModel()
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	actualizado, err := h.service.Actualizar(c.Request.Context(), id, l)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, actualizado.ToDTO())
}

// Delete responde DELETE /libros/:id.
func (h *Handler) Delete(c *gin.Context) {
	id := c.Param("id")
	if err := h.service.Eliminar(c.Request.Context(), id); err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}
	c.Status(http.StatusNoContent)
}

// RegisterRoutes agrupa todas las rutas del dominio bajo /libros (ver Clase 5
// — "router.Group() — organizar rutas por dominio"). La ruta de búsqueda va
// ANTES de "/:id" a propósito: si "/:id" estuviera primero, Gin interpretaría
// "buscar" como un valor de :id y la ruta de búsqueda nunca se alcanzaría.
func RegisterRoutes(router *gin.Engine, h *Handler) {
	libros := router.Group("/libros")
	{
		libros.GET("", h.List)
		libros.GET("/buscar", h.Buscar)
		libros.GET("/:id", h.GetByID)
		libros.POST("", h.Create)
		libros.PUT("/:id", h.Update)
		libros.DELETE("/:id", h.Delete)
	}
}

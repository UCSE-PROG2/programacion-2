package libro

import (
	"net/http"
	"strconv"
	"time"

	"fmt"

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
	usuarioContexto, _ := c.Get("usuarioContexto")
	fmt.Println(usuarioContexto)
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

// formatoFechaQuery es el layout esperado para fechaDesde/fechaHasta en la
// query string de Buscar — mismo formato AAAA-MM-DD que LibroDTO.FechaIngreso
// (dto.go), para que lo que se manda al crear un libro y lo que se usa para
// filtrarlo se escriban igual.
const formatoFechaQuery = "2006-01-02"

// Buscar responde GET /libros/buscar — TODOS los filtros son opcionales y se
// combinan con AND (Clase 4 — "Filtros opcionales"): mandar solo los que
// interesan. A diferencia de una versión anterior de este mismo handler (que
// traía TODO con ListarTodos y filtraba acá, en memoria, con "if"), ahora se
// arma un BusquedaLibros y es MongoRepository.Buscar quien arma el bson.M con
// los operadores de Mongo — el filtrado lo hace la base, no Go.
//
//	titulo                -> aproximación de texto ($regex, case-insensitive)
//	autor                 -> igualdad exacta
//	disponible             -> true/false (si no viene, no filtra por disponibilidad)
//	anioDesde / anioHasta  -> $gte/$lte sobre anio_edicion
//	fechaDesde / fechaHasta -> $gte/$lte sobre fecha_ingreso (formato AAAA-MM-DD)
func (h *Handler) Buscar(c *gin.Context) {
	filtro := BusquedaLibros{
		Titulo: c.Query("titulo"),
		Autor:  c.Query("autor"),
	}

	if disponibleStr := c.Query("disponible"); disponibleStr != "" {
		disponible, err := strconv.ParseBool(disponibleStr)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "disponible debe ser true o false"})
			return
		}
		filtro.Disponible = &disponible
	}

	if anioDesdeStr := c.Query("anioDesde"); anioDesdeStr != "" {
		anioDesde, err := strconv.Atoi(anioDesdeStr)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "anioDesde debe ser un número"})
			return
		}
		filtro.AnioDesde = anioDesde
	}
	if anioHastaStr := c.Query("anioHasta"); anioHastaStr != "" {
		anioHasta, err := strconv.Atoi(anioHastaStr)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "anioHasta debe ser un número"})
			return
		}
		filtro.AnioHasta = anioHasta
	}

	if fechaDesdeStr := c.Query("fechaDesde"); fechaDesdeStr != "" {
		fechaDesde, err := time.Parse(formatoFechaQuery, fechaDesdeStr)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "fechaDesde debe tener formato AAAA-MM-DD"})
			return
		}
		filtro.FechaDesde = fechaDesde
	}
	if fechaHastaStr := c.Query("fechaHasta"); fechaHastaStr != "" {
		fechaHasta, err := time.Parse(formatoFechaQuery, fechaHastaStr)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "fechaHasta debe tener formato AAAA-MM-DD"})
			return
		}
		// time.Parse deja fechaHasta en la medianoche de ese día (00:00:00) —
		// un $lte contra eso excluiría CUALQUIER libro ingresado más tarde
		// ese mismo día. "fechaHasta=2024-01-31" tiene que incluir todo el
		// 31, así que se corre al último instante del día antes de filtrar.
		filtro.FechaHasta = fechaHasta.Add(24*time.Hour - time.Nanosecond)
	}

	libros, err := h.service.Buscar(c.Request.Context(), filtro)
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
func RegisterRoutes(router *gin.Engine, h *Handler, authMiddleware gin.HandlerFunc) {
	libros := router.Group("/libros")
	{
		libros.GET("", authMiddleware, h.List)
		libros.GET("/buscar", authMiddleware, h.Buscar)
		libros.GET("/:id", authMiddleware, h.GetByID)
		libros.POST("", authMiddleware, h.Create)
		libros.PUT("/:id", authMiddleware, h.Update)
		libros.DELETE("/:id", authMiddleware, h.Delete)
	}
}

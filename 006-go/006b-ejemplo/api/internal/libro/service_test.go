package libro

// Ver Clase 6 del readme de la unidad — "Testing básico con `testing` y `go
// test`" y "Testear el `service` con un repository de prueba". Convenciones
// que sigue este archivo, punto por punto:
//   - Archivo terminado en "_test.go", en la misma carpeta que el código que
//     testea.
//   - Mismo paquete ("libro") que el código bajo test: así Libro, Repository,
//     Service y BusquedaLibros ya están disponibles sin importar nada del
//     propio proyecto.
//   - Cada función de test empieza con "Test" seguido de mayúscula y recibe
//     "t *testing.T".
//   - No hay un assertEquals incorporado: se compara el valor obtenido contra
//     el esperado con un "if", y se reporta la falla con t.Errorf (registra y
//     sigue) o t.Fatalf (registra y corta el test ahí mismo).

import (
	"context"
	"errors"
	"testing"
	"time"
)

// fakeRepository satisface la interfaz Repository sin tocar una base de datos
// real: guarda los libros en un slice en memoria. Nunca declara "implements
// Repository" — como cualquier tipo en Go, la satisface con solo tener los
// métodos correctos (satisfacción implícita, Clase 1). Esto es lo que paga el
// costo de diseño de declarar Repository como interfaz (Clase 6 — "El
// repository como interfaz — por qué, no solo cómo"): para testear el
// Service alcanza con esta versión de prueba, sin levantar Mongo.
type fakeRepository struct {
	libros []Libro
}

func (f *fakeRepository) FindAll(ctx context.Context) ([]Libro, error) {
	return f.libros, nil
}

func (f *fakeRepository) FindByID(ctx context.Context, id string) (Libro, error) {
	for _, l := range f.libros {
		if l.ID.Hex() == id {
			return l, nil
		}
	}
	return Libro{}, errors.New("libro no encontrado")
}

func (f *fakeRepository) Buscar(ctx context.Context, filtro BusquedaLibros) ([]Libro, error) {
	return f.libros, nil
}

func (f *fakeRepository) Create(ctx context.Context, l Libro) (Libro, error) {
	f.libros = append(f.libros, l)
	return l, nil
}

func (f *fakeRepository) Update(ctx context.Context, id string, l Libro) (Libro, error) {
	return l, nil
}

func (f *fakeRepository) Delete(ctx context.Context, id string) error { return nil }

// TestCrear_CompletaFechaIngresoSiFalta prueba la única regla de negocio que
// hoy tiene Service.Crear (service.go): si el LibroDTO no trajo fecha de
// ingreso, el service la completa con la fecha actual (Clase 6 — "Auditoría:
// campos comunes a todo documento" — completar campos automáticos es
// responsabilidad del service, no del handler ni del repository). Se
// construye el Service con NewService(repo), pasándole el fakeRepository en
// vez del *MongoRepository real: eso ES inyección de dependencias (Clase 6 —
// "Inyección de dependencias manual").
func TestCrear_CompletaFechaIngresoSiFalta(t *testing.T) {
	repo := &fakeRepository{}
	s := NewService(repo)

	creado, err := s.Crear(context.Background(), Libro{Titulo: "El Aleph", Autor: "Borges"})
	if err != nil {
		t.Fatalf("Crear() devolvió error: %v", err)
	}
	if creado.Titulo != "El Aleph" {
		t.Errorf("Titulo = %v; quería El Aleph", creado.Titulo)
	}
	if creado.FechaIngreso.IsZero() {
		t.Errorf("FechaIngreso no se completó automáticamente")
	}
	if len(repo.libros) != 1 {
		t.Errorf("se esperaba 1 libro guardado, hay %d", len(repo.libros))
	}
}

// TestCrear_RespetaFechaIngresoSiViene prueba el otro camino de la misma
// regla: si el llamador ya mandó una FechaIngreso, el service no la debe
// pisar con la fecha actual.
func TestCrear_RespetaFechaIngresoSiViene(t *testing.T) {
	repo := &fakeRepository{}
	s := NewService(repo)

	fechaOriginal := time.Date(2020, time.January, 1, 0, 0, 0, 0, time.UTC)
	creado, err := s.Crear(context.Background(), Libro{Titulo: "Rayuela", FechaIngreso: fechaOriginal})
	if err != nil {
		t.Fatalf("Crear() devolvió error: %v", err)
	}
	if !creado.FechaIngreso.Equal(fechaOriginal) {
		t.Errorf("FechaIngreso = %v; quería %v", creado.FechaIngreso, fechaOriginal)
	}
}

// TestListarTodos prueba que Service.ListarTodos delega en el repository y
// devuelve exactamente lo que este tiene cargado.
func TestListarTodos(t *testing.T) {
	repo := &fakeRepository{libros: []Libro{{Titulo: "Ficciones"}, {Titulo: "El Aleph"}}}
	s := NewService(repo)

	libros, err := s.ListarTodos(context.Background())
	if err != nil {
		t.Fatalf("ListarTodos() devolvió error: %v", err)
	}
	if len(libros) != 2 {
		t.Errorf("se esperaban 2 libros, hay %d", len(libros))
	}
}

// TestBuscarPorID_NoEncontrado prueba el camino de error: un ID que no existe
// en el repository de prueba debe propagarse como error, no como pánico.
func TestBuscarPorID_NoEncontrado(t *testing.T) {
	repo := &fakeRepository{}
	s := NewService(repo)

	_, err := s.BuscarPorID(context.Background(), "no-existe")
	if err == nil {
		t.Errorf("se esperaba error al buscar un id inexistente, no hubo")
	}
}

// TestEliminar prueba que Service.Eliminar delega en el repository sin
// devolver error cuando este confirma el borrado.
func TestEliminar(t *testing.T) {
	repo := &fakeRepository{}
	s := NewService(repo)

	if err := s.Eliminar(context.Background(), "cualquier-id"); err != nil {
		t.Errorf("Eliminar() devolvió error: %v", err)
	}
}

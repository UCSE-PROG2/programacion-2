import { useCallback, useEffect, useState, type FormEvent } from "react";
import { useAuth } from "../auth/AuthContext";
import {
  listarRecetas,
  crearReceta,
  actualizarReceta,
  eliminarReceta,
  ErrorAPI,
} from "../api/recetas";
import type { RecetaDTO } from "../api/types";

// Estado del FORMULARIO: los inputs numéricos (tiempo, porciones) se guardan
// como string, no como number. Un <input type="number"> controlado (Clase 2
// — "Formularios controlados") sigue devolviendo un string en
// e.target.value; si lo convirtiéramos a number en cada tecleo, borrar el
// campo por completo daría NaN en vez de "", y el input mostraría "NaN" en
// pantalla. Guardando el string tal cual, la conversión a number pasa recién
// una vez, al armar el DTO que se manda al backend (ver manejarEnvio).
interface FormularioReceta {
  nombre: string;
  categoria: string;
  tiempoPreparacionMinutos: string;
  porciones: string;
  vegetariana: boolean;
}

const FORMULARIO_VACIO: FormularioReceta = {
  nombre: "",
  categoria: "",
  tiempoPreparacionMinutos: "",
  porciones: "",
  vegetariana: false,
};

export function RecetasPage() {
  // token: esta página solo se monta dentro de RutaProtegida (ver App.tsx),
  // así que para cuando llegamos acá el AuthProvider ya terminó de resolver
  // la sesión y "token" nunca es null en la práctica — igual se lo trata
  // como posiblemente null en todo el archivo (con "if (!token) return"),
  // porque así lo tipa AuthContextValue y TypeScript no sabe de esa garantía
  // externa.
  const { token } = useAuth();

  // Estado de la LISTA: qué recetas hay, si está cargando, y el error de la
  // carga — mismo patrón de useState + useEffect que ListaRecetas en la
  // Clase 3 ("Pidiendo datos a una API con fetch").
  const [recetas, setRecetas] = useState<RecetaDTO[]>([]);
  const [cargando, setCargando] = useState(true);
  const [errorLista, setErrorLista] = useState<string | null>(null);

  // Estado del FORMULARIO: los valores tipeados, y por separado en qué
  // "modo" está — creando una receta nueva (editandoId === null) o editando
  // una existente (editandoId con el id de esa receta). Un único formulario
  // sirve para las dos operaciones, para no duplicar el mismo JSX dos veces.
  const [formulario, setFormulario] = useState<FormularioReceta>(FORMULARIO_VACIO);
  const [editandoId, setEditandoId] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [errorFormulario, setErrorFormulario] = useState<string | null>(null);

  // cargarRecetas: pide la lista completa y actualiza el estado. Es una
  // función aparte (no solo el cuerpo de useEffect) porque también hace
  // falta volver a llamarla después de crear/editar/eliminar, para que la
  // tabla refleje el cambio sin recargar la página entera.
  //
  // useCallback(fn, [token]) memoriza esta función: mientras "token" no
  // cambie entre renders, cargarRecetas mantiene la MISMA identidad de
  // función (el mismo objeto en memoria). Sin esto, cada render de
  // RecetasPage crearía una función nueva, y el useEffect de abajo (que la
  // lista como dependencia) la vería "cambiada" en cada render y se
  // dispararía sin parar.
  const cargarRecetas = useCallback(async () => {
    if (!token) return;
    setCargando(true);
    setErrorLista(null);
    try {
      const datos = await listarRecetas(token);
      setRecetas(datos);
    } catch (e) {
      setErrorLista(e instanceof ErrorAPI ? e.message : "No se pudieron cargar las recetas");
    } finally {
      setCargando(false);
    }
  }, [token]);

  // Cargar la lista al montar el componente (y de nuevo si "token" llegara a
  // cambiar, aunque en la práctica no pasa — ver el comentario sobre "token"
  // más arriba). Con cargarRecetas ya memorizada por useCallback, listarla
  // como dependencia es seguro: no dispara el efecto en cada render, solo
  // cuando "token" realmente cambia.
  useEffect(() => {
    cargarRecetas();
  }, [cargarRecetas]);

  // empezarEdicion: precarga el formulario con los datos de una fila
  // existente y pasa al modo "editar". Todavía no dispara ningún request —
  // eso pasa recién cuando se envía el formulario (manejarEnvio). Los campos
  // numéricos se convierten de vuelta a string con String(...) para poder
  // mostrarlos en el input (ver el comentario sobre FormularioReceta).
  function empezarEdicion(receta: RecetaDTO) {
    setFormulario({
      nombre: receta.nombre,
      categoria: receta.categoria,
      tiempoPreparacionMinutos: String(receta.tiempoPreparacionMinutos),
      porciones: String(receta.porciones),
      vegetariana: receta.vegetariana,
    });
    setEditandoId(receta.id);
    setErrorFormulario(null);
  }

  // cancelarEdicion: vuelve al modo "crear" con el formulario en blanco —
  // se usa tanto en el botón "Cancelar" como después de guardar con éxito.
  function cancelarEdicion() {
    setFormulario(FORMULARIO_VACIO);
    setEditandoId(null);
  }

  // manejarEnvio: un único handler para "crear" y "editar" — decide cuál de
  // las dos llamadas hacer según editandoId. Mismo patrón de formulario
  // controlado + async/await que LoginPage/RegistroPage (Clase 2 y 3):
  // preventDefault para no recargar la página, estado "enviando" para
  // deshabilitar el botón mientras el request está en vuelo, y el error (si
  // lo hay) se guarda en estado para mostrarlo en el JSX.
  async function manejarEnvio(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!token) return;

    setErrorFormulario(null);
    setEnviando(true);
    try {
      // El DTO que viaja al backend nunca incluye campos de auditoría
      // (quién la creó/actualizó, fechas) — no existen en RecetaDTO del
      // lado del cliente (ver api/types.ts), así que ni siquiera se podrían
      // mandar por error. El backend los completa solo, a partir del token.
      const dto = {
        nombre: formulario.nombre,
        categoria: formulario.categoria,
        // Number(""): da 0, pero los inputs son type="number" con
        // required, así que el navegador ya bloquea el envío si están
        // vacíos antes de que este código corra.
        tiempoPreparacionMinutos: Number(formulario.tiempoPreparacionMinutos),
        porciones: Number(formulario.porciones),
        vegetariana: formulario.vegetariana,
      };

      if (editandoId) {
        await actualizarReceta(token, editandoId, dto);
      } else {
        await crearReceta(token, dto);
      }

      cancelarEdicion(); // limpia el formulario y sale del modo edición
      await cargarRecetas(); // refresca la tabla con el dato ya guardado
    } catch (e) {
      setErrorFormulario(e instanceof ErrorAPI ? e.message : "No se pudo guardar la receta");
    } finally {
      setEnviando(false);
    }
  }

  // manejarEliminar: confirma con la persona (window.confirm es una API del
  // navegador, no de React — bloquea la ejecución hasta que se confirma o
  // cancela; alcanza para un ejemplo simple, sin armar un modal propio) y
  // recién ahí borra.
  async function manejarEliminar(id: string) {
    if (!token) return;
    if (!window.confirm("¿Eliminar esta receta?")) return;

    try {
      await eliminarReceta(token, id);
      // Si se borra justo la receta que se estaba editando, hay que salir
      // del modo edición — si no, el formulario quedaría "editando" un id
      // que ya no existe.
      if (editandoId === id) cancelarEdicion();
      await cargarRecetas();
    } catch (e) {
      setErrorLista(e instanceof ErrorAPI ? e.message : "No se pudo eliminar la receta");
    }
  }

  return (
    <div className="panel">
      <h1>Productos</h1>

      {/* Formulario: el mismo bloque de JSX sirve para crear y para editar
          — el título del botón y si aparece "Cancelar" cambian según
          editandoId. */}
      <form onSubmit={manejarEnvio}>
        <label>
          Nombre
          <input
            value={formulario.nombre}
            onChange={(e) => setFormulario({ ...formulario, nombre: e.target.value })}
            required
          />
        </label>
        <label>
          Categoría
          <input
            value={formulario.categoria}
            onChange={(e) => setFormulario({ ...formulario, categoria: e.target.value })}
            required
          />
        </label>
        <label>
          Tiempo de preparación (min)
          <input
            type="number"
            min="1"
            value={formulario.tiempoPreparacionMinutos}
            onChange={(e) =>
              setFormulario({ ...formulario, tiempoPreparacionMinutos: e.target.value })
            }
            required
          />
        </label>
        <label>
          Porciones
          <input
            type="number"
            min="1"
            value={formulario.porciones}
            onChange={(e) => setFormulario({ ...formulario, porciones: e.target.value })}
            required
          />
        </label>
        <label className="checkbox">
          {/* Un checkbox no tiene "value" en el sentido de texto — el dato
              relevante es e.target.checked (booleano), no e.target.value. */}
          <input
            type="checkbox"
            checked={formulario.vegetariana}
            onChange={(e) => setFormulario({ ...formulario, vegetariana: e.target.checked })}
          />
          Vegetariana
        </label>

        {errorFormulario && <p className="error">{errorFormulario}</p>}

        <div className="acciones">
          <button type="submit" disabled={enviando}>
            {enviando ? "Guardando..." : editandoId ? "Guardar cambios" : "Crear receta"}
          </button>
          {/* Solo aparece en modo edición — no tiene sentido "cancelar" la
              creación de un formulario que ya está vacío. */}
          {editandoId && (
            <button type="button" onClick={cancelarEdicion} disabled={enviando}>
              Cancelar
            </button>
          )}
        </div>
      </form>

      {/* Tabla: mismo patrón responsive de la Clase 2 ("Tablas con varias
          columnas, responsive") — el wrapper con overflow-x:auto evita que
          las columnas de más rompan el layout en una pantalla angosta. */}
      {cargando && <p>Cargando recetas...</p>}
      {errorLista && <p className="error">{errorLista}</p>}
      {!cargando && !errorLista && (
        <div className="tabla-wrapper">
          <table>
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Categoría</th>
                <th>Tiempo (min)</th>
                <th>Porciones</th>
                <th>Vegetariana</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {recetas.map((receta) => (
                <tr key={receta.id}>
                  <td>{receta.nombre}</td>
                  <td>{receta.categoria}</td>
                  <td>{receta.tiempoPreparacionMinutos}</td>
                  <td>{receta.porciones}</td>
                  <td>{receta.vegetariana ? "Sí" : "No"}</td>
                  <td className="acciones">
                    <button type="button" onClick={() => empezarEdicion(receta)}>
                      Editar
                    </button>
                    <button type="button" onClick={() => manejarEliminar(receta.id)}>
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

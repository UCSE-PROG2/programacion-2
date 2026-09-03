import { useState, type FormEvent } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { ErrorAPI } from "../api/usuarios";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function manejarEnvio(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    setEnviando(true);
    try {
      await login(email, password);
      navigate("/");
    } catch (err) {
      setError(err instanceof ErrorAPI ? err.message : "No se pudo iniciar sesión");
    } finally {
      setEnviando(false);
    }
  }

  return (
    // .pagina-centrada: fuera de Layout (esta página no tiene sesión
    // todavía, no hay header ni nada que navegar), centra la tarjeta sola
    // en la pantalla — ver Unidad 7, Clase 1 ("Estructura del proyecto
    // generado") para la explicación del layout general del proyecto.
    <div className="pagina-centrada">
      <div className="tarjeta">
        <h1>Iniciar sesión</h1>
        <form onSubmit={manejarEnvio}>
          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </label>
          <label>
            Contraseña
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>
          {error && <p className="error">{error}</p>}
          <button type="submit" disabled={enviando}>
            {enviando ? "Ingresando..." : "Ingresar"}
          </button>
        </form>
        <p>
          ¿No tenés cuenta? <Link to="/registro">Registrate</Link>
        </p>
      </div>
    </div>
  );
}

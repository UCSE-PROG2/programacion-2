import { useState, type FormEvent } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { ErrorAPI } from "../api/usuarios";

export function RegistroPage() {
  const { registrar } = useAuth();
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
      await registrar(email, password);
      navigate("/");
    } catch (err) {
      setError(err instanceof ErrorAPI ? err.message : "No se pudo completar el registro");
    } finally {
      setEnviando(false);
    }
  }

  return (
    // .pagina-centrada: ver el mismo comentario en LoginPage.tsx.
    <div className="pagina-centrada">
      <div className="tarjeta">
        <h1>Crear cuenta</h1>
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
            Contraseña (mínimo 8 caracteres)
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              minLength={8}
              required
            />
          </label>
          {error && <p className="error">{error}</p>}
          <button type="submit" disabled={enviando}>
            {enviando ? "Creando cuenta..." : "Crear cuenta"}
          </button>
        </form>
        <p>
          ¿Ya tenés cuenta? <Link to="/login">Iniciá sesión</Link>
        </p>
      </div>
    </div>
  );
}

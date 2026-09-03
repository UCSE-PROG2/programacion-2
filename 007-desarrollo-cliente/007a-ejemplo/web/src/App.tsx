import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./auth/AuthContext";
import { RutaProtegida } from "./components/RutaProtegida";
import { Layout } from "./components/Layout";
import { LoginPage } from "./pages/LoginPage";
import { RegistroPage } from "./pages/RegistroPage";
import { HomePage } from "./pages/HomePage";
import { RecetasPage } from "./pages/RecetasPage";

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/registro" element={<RegistroPage />} />
          {/* Home y Recetas van RutaProtegida por afuera, Layout por
              adentro: primero se confirma que hay sesión (si no, redirige a
              /login sin llegar a montar el header); recién con sesión
              confirmada se muestra el header + la página pedida. */}
          <Route
            path="/"
            element={
              <RutaProtegida>
                <Layout>
                  <HomePage />
                </Layout>
              </RutaProtegida>
            }
          />
          <Route
            path="/recetas"
            element={
              <RutaProtegida>
                <Layout>
                  <RecetasPage />
                </Layout>
              </RutaProtegida>
            }
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;

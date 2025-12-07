// src/App.tsx
import { Routes, Route } from "react-router-dom";
import AppLayout from "./components/layout/AppLayout";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ProgramListPage from "./pages/ProgramListPage";
import ProgramDetailPage from "./pages/ProgramDetailPage";
import ScreeningFormPage from "./pages/ScreeningFormPage";
import AdminUsersPage from "./pages/AdminUsersPage";
import RequireAuth from "./auth/RequireAuth";

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route path="/" element={<ProgramListPage />} />
        <Route path="/programs/:id" element={<ProgramDetailPage />} />

        <Route element={<RequireAuth />}>
          <Route
            path="/programs/:id/screenings/new"
            element={<ScreeningFormPage />}
          />
          <Route
            path="/programs/:id/screenings/:screeningId/edit"
            element={<ScreeningFormPage />}
          />
          <Route path="/admin/users" element={<AdminUsersPage />} />
        </Route>
      </Route>
    </Routes>
  );
}

export default App;

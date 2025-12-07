import { BrowserRouter, Route, Routes } from "react-router-dom";
import AppLayout from "../components/layout/AppLayout";
import LoginPage from "../pages/auth/LoginPage";
import RegisterPage from "../pages/auth/RegisterPage";
import DashboardPage from "../pages/dashboard/DashboardPage";
import ProgramListPage from "../pages/programs/ProgramListPage";
import ProgramDetailPage from "../pages/programs/ProgramDetailPage";
import AdminUsersPage from "../pages/admin/AdminUsersPage";
import { RequireAuth } from "../auth/RequireAuth";
import { RequireRole } from "../auth/RequireRole";

export const AppRouter = () => (
  <BrowserRouter>
    <Routes>
      <Route element={<AppLayout />}>
        {/* public */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/" element={<ProgramListPage mode="visitor" />} />
        <Route path="/programs/:id" element={<ProgramDetailPage />} />

        {/* authenticated (USER/ADMIN) */}
        <Route element={<RequireAuth />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/my/programs" element={<ProgramListPage mode="mine" />} />
          <Route path="/my/screenings" element={<ProgramListPage mode="my-screenings" />} />
        </Route>

        {/* admin only */}
        <Route element={<RequireRole allowedRoles={["ADMIN"]} />}>
          <Route path="/admin/users" element={<AdminUsersPage />} />
        </Route>
      </Route>
    </Routes>
  </BrowserRouter>
);

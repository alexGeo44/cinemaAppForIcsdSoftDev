// src/app/router.tsx
import { createBrowserRouter } from "react-router-dom";
import AppLayout from "../components/layout/AppLayout";
import { AuthGuard } from "./guards/AuthGuard";
import { RoleGuard } from "./guards/RoleGuard";
import { BaseRole } from "../domain/auth/auth.types";

// auth / common
import LoginPage from "../features/auth/LoginPage";
import RegisterPage from "../features/auth/RegisterPage";
import ForbiddenPage from "../features/common/ForbiddenPage";

// programs
import ProgramListPage from "../features/programs/ProgramListPage";
import ProgramDetailsPage from "../features/programs/ProgramDetailsPage";
import ProgramCreatePage from "../features/programs/ProgramCreatePage";
import ProgramEditPage from "../features/programs/ProgramEditPage";

// screenings
import ScreeningDetailsPage from "../features/screenings/ScreeningDetailsPage";
import ScreeningCreatePage from "../features/screenings/ScreeningCreatePage";
import ScreeningEditPage from "../features/screenings/ScreeningEditPage";
import MyScreeningsPage from "../features/screenings/MyScreeningsPage";
import StaffReviewPage from "../features/screenings/StaffReviewPage";
import ProgramScreeningsPage from "../features/screenings/ProgramScreeningsPage";

// users
import AccountSettingsPage from "../features/users/AccountSettingsPage";
import UserManagementPage from "../features/users/UserManagementPage";

// helper: απλός shell (όλοι οι authenticated)
function withShell(element: JSX.Element) {
  return (
    <AuthGuard>
      <AppLayout>{element}</AppLayout>
    </AuthGuard>
  );
}

// helper: shell + role guard (για ειδικούς ρόλους)
function withShellRole(roles: BaseRole[], element: JSX.Element) {
  return (
    <AuthGuard>
      <RoleGuard allow={roles}>
        <AppLayout>{element}</AppLayout>
      </RoleGuard>
    </AuthGuard>
  );
}

export const router = createBrowserRouter([
  // 🔓 PUBLIC
  {
    path: "/login",
    element: <LoginPage />,
  },
  {
    path: "/register",
    element: <RegisterPage />,
  },
  {
    path: "/forbidden",
    element: <ForbiddenPage />,
  },

  // 🔐 PROTECTED + LAYOUT
  {
    path: "/",
    element: withShell(<div>Dashboard (to be implemented)</div>),
  },

  // 🎞 PROGRAMS
  {
    path: "/programs",
    element: withShell(<ProgramListPage />),
  },
  {
    // 👉 εδώ το αφήνουμε για ΟΛΟΥΣ τους logged-in users
    // (USER, PROGRAMMER, STAFF, SUBMITTER, ADMIN) ώστε να μπορούν να δημιουργούν program
    path: "/programs/new",
    element: withShell(<ProgramCreatePage />),
  },
  {
    path: "/programs/:id",
    element: withShell(<ProgramDetailsPage />),
  },
  {
    // Edit program μόνο για PROGRAMMER + ADMIN
    path: "/programs/:id/edit",
    element: withShellRole(
      [BaseRole.PROGRAMMER, BaseRole.ADMIN],
      <ProgramEditPage />
    ),
  },

  // 🎬 SCREENINGS
  {
    path: "/screenings/new",
    element: withShell(<ScreeningCreatePage />),
  },
  {
    path: "/screenings/:id",
    element: withShell(<ScreeningDetailsPage />),
  },
  {
    path: "/screenings/:id/edit",
    element: withShell(<ScreeningEditPage />),
  },

  // 👤 USER VIEWS
  {
    path: "/my-screenings",
    element: withShell(<MyScreeningsPage />),
  },
  {
    path: "/account",
    element: withShell(<AccountSettingsPage />),
  },

  // 🧑‍💼 STAFF / PROGRAMMER
  {
    path: "/staff/review",
    element: withShellRole(
      [BaseRole.STAFF, BaseRole.ADMIN],
      <StaffReviewPage />
    ),
  },
  {
    path: "/programmer/screenings",
    element: withShellRole(
      [BaseRole.PROGRAMMER, BaseRole.ADMIN],
      <ProgramScreeningsPage />
    ),
  },

  // 🛠 ADMIN
  {
    path: "/admin/users",
    element: withShellRole([BaseRole.ADMIN], <UserManagementPage />),
  },

  // 404
  {
    path: "*",
    element: <div>Not found</div>,
  },
]);

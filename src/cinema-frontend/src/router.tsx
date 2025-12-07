import React from "react";
import { createBrowserRouter } from "react-router-dom";

import App from "./App";
import ProtectedRoute from "./components/common/ProtectedRoute";
import RoleRoute from "./components/common/RoleRoute";

import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";

import DashboardPage from "./pages/dashboard/DashboardPage";

import ProgramListPage from "./pages/programs/ProgramListPage";
import ProgramFormPage from "./pages/programs/ProgramFormPage";

import ScreeningListPage from "./pages/screenings/ScreeningListPage";
import ScreeningFormPage from "./pages/screenings/ScreeningFormPage";

import UserListPage from "./pages/users/UserListPage";

import StaffTicketsPage from "./pages/staff/StaffTicketsPage";
import StaffReservationsPage from "./pages/staff/StaffReservationsPage";

export const router = createBrowserRouter([
  // ---------- PUBLIC ROUTES ----------
  {
    path: "/login",
    element: <LoginPage />,
  },
  {
    path: "/register",
    element: <RegisterPage />,
  },

  // ---------- PROTECTED AREA ----------
  {
    path: "/",
    element: (
      <ProtectedRoute>
        <App />
      </ProtectedRoute>
    ),
    children: [
      // dashboard (όλοι οι logged-in)
      {
        index: true,
        element: <DashboardPage />,
      },

      // PROGRAMMER + ADMIN: Programs
      {
        path: "programs",
        element: (
          <RoleRoute roles={["PROGRAMMER", "ADMIN"]}>
            <ProgramListPage />
          </RoleRoute>
        ),
      },
      {
        path: "programs/new",
        element: (
          <RoleRoute roles={["PROGRAMMER", "ADMIN"]}>
            <ProgramFormPage />
          </RoleRoute>
        ),
      },
      {
        path: "programs/:id",
        element: (
          <RoleRoute roles={["PROGRAMMER", "ADMIN"]}>
            <ProgramFormPage />
          </RoleRoute>
        ),
      },

      // PROGRAMMER + ADMIN: Screenings
      {
        path: "screenings",
        element: (
          <RoleRoute roles={["PROGRAMMER", "ADMIN"]}>
            <ScreeningListPage />
          </RoleRoute>
        ),
      },
      {
        path: "screenings/new",
        element: (
          <RoleRoute roles={["PROGRAMMER", "ADMIN"]}>
            <ScreeningFormPage />
          </RoleRoute>
        ),
      },
      {
        path: "screenings/:id",
        element: (
          <RoleRoute roles={["PROGRAMMER", "ADMIN"]}>
            <ScreeningFormPage />
          </RoleRoute>
        ),
      },

      // STAFF: tickets / reservations
      {
        path: "tickets",
        element: (
          <RoleRoute roles={["STAFF"]}>
            <StaffTicketsPage />
          </RoleRoute>
        ),
      },
      {
        path: "reservations",
        element: (
          <RoleRoute roles={["STAFF"]}>
            <StaffReservationsPage />
          </RoleRoute>
        ),
      },

      // ADMIN: Users
      {
        path: "users",
        element: (
          <RoleRoute roles={["ADMIN"]}>
            <UserListPage />
          </RoleRoute>
        ),
      },
    ],
  },
]);

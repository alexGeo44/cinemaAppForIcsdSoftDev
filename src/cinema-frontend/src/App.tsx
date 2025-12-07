import React from "react";
import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "./context/AuthContext";

const App: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="sidebar-header">
          <h2>Cinema Manager</h2>
          {user && (
            <div className="sidebar-user">
              {user.username} ({user.role})
            </div>
          )}
        </div>

        <nav className="sidebar-nav">
          <NavLink to="/" end>
            Dashboard
          </NavLink>

          {(user?.role === "PROGRAMMER" || user?.role === "ADMIN") && (
            <>
              <NavLink to="/programs">Programs</NavLink>
              <NavLink to="/screenings">Screenings</NavLink>
            </>
          )}

          {user?.role === "STAFF" && (
            <>
              <NavLink to="/tickets">Tickets</NavLink>
              <NavLink to="/reservations">Reservations</NavLink>
            </>
          )}

          {user?.role === "ADMIN" && (
            <>
              <NavLink to="/users">Users</NavLink>
            </>
          )}
        </nav>

        <div className="sidebar-footer">
          <button onClick={logout}>Logout</button>
        </div>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
};

export default App;

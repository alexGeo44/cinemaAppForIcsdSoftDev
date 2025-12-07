import { Link, Outlet } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";


export default function AppLayout() {
  const { user, logout } = useAuth();

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-header-left">
          <Link to="/" className="app-logo">
            Cinema Manager
          </Link>
        </div>
        <div className="app-header-right">
          {user ? (
            <>
              <span>
                {user.fullName} ({user.role})
              </span>
              <Link to="/">Programs</Link>
              {user.role === "ADMIN" && <Link to="/admin/users">Users</Link>}
              <button onClick={logout}>Logout</button>
            </>
          ) : (
            <>
              <Link to="/login">Login</Link>
              <Link to="/register">Register</Link>
            </>
          )}
        </div>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
}

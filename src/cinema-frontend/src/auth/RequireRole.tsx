import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./useAuth";

interface Props {
  allowedRoles: ("USER" | "ADMIN")[];
}

export const RequireRole: React.FC<Props> = ({ allowedRoles }) => {
  const { user, loading } = useAuth();
  if (loading) return <div>Loading...</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (!allowedRoles.includes(user.permanentRole))
    return <div>Δεν έχεις δικαίωμα πρόσβασης.</div>;

  return <Outlet />;
};

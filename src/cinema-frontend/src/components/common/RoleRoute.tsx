import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

type Role = "ADMIN" | "PROGRAMMER" | "STAFF" | "USER";

interface RoleRouteProps {
  roles: Role[];
  children: React.ReactElement;
}

const RoleRoute: React.FC<RoleRouteProps> = ({ roles, children }) => {
  const { user } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (!roles.includes(user.role)) {
    return <div style={{ padding: "1rem" }}>Δεν έχεις πρόσβαση σε αυτή τη σελίδα.</div>;
  }

  return children;
};

export default RoleRoute;

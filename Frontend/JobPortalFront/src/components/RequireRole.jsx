import { useContext } from "react";
import { Navigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";

export default function RequireRole({ role, children }) {
  const { user } = useContext(AuthContext);

  if (!user) return <Navigate to="/" />;
  if (user.role !== role) return <Navigate to="/" />;

  return children;
}

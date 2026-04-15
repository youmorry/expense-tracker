import { Navigate, Outlet } from "react-router";

import { getToken } from "../lib/auth";

export function AuthGuard() {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  return <Outlet />;
}

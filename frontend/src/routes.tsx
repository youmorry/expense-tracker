import type { RouteObject } from "react-router";
import { redirect } from "react-router";

import { getToken } from "./lib/auth";
import AnalyticsPage from "./features/analytics/components/AnalyticsPage";
import LoginPage from "./features/auth/components/LoginPage";
import SettingsPage from "./features/settings/components/SettingsPage";
import TransactionsPage from "./features/transactions/components/TransactionsPage";

function requireAuth() {
  if (!getToken()) {
    return redirect("/login");
  }
  return null;
}

export const routes: RouteObject[] = [
  {
    path: "/",
    loader: () => redirect("/transactions"),
  },
  {
    path: "/login",
    Component: LoginPage,
  },
  {
    path: "/transactions",
    Component: TransactionsPage,
    loader: requireAuth,
  },
  {
    path: "/analytics",
    Component: AnalyticsPage,
    loader: requireAuth,
  },
  {
    path: "/settings",
    Component: SettingsPage,
    loader: requireAuth,
  },
];

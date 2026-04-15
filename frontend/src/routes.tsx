import type { RouteObject } from "react-router";
import { redirect } from "react-router";

import { AuthGuard } from "./components/AuthGuard";
import AnalyticsPage from "./features/analytics/components/AnalyticsPage";
import LoginPage from "./features/auth/components/LoginPage";
import SettingsPage from "./features/settings/components/SettingsPage";
import TransactionsPage from "./features/transactions/components/TransactionsPage";

export const routes: RouteObject[] = [
  {
    path: "/login",
    Component: LoginPage,
  },
  {
    Component: AuthGuard,
    children: [
      {
        path: "/",
        loader: () => redirect("/transactions"),
      },
      {
        path: "/transactions",
        Component: TransactionsPage,
      },
      {
        path: "/analytics",
        Component: AnalyticsPage,
      },
      {
        path: "/settings",
        Component: SettingsPage,
      },
    ],
  },
];

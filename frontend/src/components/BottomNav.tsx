import { BarChart3, List, Settings } from "lucide-react";
import { NavLink } from "react-router";

const tabs = [
  { to: "/transactions", label: "Transactions", icon: List },
  { to: "/analytics", label: "Analytics", icon: BarChart3 },
  { to: "/settings", label: "Settings", icon: Settings },
] as const;

export function BottomNav() {
  return (
    <nav className="fixed inset-x-0 bottom-0 border-t border-gray-200 bg-white">
      <ul className="flex justify-around">
        {tabs.map(({ to, label, icon: Icon }) => (
          <li key={to}>
            <NavLink
              to={to}
              className={({ isActive }) =>
                `flex flex-col items-center px-3 py-2 text-xs ${
                  isActive ? "text-blue-600" : "text-gray-500"
                }`
              }
            >
              <Icon className="h-6 w-6" />
              {label}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  );
}

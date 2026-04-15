import { Outlet } from "react-router";

import { BottomNav } from "./BottomNav";

export function AppLayout() {
  return (
    <div className="pb-16">
      <Outlet />
      <BottomNav />
    </div>
  );
}

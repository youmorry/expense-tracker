import { Outlet } from "react-router";

import { BottomNav } from "./BottomNav";

export function AppLayout() {
  return (
    <div className="flex h-dvh flex-col">
      <main className="flex-1 overflow-y-auto">
        <Outlet />
      </main>
      <BottomNav />
    </div>
  );
}

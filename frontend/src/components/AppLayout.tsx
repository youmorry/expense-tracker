import { useLayoutEffect, useRef } from "react";
import { Outlet, useLocation } from "react-router";

import { BottomNav } from "./BottomNav";

export function AppLayout() {
  const mainRef = useRef<HTMLElement>(null);
  const { pathname } = useLocation();

  // React Router 7 の <ScrollRestoration /> は document scroller 専用のため、内側スクロール
  // コンテナは手動でリセットする。useLayoutEffect は描画前に走らせて初期位置のチラつきを防ぐ。
  useLayoutEffect(() => {
    mainRef.current?.scrollTo({ top: 0, behavior: "instant" });
  }, [pathname]);

  return (
    <div className="flex h-dvh flex-col">
      <main ref={mainRef} className="flex-1 overflow-y-auto">
        <Outlet />
      </main>
      <BottomNav />
    </div>
  );
}

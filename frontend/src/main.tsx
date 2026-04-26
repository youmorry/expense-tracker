import { GoogleOAuthProvider } from "@react-oauth/google";
import { QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import type { SetupWorker } from "msw/browser";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router";
import { ToastProvider } from "./components/Toast";
import "./index.css";
import { setToken } from "./lib/auth";
import { queryClient } from "./lib/queryClient";
import { routes } from "./routes";

declare global {
  interface Window {
    __seedAuthToken?: string;
    __setAuthToken?: (token: string) => void;
    __mswWorker?: SetupWorker;
  }
}

const router = createBrowserRouter(routes);

const root = document.getElementById("root");
if (!root) {
  throw new Error("Root element not found");
}

// `import.meta.env.VITE_ENABLE_MSW` はビルド時に Vite が定数置換するため、
// 比較式ごと const-fold されて本番ビルドからは MSW のコードが完全に取り除かれる。
const ENABLE_MSW = import.meta.env.VITE_ENABLE_MSW === "true";

async function enableMocking(): Promise<void> {
  if (!ENABLE_MSW) {
    return;
  }
  const { worker } = await import("./test/mocks/browser");
  await worker.start({ onUnhandledRequest: "bypass" });
  // VITE_ENABLE_MSW が立つビルドでのみ E2E 向けのフックを公開する。
  // __seedAuthToken: addInitScript で先置きしたトークンを React 起動前に適用する
  // __setAuthToken / __mswWorker: テスト中に動的に切り替えるための窓口
  if (typeof window.__seedAuthToken === "string") {
    setToken(window.__seedAuthToken);
  }
  window.__setAuthToken = setToken;
  window.__mswWorker = worker;
}

void enableMocking().then(() => {
  createRoot(root).render(
    <StrictMode>
      <GoogleOAuthProvider clientId={String(import.meta.env.VITE_GOOGLE_CLIENT_ID ?? "")}>
        <QueryClientProvider client={queryClient}>
          <ToastProvider>
            <RouterProvider router={router} />
            {import.meta.env.DEV && <ReactQueryDevtools initialIsOpen={false} />}
          </ToastProvider>
        </QueryClientProvider>
      </GoogleOAuthProvider>
    </StrictMode>,
  );
});

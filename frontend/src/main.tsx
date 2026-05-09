import { GoogleOAuthProvider } from "@react-oauth/google";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import type { http as mswHttp, HttpResponse as MswHttpResponse } from "msw";
import type { SetupWorker } from "msw/browser";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router";
import { ToastProvider } from "./components/Toast";
import "./index.css";
import { setToken } from "./lib/auth";
import { queryClient, setOnUnauthorized } from "./lib/queryClient";
import { routes } from "./routes";

declare global {
  interface Window {
    __seedAuthToken?: string;
    __setAuthToken?: (token: string) => void;
    __mswWorker?: SetupWorker;
    __mswHttp?: typeof mswHttp;
    __mswHttpResponse?: typeof MswHttpResponse;
    __queryClient?: QueryClient;
  }
}

const router = createBrowserRouter(routes);

// AuthGuard は子の query エラーでは再評価されないため、401 はグローバルに拾って遷移させる。
// トークンクリアは API クライアント側 (`lib/api/client.ts`) が責務を持つ。
// 別アカウントで再ログインしたときに前ユーザーのキャッシュ（users/me 等）が
// staleTime の間表示されないよう、遷移前にキャッシュも破棄する。
setOnUnauthorized(() => {
  queryClient.clear();
  void router.navigate("/login", { replace: true });
});

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
  const { worker, http, HttpResponse } = await import("./test/mocks/browser");
  await worker.start({ onUnhandledRequest: "bypass" });
  // E2E から MSW を制御するために window へ橋渡しする（モジュール export では
  // page.evaluate コンテキストから到達できないため）。本番ビルドではこのブロック
  // ごと dead code として落ちる。
  if (typeof window.__seedAuthToken === "string") {
    setToken(window.__seedAuthToken);
  }
  window.__setAuthToken = setToken;
  window.__mswWorker = worker;
  window.__mswHttp = http;
  window.__mswHttpResponse = HttpResponse;
  // E2E から `worker.use()` 後に手動で再フェッチを起こすために QueryClient を公開する。
  window.__queryClient = queryClient;
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

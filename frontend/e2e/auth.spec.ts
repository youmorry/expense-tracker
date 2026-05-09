import { expect, test } from "@playwright/test";

import { mockApiError, refetchQueries, seedAuthToken } from "./fixtures";

test("未認証で /analytics に直接アクセスするとログインページにリダイレクトされる", async ({
  page,
}) => {
  await page.goto("/analytics");

  await expect(page).toHaveURL(/\/login/);
  await expect(page.getByRole("heading", { name: "Expense Tracker" })).toBeVisible();
});

test("未認証で /settings に直接アクセスするとログインページにリダイレクトされる", async ({
  page,
}) => {
  await page.goto("/settings");

  await expect(page).toHaveURL(/\/login/);
  await expect(page.getByRole("heading", { name: "Expense Tracker" })).toBeVisible();
});

test("認証済み状態で API が 401 を返すとログインページにリダイレクトされる", async ({ page }) => {
  await seedAuthToken(page);
  await page.goto("/transactions");
  await expect(page.getByText(/No transactions yet/i)).toBeVisible();

  // BE がトークン期限切れで 401 を返したケースを模倣する。
  // API クライアントがトークンを破棄し、グローバルハンドラが /login に飛ばす。
  await mockApiError(page, "get", "/api/v1/transactions", 401, {
    type: "/errors/unauthorized",
    title: "Unauthorized",
    status: 401,
    detail: "Token expired",
  });
  await refetchQueries(page);

  await expect(page).toHaveURL(/\/login/);
  await expect(page.getByRole("heading", { name: "Expense Tracker" })).toBeVisible();
});

test("401 リダイレクト時に React Query のキャッシュが破棄される", async ({ page }) => {
  // 別アカウントで再ログインしたときに前ユーザーのキャッシュが残らないことの回帰テスト。
  await seedAuthToken(page);
  await page.goto("/transactions");
  await expect(page.getByText(/No transactions yet/i)).toBeVisible();

  await mockApiError(page, "get", "/api/v1/transactions", 401, {
    type: "/errors/unauthorized",
    title: "Unauthorized",
    status: 401,
    detail: "Token expired",
  });
  await refetchQueries(page);

  await expect(page).toHaveURL(/\/login/);

  const queryCount = await page.evaluate(
    () => window.__queryClient?.getQueryCache().getAll().length ?? -1,
  );
  expect(queryCount).toBe(0);
});

test("Sign in with Google ボタンを押すとログインに成功して /transactions に遷移する", async ({
  page,
}) => {
  // E2E ビルドの GoogleSignInButton はスタブで、押下時に固定 credential を返す。
  // MSW の `/api/v1/auth/google` ハンドラがモック JWT を返し、useGoogleLogin が
  // /transactions に遷移する経路を検証する。
  await page.goto("/login");
  await expect(page.getByRole("heading", { name: "Expense Tracker" })).toBeVisible();

  await page.getByRole("button", { name: /sign in with google/i }).click();

  await expect(page).toHaveURL(/\/transactions$/);
  await expect(page.getByRole("button", { name: /add transaction/i })).toBeVisible();
});

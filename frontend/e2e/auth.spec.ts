import { expect, test } from "@playwright/test";

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

import { expect, test } from "@playwright/test";

import { seedAuthToken } from "./fixtures";

test("未認証でトップページにアクセスするとログインページにリダイレクトされる", async ({ page }) => {
  await page.goto("/");
  await expect(page).toHaveURL(/\/login/);
  await expect(page.getByRole("heading", { name: "Expense Tracker" })).toBeVisible();

  // E2E ビルドでは GoogleSignInButton が GSI iframe ではなくスタブボタンを描画する。
  // ログイン手段が画面に存在することのリグレッション検知。
  await expect(page.getByRole("button", { name: /sign in with google/i })).toBeVisible();

  await page.screenshot({ path: "screenshots/login-redirect.png", fullPage: true });
});

test("認証済みで /transactions にアクセスすると一覧画面（空状態）が表示される", async ({
  page,
}) => {
  await seedAuthToken(page);

  await page.goto("/transactions");

  await expect(page.getByRole("button", { name: /add transaction/i })).toBeVisible();
  await expect(page.getByText(/No transactions yet/i)).toBeVisible();

  await page.screenshot({ path: "screenshots/transactions-empty.png", fullPage: true });
});

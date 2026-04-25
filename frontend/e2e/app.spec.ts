import { expect, test } from "@playwright/test";

import { seedAuthToken } from "./fixtures";

test("未認証でトップページにアクセスするとログインページにリダイレクトされる", async ({ page }) => {
  await page.goto("/");
  await expect(page).toHaveURL(/\/login/);
  await expect(page.getByRole("heading", { name: "Expense Tracker" })).toBeVisible();

  // GSI が描画する Google サインインボタンの iframe が表示されていること。
  // Client ID 未設定 / GSI 初期化失敗時はこの iframe が挿入されないため、
  // 「ログイン手段が画面に存在する」ことのリグレッション検知になる。
  await expect(page.locator("iframe[src*='accounts.google.com/gsi/button']")).toBeVisible();

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

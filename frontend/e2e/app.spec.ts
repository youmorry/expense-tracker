import { test, expect } from "@playwright/test";

test("未認証でトップページにアクセスするとログインページにリダイレクトされる", async ({ page }) => {
  await page.goto("/");
  await expect(page).toHaveURL(/\/login/);
  await expect(page.getByRole("heading", { name: "Login" })).toBeVisible();
});

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

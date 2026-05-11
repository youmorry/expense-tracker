import { expect, test } from "@playwright/test";

import { mockTransactionList, refetchQueries, seedAuthToken } from "./fixtures";

test("ボトムナビで Transactions ↔ Analytics ↔ Settings を行き来できる", async ({ page }) => {
  await seedAuthToken(page);
  await page.goto("/transactions");

  await expect(page.getByRole("button", { name: /add transaction/i })).toBeVisible();

  const nav = page.getByRole("navigation");
  await nav.getByRole("link", { name: "Analytics" }).click();
  await expect(page).toHaveURL(/\/analytics$/);
  await expect(page.getByRole("heading", { name: "Category Breakdown" })).toBeVisible();

  await nav.getByRole("link", { name: "Settings" }).click();
  await expect(page).toHaveURL(/\/settings$/);
  await expect(page.getByRole("heading", { name: "Settings", level: 1 })).toBeVisible();

  await nav.getByRole("link", { name: "Transactions" }).click();
  await expect(page).toHaveURL(/\/transactions$/);
  await expect(page.getByRole("button", { name: /add transaction/i })).toBeVisible();

  await page.screenshot({ path: "screenshots/bottom-nav-transactions.png" });
});

test("ルート遷移時に内側スクロール位置が先頭に戻る", async ({ page }) => {
  await seedAuthToken(page);

  const longList = Array.from({ length: 60 }, (_, i) => ({
    id: i + 1,
    date: "2026-05-01",
    amount: "1000",
    category_id: 11,
    category_name: "Uncategorized",
    need_want_type: "UNSET" as const,
    title: `Item ${String(i + 1)}`,
    created_at: "2026-05-01T00:00:00Z",
    updated_at: "2026-05-01T00:00:00Z",
  }));

  await page.goto("/transactions");
  await mockTransactionList(page, longList);
  await refetchQueries(page);

  await expect(page.getByRole("button", { name: /^Item 1\b/ })).toBeVisible();

  const main = page.getByRole("main");
  await main.evaluate((el) => {
    el.scrollTo({ top: 1000 });
  });
  const scrolledTop = await main.evaluate((el) => el.scrollTop);
  expect(scrolledTop).toBeGreaterThan(0);

  await page.getByRole("navigation").getByRole("link", { name: "Analytics" }).click();
  await expect(page).toHaveURL(/\/analytics$/);
  await expect(page.getByRole("heading", { name: "Category Breakdown" })).toBeVisible();

  const afterNavTop = await main.evaluate((el) => el.scrollTop);
  expect(afterNavTop).toBe(0);
});

import { expect, test } from "@playwright/test";

import { seedAuthToken } from "./fixtures";

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

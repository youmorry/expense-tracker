import { expect, test } from "@playwright/test";

import { seedAuthToken } from "./fixtures";

// 現状サイドナビは未実装のため、両ブレークポイントとも BottomNav が表示される。
// PC 幅でナビが消えるリグレッションを撮影で検知する。

test("モバイル幅でも BottomNav が画面下部に表示される", async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 667 });

  await seedAuthToken(page);
  await page.goto("/transactions");

  const nav = page.getByRole("navigation");
  await expect(nav).toBeVisible();
  await expect(nav.getByRole("link", { name: "Transactions" })).toBeVisible();
  await expect(nav.getByRole("link", { name: "Analytics" })).toBeVisible();
  await expect(nav.getByRole("link", { name: "Settings" })).toBeVisible();

  await page.screenshot({ path: "screenshots/responsive-mobile-bottom-nav.png" });
});

test("PC 幅でも BottomNav が画面下部に表示される", async ({ page }) => {
  await page.setViewportSize({ width: 1280, height: 800 });

  await seedAuthToken(page);
  await page.goto("/transactions");

  const nav = page.getByRole("navigation");
  await expect(nav).toBeVisible();
  await expect(nav.getByRole("link", { name: "Transactions" })).toBeVisible();
  await expect(nav.getByRole("link", { name: "Analytics" })).toBeVisible();
  await expect(nav.getByRole("link", { name: "Settings" })).toBeVisible();

  await page.screenshot({ path: "screenshots/responsive-desktop-bottom-nav.png" });
});

// Issue #324: 短いビューポートで BottomNav がページ末尾のコンテンツを覆う回帰を防ぐ。
test("短いビューポートで Settings の Delete Account ボタンが BottomNav の裏に隠れない", async ({
  page,
}) => {
  await page.setViewportSize({ width: 375, height: 493 });

  await seedAuthToken(page);
  await page.goto("/settings");

  const deleteButton = page.getByRole("button", { name: "Delete Account" });
  await deleteButton.scrollIntoViewIfNeeded();
  await expect(deleteButton).toBeVisible();

  const buttonBox = await deleteButton.boundingBox();
  const navBox = await page.getByRole("navigation").boundingBox();
  if (buttonBox === null || navBox === null) {
    throw new Error("boundingBox returned null; element is not rendered or not visible");
  }
  expect(buttonBox.y + buttonBox.height).toBeLessThanOrEqual(navBox.y);
});

import { expect, test } from "@playwright/test";

import { seedAuthToken } from "./fixtures";

// 未入力（dirty なし）状態のクローズ経路を検証する。dirty 状態の discard confirm は
// `transactions.spec.ts` の編集系テストで間接的にカバーされる前提。

test("登録モーダルは Cancel ボタンで閉じる", async ({ page }) => {
  await seedAuthToken(page);
  await page.goto("/transactions");

  await page.getByRole("button", { name: "Add transaction" }).click();
  const dialog = page.getByRole("dialog");
  await expect(dialog).toBeVisible();

  await dialog.getByRole("button", { name: "Cancel" }).click();

  await expect(dialog).toBeHidden();
});

test("登録モーダルは ESC キーで閉じる", async ({ page }) => {
  await seedAuthToken(page);
  await page.goto("/transactions");

  await page.getByRole("button", { name: "Add transaction" }).click();
  const dialog = page.getByRole("dialog");
  await expect(dialog).toBeVisible();

  await page.keyboard.press("Escape");

  await expect(dialog).toBeHidden();
});

test("登録モーダルは右上の Close ボタンで閉じる", async ({ page }) => {
  // Radix Dialog のオーバーレイクリックは Pointer Events Capture の関係で Playwright
  // から再現が不安定なため、`DialogContent` 内の Close ボタン（X アイコン）でカバーする。
  // 「ユーザーが本文外の手段でモーダルを閉じられる」リグレッション検知としては同等。
  await seedAuthToken(page);
  await page.goto("/transactions");

  await page.getByRole("button", { name: "Add transaction" }).click();
  const dialog = page.getByRole("dialog");
  await expect(dialog).toBeVisible();

  await dialog.getByRole("button", { name: "Close" }).click();

  await expect(dialog).toBeHidden();
});

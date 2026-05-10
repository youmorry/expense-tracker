import { expect, test } from "@playwright/test";

import { mockTransactionList, refetchQueries, seedAuthToken } from "./fixtures";

test("支出を削除すると Transaction deleted トーストが表示され、3 秒後に自動で消える", async ({
  page,
}) => {
  const movie = {
    id: 99,
    date: "2026-05-07",
    amount: "1500",
    category_id: 6,
    category_name: "Entertainment",
    need_want_type: "WANT" as const,
    title: "映画",
    created_at: "2026-05-07T19:00:00Z",
    updated_at: "2026-05-07T19:00:00Z",
  };

  await seedAuthToken(page);
  await page.goto("/transactions");
  await expect(page.getByText(/No transactions yet/i)).toBeVisible();

  await mockTransactionList(page, [movie]);
  await refetchQueries(page);

  await page.getByRole("button", { name: /映画/ }).click();

  const dialog = page.getByRole("dialog");
  await expect(dialog.getByRole("heading", { name: "Edit Transaction" })).toBeVisible();
  await dialog.getByRole("button", { name: "Delete" }).click();

  const confirm = page.getByRole("alertdialog");
  await expect(confirm).toBeVisible();

  // 削除確定後の一覧 refetch で空が返るよう、Confirm 押下前に差し替える。
  await mockTransactionList(page, []);
  await confirm.getByRole("button", { name: "Delete" }).click();

  // Radix Toast はアクセシビリティ用の live region として `<span role="status">`
  // を別途レンダリングし、そこにも本文が含まれる。可視のトースト本体だけを検証するため
  // `data-slot="toast-description"` で要素を絞り込む。
  const toast = page.locator('[data-slot="toast-description"]', { hasText: "Transaction deleted" });
  await expect(toast).toBeVisible();

  await page.screenshot({ path: "screenshots/toast-success.png" });

  // SUCCESS_DURATION_MS = 3000 + Radix のフェードアニメーション分の余裕を見る。
  await expect(toast).toBeHidden({ timeout: 6000 });
});

import { expect, test } from "@playwright/test";

import { mockTransactionList, seedAuthToken } from "./fixtures";

test("支出を新規登録するとモーダルが閉じて一覧に反映される", async ({ page }) => {
  await seedAuthToken(page);
  await page.goto("/transactions");

  await expect(page.getByText(/No transactions yet/i)).toBeVisible();

  await page.getByRole("button", { name: "Add transaction" }).click();
  const dialog = page.getByRole("dialog");
  await expect(dialog).toBeVisible();
  await expect(dialog.getByRole("heading", { name: "Add transaction" })).toBeVisible();

  await dialog.getByLabel("Date").fill("2026-05-07");
  await dialog.getByLabel("Amount").fill("1200");
  await dialog.getByLabel("Category").selectOption({ label: "Food" });
  await dialog.getByRole("radio", { name: "NEED" }).click();
  await dialog.getByLabel("Title").fill("ランチ");
  await dialog.getByLabel("Memo").fill("同僚と渋谷のイタリアンへ");

  // 登録後の一覧 refetch で新規項目が返るよう、Save 押下前にハンドラを差し替える。
  await mockTransactionList(page, [
    {
      id: 1,
      date: "2026-05-07",
      amount: "1200",
      category_id: 1,
      category_name: "Food",
      need_want_type: "NEED",
      title: "ランチ",
      memo: "同僚と渋谷のイタリアンへ",
      created_at: "2026-05-07T12:00:00Z",
      updated_at: "2026-05-07T12:00:00Z",
    },
  ]);

  await dialog.getByRole("button", { name: "Save" }).click();

  await expect(dialog).toBeHidden();
  await expect(page.getByText("ランチ")).toBeVisible();
  await expect(page.getByText("NEED")).toBeVisible();

  await page.screenshot({ path: "screenshots/transaction-create-success.png" });
});

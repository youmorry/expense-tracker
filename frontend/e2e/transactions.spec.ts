import { expect, test } from "@playwright/test";

import { mockTransactionList, refetchQueries, seedAuthToken } from "./fixtures";

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

test("既存の支出をクリックして編集すると一覧の表示が更新される", async ({ page }) => {
  const lunch = {
    id: 42,
    date: "2026-05-07",
    amount: "1200",
    category_id: 1,
    category_name: "Food",
    need_want_type: "NEED" as const,
    title: "ランチ",
    memo: "同僚と渋谷のイタリアンへ",
    created_at: "2026-05-07T12:00:00Z",
    updated_at: "2026-05-07T12:00:00Z",
  };

  await seedAuthToken(page);
  await page.goto("/transactions");
  await expect(page.getByText(/No transactions yet/i)).toBeVisible();

  await mockTransactionList(page, [lunch]);
  await refetchQueries(page);

  await page.getByRole("button", { name: /ランチ/ }).click();

  const dialog = page.getByRole("dialog");
  await expect(dialog).toBeVisible();
  await expect(dialog.getByRole("heading", { name: "Edit Transaction" })).toBeVisible();
  await expect(dialog.getByLabel("Title")).toHaveValue("ランチ");

  await dialog.getByLabel("Title").fill("ディナー");

  // 更新後の一覧 refetch で変更後のタイトルが返るよう、Save 押下前にハンドラを差し替える。
  await mockTransactionList(page, [
    { ...lunch, title: "ディナー", updated_at: "2026-05-07T13:00:00Z" },
  ]);

  await dialog.getByRole("button", { name: "Save" }).click();

  await expect(dialog).toBeHidden();
  await expect(page.getByText("ディナー")).toBeVisible();
  await expect(page.getByText("ランチ")).toBeHidden();

  await page.screenshot({ path: "screenshots/transaction-edit-success.png" });
});

test("既存の支出を編集モーダルから削除すると一覧が空に戻る", async ({ page }) => {
  await seedAuthToken(page);
  await page.goto("/transactions");
  await expect(page.getByText(/No transactions yet/i)).toBeVisible();

  await mockTransactionList(page, [
    {
      id: 99,
      date: "2026-05-07",
      amount: "1500",
      category_id: 6,
      category_name: "Entertainment",
      need_want_type: "WANT",
      title: "映画",
      created_at: "2026-05-07T19:00:00Z",
      updated_at: "2026-05-07T19:00:00Z",
    },
  ]);
  await refetchQueries(page);

  await page.getByRole("button", { name: /映画/ }).click();

  const dialog = page.getByRole("dialog");
  await expect(dialog.getByRole("heading", { name: "Edit Transaction" })).toBeVisible();

  await dialog.getByRole("button", { name: "Delete" }).click();

  const confirm = page.getByRole("alertdialog");
  await expect(confirm).toBeVisible();
  await expect(confirm.getByRole("heading", { name: "Delete this transaction?" })).toBeVisible();

  // 削除確定後の一覧 refetch で空が返るよう、Confirm 押下前にハンドラを差し替える。
  await mockTransactionList(page, []);

  await confirm.getByRole("button", { name: "Delete" }).click();

  await expect(dialog).toBeHidden();
  await expect(confirm).toBeHidden();
  await expect(page.getByText(/No transactions yet/i)).toBeVisible();
  await expect(page.getByText("映画")).toBeHidden();

  await page.screenshot({ path: "screenshots/transaction-delete-success.png" });
});

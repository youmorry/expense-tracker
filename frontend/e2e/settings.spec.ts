import { expect, test } from "@playwright/test";

import { mockTransactionList, refetchQueries, seedAuthToken } from "./fixtures";

test("設定画面で通貨を切り替えると一覧の通貨表記が更新される", async ({ page }) => {
  await seedAuthToken(page);

  // useCurrency は localStorage に保存するため、テスト独立性のため明示的にクリアする。
  await page.addInitScript(() => {
    localStorage.removeItem("expense-tracker:currency");
  });

  await page.goto("/transactions");
  await expect(page.getByText(/No transactions yet/i)).toBeVisible();

  await mockTransactionList(page, [
    {
      id: 1,
      date: "2026-05-07",
      amount: "1234",
      category_id: 1,
      category_name: "Food",
      need_want_type: "NEED",
      title: "コーヒー",
      created_at: "2026-05-07T08:00:00Z",
      updated_at: "2026-05-07T08:00:00Z",
    },
  ]);
  await refetchQueries(page);

  // 既定では navigator.language（en-US）由来の USD 表示になる。
  await expect(page.getByText("$1,234.00")).toBeVisible();

  await page.getByRole("navigation").getByRole("link", { name: "Settings" }).click();
  await expect(page).toHaveURL(/\/settings$/);

  await page.getByRole("button", { name: /^Currency/ }).click();

  const picker = page.getByRole("dialog", { name: "Select currency" });
  await expect(picker).toBeVisible();
  await picker.getByRole("radio", { name: /^JPY/ }).click();

  const confirm = page.getByRole("alertdialog");
  await expect(confirm).toBeVisible();
  await expect(confirm.getByRole("heading", { name: "Change display currency?" })).toBeVisible();
  await confirm.getByRole("button", { name: "Change" }).click();

  await expect(picker).toBeHidden();
  await page.screenshot({ path: "screenshots/settings-currency-jpy.png" });

  await page.getByRole("navigation").getByRole("link", { name: "Transactions" }).click();
  await expect(page).toHaveURL(/\/transactions$/);

  // JPY は端数なしで表示される。
  await expect(page.getByText("¥1,234")).toBeVisible();
  await expect(page.getByText("$1,234.00")).toBeHidden();

  await page.screenshot({ path: "screenshots/transactions-currency-jpy.png" });
});

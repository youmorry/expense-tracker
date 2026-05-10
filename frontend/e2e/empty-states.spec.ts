import { expect, test } from "@playwright/test";

import {
  mockCategoryAnalytics,
  mockNeedWantAnalytics,
  refetchQueries,
  seedAuthToken,
} from "./fixtures";

test("分析画面で集計対象が空のときカテゴリ・Need/Want の両方に空状態が表示される", async ({
  page,
}) => {
  await seedAuthToken(page);
  await page.goto("/analytics");

  await expect(page.getByRole("heading", { name: "Category Breakdown" })).toBeVisible();

  // 全 segment が transaction_count=0 のとき NeedWantRatio は EmptyState を出す。
  await Promise.all([
    mockCategoryAnalytics(page, { total_amount: "0", categories: [] }),
    mockNeedWantAnalytics(page, {
      total_amount: "0",
      breakdown: [
        { type: "NEED", amount: "0", percentage: 0, transaction_count: 0 },
        { type: "WANT", amount: "0", percentage: 0, transaction_count: 0 },
        { type: "UNSET", amount: "0", percentage: 0, transaction_count: 0 },
      ],
    }),
  ]);
  await refetchQueries(page);

  const categorySection = page.getByRole("region", { name: "Category Breakdown" });
  await expect(categorySection.getByText("No data for this period.")).toBeVisible();

  const needWantSection = page.getByRole("region", { name: "Need / Want Ratio" });
  await expect(needWantSection.getByText("No data for this period.")).toBeVisible();

  // recharts は空状態時に描画されないが、画面下部に分析以外のレイアウトが入る可能性に
  // 備え、要素単位で撮影して fullPage の事故を避ける。
  await categorySection.screenshot({ path: "screenshots/analytics-empty-category.png" });
  await needWantSection.screenshot({ path: "screenshots/analytics-empty-need-want.png" });
});

test("設定画面で初期状態（Account / Preferences / Danger Zone）が表示される", async ({ page }) => {
  await seedAuthToken(page);
  await page.goto("/settings");

  await expect(page.getByRole("heading", { name: "Settings", level: 1 })).toBeVisible();

  const account = page.getByRole("region", { name: "Account" });
  await expect(account.getByText("test@example.com")).toBeVisible();
  await expect(account.getByText("Test User")).toBeVisible();

  const preferences = page.getByRole("region", { name: "Preferences" });
  await expect(preferences.getByRole("button", { name: /^Currency/ })).toBeVisible();

  const dangerZone = page.getByRole("region", { name: "Danger Zone" });
  await expect(dangerZone.getByRole("button", { name: "Log out" })).toBeVisible();
  await expect(dangerZone.getByRole("button", { name: "Delete Account" })).toBeVisible();

  await page.screenshot({ path: "screenshots/settings-initial.png" });
});

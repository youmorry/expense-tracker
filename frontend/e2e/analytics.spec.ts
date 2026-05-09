import { expect, test } from "@playwright/test";

import {
  mockCategoryAnalytics,
  mockNeedWantAnalytics,
  refetchQueries,
  seedAuthToken,
} from "./fixtures";

test("分析画面でカテゴリ内訳と Need/Want 比率が描画される", async ({ page }) => {
  await seedAuthToken(page);
  await page.goto("/analytics");

  // enableMocking が非同期で window フックを公開するため、初期描画を待ってから
  // worker.use() を呼ばないと差し替えが間に合わない。
  await expect(page.getByRole("heading", { name: "Category Breakdown" })).toBeVisible();

  await mockCategoryAnalytics(page, {
    total_amount: "13000",
    categories: [
      {
        category_id: 1,
        category_name: "Food",
        amount: "8000",
        percentage: 61.5,
        transaction_count: 12,
      },
      {
        category_id: 2,
        category_name: "Transport",
        amount: "5000",
        percentage: 38.5,
        transaction_count: 4,
      },
    ],
  });
  await mockNeedWantAnalytics(page, {
    total_amount: "13000",
    breakdown: [
      { type: "NEED", amount: "8000", percentage: 61.5, transaction_count: 12 },
      { type: "WANT", amount: "4000", percentage: 30.8, transaction_count: 3 },
      { type: "UNSET", amount: "1000", percentage: 7.7, transaction_count: 1 },
    ],
  });
  await refetchQueries(page);

  const categorySection = page.getByRole("region", { name: "Category Breakdown" });
  await expect(categorySection).toBeVisible();
  await expect(categorySection.getByText("Food")).toBeVisible();
  await expect(categorySection.getByText("Transport")).toBeVisible();
  await expect(categorySection.getByText("61.5%")).toBeVisible();

  const needWantSection = page.getByRole("region", { name: "Need / Want Ratio" });
  await expect(needWantSection).toBeVisible();
  await expect(needWantSection.getByLabel("NEED")).toBeVisible();
  await expect(needWantSection.getByLabel("WANT")).toBeVisible();
  await expect(needWantSection.getByLabel("UNSET")).toBeVisible();
  await expect(needWantSection.getByRole("note")).toContainText("1 transaction unset");

  // recharts の Pie は ResizeObserver で 2 段階に描画され、さらに各 sector が
  // アニメーションで描画されるため、全 sector が出揃うまで待ってから撮影する。
  await expect(categorySection.locator(".recharts-pie-sector")).toHaveCount(2);
  await page.waitForTimeout(1600);

  // recharts を含むため fullPage は避け、各セクションを要素単位で撮影する。
  await categorySection.screenshot({ path: "screenshots/analytics-category.png" });
  await needWantSection.screenshot({ path: "screenshots/analytics-need-want.png" });
});

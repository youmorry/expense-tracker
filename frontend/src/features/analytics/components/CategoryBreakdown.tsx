/**
 * カテゴリ別支出のドーナツチャートと内訳リスト。
 *
 * 入力 `data.categories` は API が金額降順でソート済み・金額 0 を除外済み。
 * 描画順は API レスポンスの順序をそのまま尊重する。
 *
 * @see docs/03-design/frontend/screen-flow.md
 */

import { Pie, PieChart, ResponsiveContainer } from "recharts";

import { EmptyState } from "../../../components/EmptyState";
import { useCurrency } from "../../../hooks/useCurrency";
import { getCategoryEmoji } from "../../../lib/category-emoji";
import type { CategoryAnalytics } from "../types";

interface CategoryBreakdownProps {
  data: CategoryAnalytics;
}

const CHART_COLOR_VARS = [
  "var(--chart-1)",
  "var(--chart-2)",
  "var(--chart-3)",
  "var(--chart-4)",
  "var(--chart-5)",
  "var(--chart-6)",
  "var(--chart-7)",
  "var(--chart-8)",
  "var(--chart-9)",
  "var(--chart-10)",
  "var(--chart-11)",
];

const PERCENT_FORMATTER = new Intl.NumberFormat("en-US", {
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
});

export function CategoryBreakdown({ data }: CategoryBreakdownProps) {
  const { formatAmount } = useCurrency();

  if (data.categories.length === 0) {
    return <EmptyState title="No data for this period." />;
  }

  const items = data.categories.map((category, index) => ({
    ...category,
    color: CHART_COLOR_VARS[index % CHART_COLOR_VARS.length],
  }));

  const chartData = items.map((item) => ({
    name: item.categoryName,
    value: Number(item.amount),
    fill: item.color,
  }));

  return (
    <div className="flex flex-col gap-4">
      <div className="h-56 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={chartData}
              dataKey="value"
              nameKey="name"
              cx="50%"
              cy="50%"
              innerRadius="55%"
              outerRadius="85%"
              paddingAngle={2}
              stroke="var(--background)"
            />
          </PieChart>
        </ResponsiveContainer>
      </div>
      <ul className="divide-border divide-y">
        {items.map((item) => {
          const emoji = getCategoryEmoji(item.categoryName);
          const amountNumber = Number(item.amount);
          return (
            <li key={item.categoryId} className="flex items-center gap-3 px-4 py-3 text-sm">
              <span
                className="size-3 shrink-0 rounded-full"
                style={{ backgroundColor: item.color }}
                aria-hidden="true"
              />
              <span className="text-xl" aria-hidden="true">
                {emoji}
              </span>
              <span className="text-foreground flex-1 truncate">{item.categoryName}</span>
              <span className="text-foreground font-medium">{formatAmount(amountNumber)}</span>
              <span className="text-muted-foreground w-12 text-right">
                {PERCENT_FORMATTER.format(item.percentage)}%
              </span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}

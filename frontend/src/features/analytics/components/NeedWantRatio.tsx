/**
 * NEED/WANT/UNSET の支出比率を横棒グラフ（プログレスバー）と内訳で表示する。
 *
 * API レスポンスの順序は仕様で固定されていないため、表示順は FE 側で
 * NEED → WANT → UNSET に揃える。
 *
 * @see docs/03-design/frontend/screen-flow.md
 */

import { EmptyState } from "../../../components/EmptyState";
import { useCurrency } from "../../../hooks/useCurrency";
import type { NeedWantType } from "../../../types/api";
import type { NeedWantAnalytics, NeedWantBreakdownItem } from "../types";

interface NeedWantRatioProps {
  data: NeedWantAnalytics;
}

const SEGMENT_ORDER: readonly NeedWantType[] = ["NEED", "WANT", "UNSET"];

// 既存の Category Breakdown と異なる色相を採用し、両セクションの視覚的衝突を避ける。
const SEGMENT_COLOR_VARS: Record<NeedWantType, string> = {
  NEED: "var(--chart-6)",
  WANT: "var(--chart-3)",
  UNSET: "var(--muted-foreground)",
};

const PERCENT_FORMATTER = new Intl.NumberFormat("en-US", {
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
});

function findSegment(
  breakdown: NeedWantBreakdownItem[],
  type: NeedWantType,
): NeedWantBreakdownItem {
  return (
    breakdown.find((item) => item.type === type) ?? {
      type,
      amount: "0",
      percentage: 0,
      transactionCount: 0,
    }
  );
}

export function NeedWantRatio({ data }: NeedWantRatioProps) {
  const { formatAmount } = useCurrency();

  if (Number(data.totalAmount) === 0) {
    return <EmptyState title="No data for this period." />;
  }

  const segments = SEGMENT_ORDER.map((type) => findSegment(data.breakdown, type));
  const unsetCount = segments.find((s) => s.type === "UNSET")?.transactionCount ?? 0;

  return (
    <div className="flex flex-col gap-3">
      <ul className="flex flex-col gap-2">
        {segments.map((segment) => {
          const color = SEGMENT_COLOR_VARS[segment.type];
          const amountNumber = Number(segment.amount);
          return (
            <li
              key={segment.type}
              aria-label={segment.type}
              className="flex items-center gap-3 px-1 py-1 text-sm"
            >
              <span className="text-foreground w-14 font-medium">{segment.type}</span>
              <div className="bg-muted relative h-2 flex-1 overflow-hidden rounded-full">
                <div
                  className="h-full rounded-full"
                  style={{
                    width: `${segment.percentage.toString()}%`,
                    backgroundColor: color,
                  }}
                />
              </div>
              <span className="text-foreground w-20 text-right font-medium">
                {formatAmount(amountNumber)}
              </span>
              <span className="text-muted-foreground w-12 text-right">
                {PERCENT_FORMATTER.format(segment.percentage)}%
              </span>
            </li>
          );
        })}
      </ul>
      {unsetCount > 0 && (
        <p className="text-muted-foreground px-1 text-xs" role="note">
          <span aria-hidden="true">⚠ </span>
          {unsetCount === 1
            ? "1 transaction unset — review needed"
            : `${unsetCount.toString()} transactions unset — review needed`}
        </p>
      )}
    </div>
  );
}

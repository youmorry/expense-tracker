import { useMemo, useState } from "react";

import {
  defaultPeriodSelectorValue,
  periodFromValue,
  type PeriodSelectorValue,
} from "../../../components/period";
import { PeriodSelector } from "../../../components/PeriodSelector";
import { Spinner } from "../../../components/ui/spinner";
import { useCategoryAnalytics } from "../api/useCategoryAnalytics";
import { CategoryBreakdown } from "./CategoryBreakdown";

export default function AnalyticsPage() {
  const [periodValue, setPeriodValue] = useState<PeriodSelectorValue>(defaultPeriodSelectorValue);
  const period = useMemo(() => periodFromValue(periodValue), [periodValue]);
  const { data } = useCategoryAnalytics(period);

  return (
    <div className="relative min-h-screen">
      <div className="border-border bg-background sticky top-0 z-10 flex flex-col gap-2 border-b px-4 py-3">
        <PeriodSelector value={periodValue} onChange={setPeriodValue} />
      </div>
      <section aria-label="Category Breakdown" className="px-4 py-4">
        <h2 className="text-foreground mb-3 text-base font-semibold">Category Breakdown</h2>
        {data === undefined ? (
          <div className="flex justify-center py-12">
            <Spinner aria-label="Loading category analytics" />
          </div>
        ) : (
          <CategoryBreakdown data={data} />
        )}
      </section>
    </div>
  );
}

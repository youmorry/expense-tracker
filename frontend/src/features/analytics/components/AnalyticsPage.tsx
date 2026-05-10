import { useMemo, useState } from "react";

import {
  defaultPeriodSelectorValue,
  periodFromValue,
  type PeriodSelectorValue,
} from "../../../components/period";
import { PeriodSelector } from "../../../components/PeriodSelector";
import { Spinner } from "../../../components/ui/spinner";
import { useCategoryAnalytics } from "../api/useCategoryAnalytics";
import { useNeedWantAnalytics } from "../api/useNeedWantAnalytics";
import { CategoryBreakdown } from "./CategoryBreakdown";
import { NeedWantRatio } from "./NeedWantRatio";

export default function AnalyticsPage() {
  const [periodValue, setPeriodValue] = useState<PeriodSelectorValue>(defaultPeriodSelectorValue);
  const period = useMemo(() => periodFromValue(periodValue), [periodValue]);
  const { data: categoryData } = useCategoryAnalytics(period);
  const { data: needWantData } = useNeedWantAnalytics(period);

  return (
    <div className="relative">
      <div className="border-border bg-background sticky top-0 z-10 flex flex-col gap-2 border-b px-4 py-3">
        <PeriodSelector value={periodValue} onChange={setPeriodValue} />
      </div>
      <section aria-label="Category Breakdown" className="px-4 py-4">
        <h2 className="text-foreground mb-3 text-base font-semibold">Category Breakdown</h2>
        {categoryData === undefined ? (
          <div className="flex justify-center py-12">
            <Spinner aria-label="Loading category analytics" />
          </div>
        ) : (
          <CategoryBreakdown data={categoryData} />
        )}
      </section>
      <section aria-label="Need / Want Ratio" className="px-4 py-4">
        <h2 className="text-foreground mb-3 text-base font-semibold">Need / Want Ratio</h2>
        {needWantData === undefined ? (
          <div className="flex justify-center py-12">
            <Spinner aria-label="Loading need/want analytics" />
          </div>
        ) : (
          <NeedWantRatio data={needWantData} />
        )}
      </section>
    </div>
  );
}

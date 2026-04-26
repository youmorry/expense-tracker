import { Plus } from "lucide-react";
import { useMemo, useState } from "react";

import { Button } from "@/components/ui/button";

import {
  defaultPeriodSelectorValue,
  type Period,
  periodFromValue,
  type PeriodSelectorValue,
} from "../../../components/period";
import { PeriodSelector } from "../../../components/PeriodSelector";
import { Spinner } from "../../../components/ui/spinner";
import { useTransactions } from "../api/useTransactions";
import { TransactionList } from "./TransactionList";

function toParams(period: Period) {
  if (period === null) return {};
  return { from: period.from, to: period.to };
}

export default function TransactionsPage() {
  const [periodValue, setPeriodValue] = useState<PeriodSelectorValue>(defaultPeriodSelectorValue);
  const period = useMemo(() => periodFromValue(periodValue), [periodValue]);
  const { data } = useTransactions(toParams(period));

  return (
    <div className="relative min-h-screen">
      <div className="border-border bg-background sticky top-0 z-10 border-b px-4 py-3">
        <PeriodSelector value={periodValue} onChange={setPeriodValue} />
      </div>
      {data === undefined ? (
        <div className="flex justify-center py-12">
          <Spinner aria-label="Loading transactions" />
        </div>
      ) : (
        <TransactionList transactions={data.items} />
      )}
      <Button
        type="button"
        size="icon"
        aria-label="Add transaction"
        className="fixed right-6 bottom-20 size-14 rounded-full shadow-lg [&_svg:not([class*='size-'])]:size-6"
      >
        <Plus />
      </Button>
    </div>
  );
}

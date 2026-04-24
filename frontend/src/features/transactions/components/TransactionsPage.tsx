import { Plus } from "lucide-react";
import { useState } from "react";

import { PeriodSelector, type Period } from "../../../components/PeriodSelector";
import { Spinner } from "../../../components/ui/spinner";
import { useTransactions } from "../api/useTransactions";
import { TransactionList } from "./TransactionList";

function toParams(period: Period) {
  if (period === null) return {};
  return { from: period.from, to: period.to };
}

export default function TransactionsPage() {
  const [period, setPeriod] = useState<Period | undefined>(undefined);
  const { data, isLoading } = useTransactions(period === undefined ? {} : toParams(period), {
    enabled: period !== undefined,
  });

  return (
    <div className="relative min-h-screen">
      <div className="sticky top-0 z-10 border-b border-gray-200 bg-white px-4 py-3">
        <PeriodSelector onChange={setPeriod} />
      </div>
      {isLoading || data === undefined ? (
        <div className="flex justify-center py-12">
          <Spinner aria-label="Loading transactions" />
        </div>
      ) : (
        <TransactionList transactions={data.items} />
      )}
      <button
        type="button"
        aria-label="Add transaction"
        className="fixed right-6 bottom-20 inline-flex h-14 w-14 items-center justify-center rounded-full bg-blue-600 text-white shadow-lg hover:bg-blue-700"
      >
        <Plus className="h-6 w-6" />
      </button>
    </div>
  );
}

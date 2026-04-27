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
import { useCategories } from "../../categories/api/useCategories";
import { useTransactions } from "../api/useTransactions";
import type { TransactionListParams } from "../types";
import {
  emptyTransactionFiltersValue,
  TransactionFilters,
  type TransactionFiltersValue,
} from "./TransactionFilterPanel";
import { TransactionList } from "./TransactionList";

const FILTERED_EMPTY_MESSAGE = "No transactions match your filters.";

function buildParams(period: Period, filters: TransactionFiltersValue): TransactionListParams {
  const params: TransactionListParams = {};
  if (period !== null) {
    params.from = period.from;
    params.to = period.to;
  }
  if (filters.categoryIds.length > 0) params.categoryIds = filters.categoryIds;
  if (filters.needWantType !== null) params.needWantType = filters.needWantType;
  const trimmedKeyword = filters.keyword.trim();
  if (trimmedKeyword.length > 0) params.keyword = trimmedKeyword;
  return params;
}

function isAnyFilterActive(filters: TransactionFiltersValue): boolean {
  return (
    filters.keyword.trim().length > 0 ||
    filters.categoryIds.length > 0 ||
    filters.needWantType !== null
  );
}

export default function TransactionsPage() {
  const [periodValue, setPeriodValue] = useState<PeriodSelectorValue>(defaultPeriodSelectorValue);
  const [filters, setFilters] = useState<TransactionFiltersValue>(emptyTransactionFiltersValue);
  const period = useMemo(() => periodFromValue(periodValue), [periodValue]);
  const params = useMemo(() => buildParams(period, filters), [period, filters]);
  const { data } = useTransactions(params);
  const { data: categoriesData } = useCategories();

  return (
    <div className="relative min-h-screen">
      <div className="border-border bg-background sticky top-0 z-10 flex flex-col gap-2 border-b px-4 py-3">
        <PeriodSelector value={periodValue} onChange={setPeriodValue} />
        <TransactionFilters
          value={filters}
          onChange={setFilters}
          categories={categoriesData?.items ?? []}
        />
      </div>
      {data === undefined ? (
        <div className="flex justify-center py-12">
          <Spinner aria-label="Loading transactions" />
        </div>
      ) : isAnyFilterActive(filters) ? (
        <TransactionList transactions={data.items} emptyMessage={FILTERED_EMPTY_MESSAGE} />
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

import { getCategoryEmoji } from "../../../lib/category-emoji";
import { useCurrency } from "../../../hooks/useCurrency";
import type { Transaction } from "../types";

interface TransactionItemProps {
  transaction: Transaction;
}

export function TransactionItem({ transaction }: TransactionItemProps) {
  const { formatAmount } = useCurrency();
  const emoji = getCategoryEmoji(transaction.categoryName);
  const displayTitle =
    transaction.title !== undefined && transaction.title.length > 0
      ? transaction.title
      : transaction.categoryName;
  const amountNumber = Number(transaction.amount);

  return (
    <div className="flex items-center gap-3 px-4 py-3">
      <span className="text-xl" aria-hidden="true">
        {emoji}
      </span>
      <span className="flex-1 truncate text-sm text-gray-900">{displayTitle}</span>
      <span className="text-sm font-medium text-gray-900">{formatAmount(amountNumber)}</span>
      {transaction.needWantType !== "UNSET" && (
        <span
          className={`rounded px-2 py-0.5 text-xs font-medium ${
            transaction.needWantType === "NEED"
              ? "bg-blue-100 text-blue-700"
              : "bg-amber-100 text-amber-700"
          }`}
        >
          {transaction.needWantType}
        </span>
      )}
    </div>
  );
}

import { Badge } from "@/components/ui/badge";

import { useCurrency } from "../../../hooks/useCurrency";
import { getCategoryEmoji } from "../../../lib/category-emoji";
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
      <span className="text-foreground flex-1 truncate text-sm">{displayTitle}</span>
      <span className="text-foreground text-sm font-medium">{formatAmount(amountNumber)}</span>
      {transaction.needWantType !== "UNSET" && (
        <Badge variant={transaction.needWantType === "NEED" ? "secondary" : "outline"}>
          {transaction.needWantType}
        </Badge>
      )}
    </div>
  );
}

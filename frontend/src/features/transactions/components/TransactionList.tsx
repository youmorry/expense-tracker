import { EmptyState } from "../../../components/EmptyState";
import type { Transaction } from "../types";
import { TransactionItem } from "./TransactionItem";

interface TransactionListProps {
  transactions: Transaction[];
}

const MONTH_DAY_FORMATTER = new Intl.DateTimeFormat("en-US", {
  month: "short",
  day: "numeric",
  timeZone: "UTC",
});
const WEEKDAY_FORMATTER = new Intl.DateTimeFormat("en-US", {
  weekday: "short",
  timeZone: "UTC",
});

function formatDateHeader(isoDate: string): string {
  const date = new Date(`${isoDate}T00:00:00Z`);
  return `${MONTH_DAY_FORMATTER.format(date)}, ${WEEKDAY_FORMATTER.format(date)}`;
}

function groupByDate(transactions: Transaction[]): { date: string; items: Transaction[] }[] {
  const groups: { date: string; items: Transaction[] }[] = [];
  for (const transaction of transactions) {
    const last = groups[groups.length - 1];
    if (last && last.date === transaction.date) {
      last.items.push(transaction);
    } else {
      groups.push({ date: transaction.date, items: [transaction] });
    }
  }
  return groups;
}

export function TransactionList({ transactions }: TransactionListProps) {
  if (transactions.length === 0) {
    return <EmptyState title="No transactions yet. Tap + to add your first one!" />;
  }

  const groups = groupByDate(transactions);

  return (
    <div className="divide-y divide-gray-100">
      {groups.map((group) => {
        const heading = formatDateHeader(group.date);
        return (
          <section key={group.date} role="group" aria-label={heading}>
            <h2 className="bg-gray-50 px-4 py-2 text-xs font-medium text-gray-500">{heading}</h2>
            <ul className="divide-y divide-gray-100">
              {group.items.map((item) => (
                <li key={item.id}>
                  <TransactionItem transaction={item} />
                </li>
              ))}
            </ul>
          </section>
        );
      })}
    </div>
  );
}

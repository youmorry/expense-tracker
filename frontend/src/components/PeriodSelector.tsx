import { ChevronLeft, ChevronRight } from "lucide-react";
import { useEffect, useRef, useState } from "react";

type Unit = "month" | "year" | "all";

export type Period = { from: string; to: string } | null;

interface PeriodSelectorProps {
  onChange: (period: Period) => void;
}

const MONTH_FORMATTER = new Intl.DateTimeFormat("en-US", { month: "long", year: "numeric" });

function pad2(n: number) {
  return n.toString().padStart(2, "0");
}

function formatDate(year: number, month: number, day: number): string {
  return `${year.toString().padStart(4, "0")}-${pad2(month)}-${pad2(day)}`;
}

function lastDayOfMonth(year: number, month: number): number {
  return new Date(year, month, 0).getDate();
}

function computePeriod(unit: Unit, anchor: Date): Period {
  if (unit === "all") return null;
  const year = anchor.getFullYear();
  if (unit === "year") {
    return { from: formatDate(year, 1, 1), to: formatDate(year, 12, 31) };
  }
  const month = anchor.getMonth() + 1;
  return {
    from: formatDate(year, month, 1),
    to: formatDate(year, month, lastDayOfMonth(year, month)),
  };
}

function formatLabel(unit: Unit, anchor: Date): string {
  if (unit === "all") return "All Transactions";
  if (unit === "year") return anchor.getFullYear().toString();
  return MONTH_FORMATTER.format(anchor);
}

function shiftAnchor(unit: Unit, anchor: Date, direction: -1 | 1): Date {
  const next = new Date(anchor);
  next.setDate(1);
  if (unit === "year") {
    next.setFullYear(next.getFullYear() + direction);
  } else {
    next.setMonth(next.getMonth() + direction);
  }
  return next;
}

export function PeriodSelector({ onChange }: PeriodSelectorProps) {
  const [unit, setUnit] = useState<Unit>("month");
  const [anchor, setAnchor] = useState<Date>(() => new Date());

  const onChangeRef = useRef(onChange);
  useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);

  useEffect(() => {
    onChangeRef.current(computePeriod(unit, anchor));
  }, [unit, anchor]);

  const handleUnitChange = (nextUnit: Unit) => {
    if (nextUnit === unit) return;
    setUnit(nextUnit);
    // 単位切替時は現在日時にリセット（「All → Month」で当月が戻るため）
    setAnchor(new Date());
  };

  const showNav = unit !== "all";

  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-center justify-center gap-2">
        {showNav && (
          <button
            type="button"
            aria-label="Previous period"
            onClick={() => {
              setAnchor((prev) => shiftAnchor(unit, prev, -1));
            }}
            className="inline-flex h-8 w-8 items-center justify-center rounded text-gray-600 hover:bg-gray-100"
          >
            <ChevronLeft className="h-5 w-5" />
          </button>
        )}
        <span className="min-w-40 text-center text-base font-medium">
          {formatLabel(unit, anchor)}
        </span>
        {showNav && (
          <button
            type="button"
            aria-label="Next period"
            onClick={() => {
              setAnchor((prev) => shiftAnchor(unit, prev, 1));
            }}
            className="inline-flex h-8 w-8 items-center justify-center rounded text-gray-600 hover:bg-gray-100"
          >
            <ChevronRight className="h-5 w-5" />
          </button>
        )}
      </div>
      <div role="group" className="flex justify-center gap-1 text-sm">
        {(["month", "year", "all"] as const).map((u) => (
          <button
            key={u}
            type="button"
            aria-pressed={unit === u}
            onClick={() => {
              handleUnitChange(u);
            }}
            className={`rounded px-3 py-1 ${
              unit === u ? "bg-blue-600 text-white" : "bg-gray-100 text-gray-700"
            }`}
          >
            {u === "month" ? "Month" : u === "year" ? "Year" : "All"}
          </button>
        ))}
      </div>
    </div>
  );
}

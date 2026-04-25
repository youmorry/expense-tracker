import { ChevronLeft, ChevronRight } from "lucide-react";
import { useEffect, useRef, useState } from "react";

import { Button } from "@/components/ui/button";
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group";

import { type IsoDate, toIsoDate } from "../lib/isoDate";

type Unit = "month" | "year" | "all";

function isUnit(value: string): value is Unit {
  return value === "month" || value === "year" || value === "all";
}

export type Period = { from: IsoDate; to: IsoDate } | null;

interface PeriodSelectorProps {
  onChange: (period: Period) => void;
}

const MONTH_FORMATTER = new Intl.DateTimeFormat("en-US", { month: "long", year: "numeric" });

function lastDayOfMonth(year: number, month: number): number {
  return new Date(year, month, 0).getDate();
}

function computePeriod(unit: Unit, anchor: Date): Period {
  if (unit === "all") return null;
  const year = anchor.getFullYear();
  if (unit === "year") {
    return { from: toIsoDate(year, 1, 1), to: toIsoDate(year, 12, 31) };
  }
  const month = anchor.getMonth() + 1;
  return {
    from: toIsoDate(year, month, 1),
    to: toIsoDate(year, month, lastDayOfMonth(year, month)),
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

  const handleUnitChange = (nextUnit: string) => {
    if (!isUnit(nextUnit) || nextUnit === unit) return;
    setUnit(nextUnit);
    // 単位切替時は現在日時にリセット（「All → Month」で当月が戻るため）
    setAnchor(new Date());
  };

  const showNav = unit !== "all";

  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-center justify-center gap-2">
        {showNav && (
          <Button
            type="button"
            variant="ghost"
            size="icon"
            aria-label="Previous period"
            onClick={() => {
              setAnchor((prev) => shiftAnchor(unit, prev, -1));
            }}
          >
            <ChevronLeft />
          </Button>
        )}
        <span className="min-w-40 text-center text-base font-medium">
          {formatLabel(unit, anchor)}
        </span>
        {showNav && (
          <Button
            type="button"
            variant="ghost"
            size="icon"
            aria-label="Next period"
            onClick={() => {
              setAnchor((prev) => shiftAnchor(unit, prev, 1));
            }}
          >
            <ChevronRight />
          </Button>
        )}
      </div>
      <ToggleGroup
        type="single"
        value={unit}
        onValueChange={handleUnitChange}
        className="self-center"
      >
        <ToggleGroupItem value="month" aria-label="Month">
          Month
        </ToggleGroupItem>
        <ToggleGroupItem value="year" aria-label="Year">
          Year
        </ToggleGroupItem>
        <ToggleGroupItem value="all" aria-label="All">
          All
        </ToggleGroupItem>
      </ToggleGroup>
    </div>
  );
}

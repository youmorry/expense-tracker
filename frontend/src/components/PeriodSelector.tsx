import { ChevronLeft, ChevronRight } from "lucide-react";

import { Button } from "@/components/ui/button";
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group";

import { type PeriodSelectorValue, type Unit } from "./period";

function isUnit(value: string): value is Unit {
  return value === "month" || value === "year" || value === "all";
}

interface PeriodSelectorProps {
  value: PeriodSelectorValue;
  onChange: (next: PeriodSelectorValue) => void;
}

const MONTH_FORMATTER = new Intl.DateTimeFormat("en-US", { month: "long", year: "numeric" });

function formatLabel(value: PeriodSelectorValue): string {
  if (value.unit === "all") return "All Transactions";
  if (value.unit === "year") return value.anchor.getFullYear().toString();
  return MONTH_FORMATTER.format(value.anchor);
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

export function PeriodSelector({ value, onChange }: PeriodSelectorProps) {
  const handleUnitChange = (nextUnit: string) => {
    if (!isUnit(nextUnit) || nextUnit === value.unit) return;
    // 単位切替時は現在日時にリセット（「All → Month」で当月が戻るため）
    onChange({ unit: nextUnit, anchor: new Date() });
  };

  const handleShift = (direction: -1 | 1) => {
    onChange({ unit: value.unit, anchor: shiftAnchor(value.unit, value.anchor, direction) });
  };

  const showNav = value.unit !== "all";

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
              handleShift(-1);
            }}
          >
            <ChevronLeft />
          </Button>
        )}
        <span className="min-w-40 text-center text-base font-medium">{formatLabel(value)}</span>
        {showNav && (
          <Button
            type="button"
            variant="ghost"
            size="icon"
            aria-label="Next period"
            onClick={() => {
              handleShift(1);
            }}
          >
            <ChevronRight />
          </Button>
        )}
      </div>
      <ToggleGroup
        type="single"
        value={value.unit}
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

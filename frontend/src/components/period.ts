import { type IsoDate, toIsoDate } from "../lib/isoDate";

export type Unit = "month" | "year" | "all";

export type Period = { from: IsoDate; to: IsoDate } | null;

export interface PeriodSelectorValue {
  unit: Unit;
  anchor: Date;
}

function lastDayOfMonth(year: number, month: number): number {
  return new Date(year, month, 0).getDate();
}

export function defaultPeriodSelectorValue(now: Date = new Date()): PeriodSelectorValue {
  return { unit: "month", anchor: now };
}

export function periodFromValue(value: PeriodSelectorValue): Period {
  if (value.unit === "all") return null;
  const year = value.anchor.getFullYear();
  if (value.unit === "year") {
    return { from: toIsoDate(year, 1, 1), to: toIsoDate(year, 12, 31) };
  }
  const month = value.anchor.getMonth() + 1;
  return {
    from: toIsoDate(year, month, 1),
    to: toIsoDate(year, month, lastDayOfMonth(year, month)),
  };
}

import { describe, expect, it } from "vitest";

import { toIsoDate } from "./isoDate";

describe("toIsoDate", () => {
  it("returns a date string in YYYY-MM-DD format", () => {
    expect(toIsoDate(2026, 2, 15)).toBe("2026-02-15");
  });

  it("pads single-digit month and day with a leading zero", () => {
    expect(toIsoDate(2026, 1, 5)).toBe("2026-01-05");
  });

  it("pads year to four digits", () => {
    expect(toIsoDate(42, 12, 31)).toBe("0042-12-31");
  });

  it("accepts February 29 in a leap year", () => {
    expect(toIsoDate(2024, 2, 29)).toBe("2024-02-29");
  });

  it.each([
    ["month below range", 2026, 0, 15],
    ["month above range", 2026, 13, 15],
    ["negative month", 2026, -1, 15],
    ["day below range", 2026, 1, 0],
    ["day above range", 2026, 1, 32],
    ["negative day", 2026, 1, -1],
  ])("throws RangeError when %s", (_label, year, month, day) => {
    expect(() => toIsoDate(year, month, day)).toThrow(RangeError);
  });

  it("throws RangeError when the date does not exist in the month", () => {
    expect(() => toIsoDate(2026, 2, 30)).toThrow(RangeError);
  });

  it("throws RangeError when February 29 is given in a non-leap year", () => {
    expect(() => toIsoDate(2025, 2, 29)).toThrow(RangeError);
  });

  it.each([
    ["year below range", 0, 1, 1],
    ["year above range", 10000, 1, 1],
    ["negative year", -1, 1, 1],
  ])("throws RangeError when %s", (_label, year, month, day) => {
    expect(() => toIsoDate(year, month, day)).toThrow(RangeError);
  });

  it.each([
    ["year is not an integer", 2026.5, 1, 1],
    ["month is not an integer", 2026, 1.5, 1],
    ["day is not an integer", 2026, 1, 1.5],
  ])("throws RangeError when %s", (_label, year, month, day) => {
    expect(() => toIsoDate(year, month, day)).toThrow(RangeError);
  });
});

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
});

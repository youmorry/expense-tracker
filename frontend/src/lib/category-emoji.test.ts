import { describe, expect, it } from "vitest";

import { getCategoryEmoji } from "./category-emoji";

describe("getCategoryEmoji", () => {
  it.each([
    ["Food", "🍽"],
    ["Transport", "🚃"],
    ["Housing", "🏠"],
    ["Daily Goods", "🛒"],
    ["Medical", "🏥"],
    ["Entertainment", "🎮"],
    ["Clothing", "👕"],
    ["Education", "📚"],
    ["Social", "🍻"],
    ["Other", "📦"],
    ["Uncategorized", "➖"],
  ])("returns %s emoji for category name %s", (name, expected) => {
    expect(getCategoryEmoji(name)).toBe(expected);
  });

  it("returns Uncategorized emoji when category name is unknown", () => {
    expect(getCategoryEmoji("NonExistentCategory")).toBe("➖");
  });
});

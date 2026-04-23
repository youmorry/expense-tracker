/**
 * カテゴリ名 → 絵文字のマッピング。
 *
 * カテゴリは全ユーザー共通で固定のため定数で保持する。
 * バックエンドが返す英名（`Food`, `Transport` など）をキーとする。
 *
 * @see docs/03-design/backend/api-design.md
 */

const UNCATEGORIZED_EMOJI = "➖";

const CATEGORY_EMOJI_MAP: Record<string, string> = {
  Food: "🍽",
  Transport: "🚃",
  Housing: "🏠",
  "Daily Goods": "🛒",
  Medical: "🏥",
  Entertainment: "🎮",
  Clothing: "👕",
  Education: "📚",
  Social: "🍻",
  Other: "📦",
  Uncategorized: UNCATEGORIZED_EMOJI,
};

export function getCategoryEmoji(name: string): string {
  return CATEGORY_EMOJI_MAP[name] ?? UNCATEGORIZED_EMOJI;
}

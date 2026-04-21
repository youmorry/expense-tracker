/**
 * YYYY-MM-DD 形式の日付文字列を表す Branded Type。
 *
 * `toIsoDate` 経由でのみ生成可能にすることでフォーマット不正を型で排除する。
 * ランタイムには単なる string であり、追加のオーバーヘッドはない。
 */
export type IsoDate = string & { readonly __brand: "IsoDate" };

function pad2(n: number): string {
  return n.toString().padStart(2, "0");
}

export function toIsoDate(year: number, month: number, day: number): IsoDate {
  const value = `${year.toString().padStart(4, "0")}-${pad2(month)}-${pad2(day)}`;
  // Branded Type を生成する唯一の入口。ここのみ型アサーションを許容する。
  // eslint-disable-next-line @typescript-eslint/consistent-type-assertions
  return value as IsoDate;
}

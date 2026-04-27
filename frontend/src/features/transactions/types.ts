import type { IsoDate } from "../../lib/isoDate";
import type { NeedWantType } from "../../types/api";

export type { Transaction } from "../../types/api";

/** 支出一覧取得（GET /api/v1/transactions）のクエリパラメータ。 */
export interface TransactionListParams {
  from?: IsoDate;
  to?: IsoDate;
  categoryIds?: number[];
  needWantType?: NeedWantType;
  keyword?: string;
}

/** 取引一覧画面のフィルター入力値。`null` / 空文字 / 空配列 はいずれも「未指定」を表す。 */
export interface TransactionFiltersValue {
  keyword: string;
  categoryIds: number[];
  needWantType: NeedWantType | null;
}

export function emptyTransactionFiltersValue(): TransactionFiltersValue {
  return { keyword: "", categoryIds: [], needWantType: null };
}

export function countActiveFilters(value: TransactionFiltersValue): number {
  let count = 0;
  if (value.keyword.trim().length > 0) count += 1;
  if (value.categoryIds.length > 0) count += 1;
  if (value.needWantType !== null) count += 1;
  return count;
}

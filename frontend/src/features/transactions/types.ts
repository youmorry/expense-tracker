import type { IsoDate } from "../../lib/isoDate";

export type { Transaction } from "../../types/api";

/**
 * 支出一覧取得（GET /api/v1/transactions）のクエリパラメータ。
 *
 * `from` / `to` は期間セレクタが計算した日付範囲。`null` は「すべて」を表し、
 * クエリから除外される。
 */
export interface TransactionListParams {
  from?: IsoDate;
  to?: IsoDate;
}

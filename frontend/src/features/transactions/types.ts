import type { IsoDate } from "../../lib/isoDate";
import type { NeedWantType } from "../../types/api";

export type { Transaction } from "../../types/api";

/**
 * 支出一覧取得（GET /api/v1/transactions）のクエリパラメータ。
 *
 * `from` / `to` は期間セレクタが計算した日付範囲。空文字・空配列・undefined は
 * 「未指定」として URL から除外される。
 */
export interface TransactionListParams {
  from?: IsoDate;
  to?: IsoDate;
  categoryIds?: number[];
  needWantType?: NeedWantType;
  keyword?: string;
}

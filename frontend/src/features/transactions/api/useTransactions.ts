/**
 * 支出一覧取得フック。
 *
 * 期間セレクタが計算した `from`/`to` を元に `GET /api/v1/transactions` を呼び出す。
 * API 側で `date DESC, id DESC` にソート済みのため FE では並び替えない。
 *
 * @see docs/03-design/backend/api-design.md
 */

import { useQuery, type UseQueryResult } from "@tanstack/react-query";
import { z } from "zod";

import { apiClient } from "../../../lib/api/client";
import { TransactionSchema } from "../../../types/api";
import type { Transaction, TransactionListParams } from "../types";

const TransactionsResponseSchema = z.object({ items: z.array(TransactionSchema) });

interface TransactionsResponse {
  items: Transaction[];
}

function buildPath(params: TransactionListParams): string {
  const search = new URLSearchParams();
  if (params.from !== undefined) search.set("from", params.from);
  if (params.to !== undefined) search.set("to", params.to);
  if (params.categoryIds !== undefined) {
    for (const id of params.categoryIds) search.append("category_id", String(id));
  }
  if (params.needWantType !== undefined) search.set("need_want_type", params.needWantType);
  if (params.keyword !== undefined && params.keyword.length > 0) {
    search.set("keyword", params.keyword);
  }
  const query = search.toString();
  return query.length > 0 ? `/api/v1/transactions?${query}` : "/api/v1/transactions";
}

export function useTransactions(
  params: TransactionListParams,
): UseQueryResult<TransactionsResponse> {
  return useQuery({
    queryKey: ["transactions", params],
    queryFn: async (): Promise<TransactionsResponse> => {
      const data = await apiClient.get(buildPath(params));
      return TransactionsResponseSchema.parse(data);
    },
  });
}

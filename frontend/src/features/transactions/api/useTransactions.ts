/**
 * 支出一覧取得フック。
 *
 * 期間セレクタが計算した `from`/`to` を元に `GET /api/v1/transactions` を呼び出す。
 * API 側で `date DESC, id DESC` にソート済みのため FE では並び替えない。
 *
 * @see docs/03-design/backend/api-design.md
 */

import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../../../lib/api/client";
import { TransactionSchema, listResponseSchema } from "../../../types/api";
import type { TransactionListParams } from "../types";

const TransactionsResponseSchema = listResponseSchema(TransactionSchema);

function buildPath(params: TransactionListParams): string {
  const search = new URLSearchParams();
  if (params.from !== undefined) search.set("from", params.from);
  if (params.to !== undefined) search.set("to", params.to);
  const query = search.toString();
  return query.length > 0 ? `/api/v1/transactions?${query}` : "/api/v1/transactions";
}

interface UseTransactionsOptions {
  enabled?: boolean;
}

export function useTransactions(
  params: TransactionListParams,
  options: UseTransactionsOptions = {},
) {
  return useQuery({
    queryKey: ["transactions", params],
    queryFn: async () => {
      const data = await apiClient.get(buildPath(params));
      return TransactionsResponseSchema.parse(data);
    },
    enabled: options.enabled,
  });
}

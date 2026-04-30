/**
 * NEED/WANT 別支出分析の取得フック。
 *
 * `Period` (`null` のとき期間指定なし＝全期間) を受け取り、
 * `GET /api/v1/analytics/need-want` を呼び出す。
 *
 * @see docs/03-design/backend/api-design.md
 */

import { useQuery, type UseQueryResult } from "@tanstack/react-query";

import type { Period } from "../../../components/period";
import { apiClient } from "../../../lib/api/client";
import { NeedWantAnalyticsSchema } from "../../../types/api";
import type { NeedWantAnalytics } from "../types";

function buildPath(period: Period): string {
  if (period === null) return "/api/v1/analytics/need-want";
  const search = new URLSearchParams({ from: period.from, to: period.to });
  return `/api/v1/analytics/need-want?${search.toString()}`;
}

export function useNeedWantAnalytics(period: Period): UseQueryResult<NeedWantAnalytics> {
  return useQuery({
    queryKey: ["analytics", "need-want", period],
    queryFn: async (): Promise<NeedWantAnalytics> => {
      const data = await apiClient.get(buildPath(period));
      return NeedWantAnalyticsSchema.parse(data);
    },
  });
}

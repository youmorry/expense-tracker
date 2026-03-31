package com.youmorry.expensetracker.presentation.analytics;

import com.youmorry.expensetracker.application.analytics.AnalyticsService;
import com.youmorry.expensetracker.domain.user.UserId;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 分析エンドポイント。 */
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  /**
   * コンストラクタ。
   *
   * @param analyticsService 分析サービス
   */
  public AnalyticsController(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  /**
   * need/want 別集計を取得する。
   *
   * @param from 集計開始日（省略可）
   * @param to 集計終了日（省略可）
   * @param userId 認証済みユーザー ID
   * @return need/want 別集計レスポンス
   */
  @GetMapping("/need-want")
  public ResponseEntity<NeedWantAnalyticsResponse> getNeedWantBreakdown(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      @AuthenticationPrincipal UserId userId) {
    var result = analyticsService.getNeedWantBreakdown(userId, from, to);
    return ResponseEntity.ok(NeedWantAnalyticsResponse.from(result));
  }

  /**
   * カテゴリ別集計を取得する。
   *
   * @param from 集計開始日（省略可）
   * @param to 集計終了日（省略可）
   * @param userId 認証済みユーザー ID
   * @return カテゴリ別集計レスポンス
   */
  @GetMapping("/category")
  public ResponseEntity<CategoryAnalyticsResponse> getCategoryBreakdown(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      @AuthenticationPrincipal UserId userId) {
    var result = analyticsService.getCategoryBreakdown(userId, from, to);
    return ResponseEntity.ok(CategoryAnalyticsResponse.from(result));
  }
}

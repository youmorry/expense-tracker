package com.youmorry.expensetracker.presentation.transaction;

import com.youmorry.expensetracker.application.TransactionSearchQuery;
import com.youmorry.expensetracker.application.TransactionService;
import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.shared.exception.ValidationException;
import com.youmorry.expensetracker.shared.exception.ValidationException.FieldError;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 支出エンドポイント。 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  /**
   * コンストラクタ。
   *
   * @param transactionService 支出サービス
   */
  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * 支出を登録する。
   *
   * @param request 支出登録リクエスト
   * @param userId 認証済みユーザー ID
   * @return 登録された支出
   */
  @PostMapping
  public ResponseEntity<TransactionResponse> create(
      @Valid @RequestBody CreateTransactionRequest request,
      @AuthenticationPrincipal UserId userId) {
    var result = transactionService.create(userId, request.toCommand());
    return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(result));
  }

  /**
   * 支出一覧を取得する。
   *
   * @param from 取得開始日
   * @param to 取得終了日
   * @param categoryId カテゴリ ID（複数指定可）
   * @param needWantType 必要/欲しい区分
   * @param keyword 検索キーワード
   * @param userId 認証済みユーザー ID
   * @return 支出一覧
   */
  @GetMapping
  public ResponseEntity<TransactionListResponse> list(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      @RequestParam(name = "category_id", required = false) List<Long> categoryId,
      @RequestParam(name = "need_want_type", required = false) NeedWantType needWantType,
      @RequestParam(required = false) String keyword,
      @AuthenticationPrincipal UserId userId) {
    List<CategoryId> categoryIds;
    try {
      categoryIds =
          categoryId != null
              ? categoryId.stream().map(CategoryId::new).toList()
              : List.of();
    } catch (IllegalArgumentException e) {
      throw new ValidationException(
          e.getMessage(),
          List.of(new FieldError("Invalid category ID.", "category_id")));
    }
    var query = new TransactionSearchQuery(from, to, categoryIds, needWantType, keyword);
    var results = transactionService.search(userId, query);
    return ResponseEntity.ok(TransactionListResponse.from(results));
  }
}

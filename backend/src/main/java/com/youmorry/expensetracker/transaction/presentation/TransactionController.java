package com.youmorry.expensetracker.transaction.presentation;

import com.youmorry.expensetracker.category.domain.CategoryId;
import com.youmorry.expensetracker.transaction.application.TransactionResult;
import com.youmorry.expensetracker.transaction.application.TransactionSearchQuery;
import com.youmorry.expensetracker.transaction.application.TransactionService;
import com.youmorry.expensetracker.transaction.domain.NeedWantType;
import com.youmorry.expensetracker.transaction.domain.TransactionId;
import com.youmorry.expensetracker.user.domain.UserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    TransactionResult result = transactionService.create(userId, request.toCommand());
    return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(result));
  }

  /**
   * 支出詳細を取得する。
   *
   * @param id 支出 ID
   * @param userId 認証済みユーザー ID
   * @return 支出詳細
   */
  @GetMapping("/{id}")
  public ResponseEntity<TransactionResponse> getById(
      @PathVariable @Min(1) long id, @AuthenticationPrincipal UserId userId) {
    TransactionResult result = transactionService.findById(userId, new TransactionId(id));
    return ResponseEntity.ok(TransactionResponse.from(result));
  }

  /**
   * 支出を更新する（全量更新）。
   *
   * @param id 支出 ID
   * @param request 支出更新リクエスト
   * @param userId 認証済みユーザー ID
   * @return 更新された支出
   */
  @PutMapping("/{id}")
  public ResponseEntity<TransactionResponse> update(
      @PathVariable @Min(1) long id,
      @Valid @RequestBody UpdateTransactionRequest request,
      @AuthenticationPrincipal UserId userId) {
    TransactionResult result =
        transactionService.update(userId, new TransactionId(id), request.toCommand());
    return ResponseEntity.ok(TransactionResponse.from(result));
  }

  /**
   * 支出を削除する。
   *
   * @param id 支出 ID
   * @param userId 認証済みユーザー ID
   * @return 204 No Content
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable @Min(1) long id, @AuthenticationPrincipal UserId userId) {
    transactionService.delete(userId, new TransactionId(id));
    return ResponseEntity.noContent().build();
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
      @RequestParam(name = "category_id", required = false) List<@Min(1) Long> categoryId,
      @RequestParam(name = "need_want_type", required = false) NeedWantType needWantType,
      @RequestParam(required = false) String keyword,
      @AuthenticationPrincipal UserId userId) {
    List<CategoryId> categoryIds =
        categoryId == null ? List.of() : categoryId.stream().map(v -> new CategoryId(v)).toList();

    var query = new TransactionSearchQuery(from, to, categoryIds, needWantType, keyword);
    List<TransactionResult> results = transactionService.search(userId, query);
    return ResponseEntity.ok(TransactionListResponse.from(results));
  }
}

package com.youmorry.expensetracker.presentation.transaction;

import com.youmorry.expensetracker.application.TransactionService;
import com.youmorry.expensetracker.domain.user.UserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}

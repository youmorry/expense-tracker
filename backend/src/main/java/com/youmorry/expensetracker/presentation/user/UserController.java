package com.youmorry.expensetracker.presentation.user;

import com.youmorry.expensetracker.application.UserService;
import com.youmorry.expensetracker.domain.user.UserId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** ユーザーエンドポイント。自分のユーザー情報の取得・更新・削除を行う。 */
@RestController
@RequestMapping("/api/v1/users/me")
public class UserController {

  private final UserService userService;

  /**
   * コンストラクタ。
   *
   * @param userService ユーザーサービス
   */
  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * 自分のユーザー情報を取得する。
   *
   * @param userId 認証済みユーザーの ID
   * @return ユーザー情報
   */
  @GetMapping
  public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserId userId) {
    var user = userService.getMe(userId);
    return ResponseEntity.ok(UserResponse.from(user));
  }

  /**
   * 通貨コードを更新する。
   *
   * @param userId 認証済みユーザーの ID
   * @param request 通貨コード更新リクエスト
   * @return 更新されたユーザー情報
   */
  @PatchMapping("/currency")
  public ResponseEntity<UserResponse> updateCurrency(
      @AuthenticationPrincipal UserId userId, @Valid @RequestBody UpdateCurrencyRequest request) {
    var user = userService.updateCurrency(userId, request.currencyCode());
    return ResponseEntity.ok(UserResponse.from(user));
  }

  /**
   * アカウントを削除する。
   *
   * @param userId 認証済みユーザーの ID
   * @return 204 No Content
   */
  @DeleteMapping
  public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserId userId) {
    userService.deleteAccount(userId);
    return ResponseEntity.noContent().build();
  }
}

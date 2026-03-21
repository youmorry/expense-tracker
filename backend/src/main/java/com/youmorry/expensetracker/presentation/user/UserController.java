package com.youmorry.expensetracker.presentation.user;

import com.youmorry.expensetracker.application.UserService;
import com.youmorry.expensetracker.domain.model.user.UserId;
import com.youmorry.expensetracker.presentation.auth.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
   * @param jwt JWT トークン
   * @return ユーザー情報
   */
  @GetMapping
  public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
    var user = userService.getMe(extractUserId(jwt));
    return ResponseEntity.ok(UserResponse.from(user));
  }

  /**
   * 通貨コードを更新する。
   *
   * @param jwt JWT トークン
   * @param request 通貨コード更新リクエスト
   * @return 更新されたユーザー情報
   */
  @PatchMapping("/currency")
  public ResponseEntity<UserResponse> updateCurrency(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateCurrencyRequest request) {
    var user = userService.updateCurrency(extractUserId(jwt), request.currencyCode());
    return ResponseEntity.ok(UserResponse.from(user));
  }

  /**
   * アカウントを削除する。
   *
   * @param jwt JWT トークン
   * @return 204 No Content
   */
  @DeleteMapping
  public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal Jwt jwt) {
    userService.deleteAccount(extractUserId(jwt));
    return ResponseEntity.noContent().build();
  }

  private UserId extractUserId(Jwt jwt) {
    return new UserId(Long.valueOf(jwt.getSubject()));
  }
}

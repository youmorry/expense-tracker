package com.youmorry.expensetracker.user.presentation;

import com.youmorry.expensetracker.user.application.UserService;
import com.youmorry.expensetracker.user.domain.User;
import com.youmorry.expensetracker.user.domain.UserId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** ユーザーエンドポイント。自分のユーザー情報の取得・削除を行う。 */
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
    User user = userService.getMe(userId);
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

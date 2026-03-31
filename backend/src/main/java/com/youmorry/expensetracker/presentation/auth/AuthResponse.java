package com.youmorry.expensetracker.presentation.auth;

import com.youmorry.expensetracker.application.auth.AuthService.AuthResult;
import com.youmorry.expensetracker.presentation.user.UserResponse;

/** Google 認証レスポンス。JWT アクセストークンとユーザー情報を返す。 */
public record AuthResponse(String accessToken, UserResponse user) {

  /**
   * AuthResult からレスポンスを生成する。
   *
   * @param result 認証結果
   * @return 認証レスポンス
   */
  public static AuthResponse from(AuthResult result) {
    return new AuthResponse(result.accessToken(), UserResponse.from(result.user()));
  }
}

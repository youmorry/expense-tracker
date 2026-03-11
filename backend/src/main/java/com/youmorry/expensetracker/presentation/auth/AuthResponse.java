package com.youmorry.expensetracker.presentation.auth;

import com.youmorry.expensetracker.application.AuthService.AuthResult;

/** Google 認証レスポンス。JWT アクセストークンとユーザー情報を返す。 */
public record AuthResponse(String accessToken, UserResponse user) {

  /** AuthResult からレスポンスを生成する。 */
  public static AuthResponse from(AuthResult result) {
    return new AuthResponse(result.accessToken(), UserResponse.from(result.user()));
  }
}

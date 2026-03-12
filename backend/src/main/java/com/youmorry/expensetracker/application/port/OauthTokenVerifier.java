package com.youmorry.expensetracker.application.port;

/**
 * OAuth ID トークンを検証し、ユーザー情報を取得するポート。
 *
 * <p>プロバイダごとに実装を提供する（例: Google, Apple）。
 */
public interface OauthTokenVerifier {

  /**
   * ID トークンを検証し、ユーザー情報を返す。
   *
   * @param idToken ID トークン文字列
   * @return 検証済みのユーザー情報
   * @throws com.youmorry.expensetracker.shared.exception.UnauthorizedException 検証失敗時
   */
  OauthUserInfo verify(String idToken);
}

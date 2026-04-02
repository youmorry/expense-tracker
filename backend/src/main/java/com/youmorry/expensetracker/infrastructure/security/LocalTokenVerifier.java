package com.youmorry.expensetracker.infrastructure.security;

import com.youmorry.expensetracker.application.auth.port.OauthTokenVerifier;
import com.youmorry.expensetracker.application.auth.port.OauthUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * ローカル開発用の {@link OauthTokenVerifier} スタブ実装。
 *
 * <p>任意の ID トークンを受け入れ、設定値ベースの固定ユーザー情報を返す。 本番環境では絶対に使用しないこと。
 */
@Component
@Profile("local")
public class LocalTokenVerifier implements OauthTokenVerifier {

  private static final Logger log = LoggerFactory.getLogger(LocalTokenVerifier.class);
  private final OauthUserInfo fixedUserInfo;

  /**
   * コンストラクタ。起動時に WARNING ログを出力し、本番誤用を防止する。
   *
   * @param subject OAuth subject（デフォルト: dev-user-001）
   * @param email メールアドレス（デフォルト: dev@example.com）
   * @param name 表示名（デフォルト: Dev User）
   */
  public LocalTokenVerifier(
      @Value("${app.auth.local.subject:dev-user-001}") String subject,
      @Value("${app.auth.local.email:dev@example.com}") String email,
      @Value("${app.auth.local.name:Dev User}") String name) {
    this.fixedUserInfo = new OauthUserInfo(subject, email, name);
    log.warn(">>> LocalTokenVerifier is active. OAuth token verification is DISABLED. <<<");
  }

  @Override
  public OauthUserInfo verify(String idToken) {
    return fixedUserInfo;
  }
}

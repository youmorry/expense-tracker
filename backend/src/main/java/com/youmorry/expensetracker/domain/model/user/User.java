package com.youmorry.expensetracker.domain.model.user;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * ユーザーを表すエンティティ。
 *
 * <p>現在は Google OAuth2 のみをサポートしているため {@code googleId} を直接保持しているが、
 * 複数の認証プロバイダをサポートする場合は {@code LinkedAccount} 等の別エンティティに分離すること。
 */
@Table("users")
public record User(
    @Id @Nullable UserId id,
    String googleId,
    String email,
    String displayName,
    String currencyCode,
    @Nullable Instant createdAt) {

  /**
   * 新規ユーザーを生成する。ID と作成日時は永続化時に設定される。
   *
   * @param googleId Google ユーザーの一意識別子
   * @param email メールアドレス
   * @param displayName 表示名
   * @param currencyCode 通貨コード（ISO 4217）
   * @return 新規ユーザー
   */
  public static User createNew(
      String googleId, String email, String displayName, String currencyCode) {
    return new User(null, googleId, email, displayName, currencyCode, null);
  }
}

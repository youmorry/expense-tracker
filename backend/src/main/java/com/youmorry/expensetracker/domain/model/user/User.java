package com.youmorry.expensetracker.domain.model.user;

import java.time.Instant;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * ユーザーを表すエンティティ。
 *
 * <p>現在は Google OAuth2 のみをサポートしているため {@code googleId} を直接保持しているが、 複数の認証プロバイダをサポートする場合は {@code
 * LinkedAccount} 等の別エンティティに分離すること。
 */
@Table("users")
public record User(
    @Id @Nullable UserId id,
    String googleId,
    String email,
    String displayName,
    CurrencyCode currencyCode,
    @Nullable Instant createdAt) {

  private static final int DISPLAY_NAME_MAX_LENGTH = 100;

  /** 不変条件を検証する。 */
  public User {
    Objects.requireNonNull(googleId, "googleId must not be null");
    if (googleId.isBlank()) {
      throw new IllegalArgumentException("googleId must not be blank");
    }
    Objects.requireNonNull(email, "email must not be null");
    if (email.isBlank()) {
      throw new IllegalArgumentException("email must not be blank");
    }
    Objects.requireNonNull(displayName, "displayName must not be null");
    if (displayName.isBlank()) {
      throw new IllegalArgumentException("displayName must not be blank");
    }
    if (displayName.length() > DISPLAY_NAME_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "displayName must not exceed "
              + DISPLAY_NAME_MAX_LENGTH
              + " characters, but was: "
              + displayName.length());
    }
    Objects.requireNonNull(currencyCode, "currencyCode must not be null");
  }

  /**
   * 新規ユーザーを生成する。ID と作成日時は永続化時に設定される。
   *
   * @param googleId Google ユーザーの一意識別子
   * @param email メールアドレス
   * @param displayName 表示名
   * @param currencyCode 通貨コード
   * @return 新規ユーザー
   */
  public static User createNew(
      String googleId, String email, String displayName, CurrencyCode currencyCode) {
    return new User(null, googleId, email, displayName, currencyCode, null);
  }

  /**
   * 通貨コードを変更した新しいユーザーを返す。
   *
   * @param newCurrencyCode 通貨コード
   * @return 通貨コードが更新されたユーザー
   */
  public User changeCurrencyCode(CurrencyCode newCurrencyCode) {
    Objects.requireNonNull(newCurrencyCode, "Currency code must not be null");
    if (currencyCode.equals(newCurrencyCode)) {
      return this;
    }
    return new User(id, googleId, email, displayName, newCurrencyCode, createdAt);
  }
}

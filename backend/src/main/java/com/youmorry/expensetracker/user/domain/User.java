package com.youmorry.expensetracker.user.domain;

import java.time.Instant;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * ユーザーを表すエンティティ。
 *
 * <p>現在は Google OAuth2 のみをサポートしているため {@code googleId} を直接保持しているが、 複数の認証プロバイダをサポートする場合は {@code
 * LinkedAccount} 等の別エンティティに分離すること。
 *
 * @param id ユーザー ID（永続化時に採番）
 * @param googleId Google ユーザーの一意識別子
 * @param email メールアドレス
 * @param displayName 表示名
 * @param createdAt 作成日時（永続化時に設定）
 */
@Table("users")
public record User(
    @Id @Nullable UserId id,
    String googleId,
    String email,
    String displayName,
    @CreatedDate @Nullable Instant createdAt) {

  private static final int DISPLAY_NAME_MAX_LENGTH = 100;

  /**
   * 不変条件を検証する。
   *
   * @throws NullPointerException googleId, email, displayName が null の場合
   * @throws IllegalArgumentException googleId, email, displayName が空白、または displayName が 100 文字超過の場合
   */
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
  }

  /**
   * 新規ユーザーを生成する。ID と作成日時は永続化時に設定される。
   *
   * @param googleId Google ユーザーの一意識別子
   * @param email メールアドレス
   * @param displayName 表示名
   * @return 新規ユーザー
   */
  public static User createNew(String googleId, String email, String displayName) {
    return new User(null, googleId, email, displayName, null);
  }
}

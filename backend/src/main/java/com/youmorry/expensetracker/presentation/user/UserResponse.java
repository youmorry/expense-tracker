package com.youmorry.expensetracker.presentation.user;

import com.youmorry.expensetracker.domain.user.User;
import java.time.Instant;
import java.util.Objects;

/** ユーザー情報のレスポンス DTO。 */
public record UserResponse(Long id, String email, String displayName, Instant createdAt) {

  /**
   * 不変条件を検証する。
   *
   * @throws NullPointerException id, createdAt が null の場合
   */
  public UserResponse {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
  }

  /**
   * User エンティティからレスポンスを生成する。
   *
   * @param user ユーザーエンティティ
   * @return ユーザーレスポンス
   */
  public static UserResponse from(User user) {
    return new UserResponse(
        Objects.requireNonNull(user.id()).value(),
        user.email(),
        user.displayName(),
        Objects.requireNonNull(user.createdAt()));
  }
}

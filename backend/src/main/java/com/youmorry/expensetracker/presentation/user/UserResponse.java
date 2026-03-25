package com.youmorry.expensetracker.presentation.user;

import com.youmorry.expensetracker.domain.user.User;
import java.time.Instant;

/** ユーザー情報のレスポンス DTO。 */
public record UserResponse(
    Long id, String email, String displayName, String currencyCode, Instant createdAt) {

  /**
   * User エンティティからレスポンスを生成する。
   *
   * @param user ユーザーエンティティ
   * @return ユーザーレスポンス
   */
  public static UserResponse from(User user) {
    return new UserResponse(
        user.id().value(),
        user.email(),
        user.displayName(),
        user.currencyCode().value(),
        user.createdAt());
  }
}

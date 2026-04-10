package com.youmorry.expensetracker.auth.application;

import com.youmorry.expensetracker.user.domain.UserId;

/** JWT アクセストークンの生成を担うポート。 */
public interface JwtTokenGenerator {

  /**
   * 指定されたユーザー情報から JWT アクセストークンを生成する。
   *
   * @param userId ユーザー ID
   * @param email メールアドレス
   * @return JWT アクセストークン文字列
   */
  String generateToken(UserId userId, String email);
}

package com.youmorry.expensetracker.domain.model.user;

import java.util.Optional;

/** ユーザーの永続化を担うリポジトリインターフェース。 */
public interface UserRepository {

  /**
   * 指定された ID のユーザーを取得する。
   *
   * @param id ユーザー ID
   * @return ユーザー。存在しない場合は空
   */
  Optional<User> findById(UserId id);

  /**
   * 指定された Google ID のユーザーを取得する。
   *
   * @param googleId Google ユーザーの一意識別子
   * @return ユーザー。存在しない場合は空
   */
  Optional<User> findByGoogleId(String googleId);

  /**
   * ユーザーを保存する。
   *
   * @param user 保存するユーザー
   * @return 保存されたユーザー（ID 採番済み）
   */
  User save(User user);
}

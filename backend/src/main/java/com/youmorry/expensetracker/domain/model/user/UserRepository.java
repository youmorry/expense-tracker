package com.youmorry.expensetracker.domain.model.user;

import java.util.Optional;

/** ユーザーの永続化を担うリポジトリインターフェース。 */
public interface UserRepository {

  /** 指定された ID のユーザーを取得する。 */
  Optional<User> findById(UserId id);

  /** 指定された Google ID のユーザーを取得する。 */
  Optional<User> findByGoogleId(String googleId);

  /** ユーザーを保存する。 */
  User save(User user);
}

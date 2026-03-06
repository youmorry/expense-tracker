package com.youmorry.expensetracker.domain.model.user;

import java.util.Optional;

/** ユーザーの永続化を担うリポジトリインターフェース。 */
public interface UserRepository {

  Optional<User> findById(UserId id);

  Optional<User> findByGoogleId(String googleId);

  User save(User user);
}

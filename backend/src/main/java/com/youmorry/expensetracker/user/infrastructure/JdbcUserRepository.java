package com.youmorry.expensetracker.user.infrastructure;

import com.youmorry.expensetracker.user.domain.User;
import com.youmorry.expensetracker.user.domain.UserId;
import com.youmorry.expensetracker.user.domain.UserRepository;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JDBC による {@link UserRepository} の実装。 */
@Repository
public interface JdbcUserRepository extends UserRepository, CrudRepository<User, UserId> {

  @Override
  @Query("SELECT * FROM users WHERE google_id = :googleId")
  Optional<User> findByGoogleId(String googleId);
}

package com.youmorry.expensetracker.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.youmorry.expensetracker.domain.model.user.CurrencyCode;
import com.youmorry.expensetracker.domain.model.user.User;
import com.youmorry.expensetracker.domain.model.user.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;

/** Spring Data JDBC Auditing によるタイムスタンプ自動設定の統合テスト。 */
@DataJdbcTest
class JdbcAuditingTest extends AbstractRepositoryTest {

  @Autowired private UserRepository userRepository;

  @Test
  void save_newEntity_setsCreatedAt() {
    Instant before = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    User saved =
        userRepository.save(
            User.createNew("google-audit-1", "audit@example.com", "Audit User", CurrencyCode.JPY));

    assertThat(saved.createdAt()).isNotNull();
    assertThat(saved.createdAt()).isAfterOrEqualTo(before);
    assertThat(saved.createdAt()).isBeforeOrEqualTo(Instant.now());
  }

  @Test
  void save_existingEntity_doesNotChangeCreatedAt() {
    User saved =
        userRepository.save(
            User.createNew(
                "google-audit-2", "audit2@example.com", "Audit User 2", CurrencyCode.USD));
    Instant originalCreatedAt = saved.createdAt();

    User updated = userRepository.save(saved.changeCurrencyCode(CurrencyCode.EUR));

    assertThat(updated.createdAt()).isEqualTo(originalCreatedAt);
  }
}

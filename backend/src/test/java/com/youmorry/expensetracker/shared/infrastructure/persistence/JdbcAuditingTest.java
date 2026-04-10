package com.youmorry.expensetracker.shared.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.youmorry.expensetracker.category.domain.CategoryId;
import com.youmorry.expensetracker.transaction.domain.Money;
import com.youmorry.expensetracker.transaction.domain.NeedWantType;
import com.youmorry.expensetracker.transaction.domain.Transaction;
import com.youmorry.expensetracker.transaction.domain.TransactionRepository;
import com.youmorry.expensetracker.user.domain.User;
import com.youmorry.expensetracker.user.domain.UserId;
import com.youmorry.expensetracker.user.domain.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;

/** Spring Data JDBC Auditing によるタイムスタンプ自動設定の統合テスト。 */
@DataJdbcTest
class JdbcAuditingTest extends AbstractRepositoryTest {

  @Autowired private UserRepository userRepository;
  @Autowired private TransactionRepository transactionRepository;

  @Test
  void save_newEntity_setsCreatedAt() {
    Instant before = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    User saved =
        userRepository.save(User.createNew("google-audit-1", "audit@example.com", "Audit User"));

    assertThat(saved.createdAt()).isNotNull();
    assertThat(saved.createdAt()).isAfterOrEqualTo(before);
    assertThat(saved.createdAt()).isBeforeOrEqualTo(Instant.now());
  }

  @Test
  void save_existingEntity_doesNotChangeCreatedAt() {
    User saved =
        userRepository.save(User.createNew("google-audit-2", "audit2@example.com", "Audit User 2"));
    Instant originalCreatedAt = saved.createdAt();

    User updated =
        userRepository.save(
            new User(
                saved.id(),
                saved.googleId(),
                saved.email(),
                saved.displayName(),
                saved.createdAt()));

    assertThat(updated.createdAt()).isEqualTo(originalCreatedAt);
  }

  @Test
  void save_newTransaction_setsCreatedAtAndUpdatedAt() {
    User user = saveUser("google-audit-tx-1");
    Instant before = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    Transaction saved = transactionRepository.save(newTransaction(user.id()));

    assertThat(saved.createdAt()).isNotNull();
    assertThat(saved.createdAt()).isAfterOrEqualTo(before);
    assertThat(saved.updatedAt()).isNotNull();
    assertThat(saved.updatedAt()).isAfterOrEqualTo(before);
  }

  @Test
  void save_existingTransaction_updatesUpdatedAtOnly() {
    User user = saveUser("google-audit-tx-2");
    Transaction saved = transactionRepository.save(newTransaction(user.id()));
    Instant originalCreatedAt = saved.createdAt();
    Instant originalUpdatedAt = saved.updatedAt();

    Transaction updated =
        transactionRepository.save(
            new Transaction(
                saved.id(),
                saved.userId(),
                saved.date(),
                new Money(new BigDecimal("999")),
                saved.categoryId(),
                saved.needWantType(),
                saved.title(),
                saved.memo(),
                saved.createdAt(),
                saved.updatedAt()));

    assertThat(updated.createdAt()).isEqualTo(originalCreatedAt);
    assertThat(updated.updatedAt()).isAfterOrEqualTo(originalUpdatedAt);
  }

  private User saveUser(String googleId) {
    return userRepository.save(User.createNew(googleId, googleId + "@example.com", "Audit User"));
  }

  private Transaction newTransaction(UserId userId) {
    return new Transaction(
        null,
        userId,
        LocalDate.of(2025, 1, 15),
        new Money(new BigDecimal("500")),
        new CategoryId(1),
        NeedWantType.NEED,
        "Audit Test",
        null,
        null,
        null);
  }
}

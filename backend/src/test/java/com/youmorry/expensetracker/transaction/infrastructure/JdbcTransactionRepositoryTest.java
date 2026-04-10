package com.youmorry.expensetracker.transaction.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.youmorry.expensetracker.shared.infrastructure.persistence.AbstractRepositoryTest;

import com.youmorry.expensetracker.category.domain.CategoryId;
import com.youmorry.expensetracker.transaction.domain.Money;
import com.youmorry.expensetracker.transaction.domain.NeedWantType;
import com.youmorry.expensetracker.transaction.domain.Transaction;
import com.youmorry.expensetracker.transaction.domain.TransactionId;
import com.youmorry.expensetracker.transaction.domain.TransactionRepository;
import com.youmorry.expensetracker.user.domain.User;
import com.youmorry.expensetracker.user.domain.UserId;
import com.youmorry.expensetracker.user.domain.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;

@DataJdbcTest
class JdbcTransactionRepositoryTest extends AbstractRepositoryTest {

  @Autowired private TransactionRepository transactionRepository;
  @Autowired private UserRepository userRepository;

  private static final CategoryId CATEGORY_ID = new CategoryId(1);

  @Test
  void save_newTransaction_assignsId() {
    User user = saveUser("google-tx-1");

    Transaction saved = transactionRepository.save(newTransaction(user.id()));

    assertThat(saved.id()).isNotNull();
  }

  @Test
  void save_newTransaction_persistsMoneyValue() {
    User user = saveUser("google-tx-2");
    Money amount = new Money(new BigDecimal("1234.5678"));

    Transaction saved =
        transactionRepository.save(
            new Transaction(
                null,
                user.id(),
                LocalDate.of(2025, 1, 15),
                amount,
                CATEGORY_ID,
                NeedWantType.NEED,
                null,
                null,
                null,
                null));

    Transaction found = transactionRepository.findById(saved.id()).orElseThrow();
    assertThat(found.amount()).isEqualTo(amount);
  }

  @Test
  void findById_existingId_returnsTransaction() {
    User user = saveUser("google-tx-3");
    Transaction saved = transactionRepository.save(newTransaction(user.id()));

    Optional<Transaction> found = transactionRepository.findById(saved.id());

    assertThat(found).hasValueSatisfying(tx -> assertThat(tx.id()).isEqualTo(saved.id()));
  }

  @Test
  void findById_nonExistingId_returnsEmpty() {
    Optional<Transaction> found = transactionRepository.findById(new TransactionId(9999));

    assertThat(found).isEmpty();
  }

  @Test
  void findByUserId_existingTransactions_returnsList() {
    User user = saveUser("google-tx-4");
    transactionRepository.save(newTransaction(user.id()));
    transactionRepository.save(newTransaction(user.id()));

    List<Transaction> found = transactionRepository.findByUserId(user.id());

    assertThat(found).hasSize(2);
  }

  @Test
  void findByUserId_multipleTransactions_returnsOrderedByDateDesc() {
    User user = saveUser("google-tx-sort");
    Transaction jan =
        transactionRepository.save(
            new Transaction(
                null,
                user.id(),
                LocalDate.of(2025, 1, 10),
                new Money(new BigDecimal("100")),
                CATEGORY_ID,
                NeedWantType.NEED,
                null,
                null,
                null,
                null));
    Transaction mar =
        transactionRepository.save(
            new Transaction(
                null,
                user.id(),
                LocalDate.of(2025, 3, 20),
                new Money(new BigDecimal("200")),
                CATEGORY_ID,
                NeedWantType.WANT,
                null,
                null,
                null,
                null));
    Transaction feb =
        transactionRepository.save(
            new Transaction(
                null,
                user.id(),
                LocalDate.of(2025, 2, 15),
                new Money(new BigDecimal("300")),
                CATEGORY_ID,
                NeedWantType.UNSET,
                null,
                null,
                null,
                null));

    List<Transaction> found = transactionRepository.findByUserId(user.id());

    assertThat(found).extracting(Transaction::id).containsExactly(mar.id(), feb.id(), jan.id());
  }

  @Test
  void findByUserId_noTransactions_returnsEmptyList() {
    User user = saveUser("google-tx-5");

    List<Transaction> found = transactionRepository.findByUserId(user.id());

    assertThat(found).isEmpty();
  }

  @Test
  void deleteById_existingTransaction_removesTransaction() {
    User user = saveUser("google-tx-6");
    Transaction saved = transactionRepository.save(newTransaction(user.id()));

    transactionRepository.deleteById(saved.id());

    assertThat(transactionRepository.findById(saved.id())).isEmpty();
  }

  @Test
  void save_nonExistingUserId_throwsException() {
    UserId nonExistingUserId = new UserId(9999);

    assertThatThrownBy(() -> transactionRepository.save(newTransaction(nonExistingUserId)))
        .isInstanceOf(Exception.class);
  }

  @Test
  void save_nonExistingCategoryId_throwsException() {
    User user = saveUser("google-tx-7");
    CategoryId nonExistingCategoryId = new CategoryId(9999);

    assertThatThrownBy(
            () ->
                transactionRepository.save(
                    new Transaction(
                        null,
                        user.id(),
                        LocalDate.of(2025, 1, 15),
                        new Money(new BigDecimal("100")),
                        nonExistingCategoryId,
                        NeedWantType.UNSET,
                        null,
                        null,
                        null,
                        null)))
        .isInstanceOf(Exception.class);
  }

  private User saveUser(String googleId) {
    return userRepository.save(User.createNew(googleId, googleId + "@example.com", "Test User"));
  }

  private Transaction newTransaction(UserId userId) {
    return new Transaction(
        null,
        userId,
        LocalDate.of(2025, 1, 15),
        new Money(new BigDecimal("500")),
        CATEGORY_ID,
        NeedWantType.NEED,
        "Test Transaction",
        "memo",
        null,
        null);
  }
}

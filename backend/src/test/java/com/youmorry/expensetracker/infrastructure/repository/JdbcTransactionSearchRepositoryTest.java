package com.youmorry.expensetracker.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.transaction.Money;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import com.youmorry.expensetracker.domain.transaction.Transaction;
import com.youmorry.expensetracker.domain.transaction.TransactionRepository;
import com.youmorry.expensetracker.domain.transaction.TransactionSearchCriteria;
import com.youmorry.expensetracker.domain.transaction.TransactionSearchRepository;
import com.youmorry.expensetracker.domain.user.User;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.domain.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.context.annotation.Import;

@DataJdbcTest
@Import(JdbcTransactionSearchRepository.class)
class JdbcTransactionSearchRepositoryTest extends AbstractRepositoryTest {

  @Autowired private TransactionSearchRepository transactionSearchRepository;
  @Autowired private TransactionRepository transactionRepository;
  @Autowired private UserRepository userRepository;

  private static final CategoryId FOOD = new CategoryId(1);
  private static final CategoryId TRANSPORT = new CategoryId(2);
  private static final CategoryId HOUSING = new CategoryId(3);

  @Test
  void search_withNoFilters_returnsAllUserTransactions() {
    User user = saveUser("google-search-1");
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 1, 10), FOOD));
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 2, 15), TRANSPORT));

    List<Transaction> result = transactionSearchRepository.search(user.id(), emptyCriteria());

    assertThat(result).hasSize(2);
  }

  @Test
  void search_withDateRange_returnsFilteredTransactions() {
    User user = saveUser("google-search-2");
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 1, 10), FOOD));
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 2, 15), FOOD));
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 3, 20), FOOD));

    List<Transaction> result =
        transactionSearchRepository.search(
            user.id(),
            new TransactionSearchCriteria(
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), List.of(), null, null));

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().date()).isEqualTo(LocalDate.of(2026, 2, 15));
  }

  @Test
  void search_withFromOnly_returnsTransactionsFromDate() {
    User user = saveUser("google-search-3");
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 1, 10), FOOD));
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 3, 20), FOOD));

    List<Transaction> result =
        transactionSearchRepository.search(
            user.id(),
            new TransactionSearchCriteria(LocalDate.of(2026, 2, 1), null, List.of(), null, null));

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().date()).isEqualTo(LocalDate.of(2026, 3, 20));
  }

  @Test
  void search_withToOnly_returnsTransactionsUpToDate() {
    User user = saveUser("google-search-4");
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 1, 10), FOOD));
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 3, 20), FOOD));

    List<Transaction> result =
        transactionSearchRepository.search(
            user.id(),
            new TransactionSearchCriteria(null, LocalDate.of(2026, 2, 28), List.of(), null, null));

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().date()).isEqualTo(LocalDate.of(2026, 1, 10));
  }

  @Test
  void search_withSingleCategoryId_returnsFilteredTransactions() {
    User user = saveUser("google-search-5");
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 1, 10), FOOD));
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 1, 11), TRANSPORT));

    List<Transaction> result =
        transactionSearchRepository.search(
            user.id(), new TransactionSearchCriteria(null, null, List.of(FOOD), null, null));

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().categoryId()).isEqualTo(FOOD);
  }

  @Test
  void search_withMultipleCategoryIds_returnsFilteredTransactions() {
    User user = saveUser("google-search-6");
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 1, 10), FOOD));
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 1, 11), TRANSPORT));
    transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 1, 12), HOUSING));

    List<Transaction> result =
        transactionSearchRepository.search(
            user.id(),
            new TransactionSearchCriteria(null, null, List.of(FOOD, HOUSING), null, null));

    assertThat(result).hasSize(2);
    assertThat(result).extracting(Transaction::categoryId).containsExactlyInAnyOrder(FOOD, HOUSING);
  }

  @Test
  void search_withNeedWantType_returnsFilteredTransactions() {
    User user = saveUser("google-search-7");
    transactionRepository.save(
        newTransactionWithType(user.id(), LocalDate.of(2026, 1, 10), NeedWantType.NEED));
    transactionRepository.save(
        newTransactionWithType(user.id(), LocalDate.of(2026, 1, 11), NeedWantType.WANT));
    transactionRepository.save(
        newTransactionWithType(user.id(), LocalDate.of(2026, 1, 12), NeedWantType.UNSET));

    List<Transaction> result =
        transactionSearchRepository.search(
            user.id(),
            new TransactionSearchCriteria(null, null, List.of(), NeedWantType.NEED, null));

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().needWantType()).isEqualTo(NeedWantType.NEED);
  }

  @Test
  void search_withKeyword_returnsMatchingTitleOrMemo() {
    User user = saveUser("google-search-8");
    transactionRepository.save(newTransactionWithTitleMemo(user.id(), "Lunch at cafe", null));
    transactionRepository.save(newTransactionWithTitleMemo(user.id(), "Dinner", "paid at cafe"));
    transactionRepository.save(newTransactionWithTitleMemo(user.id(), "Train fare", "commute"));

    List<Transaction> result =
        transactionSearchRepository.search(
            user.id(), new TransactionSearchCriteria(null, null, List.of(), null, "cafe"));

    assertThat(result).hasSize(2);
  }

  @Test
  void search_withKeyword_returnsCaseInsensitiveMatches() {
    User user = saveUser("google-search-9");
    transactionRepository.save(newTransactionWithTitleMemo(user.id(), "LUNCH", null));
    transactionRepository.save(newTransactionWithTitleMemo(user.id(), "lunch", null));

    List<Transaction> result =
        transactionSearchRepository.search(
            user.id(), new TransactionSearchCriteria(null, null, List.of(), null, "Lunch"));

    assertThat(result).hasSize(2);
  }

  @Test
  void search_withMultipleFilters_returnsIntersection() {
    User user = saveUser("google-search-10");
    transactionRepository.save(
        new Transaction(
            null,
            user.id(),
            LocalDate.of(2026, 2, 15),
            new Money(new BigDecimal("500")),
            FOOD,
            NeedWantType.NEED,
            "Lunch",
            null,
            null,
            null));
    transactionRepository.save(
        new Transaction(
            null,
            user.id(),
            LocalDate.of(2026, 2, 20),
            new Money(new BigDecimal("300")),
            TRANSPORT,
            NeedWantType.NEED,
            "Bus",
            null,
            null,
            null));
    transactionRepository.save(
        new Transaction(
            null,
            user.id(),
            LocalDate.of(2026, 3, 10),
            new Money(new BigDecimal("700")),
            FOOD,
            NeedWantType.WANT,
            "Dinner",
            null,
            null,
            null));

    List<Transaction> result =
        transactionSearchRepository.search(
            user.id(),
            new TransactionSearchCriteria(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28),
                List.of(FOOD),
                NeedWantType.NEED,
                null));

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().title()).isEqualTo("Lunch");
  }

  @Test
  void search_returnsOrderedByDateDescCreatedAtDesc() {
    User user = saveUser("google-search-11");
    Transaction jan =
        transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 1, 10), FOOD));
    Transaction mar =
        transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 3, 20), FOOD));
    Transaction feb =
        transactionRepository.save(newTransaction(user.id(), LocalDate.of(2026, 2, 15), FOOD));

    List<Transaction> result = transactionSearchRepository.search(user.id(), emptyCriteria());

    assertThat(result).extracting(Transaction::id).containsExactly(mar.id(), feb.id(), jan.id());
  }

  @Test
  void search_doesNotReturnOtherUsersTransactions() {
    User user1 = saveUser("google-search-12a");
    User user2 = saveUser("google-search-12b");
    transactionRepository.save(newTransaction(user1.id(), LocalDate.of(2026, 1, 10), FOOD));
    transactionRepository.save(newTransaction(user2.id(), LocalDate.of(2026, 1, 11), FOOD));

    List<Transaction> result = transactionSearchRepository.search(user1.id(), emptyCriteria());

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().userId()).isEqualTo(user1.id());
  }

  private TransactionSearchCriteria emptyCriteria() {
    return new TransactionSearchCriteria(null, null, List.of(), null, null);
  }

  private User saveUser(String googleId) {
    return userRepository.save(User.createNew(googleId, googleId + "@example.com", "Test User"));
  }

  private Transaction newTransaction(UserId userId, LocalDate date, CategoryId categoryId) {
    return new Transaction(
        null,
        userId,
        date,
        new Money(new BigDecimal("500")),
        categoryId,
        NeedWantType.NEED,
        null,
        null,
        null,
        null);
  }

  private Transaction newTransactionWithType(
      UserId userId, LocalDate date, NeedWantType needWantType) {
    return new Transaction(
        null,
        userId,
        date,
        new Money(new BigDecimal("500")),
        FOOD,
        needWantType,
        null,
        null,
        null,
        null);
  }

  private Transaction newTransactionWithTitleMemo(UserId userId, String title, String memo) {
    return new Transaction(
        null,
        userId,
        LocalDate.of(2026, 1, 10),
        new Money(new BigDecimal("500")),
        FOOD,
        NeedWantType.NEED,
        title,
        memo,
        null,
        null);
  }
}

package com.youmorry.expensetracker.analytics.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.youmorry.expensetracker.analytics.domain.AnalyticsRepository;
import com.youmorry.expensetracker.analytics.domain.CategoryBreakdown;
import com.youmorry.expensetracker.analytics.domain.NeedWantBreakdown;
import com.youmorry.expensetracker.category.domain.CategoryId;
import com.youmorry.expensetracker.shared.infrastructure.persistence.AbstractRepositoryTest;
import com.youmorry.expensetracker.transaction.domain.Money;
import com.youmorry.expensetracker.transaction.domain.NeedWantType;
import com.youmorry.expensetracker.transaction.domain.Transaction;
import com.youmorry.expensetracker.transaction.domain.TransactionRepository;
import com.youmorry.expensetracker.user.domain.User;
import com.youmorry.expensetracker.user.domain.UserId;
import com.youmorry.expensetracker.user.domain.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.context.annotation.Import;

@DataJdbcTest
@Import(JdbcAnalyticsRepository.class)
class JdbcAnalyticsRepositoryTest extends AbstractRepositoryTest {

  @Autowired private AnalyticsRepository analyticsRepository;
  @Autowired private TransactionRepository transactionRepository;
  @Autowired private UserRepository userRepository;

  private static final CategoryId FOOD = new CategoryId(1);
  private static final CategoryId TRANSPORT = new CategoryId(2);

  @Test
  void findCategoryBreakdown_withNoFilters_returnsAllCategories() {
    User user = saveUser("google-analytics-1");
    saveTransaction(user.id(), FOOD, "1000");
    saveTransaction(user.id(), FOOD, "500");
    saveTransaction(user.id(), TRANSPORT, "200");

    List<CategoryBreakdown> result =
        analyticsRepository.findCategoryBreakdown(user.id(), null, null);

    assertThat(result).isNotEmpty();
    CategoryBreakdown food = findByCategory(result, FOOD);
    assertThat(food.amount()).isEqualByComparingTo(new BigDecimal("1500"));
    assertThat(food.transactionCount()).isEqualTo(2);
    CategoryBreakdown transport = findByCategory(result, TRANSPORT);
    assertThat(transport.amount()).isEqualByComparingTo(new BigDecimal("200"));
    assertThat(transport.transactionCount()).isEqualTo(1);
  }

  @Test
  void findCategoryBreakdown_withDateRange_returnsFilteredAggregation() {
    User user = saveUser("google-analytics-2");
    saveTransactionOnDate(user.id(), FOOD, "1000", LocalDate.of(2026, 1, 15));
    saveTransactionOnDate(user.id(), FOOD, "500", LocalDate.of(2026, 3, 10));

    List<CategoryBreakdown> result =
        analyticsRepository.findCategoryBreakdown(
            user.id(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

    CategoryBreakdown food = findByCategory(result, FOOD);
    assertThat(food.amount()).isEqualByComparingTo(new BigDecimal("1000"));
    assertThat(food.transactionCount()).isEqualTo(1);
  }

  @Test
  void findCategoryBreakdown_withNoTransactions_returnsZeroAmountForAllCategories() {
    User user = saveUser("google-analytics-3");

    List<CategoryBreakdown> result =
        analyticsRepository.findCategoryBreakdown(user.id(), null, null);

    assertThat(result).isNotEmpty();
    assertThat(result)
        .allSatisfy(
            b -> {
              assertThat(b.amount()).isEqualByComparingTo(BigDecimal.ZERO);
              assertThat(b.transactionCount()).isEqualTo(0);
            });
  }

  @Test
  void findCategoryBreakdown_doesNotIncludeOtherUsersData() {
    User user1 = saveUser("google-analytics-4a");
    User user2 = saveUser("google-analytics-4b");
    saveTransaction(user1.id(), FOOD, "1000");
    saveTransaction(user2.id(), FOOD, "9999");

    List<CategoryBreakdown> result =
        analyticsRepository.findCategoryBreakdown(user1.id(), null, null);

    CategoryBreakdown food = findByCategory(result, FOOD);
    assertThat(food.amount()).isEqualByComparingTo(new BigDecimal("1000"));
  }

  @Test
  void findCategoryBreakdown_returnsOrderedByAmountDescThenDisplayOrderAsc() {
    User user = saveUser("google-analytics-5");
    saveTransaction(user.id(), TRANSPORT, "300");
    saveTransaction(user.id(), FOOD, "1000");

    List<CategoryBreakdown> result =
        analyticsRepository.findCategoryBreakdown(user.id(), null, null);

    List<CategoryBreakdown> nonZero =
        result.stream().filter(b -> b.amount().compareTo(BigDecimal.ZERO) > 0).toList();
    assertThat(nonZero.getFirst().categoryId()).isEqualTo(FOOD);
    assertThat(nonZero.get(1).categoryId()).isEqualTo(TRANSPORT);
  }

  @Test
  void findNeedWantBreakdown_withNoFilters_returnsAllTypes() {
    User user = saveUser("google-nw-1");
    saveTransactionWithType(user.id(), FOOD, "1000", NeedWantType.NEED);
    saveTransactionWithType(user.id(), FOOD, "500", NeedWantType.NEED);
    saveTransactionWithType(user.id(), TRANSPORT, "300", NeedWantType.WANT);

    List<NeedWantBreakdown> result =
        analyticsRepository.findNeedWantBreakdown(user.id(), null, null);

    assertThat(result).hasSize(3);
    NeedWantBreakdown need = findByType(result, NeedWantType.NEED);
    assertThat(need.amount()).isEqualByComparingTo(new BigDecimal("1500"));
    assertThat(need.transactionCount()).isEqualTo(2);
    NeedWantBreakdown want = findByType(result, NeedWantType.WANT);
    assertThat(want.amount()).isEqualByComparingTo(new BigDecimal("300"));
    assertThat(want.transactionCount()).isEqualTo(1);
    NeedWantBreakdown unset = findByType(result, NeedWantType.UNSET);
    assertThat(unset.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(unset.transactionCount()).isEqualTo(0);
  }

  @Test
  void findNeedWantBreakdown_withDateRange_returnsFilteredAggregation() {
    User user = saveUser("google-nw-2");
    saveTransactionWithTypeOnDate(
        user.id(), FOOD, "1000", NeedWantType.NEED, LocalDate.of(2026, 1, 15));
    saveTransactionWithTypeOnDate(
        user.id(), FOOD, "500", NeedWantType.NEED, LocalDate.of(2026, 3, 10));

    List<NeedWantBreakdown> result =
        analyticsRepository.findNeedWantBreakdown(
            user.id(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

    NeedWantBreakdown need = findByType(result, NeedWantType.NEED);
    assertThat(need.amount()).isEqualByComparingTo(new BigDecimal("1000"));
    assertThat(need.transactionCount()).isEqualTo(1);
  }

  @Test
  void findNeedWantBreakdown_withNoTransactions_returnsZeroForAllTypes() {
    User user = saveUser("google-nw-3");

    List<NeedWantBreakdown> result =
        analyticsRepository.findNeedWantBreakdown(user.id(), null, null);

    assertThat(result).hasSize(3);
    assertThat(result)
        .allSatisfy(
            b -> {
              assertThat(b.amount()).isEqualByComparingTo(BigDecimal.ZERO);
              assertThat(b.transactionCount()).isEqualTo(0);
            });
  }

  @Test
  void findNeedWantBreakdown_doesNotIncludeOtherUsersData() {
    User user1 = saveUser("google-nw-4a");
    User user2 = saveUser("google-nw-4b");
    saveTransactionWithType(user1.id(), FOOD, "1000", NeedWantType.NEED);
    saveTransactionWithType(user2.id(), FOOD, "9999", NeedWantType.NEED);

    List<NeedWantBreakdown> result =
        analyticsRepository.findNeedWantBreakdown(user1.id(), null, null);

    NeedWantBreakdown need = findByType(result, NeedWantType.NEED);
    assertThat(need.amount()).isEqualByComparingTo(new BigDecimal("1000"));
  }

  private User saveUser(String googleId) {
    return userRepository.save(User.createNew(googleId, googleId + "@example.com", "Test User"));
  }

  private void saveTransaction(UserId userId, CategoryId categoryId, String amount) {
    saveTransactionOnDate(userId, categoryId, amount, LocalDate.of(2026, 1, 10));
  }

  private void saveTransactionOnDate(
      UserId userId, CategoryId categoryId, String amount, LocalDate date) {
    transactionRepository.save(
        new Transaction(
            null,
            userId,
            date,
            new Money(new BigDecimal(amount)),
            categoryId,
            NeedWantType.NEED,
            null,
            null,
            null,
            null));
  }

  private void saveTransactionWithType(
      UserId userId, CategoryId categoryId, String amount, NeedWantType type) {
    saveTransactionWithTypeOnDate(userId, categoryId, amount, type, LocalDate.of(2026, 1, 10));
  }

  private void saveTransactionWithTypeOnDate(
      UserId userId, CategoryId categoryId, String amount, NeedWantType type, LocalDate date) {
    transactionRepository.save(
        new Transaction(
            null,
            userId,
            date,
            new Money(new BigDecimal(amount)),
            categoryId,
            type,
            null,
            null,
            null,
            null));
  }

  private CategoryBreakdown findByCategory(
      List<CategoryBreakdown> breakdowns, CategoryId categoryId) {
    return breakdowns.stream()
        .filter(b -> b.categoryId().equals(categoryId))
        .findFirst()
        .orElseThrow();
  }

  private NeedWantBreakdown findByType(List<NeedWantBreakdown> breakdowns, NeedWantType type) {
    return breakdowns.stream().filter(b -> b.type().equals(type)).findFirst().orElseThrow();
  }
}

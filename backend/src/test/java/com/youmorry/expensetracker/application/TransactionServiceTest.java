package com.youmorry.expensetracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.youmorry.expensetracker.domain.category.Category;
import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.category.CategoryRepository;
import com.youmorry.expensetracker.domain.transaction.Money;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import com.youmorry.expensetracker.domain.transaction.Transaction;
import com.youmorry.expensetracker.domain.transaction.TransactionId;
import com.youmorry.expensetracker.domain.transaction.TransactionRepository;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.shared.exception.ValidationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  @Mock private TransactionRepository transactionRepository;
  @Mock private CategoryRepository categoryRepository;
  @InjectMocks private TransactionService transactionService;

  @Test
  void create_withAllFields_returnsTransactionResult() {
    var userId = new UserId(1L);
    var categoryId = new CategoryId(1L);
    var category = new Category(categoryId, "Food", 1);
    var command =
        new TransactionCreateCommand(
            LocalDate.of(2026, 3, 25),
            new BigDecimal("1200"),
            1L,
            NeedWantType.NEED,
            "Lunch",
            "with friends");
    var savedTransaction =
        new Transaction(
            new TransactionId(42L),
            userId,
            LocalDate.of(2026, 3, 25),
            new Money(new BigDecimal("1200")),
            categoryId,
            NeedWantType.NEED,
            "Lunch",
            "with friends",
            Instant.parse("2026-03-25T10:00:00Z"),
            Instant.parse("2026-03-25T10:00:00Z"));
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

    var result = transactionService.create(userId, command);

    assertEquals(savedTransaction, result.transaction());
    assertEquals("Food", result.categoryName());
    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  void create_withoutCategoryId_usesUncategorized() {
    var userId = new UserId(1L);
    var uncategorizedId = new CategoryId(11L);
    var uncategorized = new Category(uncategorizedId, "Uncategorized", 11);
    var command =
        new TransactionCreateCommand(
            LocalDate.of(2026, 3, 25),
            new BigDecimal("500"),
            null,
            NeedWantType.WANT,
            "Coffee",
            null);
    var savedTransaction =
        new Transaction(
            new TransactionId(43L),
            userId,
            LocalDate.of(2026, 3, 25),
            new Money(new BigDecimal("500")),
            uncategorizedId,
            NeedWantType.WANT,
            "Coffee",
            null,
            Instant.parse("2026-03-25T10:00:00Z"),
            Instant.parse("2026-03-25T10:00:00Z"));
    when(categoryRepository.findById(uncategorizedId)).thenReturn(Optional.of(uncategorized));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

    var result = transactionService.create(userId, command);

    assertEquals(uncategorizedId, result.transaction().categoryId());
    assertEquals("Uncategorized", result.categoryName());
  }

  @Test
  void create_withoutNeedWantType_usesUnset() {
    var userId = new UserId(1L);
    var categoryId = new CategoryId(2L);
    var category = new Category(categoryId, "Transport", 2);
    var command =
        new TransactionCreateCommand(
            LocalDate.of(2026, 3, 25), new BigDecimal("300"), 2L, null, "Bus", null);
    var savedTransaction =
        new Transaction(
            new TransactionId(44L),
            userId,
            LocalDate.of(2026, 3, 25),
            new Money(new BigDecimal("300")),
            categoryId,
            NeedWantType.UNSET,
            "Bus",
            null,
            Instant.parse("2026-03-25T10:00:00Z"),
            Instant.parse("2026-03-25T10:00:00Z"));
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

    var result = transactionService.create(userId, command);

    assertEquals(NeedWantType.UNSET, result.transaction().needWantType());
  }

  @Test
  void create_withInvalidCategoryId_throwsValidationException() {
    var userId = new UserId(1L);
    var command =
        new TransactionCreateCommand(
            LocalDate.of(2026, 3, 25),
            new BigDecimal("1000"),
            999L,
            NeedWantType.NEED,
            "Test",
            null);
    when(categoryRepository.findById(new CategoryId(999L))).thenReturn(Optional.empty());

    assertThrows(ValidationException.class, () -> transactionService.create(userId, command));
  }

  @Test
  void create_withNegativeAmount_throwsValidationException() {
    var userId = new UserId(1L);
    var command =
        new TransactionCreateCommand(
            LocalDate.of(2026, 3, 25), new BigDecimal("-100"), 1L, NeedWantType.NEED, "Test", null);

    assertThrows(ValidationException.class, () -> transactionService.create(userId, command));
  }
}

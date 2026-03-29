package com.youmorry.expensetracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.youmorry.expensetracker.shared.exception.ResourceNotFoundException;

import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.category.CategoryType;
import com.youmorry.expensetracker.domain.transaction.Money;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import com.youmorry.expensetracker.domain.transaction.Transaction;
import com.youmorry.expensetracker.domain.transaction.TransactionId;
import com.youmorry.expensetracker.domain.transaction.TransactionRepository;
import com.youmorry.expensetracker.domain.transaction.TransactionSearchRepository;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.shared.exception.ValidationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  @Mock private TransactionRepository transactionRepository;
  @Mock private TransactionSearchRepository transactionSearchRepository;
  @InjectMocks private TransactionService transactionService;

  @Test
  void create_withAllFields_returnsTransactionResult() {
    var userId = new UserId(1L);
    var categoryId = new CategoryId(1L);
    var command =
        new TransactionCreateCommand(
            LocalDate.of(2026, 3, 25),
            new BigDecimal("1200"),
            new CategoryId(1L),
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
    when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

    var result = transactionService.create(userId, command);

    assertEquals(savedTransaction, result.transaction());
    assertEquals("Food", result.categoryName());
    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  void create_withoutCategoryId_usesUncategorized() {
    var userId = new UserId(1L);
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
            CategoryType.UNCATEGORIZED.id(),
            NeedWantType.WANT,
            "Coffee",
            null,
            Instant.parse("2026-03-25T10:00:00Z"),
            Instant.parse("2026-03-25T10:00:00Z"));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

    var result = transactionService.create(userId, command);

    assertEquals(CategoryType.UNCATEGORIZED.id(), result.transaction().categoryId());
    assertEquals("Uncategorized", result.categoryName());
  }

  @Test
  void create_withoutNeedWantType_usesUnset() {
    var userId = new UserId(1L);
    var categoryId = new CategoryId(2L);
    var command =
        new TransactionCreateCommand(
            LocalDate.of(2026, 3, 25),
            new BigDecimal("300"),
            new CategoryId(2L),
            null,
            "Bus",
            null);
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
            new CategoryId(999L),
            NeedWantType.NEED,
            "Test",
            null);

    assertThrows(ValidationException.class, () -> transactionService.create(userId, command));
  }

  @Test
  void search_withQuery_returnsResultList() {
    var userId = new UserId(1L);
    var query = new TransactionSearchQuery(null, null, List.of(), null, null);
    var transaction1 =
        new Transaction(
            new TransactionId(1L),
            userId,
            LocalDate.of(2026, 3, 25),
            new Money(new BigDecimal("500")),
            new CategoryId(1L),
            NeedWantType.NEED,
            "Lunch",
            null,
            Instant.parse("2026-03-25T10:00:00Z"),
            Instant.parse("2026-03-25T10:00:00Z"));
    var transaction2 =
        new Transaction(
            new TransactionId(2L),
            userId,
            LocalDate.of(2026, 3, 24),
            new Money(new BigDecimal("300")),
            new CategoryId(2L),
            NeedWantType.WANT,
            "Bus",
            null,
            Instant.parse("2026-03-24T10:00:00Z"),
            Instant.parse("2026-03-24T10:00:00Z"));
    when(transactionSearchRepository.search(userId, query.toCriteria()))
        .thenReturn(List.of(transaction1, transaction2));

    var results = transactionService.search(userId, query);

    assertEquals(2, results.size());
    assertEquals("Food", results.get(0).categoryName());
    assertEquals("Transport", results.get(1).categoryName());
  }

  @Test
  void findById_withExistingTransaction_returnsTransactionResult() {
    var userId = new UserId(1L);
    var transactionId = new TransactionId(42L);
    var transaction =
        new Transaction(
            transactionId,
            userId,
            LocalDate.of(2026, 3, 25),
            new Money(new BigDecimal("1200")),
            new CategoryId(1L),
            NeedWantType.NEED,
            "Lunch",
            "with friends",
            Instant.parse("2026-03-25T10:00:00Z"),
            Instant.parse("2026-03-25T10:00:00Z"));
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

    var result = transactionService.findById(userId, transactionId);

    assertEquals(transaction, result.transaction());
    assertEquals("Food", result.categoryName());
  }

  @Test
  void findById_withNonExistentTransaction_throwsResourceNotFoundException() {
    var userId = new UserId(1L);
    var transactionId = new TransactionId(999L);
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> transactionService.findById(userId, transactionId));
  }

  @Test
  void findById_withOtherUsersTransaction_throwsResourceNotFoundException() {
    var userId = new UserId(1L);
    var otherUserId = new UserId(2L);
    var transactionId = new TransactionId(42L);
    var transaction =
        new Transaction(
            transactionId,
            otherUserId,
            LocalDate.of(2026, 3, 25),
            new Money(new BigDecimal("1200")),
            new CategoryId(1L),
            NeedWantType.NEED,
            "Lunch",
            null,
            Instant.parse("2026-03-25T10:00:00Z"),
            Instant.parse("2026-03-25T10:00:00Z"));
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

    assertThrows(
        ResourceNotFoundException.class,
        () -> transactionService.findById(userId, transactionId));
  }

  @Test
  void update_withValidCommand_returnsUpdatedTransactionResult() {
    var userId = new UserId(1L);
    var transactionId = new TransactionId(42L);
    var existing =
        new Transaction(
            transactionId,
            userId,
            LocalDate.of(2026, 3, 25),
            new Money(new BigDecimal("1200")),
            new CategoryId(1L),
            NeedWantType.NEED,
            "Lunch",
            "with friends",
            Instant.parse("2026-03-25T10:00:00Z"),
            Instant.parse("2026-03-25T10:00:00Z"));
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existing));
    var command =
        new TransactionUpdateCommand(
            LocalDate.of(2026, 3, 26),
            new BigDecimal("1500"),
            new CategoryId(2L),
            NeedWantType.WANT,
            "Dinner",
            "at restaurant");
    var savedTransaction =
        new Transaction(
            transactionId,
            userId,
            LocalDate.of(2026, 3, 26),
            new Money(new BigDecimal("1500")),
            new CategoryId(2L),
            NeedWantType.WANT,
            "Dinner",
            "at restaurant",
            Instant.parse("2026-03-25T10:00:00Z"),
            Instant.parse("2026-03-26T10:00:00Z"));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

    var result = transactionService.update(userId, transactionId, command);

    assertEquals(savedTransaction, result.transaction());
    assertEquals("Transport", result.categoryName());
  }

  @Test
  void update_withNonExistentTransaction_throwsResourceNotFoundException() {
    var userId = new UserId(1L);
    var transactionId = new TransactionId(999L);
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());
    var command =
        new TransactionUpdateCommand(
            LocalDate.of(2026, 3, 26), new BigDecimal("1500"), null, null, null, null);

    assertThrows(
        ResourceNotFoundException.class,
        () -> transactionService.update(userId, transactionId, command));
  }

  @Test
  void update_withOtherUsersTransaction_throwsResourceNotFoundException() {
    var userId = new UserId(1L);
    var otherUserId = new UserId(2L);
    var transactionId = new TransactionId(42L);
    var transaction =
        new Transaction(
            transactionId,
            otherUserId,
            LocalDate.of(2026, 3, 25),
            new Money(new BigDecimal("1200")),
            new CategoryId(1L),
            NeedWantType.NEED,
            "Lunch",
            null,
            Instant.parse("2026-03-25T10:00:00Z"),
            Instant.parse("2026-03-25T10:00:00Z"));
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
    var command =
        new TransactionUpdateCommand(
            LocalDate.of(2026, 3, 26), new BigDecimal("1500"), null, null, null, null);

    assertThrows(
        ResourceNotFoundException.class,
        () -> transactionService.update(userId, transactionId, command));
  }

  @Test
  void update_withInvalidCategoryId_throwsValidationException() {
    var userId = new UserId(1L);
    var transactionId = new TransactionId(42L);
    var existing =
        new Transaction(
            transactionId,
            userId,
            LocalDate.of(2026, 3, 25),
            new Money(new BigDecimal("1200")),
            new CategoryId(1L),
            NeedWantType.NEED,
            "Lunch",
            null,
            Instant.parse("2026-03-25T10:00:00Z"),
            Instant.parse("2026-03-25T10:00:00Z"));
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existing));
    var command =
        new TransactionUpdateCommand(
            LocalDate.of(2026, 3, 26),
            new BigDecimal("1500"),
            new CategoryId(999L),
            NeedWantType.NEED,
            "Test",
            null);

    assertThrows(
        ValidationException.class,
        () -> transactionService.update(userId, transactionId, command));
  }
}

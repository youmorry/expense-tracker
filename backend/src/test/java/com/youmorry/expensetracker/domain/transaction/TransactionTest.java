package com.youmorry.expensetracker.domain.transaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.user.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TransactionTest {

  @Test
  void constructor_withValidArgs_createsTransaction() {
    assertDoesNotThrow(
        () ->
            new Transaction(
                new TransactionId(1L),
                new UserId(1L),
                LocalDate.of(2026, 1, 1),
                new Money(new BigDecimal("1000")),
                new CategoryId(1L),
                NeedWantType.NEED,
                "ランチ",
                "メモ",
                Instant.now(),
                Instant.now()));
  }

  @Test
  void constructor_withNullTitle_createsTransaction() {
    assertDoesNotThrow(
        () ->
            new Transaction(
                new TransactionId(1L),
                new UserId(1L),
                LocalDate.of(2026, 1, 1),
                new Money(new BigDecimal("1000")),
                new CategoryId(1L),
                NeedWantType.NEED,
                null,
                null,
                Instant.now(),
                Instant.now()));
  }

  @Test
  void constructor_withMaxLengthTitle_createsTransaction() {
    var title = "a".repeat(200);

    assertDoesNotThrow(
        () ->
            new Transaction(
                new TransactionId(1L),
                new UserId(1L),
                LocalDate.of(2026, 1, 1),
                new Money(new BigDecimal("1000")),
                new CategoryId(1L),
                NeedWantType.NEED,
                title,
                null,
                Instant.now(),
                Instant.now()));
  }

  @Test
  void constructor_withNullUserId_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new Transaction(
                new TransactionId(1L),
                null,
                LocalDate.of(2026, 1, 1),
                new Money(new BigDecimal("1000")),
                new CategoryId(1L),
                NeedWantType.NEED,
                "ランチ",
                null,
                Instant.now(),
                Instant.now()));
  }

  @Test
  void constructor_withNullDate_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new Transaction(
                new TransactionId(1L),
                new UserId(1L),
                null,
                new Money(new BigDecimal("1000")),
                new CategoryId(1L),
                NeedWantType.NEED,
                "ランチ",
                null,
                Instant.now(),
                Instant.now()));
  }

  @Test
  void constructor_withNullAmount_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new Transaction(
                new TransactionId(1L),
                new UserId(1L),
                LocalDate.of(2026, 1, 1),
                null,
                new CategoryId(1L),
                NeedWantType.NEED,
                "ランチ",
                null,
                Instant.now(),
                Instant.now()));
  }

  @Test
  void constructor_withNullCategoryId_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new Transaction(
                new TransactionId(1L),
                new UserId(1L),
                LocalDate.of(2026, 1, 1),
                new Money(new BigDecimal("1000")),
                null,
                NeedWantType.NEED,
                "ランチ",
                null,
                Instant.now(),
                Instant.now()));
  }

  @Test
  void constructor_withNullNeedWantType_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new Transaction(
                new TransactionId(1L),
                new UserId(1L),
                LocalDate.of(2026, 1, 1),
                new Money(new BigDecimal("1000")),
                new CategoryId(1L),
                null,
                "ランチ",
                null,
                Instant.now(),
                Instant.now()));
  }

  @Test
  void constructor_withNullIdAndTimestamps_createsTransaction() {
    assertDoesNotThrow(
        () ->
            new Transaction(
                null,
                new UserId(1L),
                LocalDate.of(2026, 1, 1),
                new Money(new BigDecimal("1000")),
                new CategoryId(1L),
                NeedWantType.NEED,
                "ランチ",
                "メモ",
                null,
                null));
  }

  @Test
  void constructor_withTitleExceeding200Chars_throwsIllegalArgumentException() {
    var title = "a".repeat(201);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Transaction(
                new TransactionId(1L),
                new UserId(1L),
                LocalDate.of(2026, 1, 1),
                new Money(new BigDecimal("1000")),
                new CategoryId(1L),
                NeedWantType.NEED,
                title,
                null,
                Instant.now(),
                Instant.now()));
  }
}

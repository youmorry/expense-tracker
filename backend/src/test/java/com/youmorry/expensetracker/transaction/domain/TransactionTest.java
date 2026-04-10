package com.youmorry.expensetracker.transaction.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.youmorry.expensetracker.category.domain.CategoryId;
import com.youmorry.expensetracker.user.domain.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

  @ParameterizedTest
  @MethodSource("nullFieldArgs")
  void constructor_withNullRequiredField_throwsNullPointerException(
      UserId userId,
      LocalDate date,
      Money amount,
      CategoryId categoryId,
      NeedWantType needWantType) {
    assertThrows(
        NullPointerException.class,
        () ->
            new Transaction(
                new TransactionId(1L),
                userId,
                date,
                amount,
                categoryId,
                needWantType,
                "ランチ",
                null,
                Instant.now(),
                Instant.now()));
  }

  static Stream<Arguments> nullFieldArgs() {
    return Stream.of(
        Arguments.of(
            null,
            LocalDate.of(2026, 1, 1),
            new Money(new BigDecimal("1000")),
            new CategoryId(1L),
            NeedWantType.NEED),
        Arguments.of(
            new UserId(1L),
            null,
            new Money(new BigDecimal("1000")),
            new CategoryId(1L),
            NeedWantType.NEED),
        Arguments.of(
            new UserId(1L), LocalDate.of(2026, 1, 1), null, new CategoryId(1L), NeedWantType.NEED),
        Arguments.of(
            new UserId(1L),
            LocalDate.of(2026, 1, 1),
            new Money(new BigDecimal("1000")),
            null,
            NeedWantType.NEED),
        Arguments.of(
            new UserId(1L),
            LocalDate.of(2026, 1, 1),
            new Money(new BigDecimal("1000")),
            new CategoryId(1L),
            null));
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

  @Test
  void constructor_withMaxLengthMemo_createsTransaction() {
    var memo = "a".repeat(2000);

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
                memo,
                Instant.now(),
                Instant.now()));
  }

  @Test
  void constructor_withMemoExceeding2000Chars_throwsIllegalArgumentException() {
    var memo = "a".repeat(2001);

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
                "ランチ",
                memo,
                Instant.now(),
                Instant.now()));
  }
}

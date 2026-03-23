package com.youmorry.expensetracker.infrastructure.persistence;

import com.youmorry.expensetracker.domain.model.category.CategoryId;
import com.youmorry.expensetracker.domain.model.transaction.TransactionId;
import com.youmorry.expensetracker.domain.model.user.UserId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

/**
 * Spring Data JDBC 用の ID 型コンバーター。
 *
 * <p>{@code Long ↔ CategoryId}, {@code Long ↔ UserId}, {@code Long ↔ TransactionId} の変換を提供する。
 */
final class IdConverters {

  private IdConverters() {}

  @ReadingConverter
  enum LongToCategoryId implements Converter<Long, CategoryId> {
    INSTANCE;

    @Override
    public CategoryId convert(Long source) {
      return new CategoryId(source);
    }
  }

  @WritingConverter
  enum CategoryIdToLong implements Converter<CategoryId, Long> {
    INSTANCE;

    @Override
    public Long convert(CategoryId source) {
      return source.value();
    }
  }

  @ReadingConverter
  enum LongToUserId implements Converter<Long, UserId> {
    INSTANCE;

    @Override
    public UserId convert(Long source) {
      return new UserId(source);
    }
  }

  @WritingConverter
  enum UserIdToLong implements Converter<UserId, Long> {
    INSTANCE;

    @Override
    public Long convert(UserId source) {
      return source.value();
    }
  }

  @ReadingConverter
  enum LongToTransactionId implements Converter<Long, TransactionId> {
    INSTANCE;

    @Override
    public TransactionId convert(Long source) {
      return new TransactionId(source);
    }
  }

  @WritingConverter
  enum TransactionIdToLong implements Converter<TransactionId, Long> {
    INSTANCE;

    @Override
    public Long convert(TransactionId source) {
      return source.value();
    }
  }
}

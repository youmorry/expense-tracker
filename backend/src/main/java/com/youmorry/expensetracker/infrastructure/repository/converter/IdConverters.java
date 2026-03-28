package com.youmorry.expensetracker.infrastructure.repository.converter;

import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.transaction.TransactionId;
import com.youmorry.expensetracker.domain.user.UserId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

/**
 * Spring Data JDBC 用の ID 型コンバーター。
 *
 * <p>{@code Long ↔ CategoryId}, {@code Long ↔ UserId}, {@code Long ↔ TransactionId} の変換を提供する。
 */
@SuppressWarnings("checkstyle:MissingJavadocType")
public final class IdConverters {

  private IdConverters() {}

  @ReadingConverter
  public enum LongToCategoryId implements Converter<Long, CategoryId> {
    INSTANCE;

    @Override
    public CategoryId convert(Long source) {
      return new CategoryId(source);
    }
  }

  @WritingConverter
  public enum CategoryIdToLong implements Converter<CategoryId, Long> {
    INSTANCE;

    @Override
    public Long convert(CategoryId source) {
      return source.value();
    }
  }

  @ReadingConverter
  public enum LongToUserId implements Converter<Long, UserId> {
    INSTANCE;

    @Override
    public UserId convert(Long source) {
      return new UserId(source);
    }
  }

  @WritingConverter
  public enum UserIdToLong implements Converter<UserId, Long> {
    INSTANCE;

    @Override
    public Long convert(UserId source) {
      return source.value();
    }
  }

  @ReadingConverter
  public enum LongToTransactionId implements Converter<Long, TransactionId> {
    INSTANCE;

    @Override
    public TransactionId convert(Long source) {
      return new TransactionId(source);
    }
  }

  @WritingConverter
  public enum TransactionIdToLong implements Converter<TransactionId, Long> {
    INSTANCE;

    @Override
    public Long convert(TransactionId source) {
      return source.value();
    }
  }
}

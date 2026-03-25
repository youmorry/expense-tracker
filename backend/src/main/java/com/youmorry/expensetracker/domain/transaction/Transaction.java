package com.youmorry.expensetracker.domain.transaction;

import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.user.UserId;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** ユーザーが記録する1件の支出を表すエンティティ。 */
@Table("transactions")
public record Transaction(
    @Id @Nullable TransactionId id,
    UserId userId,
    LocalDate date,
    @Column("amount") Money amount,
    CategoryId categoryId,
    NeedWantType needWantType,
    String title,
    String memo,
    @CreatedDate @Nullable Instant createdAt,
    @LastModifiedDate @Nullable Instant updatedAt) {

  private static final int TITLE_MAX_LENGTH = 200;

  /** 不変条件を検証する。 */
  public Transaction {
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(date, "date must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(categoryId, "categoryId must not be null");
    Objects.requireNonNull(needWantType, "needWantType must not be null");
    if (title != null && title.length() > TITLE_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "title must not exceed " + TITLE_MAX_LENGTH + " characters, but was: " + title.length());
    }
  }
}

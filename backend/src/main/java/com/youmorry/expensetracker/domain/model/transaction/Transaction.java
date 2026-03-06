package com.youmorry.expensetracker.domain.model.transaction;

import com.youmorry.expensetracker.domain.model.category.CategoryId;
import com.youmorry.expensetracker.domain.model.user.UserId;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

/** ユーザーが記録する1件の支出を表すエンティティ。 */
@Table("transactions")
public record Transaction(
    @Id TransactionId id,
    UserId userId,
    LocalDate date,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL) Money amount,
    CategoryId categoryId,
    NeedWantType needWantType,
    String title,
    String memo,
    Instant createdAt,
    Instant updatedAt) {}

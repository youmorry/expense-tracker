package com.youmorry.expensetracker.domain.transaction;

import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.user.domain.UserId;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * ユーザーが記録する1件の支出を表すエンティティ。
 *
 * @param id 支出記録 ID（永続化時に採番）
 * @param userId 記録したユーザーの ID
 * @param date 支出日
 * @param amount 金額
 * @param categoryId カテゴリ ID
 * @param needWantType 必要/欲しい 区分
 * @param title タイトル
 * @param memo メモ
 * @param createdAt 作成日時（永続化時に設定）
 * @param updatedAt 更新日時（永続化時に設定）
 */
@Table("transactions")
public record Transaction(
    @Id @Nullable TransactionId id,
    UserId userId,
    LocalDate date,
    @Column("amount") Money amount,
    CategoryId categoryId,
    NeedWantType needWantType,
    @Nullable String title,
    @Nullable String memo,
    @CreatedDate @Nullable Instant createdAt,
    @LastModifiedDate @Nullable Instant updatedAt) {

  private static final int TITLE_MAX_LENGTH = 200;
  private static final int MEMO_MAX_LENGTH = 2000;

  /**
   * 不変条件を検証する。
   *
   * @throws NullPointerException userId, date, amount, categoryId, needWantType が null の場合
   * @throws IllegalArgumentException title が 200 文字、または memo が 2000 文字を超過する場合
   */
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
    if (memo != null && memo.length() > MEMO_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "memo must not exceed " + MEMO_MAX_LENGTH + " characters, but was: " + memo.length());
    }
  }
}

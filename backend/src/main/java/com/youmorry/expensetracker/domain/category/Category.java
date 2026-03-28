package com.youmorry.expensetracker.domain.category;

import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 支出を分類するカテゴリのエンティティ。
 *
 * @param id カテゴリ ID
 * @param name カテゴリ名
 * @param displayOrder 表示順
 */
@Table("categories")
public record Category(@Id CategoryId id, String name, int displayOrder) {

  private static final int NAME_MAX_LENGTH = 50;

  /**
   * 不変条件を検証する。
   *
   * @throws NullPointerException name が null の場合
   * @throws IllegalArgumentException name が空白、50 文字超過、または displayOrder が 0 以下の場合
   */
  public Category {
    Objects.requireNonNull(name, "name must not be null");
    if (name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (name.length() > NAME_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "name must not exceed " + NAME_MAX_LENGTH + " characters, but was: " + name.length());
    }
    if (displayOrder <= 0) {
      throw new IllegalArgumentException("displayOrder must be positive, but was: " + displayOrder);
    }
  }
}

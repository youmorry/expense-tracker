package com.youmorry.expensetracker.domain.model.category;

import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** 支出を分類するカテゴリのエンティティ。 */
@Table("categories")
public record Category(@Id CategoryId id, String name, Integer displayOrder) {

  private static final int NAME_MAX_LENGTH = 50;

  /** 不変条件を検証する。 */
  public Category {
    Objects.requireNonNull(name, "name must not be null");
    if (name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (name.length() > NAME_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "name must not exceed " + NAME_MAX_LENGTH + " characters, but was: " + name.length());
    }
    Objects.requireNonNull(displayOrder, "displayOrder must not be null");
    if (displayOrder <= 0) {
      throw new IllegalArgumentException("displayOrder must be positive, but was: " + displayOrder);
    }
  }
}

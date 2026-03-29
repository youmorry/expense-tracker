package com.youmorry.expensetracker.domain.category;

import java.util.Arrays;

/**
 * 支出を分類するカテゴリの列挙型。
 *
 * <p>各定数はデータベースの {@code categories} テーブルに対応する ID・表示名・表示順を保持する。
 */
public enum CategoryType {
  FOOD(1L, "Food", 1),
  TRANSPORT(2L, "Transport", 2),
  HOUSING(3L, "Housing", 3),
  DAILY_GOODS(4L, "Daily Goods", 4),
  MEDICAL(5L, "Medical", 5),
  ENTERTAINMENT(6L, "Entertainment", 6),
  CLOTHING(7L, "Clothing", 7),
  EDUCATION(8L, "Education", 8),
  SOCIAL(9L, "Social", 9),
  OTHER(10L, "Other", 10),
  UNCATEGORIZED(11L, "Uncategorized", 11);

  private final CategoryId id;
  private final String displayName;
  private final int displayOrder;

  CategoryType(long id, String displayName, int displayOrder) {
    this.id = new CategoryId(id);
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  /**
   * カテゴリ ID を返す。
   *
   * @return カテゴリ ID
   */
  public CategoryId id() {
    return id;
  }

  /**
   * 表示名を返す。
   *
   * @return 表示名
   */
  public String displayName() {
    return displayName;
  }

  /**
   * 表示順を返す。
   *
   * @return 表示順
   */
  public int displayOrder() {
    return displayOrder;
  }

  /**
   * カテゴリ ID から対応する {@link CategoryType} を返す。
   *
   * @param id カテゴリ ID
   * @return 対応する CategoryType
   * @throws IllegalArgumentException 該当するカテゴリが存在しない場合
   */
  public static CategoryType fromId(CategoryId id) {
    return Arrays.stream(values())
        .filter(type -> type.id.equals(id))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown category id: " + id.value()));
  }
}

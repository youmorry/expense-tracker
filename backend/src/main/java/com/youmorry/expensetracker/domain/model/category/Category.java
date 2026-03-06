package com.youmorry.expensetracker.domain.model.category;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** 支出を分類するカテゴリのエンティティ。 */
@Table("categories")
public record Category(
    @Id CategoryId id,
    String name,
    Integer displayOrder) {}

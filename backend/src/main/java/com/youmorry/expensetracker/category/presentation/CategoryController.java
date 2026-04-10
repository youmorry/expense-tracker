package com.youmorry.expensetracker.category.presentation;

import com.youmorry.expensetracker.category.application.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** カテゴリエンドポイント。 */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

  private final CategoryService categoryService;

  /**
   * コンストラクタ。
   *
   * @param categoryService カテゴリサービス
   */
  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  /**
   * プリセットカテゴリの一覧を display_order 昇順で返す。
   *
   * @return カテゴリ一覧
   */
  @GetMapping
  public ResponseEntity<CategoryListResponse> getCategories() {
    return ResponseEntity.ok(CategoryListResponse.from(categoryService.findAll()));
  }
}

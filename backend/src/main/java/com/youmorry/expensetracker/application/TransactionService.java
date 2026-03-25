package com.youmorry.expensetracker.application;

import com.youmorry.expensetracker.domain.category.CategoryId;
import com.youmorry.expensetracker.domain.category.CategoryRepository;
import com.youmorry.expensetracker.domain.transaction.Money;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import com.youmorry.expensetracker.domain.transaction.Transaction;
import com.youmorry.expensetracker.domain.transaction.TransactionRepository;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.shared.exception.ValidationException;
import com.youmorry.expensetracker.shared.exception.ValidationException.FieldError;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 支出の登録・取得・更新・削除のユースケースを実装する。 */
@Service
public class TransactionService {

  private static final CategoryId UNCATEGORIZED_ID = new CategoryId(11L);

  private final TransactionRepository transactionRepository;
  private final CategoryRepository categoryRepository;

  /**
   * コンストラクタ。
   *
   * @param transactionRepository トランザクションリポジトリ
   * @param categoryRepository カテゴリリポジトリ
   */
  public TransactionService(
      TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
    this.transactionRepository = transactionRepository;
    this.categoryRepository = categoryRepository;
  }

  /**
   * 支出を登録する。
   *
   * @param userId ユーザー ID
   * @param command 登録コマンド
   * @return 登録結果
   * @throws ValidationException 金額が不正、またはカテゴリが存在しない場合
   */
  @Transactional
  public TransactionResult create(UserId userId, TransactionCreateCommand command) {
    var categoryId =
        command.categoryId() != null ? new CategoryId(command.categoryId()) : UNCATEGORIZED_ID;
    var needWantType = command.needWantType() != null ? command.needWantType() : NeedWantType.UNSET;

    Money amount;
    try {
      amount = new Money(new BigDecimal(command.amount()));
    } catch (IllegalArgumentException e) {
      throw new ValidationException(
          e.getMessage(), List.of(new FieldError("Invalid amount.", "#/amount")));
    }

    var category =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(
                () ->
                    new ValidationException(
                        "Category not found: " + categoryId.value(),
                        List.of(new FieldError("Category does not exist.", "#/category_id"))));

    var transaction =
        new Transaction(
            null,
            userId,
            command.date(),
            amount,
            categoryId,
            needWantType,
            command.title(),
            command.memo(),
            null,
            null);
    var saved = transactionRepository.save(transaction);
    return new TransactionResult(saved, category.name());
  }
}

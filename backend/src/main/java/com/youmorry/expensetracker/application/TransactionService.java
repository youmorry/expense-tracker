package com.youmorry.expensetracker.application;

import com.youmorry.expensetracker.domain.category.CategoryType;
import com.youmorry.expensetracker.domain.transaction.Money;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import com.youmorry.expensetracker.domain.transaction.Transaction;
import com.youmorry.expensetracker.domain.transaction.TransactionRepository;
import com.youmorry.expensetracker.domain.transaction.TransactionSearchRepository;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.shared.exception.ValidationException;
import com.youmorry.expensetracker.shared.exception.ValidationException.FieldError;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 支出の登録・取得・更新・削除のユースケースを実装する。 */
@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final TransactionSearchRepository transactionSearchRepository;

  /**
   * コンストラクタ。
   *
   * @param transactionRepository トランザクションリポジトリ
   * @param transactionSearchRepository 支出検索リポジトリ
   */
  public TransactionService(
      TransactionRepository transactionRepository,
      TransactionSearchRepository transactionSearchRepository) {
    this.transactionRepository = transactionRepository;
    this.transactionSearchRepository = transactionSearchRepository;
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
        command.categoryId() != null ? command.categoryId() : CategoryType.UNCATEGORIZED.id();
    var needWantType = command.needWantType() != null ? command.needWantType() : NeedWantType.UNSET;

    var amount = new Money(command.amount());

    CategoryType categoryType;
    try {
      categoryType = CategoryType.fromId(categoryId);
    } catch (IllegalArgumentException e) {
      throw new ValidationException(
          "Category not found: " + categoryId.value(),
          List.of(new FieldError("Category does not exist.", "categoryId")));
    }

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
    return new TransactionResult(saved, categoryType.displayName());
  }

  /**
   * 支出を検索条件に基づいて取得する。
   *
   * @param userId ユーザー ID
   * @param query 検索クエリ
   * @return 検索結果のリスト
   */
  @Transactional(readOnly = true)
  public List<TransactionResult> search(UserId userId, TransactionSearchQuery query) {
    return transactionSearchRepository.search(userId, query.toCriteria()).stream()
        .map(tx -> new TransactionResult(tx, CategoryType.fromId(tx.categoryId()).displayName()))
        .toList();
  }
}

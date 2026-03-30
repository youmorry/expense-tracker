package com.youmorry.expensetracker.application;

import com.youmorry.expensetracker.domain.category.CategoryType;
import com.youmorry.expensetracker.domain.transaction.Money;
import com.youmorry.expensetracker.domain.transaction.NeedWantType;
import com.youmorry.expensetracker.domain.transaction.Transaction;
import com.youmorry.expensetracker.domain.transaction.TransactionId;
import com.youmorry.expensetracker.domain.transaction.TransactionRepository;
import com.youmorry.expensetracker.domain.transaction.TransactionSearchRepository;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.shared.exception.ResourceNotFoundException;
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
   * 支出を ID で取得する。所有者チェック付き。
   *
   * @param userId ユーザー ID
   * @param transactionId 支出 ID
   * @return 支出結果
   * @throws ResourceNotFoundException 支出が存在しない、または他ユーザーの支出の場合
   */
  @Transactional(readOnly = true)
  public TransactionResult findById(UserId userId, TransactionId transactionId) {
    var transaction = findByIdAndVerifyOwnership(userId, transactionId);
    return new TransactionResult(
        transaction, CategoryType.fromId(transaction.categoryId()).displayName());
  }

  private Transaction findByIdAndVerifyOwnership(UserId userId, TransactionId transactionId) {
    return transactionRepository
        .findById(transactionId)
        .filter(tx -> tx.userId().equals(userId))
        .orElseThrow(
            () -> new ResourceNotFoundException("Transaction not found: " + transactionId.value()));
  }

  /**
   * 支出を全量更新する。
   *
   * @param userId ユーザー ID
   * @param transactionId 支出 ID
   * @param command 更新コマンド
   * @return 更新結果
   * @throws ResourceNotFoundException 支出が存在しない、または他ユーザーの支出の場合
   * @throws ValidationException カテゴリが存在しない場合
   */
  @Transactional
  public TransactionResult update(
      UserId userId, TransactionId transactionId, TransactionUpdateCommand command) {
    var existing = findByIdAndVerifyOwnership(userId, transactionId);

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

    var updated =
        new Transaction(
            existing.id(),
            userId,
            command.date(),
            amount,
            categoryId,
            needWantType,
            command.title(),
            command.memo(),
            existing.createdAt(),
            null);
    var saved = transactionRepository.save(updated);
    return new TransactionResult(saved, categoryType.displayName());
  }

  /**
   * 支出を削除する。所有者チェック付き。
   *
   * @param userId ユーザー ID
   * @param transactionId 支出 ID
   * @throws ResourceNotFoundException 支出が存在しない、または他ユーザーの支出の場合
   */
  @Transactional
  public void delete(UserId userId, TransactionId transactionId) {
    findByIdAndVerifyOwnership(userId, transactionId);
    transactionRepository.deleteById(transactionId);
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

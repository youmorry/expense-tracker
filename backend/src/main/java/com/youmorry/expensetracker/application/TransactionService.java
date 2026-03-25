package com.youmorry.expensetracker.application;

import com.youmorry.expensetracker.domain.category.CategoryRepository;
import com.youmorry.expensetracker.domain.transaction.TransactionRepository;
import com.youmorry.expensetracker.domain.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 支出の登録・取得・更新・削除のユースケースを実装する。 */
@Service
public class TransactionService {

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
   */
  @Transactional
  public TransactionResult create(UserId userId, TransactionCreateCommand command) {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}

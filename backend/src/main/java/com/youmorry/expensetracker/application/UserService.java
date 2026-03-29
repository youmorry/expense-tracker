package com.youmorry.expensetracker.application;

import com.youmorry.expensetracker.domain.user.User;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.domain.user.UserRepository;
import com.youmorry.expensetracker.shared.exception.ResourceNotFoundException;
import java.util.Currency;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** ユーザー情報の取得・更新・削除のユースケースを実装する。 */
@Service
public class UserService {

  private final UserRepository userRepository;

  /**
   * コンストラクタ。
   *
   * @param userRepository ユーザーリポジトリ
   */
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * 指定されたユーザーの情報を取得する。
   *
   * @param userId ユーザー ID
   * @return ユーザー
   * @throws ResourceNotFoundException ユーザーが存在しない場合
   */
  @Transactional(readOnly = true)
  public User getMe(UserId userId) {
    return findUserOrThrow(userId);
  }

  /**
   * ユーザーの通貨コードを更新する。
   *
   * @param userId ユーザー ID
   * @param currencyCode 通貨コード
   * @return 更新されたユーザー
   * @throws ResourceNotFoundException ユーザーが存在しない場合
   */
  @Transactional
  public User updateCurrency(UserId userId, Currency currencyCode) {
    var user = findUserOrThrow(userId);
    var updated = user.changeCurrencyCode(currencyCode);
    return userRepository.save(updated);
  }

  /**
   * ユーザーアカウントを削除する。関連するトランザクションも CASCADE で削除される。
   *
   * @param userId ユーザー ID
   * @throws ResourceNotFoundException ユーザーが存在しない場合
   */
  @Transactional
  public void deleteAccount(UserId userId) {
    findUserOrThrow(userId);
    userRepository.deleteById(userId);
  }

  private User findUserOrThrow(UserId userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found."));
  }
}

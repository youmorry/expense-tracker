package com.youmorry.expensetracker.application.port;

/**
 * OAuth ID トークンから取得したユーザー情報を保持する。
 *
 * @param subject プロバイダ側のユーザー一意識別子
 * @param email メールアドレス
 * @param name 表示名
 * @param locale ロケール（通貨コード推定に使用）
 */
public record OauthUserInfo(String subject, String email, String name, String locale) {}

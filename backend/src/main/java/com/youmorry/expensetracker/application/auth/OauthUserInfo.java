package com.youmorry.expensetracker.application.auth;

import org.jspecify.annotations.Nullable;

/**
 * OAuth ID トークンから取得したユーザー情報を保持する。
 *
 * @param subject プロバイダ側のユーザー一意識別子
 * @param email メールアドレス
 * @param name 表示名（プロバイダから取得できない場合は null）
 */
public record OauthUserInfo(String subject, String email, @Nullable String name) {}

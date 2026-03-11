package com.youmorry.expensetracker.infrastructure.security;

/** Google ID トークンの検証結果を保持する。 */
public record GoogleIdTokenPayload(String sub, String email, String name, String locale) {}

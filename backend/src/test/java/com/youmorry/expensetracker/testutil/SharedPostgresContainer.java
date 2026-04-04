package com.youmorry.expensetracker.testutil;

import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * テスト全体で共有する PostgreSQL コンテナ（Singleton Container パターン）。
 *
 * <p>UT（{@code @DataJdbcTest}）と IT（{@code @SpringBootTest}）の両方から参照し、コンテナの重複起動を防止する。
 */
public final class SharedPostgresContainer {

  @SuppressWarnings("resource")
  public static final PostgreSQLContainer INSTANCE = new PostgreSQLContainer("postgres:17");

  static {
    INSTANCE.start();
  }

  private SharedPostgresContainer() {}
}

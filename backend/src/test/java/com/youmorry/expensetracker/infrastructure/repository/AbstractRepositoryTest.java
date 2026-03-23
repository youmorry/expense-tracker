package com.youmorry.expensetracker.infrastructure.repository;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * リポジトリ統合テストの基底クラス。
 *
 * <p>Testcontainers で PostgreSQL コンテナを起動し、{@code @ServiceConnection} で Spring に接続情報を自動注入する。 Flyway
 * マイグレーションはスライステスト起動時に自動実行される。
 */
@Testcontainers
abstract class AbstractRepositoryTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17");
}

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

個人向け支出記録・分析 Web アプリ。モノレポ構成（`backend/` + `frontend/`）。

## Commands

```bash
docker compose up                        # ローカル DB・モックサーバー・Swagger UI 起動

# Backend (cd backend)
./gradlew bootRun                        # 開発サーバー
./gradlew test                           # 全テスト
./gradlew test --tests "com.youmorry.expensetracker.SomeTest"           # クラス単位
./gradlew test --tests "com.youmorry.expensetracker.SomeTest.method"    # メソッド単位
./gradlew build                          # ビルド
./gradlew checkstyleMain checkstyleTest  # Checkstyle
./gradlew spotlessCheck                  # Spotless チェック
./gradlew spotlessApply                  # Spotless 自動修正

# Frontend (cd frontend) ※未実装
npm install
npm run dev                              # 開発サーバー
npm test                                 # テスト
npm run build                            # ビルド
```

## Tech Stack

- Backend: Java 25, Spring Boot 4.0, Spring Data JDBC, Flyway, PostgreSQL
- Frontend: React, TypeScript, Vite, TanStack Query（未実装）
- Infra: Docker Compose（ローカル開発）

## Architecture

アーキテクチャ・設計判断の詳細は `.claude/rules/` を参照（該当コード編集時に自動ロード）:

- `.claude/rules/backend.md` — DDD + 4層アーキテクチャ、API 規約、命名規約
- `.claude/rules/frontend.md` — Feature-Sliced Design、状態管理方針、認証フロー
- `.claude/rules/design-principles.md` — ドメインモデル、クラス・モジュール設計の原則

## Conventions

- ブランチ戦略: GitHub Flow（`main` から feature ブランチを切り、PR でマージ）
- ブランチ命名: `feature/<説明>`（例: `feature/add-expense-api`）
- 言語: コードは英語

## Skills

- `/commit` — プロジェクトのコミットガイドラインに従ってコミットを作成する
- `/create-pr` — プロジェクトの規約に従って GitHub Pull Request を作成する

## Bash コマンド実行ルール

- ファイルの読み書きは Bash ではなく Read / Edit / Write ツールを使う

## 参照ドキュメント

- `docs/01-planning/project-overview.md` — プロジェクト概要
- `docs/01-planning/tech-stack.md` — 技術スタック詳細・選定理由
- `docs/02-requirements/requirements.md` — 要件定義
- `docs/03-design/domain-model.md` — Entity / VO / Aggregate 設計
- `docs/03-design/er-diagram.md` — ER図
- `docs/03-design/api-design.md` — API エンドポイント仕様
- `docs/03-design/error-handling.md` — 例外階層・RFC 9457 レスポンス
- `docs/03-design/database-schema.md` — DDL・インデックス・Flyway
- `docs/03-design/auth-design.md` — 認証フロー・JWT・セキュリティ
- `docs/03-design/screen-flow.md` — 画面遷移
- `docs/03-design/repository-structure.md` — リポジトリ構成
- `docs/java-coding-standards.md` — Java コーディング規約

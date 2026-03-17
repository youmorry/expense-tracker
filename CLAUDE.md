# CLAUDE.md

個人向け支出記録・分析 Web アプリ。モノレポ構成（`backend/` + `frontend/`）。

## Development Philosophy

### Before Implementation

- 新しい機能を実装する前に、以下を必ず確認する：
  1. 既存のコードベース内に同様の実装やユーティリティがないか検索する
  2. 使用しているライブラリ/フレームワークに該当する API や機能がないか確認する
  3. Context7 MCP（`mcp__context7__`）を使ってライブラリのドキュメントを参照する
- 「自分で実装する」より「既存の解決策を使う」を優先する

### Test-Driven Development (TDD)

機能実装は以下のサイクルで進める:

1. **テスト作成** — 期待する振る舞いに基づきテストを書く。実装コードには触れない
2. **Red 確認** — テストを実行し、失敗することを確認する
3. **コミット** — テストが正しいことを確認できた段階でコミットする
4. **実装** — テストをパスさせる最小限のコードを書く。テストは変更しない
5. **Green 確認** — すべてのテストが通過するまで実装を修正する
6. **コミット** — テストと実装を同一コミットにまとめない。テスト→実装の順でコミット履歴を残す

テスト規約の詳細は `.claude/rules/backend.md` の「テスト方針」セクションに従うこと。
※frontendは現在未定義

## Tech Stack

- Backend: Java 25, Spring Boot 4.0, Spring Data JDBC, Flyway, PostgreSQL
- Frontend: React, TypeScript, Vite, TanStack Query（未実装）
- Infra: Docker Compose（ローカル開発）

## Architecture

アーキテクチャ・設計判断の詳細は `.claude/rules/` を参照（該当コード編集時に自動ロード）:

- `.claude/rules/backend.md` — DDD + 4層アーキテクチャ、API 規約、命名規約
- `.claude/rules/frontend.md` — Feature-Sliced Design、状態管理方針、認証フロー
- `.claude/rules/design-principles.md` — ドメインモデル、クラス・モジュール設計の原則

## Workflow

Issue やタスクの作業を開始するとき、以下の手順を守ること:

1. **作業開始**: `/start-issue` スキルを使い、Issue の確認とブランチ作成を行う
2. **実装**: コードを書く
3. **コミット**: `/commit` スキルを使う（コミット分割・メッセージ規約はスキル側で定義済み）
4. **PR 作成**: `/create-pr` スキルを使う

## Conventions

- ブランチ戦略: GitHub Flow
  - `main` は常にデプロイ可能な状態を維持する
  - `main` から feature/hotfix ブランチを切り、PR でマージする
  - マージ後のブランチは削除する
- ブランチ命名:
  - 機能追加: `feature/<説明>`（例: `feature/add-expense-api`）
  - バグ修正: `hotfix/<説明>`（例: `hotfix/fix-date-validation`）
  - 英語・ケバブケースで記述する
- 言語: コードは英語

## Commands

```bash
docker compose up                        # ローカル DB・モックサーバー・Scalar 起動

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

## Skills

- `/start-issue` — GitHub Issue の内容を確認し、ブランチを作成して作業を開始する
- `/commit` — プロジェクトのコミットガイドラインに従ってコミットを作成する
- `/create-pr` — プロジェクトの規約に従って GitHub Pull Request を作成する

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

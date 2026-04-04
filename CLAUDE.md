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
2. **Red 確認** — テストを実行し、期待通りの理由で失敗することを確認する
3. **コミット** — 失敗するテストをコミットする
4. **実装** — テストをパスさせる最小限のコードを書く。テストは変更しない
5. **Green 確認** — すべてのテストが通過するまで実装を修正する
6. **リファクタリング** — テストが通る状態を維持しながら、実装を整理する（必要な場合のみ）
7. **コミット** — 実装（とリファクタリング）をコミットする

※ テストと実装を同一コミットにまとめない。テスト→実装の順でコミット履歴を残す。

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

1. **作業開始〜実装完了**: `/start-issue <番号>` で Issue 確認・計画・ブランチ作成・TDD 実装・ビルド確認まで行う
2. **PR 作成**: `/create-pr` で PR を作成する

## Conventions

- 言語: コード（識別子・メソッド名等）は英語。コメント・Javadoc は日本語可
- ブランチ戦略: GitHub Flow
  - `main` は常にデプロイ可能な状態を維持する
  - `main` から feature/hotfix ブランチを切り、PR でマージする
- ブランチ命名:
  - 機能追加: `feature/<説明>`（例: `feature/add-expense-api`）
  - バグ修正: `hotfix/<説明>`（例: `hotfix/fix-date-validation`）
  - 英語・ケバブケースで記述する

## Commands

### Bash操作のルール

- ファイル操作は絶対パスを優先すること
- 可能な限り `$$, |, $()` などを避け、不要なユーザー承認を避けること
- `gh` する場合は `--repo` は必要ないため使わないこと
  - OK `gh issue view 1`
  - NG `gh issue view 1 --repo /path/to/repo`
- `git` 操作では `cd` しないこと
  - OK `git add /path/to/repo/add-file.java`
  - NG `cd /path/to/repo && git add add-file.java`
- `cd` が必要な場合は事前に `cd` してから後続のコマンドを実行すること
  - OK
    1. `cd /path/to/work`
    2. `./gradlew build`
  - NG `cd /path/to/work && ./gradlew build`

### よく使うコマンド

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

- `.claude/skills/start-issue/SKILL.md` — Issue 確認〜計画〜ブランチ作成〜TDD 実装〜ビルド確認まで
- `.claude/skills/commit/SKILL.md` — プロジェクトのコミットガイドラインに従ってコミットを作成する
- `.claude/skills/create-pr/SKILL.md` — ビルド確認済みの実装を PR として作成する

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

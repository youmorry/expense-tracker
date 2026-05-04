# AGENTS.md

個人向け支出記録・分析 Web アプリ。モノレポ構成（`backend/` + `frontend/`）。本ファイルは AI コーディングエージェント向けのプロジェクトコンテキスト。

不変の原則は `docs/00-constitution.md` を参照する。各レイヤーのルールはサブディレクトリの `AGENTS.md` を参照する（ディレクトリツリーで最も近い `AGENTS.md` が優先される）。

## Tech Stack

- Backend: Java 25, Spring Boot 4.0, Spring Data JDBC, Flyway, PostgreSQL
- Frontend: React 19, TypeScript 6, Vite 8, React Router 7, TanStack Query v5, Tailwind CSS 4, shadcn/ui, Vitest 4
- Infra: Docker Compose（ローカル開発）

## Architecture

- `docs/00-constitution.md` — プロジェクト不変原則（TDD・GitHub Flow・命名・コミット規約・設計の心得）
- `backend/AGENTS.md` — Backend (Spring Boot / Java) のレイヤー設計、API 規約、テスト方針
- `frontend/AGENTS.md` — Frontend (React / TypeScript) の Feature-Sliced Design、状態管理、認証フロー、テスト方針

## Workflow

Issue やタスクの作業を開始するとき、以下の手順を守る。

1. **作業開始〜実装完了**: `/start-issue <番号>` で Issue 確認・計画・ブランチ作成・TDD 実装・ビルド確認まで行う
2. **PR 作成**: `/create-pr` で PR を作成する

## AI エージェントの振る舞い

- ユーザーの入力が曖昧な場合は、AskUserQuestion を積極的に使う

### Context7 MCP の使用

ライブラリ・フレームワーク・API について作業するときは、訓練データではなく Context7 MCP で最新ドキュメントを取得する。セットアップ質問・コード生成・API リファレンス・特定パッケージに関する話題に適用される。

1. `resolve-library-id` でライブラリ名と質問を渡す
2. ベストマッチを選ぶ（バージョン指定があれば優先）
3. `query-docs` で選択したライブラリ ID と質問を渡す
4. 取得したドキュメントを使って回答する（コード例を含め、バージョンを明記）

## Commands

### Bash 操作のルール

- ファイル操作は絶対パスを優先すること
- 可能な限り `$$, |, $()` などを避け、不要なユーザー承認を避けること
- `gh` する場合は `--repo` は必要ないため使わないこと
  - OK `gh issue view 1`
  - NG `gh issue view 1 --repo /path/to/repo`
- `git` 操作では `cd` しないこと
  - OK `git -C /path/to/repo add add-file.java`
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
./gradlew test                           # 単体テスト
./gradlew test --tests "com.youmorry.expensetracker.SomeTest"           # クラス単位
./gradlew test --tests "com.youmorry.expensetracker.SomeTest.method"    # メソッド単位
./gradlew integrationTest                # 統合テスト
./gradlew check                          # 全チェック（テスト + 統合テスト + Checkstyle + Spotless）
./gradlew build                          # ビルド
./gradlew clean                          # ビルド成果物削除
./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest  # Checkstyle
./gradlew spotlessCheck                  # Spotless チェック
./gradlew spotlessApply                  # Spotless 自動修正

# Frontend (cd frontend)
npm install
npm run dev                              # 開発サーバー（Vite 8）
npm test                                 # テスト（Vitest）
npm run test:watch                       # テスト（ウォッチモード）
npm run test:coverage                    # テスト（カバレッジ付き）
npm run test:e2e                         # E2E テスト（Playwright）
npm run build                            # ビルド
npm run lint:fix                         # ESLint 自動修正
npm run format                           # Prettier フォーマット
npm run check                            # 全チェック（Prettier + ESLint + tsc + Vitest + ビルド）
```

## Skills

- `.claude/skills/start-issue/SKILL.md` — Issue 確認〜計画〜ブランチ作成〜TDD 実装〜ビルド確認まで
- `.claude/skills/commit/SKILL.md` — プロジェクトのコミットガイドラインに従ってコミットを作成する
- `.claude/skills/create-pr/SKILL.md` — ビルド確認済みの実装を PR として作成する

## 参照ドキュメント

- `docs/00-constitution.md` — プロジェクト不変原則
- `docs/01-planning/project-overview.md` — プロジェクト概要
- `docs/01-planning/tech-stack.md` — 技術スタック概要・共通インフラ
- `docs/01-planning/tech-stack-backend.md` — バックエンド技術スタック詳細・選定理由
- `docs/01-planning/tech-stack-frontend.md` — フロントエンド技術スタック詳細・選定理由
- `docs/02-requirements/requirements.md` — 要件定義
- `docs/03-design/backend/domain-model.md` — Entity / VO / Aggregate 設計
- `docs/03-design/backend/er-diagram.md` — ER図
- `docs/03-design/backend/api-design.md` — API エンドポイント仕様
- `docs/03-design/common/error-handling.md` — 例外階層・RFC 9457 レスポンス
- `docs/03-design/backend/validation-strategy.md` — バリデーション方針・層別の責務
- `docs/03-design/backend/database-schema.md` — DDL・インデックス
- `docs/03-design/backend/flyway-migrations.md` — Flyway マイグレーション一覧
- `docs/03-design/common/auth-design.md` — 認証フロー・JWT・セキュリティ
- `docs/03-design/frontend/screen-flow.md` — 画面遷移
- `docs/03-design/common/repository-structure.md` — リポジトリ構成
- `docs/03-design/backend/java-coding-standards.md` — Java コーディング規約
- `docs/04-decisions/` — ADR（アーキテクチャ決定記録）

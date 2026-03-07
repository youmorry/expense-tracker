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

# Frontend (cd frontend)
npm install
npm run dev                              # 開発サーバー
npm test                                 # テスト
npm run build                            # ビルド
```

## Architecture

アーキテクチャ・設計判断の詳細は `.claude/rules/` を参照（該当コード編集時に自動ロード）:

- `.claude/rules/backend.md` — DDD + 4層アーキテクチャ、API 規約、命名規約
- `.claude/rules/frontend.md` — Feature-Sliced Design、状態管理方針、認証フロー

## Bash コマンド実行ルール

- シェルオペレーター (`&&`, `||`, `;`, `|`, `>`, `$(...)`) でコマンドを連結せず、各コマンドを個別に実行する
- ヒアドキュメント (`<<`) を使わない
- ファイルへの書き込みはリダイレクトではなく Edit/Write ツールを使う

### Git

- コミットメッセージ: `git commit -m "タイトル" -m "本文"` の形式を使う
- trailerも `-m` で追加する

### GitHub CLI

- `--body` でインライン指定、長文は `--body-file` を使う

## コミットルール

- ブランチを切ってから作業を始める
- 1コミット = 1つの論理的変更。機能変更とリファクタリングは混ぜない
- すべてのコミットでビルド・テストが通る状態を維持する
- 一括コミットせず、論理的なまとまりごとに都度コミットする
- Conventional Commits（日本語）に従う: feat / fix / refactor / test / docs / chore

詳細は `docs/commit-guidelines.md` を参照すること。

## Claude による作業の明示

ユーザーに代わって行うすべての対外的なアクションに、Claude が実行したことを明示すること。

## Conventions

- ブランチ戦略: GitHub Flow（`main` から feature ブランチを切り、PR でマージ）
- ブランチ命名: `feature/<説明>`（例: `feature/add-expense-api`）
- ドキュメント: 日本語、コード: 英語

## 参照ドキュメント

- `docs/01-planning/tech-stack.md` — 技術スタック詳細・選定理由
- `docs/03-design/domain-model.md` — Entity / VO / Aggregate 設計
- `docs/03-design/api-design.md` — API エンドポイント仕様
- `docs/03-design/error-handling.md` — 例外階層・RFC 9457 レスポンス
- `docs/03-design/database-schema.md` — DDL・インデックス・Flyway
- `docs/03-design/auth-design.md` — 認証フロー・JWT・セキュリティ
- `docs/java-coding-standards.md` — Java コーディング規約

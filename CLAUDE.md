# Expense Tracker

個人向け支出記録・分析 Web アプリ。モノレポ構成（`backend/` + `frontend/`）。技術スタックは @docs/01-planning/tech-stack.md、設計は `docs/03-design/` 配下を参照。

## シェルコマンドのルール

- コマンドは `&&`、`;`、`|` で連結せず、必ず1つずつ個別に実行すること
- 複雑な入力はまず一時ファイルに書き出し、そのファイルを引数で渡すこと

## コミットルール

- 1コミット = 1つの論理的変更。機能変更とリファクタリングは混ぜない
- すべてのコミットでビルド・テストが通る状態を維持する
- 一括コミットせず、論理的なまとまりごとに都度コミットする
- Conventional Commits（日本語）に従う: feat / fix / refactor / test / docs / chore

詳細は `docs/commit-guidelines.md` を参照すること。

## Commands

```bash
docker compose up                        # ローカル DB 起動

# Backend (cd backend)
./gradlew bootRun                        # 開発サーバー
./gradlew test                           # テスト
./gradlew build                          # ビルド

# Frontend (cd frontend)
npm install && npm run dev               # 開発サーバー
npm test                                 # テスト
npm run build                            # ビルド
```

## Conventions

- ブランチ戦略: GitHub Flow（`main` から feature ブランチを切り、PR でマージ）
- ブランチ命名: `feature/<説明>`（例: `feature/add-expense-api`）
- ドキュメント: 日本語、コード: 英語

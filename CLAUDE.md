# Expense Tracker

個人向け支出記録・分析 Web アプリ。モノレポ構成（`backend/` + `frontend/`）。技術スタックは @docs/01-planning/tech-stack.md、設計は `docs/03-design/` 配下を参照。

## シェルコマンドのルール

- コマンドは `&&`、`;`、`|` で連結せず、必ず1つずつ個別に実行すること
- 複雑な入力はまず一時ファイルに書き出し、そのファイルを引数で渡すこと

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
- コミットメッセージ: `<type>: <subject>`（日本語可、例: `feat: 支出登録APIを実装`）
- ドキュメント: 日本語、コード: 英語

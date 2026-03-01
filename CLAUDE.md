# Expense Tracker

個人向け支出記録・分析 Web アプリ。モノレポ構成（`backend/` + `frontend/`）。技術スタックは @docs/01-planning/tech-stack.md、設計は `docs/03-design/` 配下を参照。

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
- コミットメッセージ: `<type>: <subject>`（日本語可、例: `feat: 支出登録APIを実装`）
- ドキュメント: 日本語、コード: 英語

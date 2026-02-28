# Expense Tracker

個人向け支出記録・分析 Web アプリ。支出を「必要 (NEED) / 欲しい (WANT)」に分類し、カテゴリ別・分類別に分析する。モノレポ構成（`backend/` + `frontend/`）。設計ドキュメント完了、実装未着手（MVP フェーズ）。

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 4.0, Java 25, Gradle (Kotlin DSL) |
| Frontend | React 19, TypeScript 5, Vite, Tailwind CSS |
| Data Access | Spring Data JDBC（JPA ではなく DDD 親和性のため採用） |
| State Management | TanStack Query v5 |
| Auth | Google OAuth2 only, JWT (HS256, メモリ保持) |
| Database | PostgreSQL 18, Flyway migration |
| Hosting | Render (FE: Static Site, BE: Web Service), Neon (DB) |

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

## API Conventions（プロジェクト固有）

- `/api/v1/` prefix、URL リソース名は複数形
- JSON キー・クエリパラメータは snake_case
- 金額は String 型（浮動小数点誤差回避）
- null の場合はキーを省略（null ではなく undefined）
- エラーレスポンスは RFC 9457 Problem Details 準拠
- 他ユーザーのリソースアクセスは 404 を返す（403 ではなく存在を隠す）
- Pagination なし（個人利用で年間 ~3000 件のため全件取得）

## Conventions

- ブランチ戦略: GitHub Flow（`main` から feature ブランチを切り、PR でマージ）
- コミットメッセージ: `<type>: <subject>`（日本語可、例: `feat: 支出登録APIを実装`）
- ドキュメント: 日本語、コード: 英語
- DB カラム / JSON キー: snake_case、Java / TypeScript: camelCase
- Java コーディング規約: Google Java Style Guide 準拠（詳細は @docs/java-coding-standards.md）

## Design Documents

詳細な設計は `docs/03-design/` 配下を参照（API仕様、認証フロー、DBスキーマ、ドメインモデル、エラーハンドリング、画面遷移、ディレクトリ構成）。

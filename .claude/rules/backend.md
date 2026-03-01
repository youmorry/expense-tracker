---
description: Backend (Spring Boot / Java) コード実装時に適用
paths:
  - "backend/**"
---

# Backend ルール

## アーキテクチャ: DDD + Layered

```
com.example.expensetracker/
├── domain/           # Entity, ValueObject, Repository interface（フレームワーク非依存）
├── application/      # Service（ユースケース実装）
├── infrastructure/   # Spring Data JDBC Repository 実装, 外部 API
└── presentation/     # REST Controller, DTO
```

- `domain` と `application` を明示的に分離し、ドメインロジックを `domain` に集約
- Spring Data JDBC の Aggregate/Repository 概念を DDD にそのまま対応させる
- `domain` 層は Spring 等のフレームワークに依存しない

## 設計判断

- **Spring Data JDBC over JPA**: 暗黙の挙動（遅延ロード・ダーティチェック）を排除し SQL を透明に
- **HS256 (symmetric JWT)**: 単一サーバー構成のためシンプルな対称鍵で十分

## API Conventions

- `/api/v1/` prefix、URL リソース名は複数形
- JSON キー・クエリパラメータは snake_case
- 金額は String 型（浮動小数点誤差回避）
- null の場合はキーを省略（null ではなく undefined）
- エラーレスポンスは RFC 9457 Problem Details 準拠
- 他ユーザーのリソースアクセスは 404 を返す（403 ではなく存在を隠す）
- Pagination なし（個人利用で年間 ~3000 件のため全件取得）

## 命名規約

- DB カラム / JSON キー: snake_case
- Java: camelCase

## 参照ドキュメント

常時参照（コンテキストに自動ロード）:

- @docs/java-coding-standards.md

必要時に参照:

- `docs/03-design/domain-model.md` — Entity / VO / Aggregate 設計
- `docs/03-design/error-handling.md` — 例外階層・RFC 9457 レスポンス
- `docs/03-design/database-schema.md` — DDL・インデックス・Flyway
- `docs/03-design/api-design.md` — API エンドポイント仕様
- `docs/03-design/auth-design.md` — 認証フロー・JWT・セキュリティ

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

## コーディング規約

@docs/java-coding-standards.md を参照。

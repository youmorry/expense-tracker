---
description: Frontend (React / TypeScript) コード実装時に適用
paths:
  - "frontend/**"
---

# Frontend ルール

## アーキテクチャ: Feature-Sliced Design

```
src/
├── components/       # 共通 UI コンポーネント
├── features/         # 機能単位モジュール（api/, components/, types.ts）
├── hooks/            # カスタムフック
├── lib/              # API クライアント・ユーティリティ
└── types/            # 共通型定義
```

## 設計判断

- **TanStack Query**: サーバー状態管理に使用。グローバル状態管理ライブラリは不要
- **JWT in memory**: localStorage/Cookie 不使用。XSS でのトークン窃取リスク最小化（CSRF も不要に）
- **認証フロー**: Google OAuth2 でクライアントサイド ID トークン取得 → `POST /api/v1/auth/google` → JWT 返却。詳細は @docs/03-design/auth-design.md

## 命名規約

- API レスポンスの JSON キーは snake_case → TypeScript では camelCase に変換

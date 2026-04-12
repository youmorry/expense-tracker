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

### features/ の構成ルール

- 1 feature = 1ディレクトリ（例: `features/transactions/`）
- 各 feature は以下の構成:
  - `api/` — TanStack Query の hooks（useQuery / useMutation）
  - `components/` — feature 固有の UI コンポーネント
  - `types.ts` — feature 固有の型定義
- feature 間の直接 import は禁止。共通化が必要なら `components/` または `hooks/` に移動

## 設計判断

- **TanStack Query**: サーバー状態管理に使用。グローバル状態管理ライブラリは不要
- **JWT in memory**: localStorage/Cookie 不使用。XSS でのトークン窃取リスク最小化（CSRF も不要に）
- **認証フロー**: Google OAuth2 でクライアントサイド ID トークン取得 → `POST /api/v1/auth/google` → JWT 返却。詳細は @docs/03-design/common/auth-design.md

## コンポーネント設計

- Props は interface で型定義し、コンポーネントと同じファイルに配置
- イベントハンドラの props は `onXxx` 命名（例: `onSubmit`, `onClose`）
- ビジネスロジック・API 呼び出しはカスタムフックに分離し、コンポーネントは表示に専念

## エラーハンドリング

- API エラーは `ApiException`（RFC 9457 Problem Details）として構造化
- 422 → フォームのインラインエラー、その他 → トースト通知
- 401 → JWT クリア＋ログイン画面リダイレクト（グローバル処理）
- 詳細は @docs/03-design/common/error-handling.md

## テスト方針

- テストフレームワークは Vitest を使用（Vite の設定・変換パイプラインを共有）
- ユーザー操作ベースでテストする（実装詳細に依存しない）
- Testing Library の `getByRole`, `getByLabelText` を優先（`getByTestId` は最終手段）
- API モックは MSW を使用

## 命名規約

- API レスポンスの JSON キーは snake_case → TypeScript では camelCase に変換
- コンポーネント: PascalCase（例: `TransactionForm.tsx`）
- hooks: `use` prefix + camelCase（例: `useTransactions.ts`）
- 型定義: PascalCase（例: `Transaction`, `CreateTransactionRequest`）

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
│   └── ui/           # shadcn/ui プリミティブ（CLI で追加）
├── features/         # 機能単位モジュール（api/, components/, types.ts）
├── hooks/            # カスタムフック
├── lib/              # API クライアント・ユーティリティ（`utils.ts` に `cn` ヘルパーを含む）
├── types/            # 共通型定義
└── test/             # テスト共通設定（MSW サーバー等）
```

### features/ の構成ルール

- 1 feature = 1ディレクトリ（例: `features/transactions/`）
- 各 feature は以下の構成:
  - `api/` — TanStack Query の hooks（useQuery / useMutation）
  - `components/` — feature 固有の UI コンポーネント
  - `types.ts` — feature 固有の型定義
- feature 間の直接 import は禁止。共通化が必要なら `components/` または `hooks/` に移動

### 層間の依存ルール

- `features/` 内のコンポーネントは同一 feature 内の `api/` と `types.ts` のみ import 可
- `components/`（共通）は `features/` に依存しない
- `hooks/` は `lib/` と `types/` のみに依存する
- `lib/` は外部ライブラリと `types/` のみに依存する

### 禁止事項

- コンポーネント内に直接 `fetch` / `axios` を書かない（TanStack Query の hooks を経由する）
- グローバル状態管理ライブラリ（Redux, Zustand 等）を導入しない（サーバー状態は TanStack Query で管理）
- `useEffect` で API コールしない（TanStack Query の `useQuery` / `useMutation` を使う）
- `any` 型を使わない（`unknown` + 型ガードで安全に処理する）
- `index.ts` でのバレルエクスポートは使わない（ツリーシェイキングの妨げになるため）

## 設計判断

- **TanStack Query**: サーバー状態管理に使用。グローバル状態管理ライブラリは不要
- **JWT in memory**: localStorage/Cookie 不使用。XSS でのトークン窃取リスク最小化（CSRF も不要に）
- **認証フロー**: Google OAuth2 でクライアントサイド ID トークン取得 → `POST /api/v1/auth/google` → JWT 返却。詳細は @docs/03-design/common/auth-design.md
- **UI プリミティブ**: shadcn/ui（Radix UI + Tailwind CSS ベース）を採用。コピー & ペースト型で、ソースコードをプロジェクト内に取り込む

## UI コンポーネントライブラリ: shadcn/ui

- プリミティブ（`Button`, `Dialog`, `Spinner` など）は `npx shadcn@latest add <component>` で `src/components/ui/` に追加する
- 追加前に必ず [shadcn/ui のカタログ](https://ui.shadcn.com/docs/components) を確認し、既存コンポーネントがあれば優先する
- `src/components/ui/` のファイルは shadcn CLI が生成したコードをそのまま保持する。独自の修正を加える場合は最小限にとどめ、コメントで理由を残す
- 共通の組み合わせコンポーネント（例: `ConfirmDialog`, `EmptyState`）は `src/components/` 直下に配置し、shadcn プリミティブをラップして実装する
- アイコンは `lucide-react` を使用（shadcn のデフォルト）
- クラス名の合成は `@/lib/utils` の `cn` を使う（`clsx` + `tailwind-merge`）

## コンポーネント設計

- Props は interface で型定義し、コンポーネントと同じファイルに配置
- イベントハンドラの props は `onXxx` 命名（例: `onSubmit`, `onClose`）
- ビジネスロジック・API 呼び出しはカスタムフックに分離し、コンポーネントは表示に専念
- コンポーネントはデフォルトエクスポートではなく名前付きエクスポートを使う（re-export 時の明確さのため）
  - ただしページコンポーネント（React Router の `lazy` で使う）はデフォルトエクスポート可

### コード例

#### 良い例: feature 内の API hooks

```tsx
// features/transactions/api/useTransactions.ts
export function useTransactions(params: TransactionSearchParams) {
  return useQuery({
    queryKey: ["transactions", params],
    queryFn: () => fetchTransactions(params),
  });
}
```

#### 悪い例: コンポーネント内で直接 fetch

```tsx
// NG: コンポーネント内で直接 API コール
function TransactionList() {
  const [data, setData] = useState([]);
  useEffect(() => {
    fetch("/api/v1/transactions").then(r => r.json()).then(setData);
  }, []);
  // ...
}
```

#### 良い例: 表示とロジックの分離

```tsx
// features/transactions/components/TransactionForm.tsx
interface TransactionFormProps {
  onSubmit: (data: CreateTransactionRequest) => void;
  isSubmitting: boolean;
}

export function TransactionForm({ onSubmit, isSubmitting }: TransactionFormProps) {
  // 表示のみに専念
}
```

#### 悪い例: コンポーネントにロジックが混在

```tsx
// NG: コンポーネント内で mutation を直接管理
function TransactionForm() {
  const mutation = useMutation({ ... });
  const validate = (data) => { /* ビジネスルール */ };
  // 表示とロジックが混在
}
```

## コメント原則

- **「Why」を書き、「What」は書かない** — コードが語れることをコメントで繰り返さない
- 型名・関数名の和訳コメントは不要（例: `/** リクエストごとのオプション */` on `RequestOptions`）
- ファイルレベルコメントはモジュールの **責務と設計判断** を記載し、`@see` で設計ドキュメントを参照する
- JSDoc はコードから読み取れない情報（対応する API エンドポイント、非自明なエッジケース、具体的な使用例）がある場合のみ付与する
- 同じ情報を複数箇所に書かない（メンテナンス時に乖離する原因になる）

## エラーハンドリング

- API エラーは `ApiException`（RFC 9457 Problem Details）として構造化
- 422 → フォームのインラインエラー、その他 → トースト通知
- 401 → JWT クリア＋ログイン画面リダイレクト（グローバル処理）
- 詳細は @docs/03-design/common/error-handling.md

## スタイリング

- Tailwind CSS 4 を使用（`@theme` ベースの CSS 設定）
- インラインスタイル（`style` 属性）は使わない（Tailwind のユーティリティクラスで統一）
- マジックナンバーのカラーコードを直接書かない（`@theme` で定義したデザイントークンを使う）
- レスポンシブ: モバイルファーストで実装し、`sm:`, `md:`, `lg:` で拡張

## リンティング・フォーマッティング

- ESLint: `typescript-eslint` の `strictTypeChecked` を使用
- Prettier: `printWidth: 100`、`prettier-plugin-tailwindcss` でクラス名のソートを自動化
- コード変更時は `npm run lint` と `npm run format:check` でチェックする
- フルチェックは `npm run check`（Prettier + ESLint + tsc + Vitest + build）

## テスト方針

### テスト種別

| テスト対象 | テスト種別 | ツール | 説明 |
|-----------|-----------|--------|------|
| コンポーネント | 単体テスト | Vitest + React Testing Library | ユーザー操作ベースでテスト |
| カスタムフック | 単体テスト | Vitest + `renderHook` | hooks のロジックを検証 |
| API hooks | 統合テスト | Vitest + MSW | MSW でモックサーバーを立てて検証 |
| ユーティリティ | 純粋な単体テスト | Vitest | 純関数のテスト |
| E2E | E2E テスト | Playwright | ブラウザ上での画面遷移・操作を検証 |

### テストクラスの配置

- テストファイルは対象ファイルと同じディレクトリに配置する（コロケーション）
- ファイル名: `<対象ファイル>.test.tsx`（例: `TransactionForm.tsx` → `TransactionForm.test.tsx`）
- テスト共通設定（MSW サーバー等）は `src/test/` に配置する

### テストの書き方

- テスト名は振る舞いを説明する自然な英語で書く（`it` に続けて英文として読める形）
  - パターン: `<動詞> <対象> when <条件>`
  - 例: `renders transaction list when data is loaded`
  - 例: `shows error message when API returns 422`
  - 条件が不要な場合は省略可: `renders three navigation links`
- Arrange-Act-Assert パターンで構造化し、各セクションを空行で区切る
- Testing Library の `getByRole`, `getByLabelText` を優先（`getByTestId` は最終手段）
- API モックは MSW を使用（`src/test/mocks/handlers.ts` にデフォルトハンドラを定義）
- テスト固有のハンドラオーバーライドは `server.use()` でテストメソッド内に記述する
- 1 テストにつき 1 つの振る舞いを検証する
- テストデータはテスト内でローカルに生成し、テスト間で共有しない

### コード例

#### 良い例: ユーザー操作ベースのテスト

```tsx
it("shows validation error when amount is empty", async () => {
  const user = userEvent.setup();
  render(<TransactionForm onSubmit={vi.fn()} isSubmitting={false} />);

  await user.click(screen.getByRole("button", { name: /save/i }));

  expect(screen.getByText(/amount is required/i)).toBeInTheDocument();
});
```

#### 悪い例: 実装詳細に依存したテスト

```tsx
// NG: 内部 state や実装詳細をテストしている
it("sets hasError state to true", () => {
  const { result } = renderHook(() => useTransactionForm());
  act(() => result.current.setAmount(""));
  expect(result.current.hasError).toBe(true);
});
```

## 画面検証（実装中の手動確認）

UI を実装・修正する際は、自動テストとは別に画面を実際にレンダリングして見た目と挙動を確認する。

- 開発サーバー（`npm run dev`）を起動し、Playwright MCP（`mcp__playwright__browser_*`）で対象画面を操作・キャプチャする
- スクリーンショットは `frontend/screenshots/` に保存する（このディレクトリは `.gitignore` 管理。Git にはコミットしない）
- ファイル名は内容がわかる名前にする（例: `transaction-list-empty.png`, `login-error-422.png`）
- 検証対象はゴールデンパス＋主要なエッジケース（空状態・エラー表示・ローディング・レスポンシブの主要ブレークポイント）
- 検証完了後、保存したファイルのパスをユーザーに報告する。PR への添付はユーザーが本文にドラッグ&ドロップする運用（`gh` CLI から GitHub の画像 CDN に直接アップロードする手段がないため）

## 命名規約

| 対象 | 規約 | 例 |
|------|------|----|
| コンポーネントファイル | PascalCase | `TransactionForm.tsx` |
| hooks ファイル | camelCase + `use` prefix | `useTransactions.ts` |
| 型定義 | PascalCase | `Transaction`, `CreateTransactionRequest` |
| 定数 | UPPER_SNAKE_CASE | `API_BASE_URL`, `MAX_AMOUNT` |
| ユーティリティ関数 | camelCase | `formatCurrency`, `parseDate` |
| テストファイル | 対象ファイル名 + `.test` | `TransactionForm.test.tsx` |
| API レスポンスの JSON キー | snake_case → camelCase に変換 | `created_at` → `createdAt` |

## 参照ドキュメント

必要時に参照（CLAUDE.md の参照ドキュメントも併せて確認）:

- `docs/03-design/frontend/screen-flow.md` — 画面遷移・UI 仕様
- `docs/03-design/common/error-handling.md` — 例外階層・RFC 9457 レスポンス
- `docs/03-design/common/auth-design.md` — 認証フロー・JWT・セキュリティ
- `docs/01-planning/tech-stack-frontend.md` — フロントエンド技術スタック詳細・選定理由

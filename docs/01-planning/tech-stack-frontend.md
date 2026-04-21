# 技術スタック — フロントエンド

## コアフレームワーク

| 技術 | 用途 |
|------|------|
| React 19 | UI フレームワーク |
| TypeScript 6 | 型安全な開発 |
| Vite 8 | ビルドツール・開発サーバー |

> **TypeScript 6 について**
> 2026年3月リリースの最新安定版。JS ベースの最終メジャーバージョン。
> 次期 TypeScript 7 は Go ネイティブへの移行が進行中だが、まだプレビュー段階のため見送り。

> **Vite 8 について**
> 2026年3月リリース。Rolldown（Rust ベースバンドラー）を統合し、esbuild + Rollup を統一。
> 10〜30 倍のビルド高速化を実現しつつ、プラグイン互換性を維持。Node.js 20.19+ が必要。

## ルーティング

| 技術 | 用途 |
|------|------|
| React Router 7 | クライアントサイドルーティング |

> **React Router 7 について**
> Remix と統合された最新バージョン。12M+ weekly downloads の安定ライブラリ。
> 4画面の SPA にはシンプルな React Router が適しており、TanStack Router は型安全性に優れるが
> この規模のアプリには過剰と判断した。

## 状態管理・データフェッチ

| 技術 | 用途 |
|------|------|
| TanStack Query (React Query v5) | サーバー状態管理・キャッシュ |

> **TanStack Query について**
> APIから取得するデータの「ローディング・エラー・キャッシュ・再取得」をまとめて管理するライブラリ。
> `useState` + `useEffect` によるデータフェッチの定型コードを削減できる。

## スタイリング

| 技術 | 用途 |
|------|------|
| Tailwind CSS 4 | ユーティリティファーストCSS |

> **Tailwind CSS 4 について**
> 設定モデルが `tailwind.config.js`（JavaScript）から CSS ベース（`@theme`）に変更された。
> コンテンツ検出が自動化され、設定がシンプルになった。新規プロジェクトなので移行コストなし。

## UI コンポーネント

| 技術 | 用途 |
|------|------|
| shadcn/ui | UI プリミティブ（Radix UI ベース） |
| lucide-react | アイコンライブラリ |

> **shadcn/ui について**
> コピー & ペースト型のコンポーネントライブラリ。npm パッケージとして依存するのではなく、
> ソースコードを `src/components/ui/` に取り込む形式。Radix UI（アクセシビリティ対応のヘッドレス UI プリミティブ）と
> Tailwind CSS をベースに、カスタマイズ可能なコンポーネントを提供する。
> 追加は `npx shadcn@latest add <component>` で行う。設定は `frontend/components.json`。

## 認証

| 技術 | 用途 |
|------|------|
| @react-oauth/google | Google OAuth2 クライアント |

## テスティング

| 技術 | 用途 |
|------|------|
| Vitest 4 | テストフレームワーク |
| React Testing Library | コンポーネントテスト |
| MSW (Mock Service Worker) | API モック |

> **Vitest について**
> Vite ベースのテストフレームワーク。Vite の設定・変換パイプラインを共有でき、
> Jest 互換の API を提供しつつ、CI 環境で 30〜70% 高速に動作する。

## リンター・フォーマッター

| 技術 | 用途 |
|------|------|
| ESLint | 静的解析・コード品質チェック |
| Prettier | コードフォーマット |

> **ESLint + Prettier について**
> バックエンドで Checkstyle + Spotless を使用しているように、フロントエンドにも静的解析を導入する。
> Biome も候補だが、エコシステムの成熟度（プラグイン・IDE サポート）で ESLint + Prettier が安定。

---

## 技術選定の理由

### React 19 + TypeScript 6

React 19は2024年12月に正式リリースされた安定版。Actions API・Server Components（フレームワーク経由）・フォーム処理の改善など実用的な機能が追加されている。TypeScript 6は2026年3月リリースのJS ベース最終版。次期 TypeScript 7（Go ネイティブ）はプレビュー段階のため見送り。

### Vite 8

Vite 8は2026年3月にリリースされた最新メジャーバージョン。Rolldown（Rust ベースバンドラー）を統合し、
開発時の esbuild と本番ビルドの Rollup を単一バンドラーに統一した。10〜30 倍のビルド高速化を実現しつつ、
プラグイン互換性を維持している。

### React Router 7

Remix と統合された React Router の最新メジャーバージョン。12M+ weekly downloads の安定ライブラリ。
TanStack Router は型安全性に優れるが、4画面の小規模 SPA には React Router のシンプルさが適している。

### Vitest 4 + React Testing Library + MSW

Vite ベースのプロジェクトでは Vitest が自然な選択。Vite の設定・変換パイプラインを共有でき、
Jest 互換の API を提供する。React Testing Library でユーザー操作ベースのテストを書き、
MSW で API モックを行う。

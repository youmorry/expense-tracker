# 統合テスト（Integration Test）実装状況

## 概要

Controller 層の統合テスト実装状況をまとめる。
Unit Test（`@WebMvcTest`）は全 Controller で実装済みのため、本ドキュメントでは統合テストに焦点を当てる。

## 実装状況

| Controller | エンドポイント | Unit Tests | Integration Tests | 備考 |
|---|---|---|---|---|
| AuthController | `POST /api/v1/auth/google` | 4件 | 3件 | |
| TransactionController | `POST/GET/PUT/DELETE /api/v1/transactions` | 20件 | 5件 + SearchIntegrationTest | |
| CategoryController | `GET /api/v1/categories` | 2件 | なし | 不要（後述） |
| UserController | `GET/DELETE /api/v1/users/me` | 6件 | 3件 | |
| AnalyticsController | `GET /api/v1/analytics/{need-want,category}` | 6件 | 6件 | |
| GlobalExceptionHandler | 5ハンドラ | 10件 | N/A | エンドポイントではないため統合テスト対象外 |

## CategoryController に統合テストが不要な理由

CategoryController にのみ統合テストが存在しないが、以下の理由から追加は不要と判断した。

1. **DB アクセスが存在しない** — `CategoryService.findAll()` は `CategoryType.values()`（enum）を返すだけで、Repository や外部 API 呼び出しがない
2. **統合テストで検証すべき結合点がない** — 処理パスは `Controller → Service → enum` であり、DB・外部 API・複雑なセキュリティ制御いずれも経由しない
3. **既存の Unit Test で十分カバー済み** — JWT なしで 401、正常時のレスポンス構造（id, name, display_order）を検証済み
4. **テスト方針との整合性** — `backend.md` にて presentation 層は `@WebMvcTest + MockMvc`（スライステスト）と定義されており、統合テストは infrastructure 層（DB 結合）のために存在する

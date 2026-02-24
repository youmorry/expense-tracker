# API設計

## 概要

Expense Tracker の REST API 仕様を定義する。（[Web API設計ガイドライン](https://future-architect.github.io/arch-guidelines/documents/forWebAPI/web_api_guidelines.html)に準拠する）
バックエンド（Spring Boot）が提供する API エンドポイントの一覧、リクエスト/レスポンス形式、
エラーレスポンス形式を記述する。

詳細な OpenAPI 仕様は SpringDoc により自動生成し、Swagger UI で閲覧可能にする。

---

## 設計方針

| 項目 | 方針 |
|------|------|
| スタイル | RESTful（リソース指向） |
| データ形式 | JSON（`Content-Type: application/json`） |
| 認証 | Bearer JWT トークン（`Authorization: Bearer <token>`） |
| 日付形式 | ISO 8601（日付: `2026-02-23`、日時: `2026-02-23T10:30:00Z`） |
| 金額形式 | 文字列（`"1200.00"`）。浮動小数点の精度損失を回避するため |
| バージョニング | v1 プレフィックス（`/api/v1/...`） |
| エラー形式 | RFC 9457 Problem Details 準拠 |
| 命名規則 | URL: ケバブケース不使用・リソース名は複数形、JSON / クエリパラメータ: snake_case |
| null の扱い | 値が存在しない場合はキー自体を含めない（undefined）。null は使用しない |

---

## 認証・認可

すべての API（認証エンドポイントを除く）は JWT トークンによる認証が必要。
JWT にはユーザーの内部 ID が含まれ、リクエストごとに検証する。

```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6...
```

認証が不要なエンドポイントには「🔓不要」、必要なエンドポイントには「🔒必要」と記載する。

**認可ルール**
- 支出記録（transactions）は所有ユーザーのみが操作可能。JWT から取得した userId と一致しないリソースへのアクセスは 403 Forbidden を返す
- カテゴリ（categories）は全ユーザー共通の読み取り専用リソース

---

## エンドポイント一覧

| # | メソッド | パス | 認証 | 説明 |
|---|---------|------|------|------|
| 1 | POST | `/api/v1/auth/google` | 🔓不要 | Google 認証・JWT 発行 |
| 2 | GET | `/api/v1/users/me` | 🔒必要 | 自分のユーザー情報取得 |
| 3 | PATCH | `/api/v1/users/me/currency` | 🔒必要 | 通貨コード更新 |
| 4 | DELETE | `/api/v1/users/me` | 🔒必要 | アカウント削除 |
| 5 | GET | `/api/v1/categories` | 🔒必要 | カテゴリ一覧取得 |
| 6 | POST | `/api/v1/transactions` | 🔒必要 | 支出登録 |
| 7 | GET | `/api/v1/transactions` | 🔒必要 | 支出一覧取得 |
| 8 | GET | `/api/v1/transactions/{id}` | 🔒必要 | 支出詳細取得 |
| 9 | PUT | `/api/v1/transactions/{id}` | 🔒必要 | 支出更新 |
| 10 | DELETE | `/api/v1/transactions/{id}` | 🔒必要 | 支出削除 |
| 11 | GET | `/api/v1/analytics/category` | 🔒必要 | カテゴリ別集計 |
| 12 | GET | `/api/v1/analytics/need-want` | 🔒必要 | need/want 比率 |

---

## エンドポイント詳細

### 1. Google 認証・JWT 発行

Google OAuth2 で取得した ID トークンを検証し、JWT を発行する。
ユーザーが存在しない場合は自動作成する。

```
POST /api/v1/auth/google
```
🔓認証不要

**リクエスト**

```json
{
  "id_token": "eyJhbGciOiJSUzI1NiIs..."
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id_token | string | ○ | Google の ID トークン |

**レスポンス 200 OK**

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "user": {
    "id": 1,
    "email": "user@gmail.com",
    "display_name": "Yuto",
    "created_at": "2026-02-23T10:30:00Z"
  }
}
```

| フィールド | 型 | 説明 |
|-----------|-----|------|
| access_token | string | JWT アクセストークン |
| user | object | ユーザー情報 |
| user.currency_code | string | 未設定の場合はキーごと省略。FE はキーの有無で通貨選択画面への遷移を判断する |

**レスポンス 401 Unauthorized**

ID トークンの検証に失敗した場合。

---

### 2. 自分のユーザー情報取得

JWT から特定したユーザーの情報を返す。

```
GET /api/v1/users/me
```
🔒認証必要

**レスポンス 200 OK**

```json
{
  "id": 1,
  "email": "user@gmail.com",
  "display_name": "Yuto",
  "currency_code": "JPY",
  "created_at": "2026-02-23T10:30:00Z"
}
```

---

### 3. 通貨コード更新

ユーザーの使用通貨を更新する。初回設定および設定画面からの変更で使用する。

```
PATCH /api/v1/users/me/currency
```
🔒認証必要

**リクエスト**

```json
{
  "currency_code": "JPY"
}
```

| フィールド | 型 | 必須 | バリデーション |
|-----------|-----|------|--------------|
| currency_code | string | ○ | ISO 4217 の有効な3文字コード |

**レスポンス 200 OK**

```json
{
  "id": 1,
  "email": "user@gmail.com",
  "display_name": "Yuto",
  "currency_code": "JPY",
  "created_at": "2026-02-23T10:30:00Z"
}
```

**レスポンス 422 Unprocessable Content**

無効な通貨コードの場合。

---

### 4. アカウント削除

ユーザーアカウントと関連するすべての支出記録を削除する（CASCADE）。

```
DELETE /api/v1/users/me
```
🔒認証必要

**レスポンス 204 No Content**

レスポンスボディなし。

---

### 5. カテゴリ一覧取得

プリセットカテゴリの一覧を表示順で返す。

```
GET /api/v1/categories
```
🔒認証必要

**レスポンス 200 OK**

```json
{
  "items": [
    { "id": 1, "name": "Food", "display_order": 1 },
    { "id": 2, "name": "Transport", "display_order": 2 },
    { "id": 3, "name": "Housing", "display_order": 3 },
    { "id": 11, "name": "Uncategorized", "display_order": 11 }
  ]
}
```

> カテゴリは変更頻度が極めて低いため、FE 側で TanStack Query の `staleTime` を長めに設定してキャッシュする。

---

### 6. 支出登録

新しい支出を登録する。

```
POST /api/v1/transactions
```
🔒認証必要

**リクエスト**

```json
{
  "date": "2026-02-23",
  "amount": "1200",
  "category_id": 1,
  "need_want_type": "NEED",
  "title": "Lunch"
}
```

| フィールド | 型 | 必須 | デフォルト | バリデーション |
|-----------|-----|------|-----------|--------------|
| date | string | ○ | - | ISO 8601 日付形式 |
| amount | string | ○ | - | 正の数値。通貨に応じた小数桁数 |
| category_id | number | - | Uncategorized の ID | 存在するカテゴリ ID |
| need_want_type | string | - | `"UNSET"` | `NEED` \| `WANT` \| `UNSET` |
| title | string | - | 省略 | 最大200文字 |
| memo | string | - | 省略 | 最大2000文字 |

**レスポンス 201 Created**

```json
{
  "id": 42,
  "date": "2026-02-23",
  "amount": "1200",
  "category_id": 1,
  "category_name": "Food",
  "need_want_type": "NEED",
  "title": "Lunch",
  "created_at": "2026-02-23T10:30:00Z",
  "updated_at": "2026-02-23T10:30:00Z"
}
```

**レスポンス 422 Unprocessable Content**

バリデーションエラーの場合。

---

### 7. 支出一覧取得

ユーザーの支出を期間・フィルター条件で取得する。指定条件に合致する全件を返す。

```
GET /api/v1/transactions
```
🔒認証必要

**クエリパラメータ**

| パラメータ | 型 | 必須 | デフォルト | 説明 |
|-----------|-----|------|-----------|------|
| from | string | - | - | 取得開始日（ISO 8601 日付形式、例: `2026-02-01`）。この日付を含む |
| to | string | - | - | 取得終了日（ISO 8601 日付形式、例: `2026-02-28`）。この日付を含む |
| category_id | number | - | - | カテゴリでフィルタ（複数指定: `category_id=1&category_id=3`） |
| need_want_type | string | - | - | `NEED` \| `WANT` \| `UNSET` で絞り込み |
| keyword | string | - | - | title, memo の部分一致検索 |

**期間パラメータの組み合わせ**

| from | to | 取得範囲 |
|------|----|---------|
| `2026-02-01` | `2026-02-28` | 2026年2月（FE が月の開始・終了日を計算して指定） |
| `2026-01-01` | `2026-12-31` | 2026年全体（FE が年の開始・終了日を計算して指定） |
| `2026-02-01` | 省略 | 2026年2月1日以降すべて |
| 省略 | `2026-02-28` | 2026年2月28日以前すべて |
| 省略 | 省略 | 全期間 |

> **期間の組み立ては FE の責務**
> API は汎用的な日付範囲フィルターを提供するだけで、「月表示」「年表示」「すべて」の概念は持たない。
> FE の期間セレクタが月/年/すべてに応じた from・to を計算してリクエストする。
> これにより API と画面設計が疎結合になり、将来「四半期表示」などを追加しても API 変更が不要。

**レスポンス 200 OK**

```json
{
  "items": [
    {
      "id": 42,
      "date": "2026-02-23",
      "amount": "1200",
      "category_id": 1,
      "category_name": "Food",
      "need_want_type": "NEED",
      "title": "Lunch",
      "created_at": "2026-02-23T10:30:00Z",
      "updated_at": "2026-02-23T10:30:00Z"
    }
  ]
}
```

> 将来ページネーション導入時に `total_count` 等のメタデータを追加可能な構造としている。

**ソート順**
- `date DESC, created_at DESC`（日付の新しい順。同日内は登録の新しい順）
- ソート順は固定。クライアントからの変更は不可

**全件取得とした理由**

個人利用のアプリであり、月単位では数十件、年単位でも数千件程度を想定している。
このデータ量であれば全件取得でパフォーマンス上の問題はなく、ページネーションの実装コストに見合わない。
将来データ量が増加した場合はカーソル方式のページネーションを導入する。

---

### 8. 支出詳細取得

指定した ID の支出を取得する。

```
GET /api/v1/transactions/{id}
```
🔒認証必要

**パスパラメータ**

| パラメータ | 型 | 説明 |
|-----------|-----|------|
| id | number | 支出 ID |

**レスポンス 200 OK**

```json
{
  "id": 42,
  "date": "2026-02-23",
  "amount": "1200",
  "category_id": 1,
  "category_name": "Food",
  "need_want_type": "NEED",
  "title": "Lunch",
  "memo": "Company cafeteria",
  "created_at": "2026-02-23T10:30:00Z",
  "updated_at": "2026-02-23T10:30:00Z"
}
```

**レスポンス 404 Not Found**

指定した ID の支出が存在しない、または自分の支出でない場合。

> 他ユーザーの支出 ID を指定した場合も 404 を返す（403 ではなく）。
> リソースの存在自体を隠すことで、ID の推測による情報漏洩を防ぐ。

---

### 9. 支出更新

指定した ID の支出を更新する。リクエストボディに含まれるフィールドですべて上書きする（全量更新）。

```
PUT /api/v1/transactions/{id}
```
🔒認証必要

**リクエスト**

```json
{
  "date": "2026-02-23",
  "amount": "1500",
  "category_id": 1,
  "need_want_type": "NEED",
  "title": "Lunch (updated)"
}
```

リクエストボディの形式は登録（POST）と同一。

**レスポンス 200 OK**

更新後のリソースを返す（レスポンス形式は支出詳細取得と同一）。

**レスポンス 404 Not Found**

指定した ID の支出が存在しない、または自分の支出でない場合。

---

### 10. 支出削除

指定した ID の支出を物理削除する。

```
DELETE /api/v1/transactions/{id}
```
🔒認証必要

**レスポンス 204 No Content**

レスポンスボディなし。

**レスポンス 404 Not Found**

指定した ID の支出が存在しない、または自分の支出でない場合。

---

### 11. カテゴリ別集計

指定期間のカテゴリ別支出額と割合を返す。

```
GET /api/v1/analytics/category
```
🔒認証必要

**クエリパラメータ**

| パラメータ | 型 | 必須 | デフォルト | 説明 |
|-----------|-----|------|-----------|------|
| from | string | - | - | 取得開始日（ISO 8601 日付形式）。この日付を含む |
| to | string | - | - | 取得終了日（ISO 8601 日付形式）。この日付を含む |

期間パラメータの仕様は支出一覧取得と同一。

**レスポンス 200 OK**

```json
{
  "total_amount": "130000",
  "categories": [
    {
      "category_id": 1,
      "category_name": "Food",
      "amount": "45000",
      "percentage": 34.6,
      "transaction_count": 28
    },
    {
      "category_id": 3,
      "category_name": "Housing",
      "amount": "30000",
      "percentage": 23.1,
      "transaction_count": 2
    }
  ]
}
```

| フィールド | 型 | 説明 |
|-----------|-----|------|
| total_amount | string | 期間内の合計金額 |
| categories | array | カテゴリ別の集計。金額降順 |
| categories[].category_id | number | カテゴリ ID |
| categories[].category_name | string | カテゴリ名 |
| categories[].amount | string | カテゴリ合計金額 |
| categories[].percentage | number | 全体に占める割合（%、小数1桁） |
| categories[].transaction_count | number | 該当する支出の件数 |

**ルール**
- 金額 0 のカテゴリは含めない（screen-flow.md の仕様に準拠）
- categories は amount の降順でソート
- percentage の合計は丸め誤差により 100.0 と一致しない場合がある

---

### 12. need/want 比率

指定期間の need/want/unset 別の支出額と割合を返す。

```
GET /api/v1/analytics/need-want
```
🔒認証必要

**クエリパラメータ**

期間パラメータの仕様は支出一覧取得と同一（`from` / `to`）。

**レスポンス 200 OK**

```json
{
  "total_amount": "130000",
  "breakdown": [
    {
      "type": "NEED",
      "amount": "80000",
      "percentage": 61.5,
      "transaction_count": 45
    },
    {
      "type": "WANT",
      "amount": "35000",
      "percentage": 26.9,
      "transaction_count": 12
    },
    {
      "type": "UNSET",
      "amount": "15000",
      "percentage": 11.5,
      "transaction_count": 3
    }
  ]
}
```

| フィールド | 型 | 説明 |
|-----------|-----|------|
| total_amount | string | 期間内の合計金額 |
| breakdown | array | NEED / WANT / UNSET の集計 |
| breakdown[].type | string | `NEED` \| `WANT` \| `UNSET` |
| breakdown[].amount | string | 合計金額 |
| breakdown[].percentage | number | 全体に占める割合（%、小数1桁） |
| breakdown[].transaction_count | number | 該当する支出の件数 |

**ルール**
- 3つの type は該当データが 0 件でもレスポンスに含める（amount: "0", percentage: 0.0, transaction_count: 0）
- UNSET の transaction_count は画面の「⚠ N transactions unset」表示に使用する

---

## 共通レスポンス形式

### エラーレスポンス

RFC 9457 Problem Details for HTTP APIs に準拠した形式で返す。
エラーレスポンスの Content-Type は `application/problem+json` を使用する。

**バリデーションエラーの例（422）**

```
HTTP/1.1 422 Unprocessable Content
Content-Type: application/problem+json

{
  "type": "/errors/validation-error",
  "title": "Your request is not valid.",
  "status": 422,
  "detail": "One or more fields have validation errors.",
  "instance": "/api/v1/transactions",
  "errors": [
    {
      "detail": "must be greater than 0",
      "pointer": "#/amount"
    },
    {
      "detail": "must be a valid date in ISO 8601 format",
      "pointer": "#/date"
    }
  ]
}
```

**認証エラーの例（401）**

```
HTTP/1.1 401 Unauthorized
Content-Type: application/problem+json

{
  "type": "/errors/unauthorized",
  "title": "Authentication required.",
  "status": 401,
  "detail": "The access token is missing or invalid."
}
```

**リソース不在の例（404）**

```
HTTP/1.1 404 Not Found
Content-Type: application/problem+json

{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "The requested transaction was not found.",
  "instance": "/api/v1/transactions/999"
}
```

**標準メンバー（RFC 9457 Section 3.1）**

| メンバー | 型 | 説明 |
|---------|-----|------|
| type | string | 問題種別を識別する URI 参照。追加のセマンティクスがない場合は `about:blank`（Section 4.2.1） |
| title | string | 問題種別の短い要約。同じ type では原則同じ値を返す（Section 3.1.3） |
| status | number | HTTP ステータスコード。実際のレスポンスステータスと一致させる（Section 3.1.2） |
| detail | string | この発生に固有の人間向け説明。クライアントによる問題修正を助ける内容にする（Section 3.1.4） |
| instance | string | この問題発生を識別する URI 参照（Section 3.1.5） |

**拡張メンバー（RFC 9457 Section 3.2）**

| メンバー | 型 | 説明 |
|---------|-----|------|
| errors | array | バリデーションエラーの詳細（422 の場合のみ）。RFC のバリデーション例に準拠 |
| errors[].detail | string | 個別フィールドのエラーメッセージ |
| errors[].pointer | string | エラー箇所を示す JSON Pointer（RFC 6901）。`#/` プレフィックス付き |

**type URI の方針**

| type 値 | 用途 |
|--------|------|
| `about:blank` | HTTP ステータスコード以上の追加情報がない場合（404, 500 など） |
| `/errors/validation-error` | バリデーションエラー（422） |
| `/errors/unauthorized` | 認証エラー（401） |
| `/errors/forbidden` | 認可エラー（403） |

- type URI はアプリのベース URL からの相対パスとする（例: `https://expense-tracker.example.com/errors/validation-error`）
- RFC Section 3.1.1 の推奨に従い、将来的に type URI を解決して人間向けドキュメントを返せるようにすることを想定する
- MVP ではドキュメントページの用意は不要。URI は識別子として使用する

### HTTP ステータスコード一覧

| コード | 意味 | 使用場面 |
|--------|------|---------|
| 200 OK | 成功 | 取得・更新成功 |
| 201 Created | 作成成功 | 支出登録成功 |
| 204 No Content | 成功（ボディなし） | 削除成功 |
| 400 Bad Request | リクエスト不正 | JSON パースエラー、不正なクエリパラメータ |
| 401 Unauthorized | 認証エラー | JWT なし・期限切れ・不正 |
| 403 Forbidden | 認可エラー | 他ユーザーのリソースへのアクセス（通常は 404 で隠す） |
| 404 Not Found | リソース不在 | 存在しない ID、他ユーザーの ID 指定 |
| 422 Unprocessable Content | バリデーションエラー | フィールドの値が不正（金額が負、無効な通貨コード等） |
| 500 Internal Server Error | サーバーエラー | 予期しないエラー |

---

## リクエスト・レスポンスの型定義

### User

```typescript
interface User {
  id: number;
  email: string;
  display_name: string;
  currency_code?: string;  // 未設定時はキー省略
  created_at: string;      // ISO 8601
}
```

### Transaction

```typescript
interface Transaction {
  id: number;
  date: string;              // "2026-02-23"
  amount: string;            // "1200" (文字列で精度を保持)
  category_id: number;
  category_name: string;
  need_want_type: "NEED" | "WANT" | "UNSET";
  title?: string;            // 未入力時はキー省略
  memo?: string;             // 未入力時はキー省略
  created_at: string;        // ISO 8601
  updated_at: string;        // ISO 8601
}
```

### TransactionRequest

```typescript
interface TransactionRequest {
  date: string;
  amount: string;
  category_id?: number;
  need_want_type?: "NEED" | "WANT" | "UNSET";
  title?: string;
  memo?: string;
}
```

### Category

```typescript
interface Category {
  id: number;
  name: string;
  display_order: number;
}
```

### CategoryAnalytics

```typescript
interface CategoryAnalytics {
  total_amount: string;
  categories: {
    category_id: number;
    category_name: string;
    amount: string;
    percentage: number;
    transaction_count: number;
  }[];
}
```

### NeedWantAnalytics

```typescript
interface NeedWantAnalytics {
  total_amount: string;
  breakdown: {
    type: "NEED" | "WANT" | "UNSET";
    amount: string;
    percentage: number;
    transaction_count: number;
  }[];
}
```

---

## 設計メモ

### 金額を文字列で返す理由

JSON の number 型は IEEE 754 浮動小数点数であり、大きな数値や小数を正確に表現できない場合がある。
金額の精度を保証するため、API のリクエスト・レスポンスともに文字列型で受け渡す。
Java 側では `BigDecimal`、TypeScript 側ではパース時に適切な処理を行う。

### 他ユーザーのリソースアクセスに 404 を返す理由

他ユーザーの支出 ID に対するリクエストで 403 Forbidden を返すと、「その ID のリソースは存在するが、
アクセス権がない」という情報を攻撃者に与えてしまう。404 Not Found を返すことで、
リソースの存在自体を秘匿する（Insecure Direct Object Reference 対策）。

### PUT を採用した理由（PATCH ではなく）

支出の更新には PUT（全量更新）を採用する。

- 登録モーダルと編集モーダルは同じフォームを使用するため、FE は常に全フィールドを送信する
- 部分更新（PATCH）は「送信されなかったフィールドは変更しない」というロジックが必要で、実装が複雑になる
- フィールド数が少ない（6項目）ため、全量更新のオーバーヘッドは無視できる

ただし、通貨コードの更新は単一フィールドの更新のため PATCH を使用する（エンドポイント3）。

### ページネーションを採用しない理由

個人利用のアプリであり、月単位では数十件、年単位でも数千件程度を想定している。
このデータ量であれば全件取得でパフォーマンス上の問題はなく、ページネーションの実装・保守コストに見合わない。
将来データ量が増加した場合はカーソル方式のページネーション導入を検討する。

### 分析エンドポイントを分離した理由

カテゴリ別集計と need/want 比率を別エンドポイントに分けた理由:

- 画面上では別セクションとして表示され、FE で個別にデータ取得・キャッシュ管理できる
- 将来、分析種別が増えた場合にエンドポイントを追加するだけで拡張できる
- 集計クエリが異なるため、バックエンドの実装もシンプルに保てる

### レスポンスに category_name を含める理由

Transaction のレスポンスに `category_id` だけでなく `category_name` も含めている理由:

- FE がカテゴリ一覧を別途取得して突き合わせる手間を省く
- 一覧画面の表示で即座にカテゴリ名を使えるため、FE の実装がシンプルになる
- カテゴリのデータ量が少なく、レスポンスサイズへの影響は微小

### memo のバリデーション上限

DB では TEXT 型（無制限）だが、API レベルで 2000 文字の上限を設ける。
個人の支出メモとして実用上十分であり、極端に大きなデータの送信を防ぐ。

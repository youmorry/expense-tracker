# API設計

## 概要

Expense Tracker の REST API 仕様を定義する。
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
| 命名規則 | URL: ケバブケース不使用・リソース名は複数形、JSON: camelCase |

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
  "idToken": "eyJhbGciOiJSUzI1NiIs..."
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| idToken | string | ○ | Google の ID トークン |

**レスポンス 200 OK**

```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "user": {
    "id": 1,
    "email": "user@gmail.com",
    "displayName": "Yuto",
    "currencyCode": null,
    "createdAt": "2026-02-23T10:30:00Z"
  }
}
```

| フィールド | 型 | 説明 |
|-----------|-----|------|
| accessToken | string | JWT アクセストークン |
| user | object | ユーザー情報 |
| user.currencyCode | string \| null | null の場合は通貨未設定（初回ログイン）。FE は通貨選択画面へ遷移する |

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
  "displayName": "Yuto",
  "currencyCode": "JPY",
  "createdAt": "2026-02-23T10:30:00Z"
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
  "currencyCode": "JPY"
}
```

| フィールド | 型 | 必須 | バリデーション |
|-----------|-----|------|--------------|
| currencyCode | string | ○ | ISO 4217 の有効な3文字コード |

**レスポンス 200 OK**

```json
{
  "id": 1,
  "email": "user@gmail.com",
  "displayName": "Yuto",
  "currencyCode": "JPY",
  "createdAt": "2026-02-23T10:30:00Z"
}
```

**レスポンス 400 Bad Request**

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
[
  { "id": 1, "name": "Food", "displayOrder": 1 },
  { "id": 2, "name": "Transport", "displayOrder": 2 },
  { "id": 3, "name": "Housing", "displayOrder": 3 },
  { "id": 11, "name": "Uncategorized", "displayOrder": 11 }
]
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
  "categoryId": 1,
  "needWantType": "NEED",
  "title": "Lunch",
  "memo": null
}
```

| フィールド | 型 | 必須 | デフォルト | バリデーション |
|-----------|-----|------|-----------|--------------|
| date | string | ○ | - | ISO 8601 日付形式 |
| amount | string | ○ | - | 正の数値。通貨に応じた小数桁数 |
| categoryId | number | - | Uncategorized の ID | 存在するカテゴリ ID |
| needWantType | string | - | `"UNSET"` | `NEED` \| `WANT` \| `UNSET` |
| title | string | - | null | 最大200文字 |
| memo | string | - | null | 最大2000文字 |

**レスポンス 201 Created**

```json
{
  "id": 42,
  "date": "2026-02-23",
  "amount": "1200",
  "categoryId": 1,
  "categoryName": "Food",
  "needWantType": "NEED",
  "title": "Lunch",
  "memo": null,
  "createdAt": "2026-02-23T10:30:00Z",
  "updatedAt": "2026-02-23T10:30:00Z"
}
```

**レスポンス 400 Bad Request**

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
| categoryId | number | - | - | カテゴリでフィルタ（複数指定: `categoryId=1&categoryId=3`） |
| needWantType | string | - | - | `NEED` \| `WANT` \| `UNSET` で絞り込み |
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
[
  {
    "id": 42,
    "date": "2026-02-23",
    "amount": "1200",
    "categoryId": 1,
    "categoryName": "Food",
    "needWantType": "NEED",
    "title": "Lunch",
    "memo": null,
    "createdAt": "2026-02-23T10:30:00Z",
    "updatedAt": "2026-02-23T10:30:00Z"
  }
]
```

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
  "categoryId": 1,
  "categoryName": "Food",
  "needWantType": "NEED",
  "title": "Lunch",
  "memo": "Company cafeteria",
  "createdAt": "2026-02-23T10:30:00Z",
  "updatedAt": "2026-02-23T10:30:00Z"
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
  "categoryId": 1,
  "needWantType": "NEED",
  "title": "Lunch (updated)",
  "memo": null
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
  "totalAmount": "130000",
  "categories": [
    {
      "categoryId": 1,
      "categoryName": "Food",
      "amount": "45000",
      "percentage": 34.6,
      "transactionCount": 28
    },
    {
      "categoryId": 3,
      "categoryName": "Housing",
      "amount": "30000",
      "percentage": 23.1,
      "transactionCount": 2
    }
  ]
}
```

| フィールド | 型 | 説明 |
|-----------|-----|------|
| totalAmount | string | 期間内の合計金額 |
| categories | array | カテゴリ別の集計。金額降順 |
| categories[].categoryId | number | カテゴリ ID |
| categories[].categoryName | string | カテゴリ名 |
| categories[].amount | string | カテゴリ合計金額 |
| categories[].percentage | number | 全体に占める割合（%、小数1桁） |
| categories[].transactionCount | number | 該当する支出の件数 |

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
  "totalAmount": "130000",
  "breakdown": [
    {
      "type": "NEED",
      "amount": "80000",
      "percentage": 61.5,
      "transactionCount": 45
    },
    {
      "type": "WANT",
      "amount": "35000",
      "percentage": 26.9,
      "transactionCount": 12
    },
    {
      "type": "UNSET",
      "amount": "15000",
      "percentage": 11.5,
      "transactionCount": 3
    }
  ]
}
```

| フィールド | 型 | 説明 |
|-----------|-----|------|
| totalAmount | string | 期間内の合計金額 |
| breakdown | array | NEED / WANT / UNSET の集計 |
| breakdown[].type | string | `NEED` \| `WANT` \| `UNSET` |
| breakdown[].amount | string | 合計金額 |
| breakdown[].percentage | number | 全体に占める割合（%、小数1桁） |
| breakdown[].transactionCount | number | 該当する支出の件数 |

**ルール**
- 3つの type は該当データが 0 件でもレスポンスに含める（amount: "0", percentage: 0.0, transactionCount: 0）
- UNSET の transactionCount は画面の「⚠ N transactions unset」表示に使用する

---

## 共通レスポンス形式

### エラーレスポンス

RFC 9457 Problem Details for HTTP APIs に準拠した形式で返す。

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/v1/transactions",
  "errors": [
    {
      "field": "amount",
      "message": "Must be greater than 0"
    },
    {
      "field": "date",
      "message": "Must be a valid date in ISO 8601 format"
    }
  ]
}
```

| フィールド | 型 | 説明 |
|-----------|-----|------|
| type | string | エラーの種別を示す URI。デフォルトは `about:blank` |
| title | string | HTTP ステータスの説明 |
| status | number | HTTP ステータスコード |
| detail | string | エラーの詳細メッセージ |
| instance | string | エラーが発生したリクエストのパス |
| errors | array | バリデーションエラーの詳細（400 の場合のみ） |

### HTTP ステータスコード一覧

| コード | 意味 | 使用場面 |
|--------|------|---------|
| 200 OK | 成功 | 取得・更新成功 |
| 201 Created | 作成成功 | 支出登録成功 |
| 204 No Content | 成功（ボディなし） | 削除成功 |
| 400 Bad Request | リクエスト不正 | バリデーションエラー、不正なパラメータ |
| 401 Unauthorized | 認証エラー | JWT なし・期限切れ・不正 |
| 403 Forbidden | 認可エラー | 他ユーザーのリソースへのアクセス（通常は 404 で隠す） |
| 404 Not Found | リソース不在 | 存在しない ID、他ユーザーの ID 指定 |
| 500 Internal Server Error | サーバーエラー | 予期しないエラー |

---

## リクエスト・レスポンスの型定義

### User

```typescript
interface User {
  id: number;
  email: string;
  displayName: string;
  currencyCode: string | null;
  createdAt: string; // ISO 8601
}
```

### Transaction

```typescript
interface Transaction {
  id: number;
  date: string;           // "2026-02-23"
  amount: string;         // "1200" (文字列で精度を保持)
  categoryId: number;
  categoryName: string;
  needWantType: "NEED" | "WANT" | "UNSET";
  title: string | null;
  memo: string | null;
  createdAt: string;      // ISO 8601
  updatedAt: string;      // ISO 8601
}
```

### TransactionRequest

```typescript
interface TransactionRequest {
  date: string;
  amount: string;
  categoryId?: number;
  needWantType?: "NEED" | "WANT" | "UNSET";
  title?: string | null;
  memo?: string | null;
}
```

### Category

```typescript
interface Category {
  id: number;
  name: string;
  displayOrder: number;
}
```

### CategoryAnalytics

```typescript
interface CategoryAnalytics {
  totalAmount: string;
  categories: {
    categoryId: number;
    categoryName: string;
    amount: string;
    percentage: number;
    transactionCount: number;
  }[];
}
```

### NeedWantAnalytics

```typescript
interface NeedWantAnalytics {
  totalAmount: string;
  breakdown: {
    type: "NEED" | "WANT" | "UNSET";
    amount: string;
    percentage: number;
    transactionCount: number;
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

### レスポンスに categoryName を含める理由

Transaction のレスポンスに `categoryId` だけでなく `categoryName` も含めている理由:

- FE がカテゴリ一覧を別途取得して突き合わせる手間を省く
- 一覧画面の表示で即座にカテゴリ名を使えるため、FE の実装がシンプルになる
- カテゴリのデータ量が少なく、レスポンスサイズへの影響は微小

### memo のバリデーション上限

DB では TEXT 型（無制限）だが、API レベルで 2000 文字の上限を設ける。
個人の支出メモとして実用上十分であり、極端に大きなデータの送信を防ぐ。

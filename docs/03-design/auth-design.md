# 認証設計

## 概要

Expense Tracker の認証・認可の仕組みを定義する。
Google OAuth2 によるソーシャルログインと、自前発行の JWT によるステートレス認証を組み合わせる。

---

## 認証フロー全体像

```mermaid
sequenceDiagram
    actor User as ブラウザ (ユーザー)
    participant FE as FE (React)
    participant BE as BE (Spring)
    participant Google as Google OAuth2

    User->>FE: 1. ログインボタン押下
    Note over FE,Google: 2-4 はポップアップ/オーバーレイ内で完結
    FE->>Google: 2. Google認証画面を表示
    Google->>User: 3. 認証・同意
    Google->>FE: 4. ID トークン返却（JSコールバック）
    FE->>BE: 5. POST /api/v1/auth/google<br>{ id_token: "..." }
    BE->>Google: 6. ID トークン検証（公開鍵で署名検証）
    Google-->>BE: 検証結果
    Note over BE: 7. ユーザー検索/作成<br>(新規: locale→通貨推定)
    Note over BE: 8. JWT 生成（HS256）
    BE-->>FE: 9. { access_token, user }
    Note over FE: 10. JWT をメモリに保持
    FE-->>User: 11. メイン画面表示
```

---

## Google OAuth2 連携

### FE 側（@react-oauth/google）

Google のクライアントサイド認証ライブラリを使用し、**暗黙的フロー（Implicit Flow）** で ID トークンを取得する。

| 項目 | 値 |
|------|-----|
| ライブラリ | `@react-oauth/google` |
| フロー | One Tap / Sign In With Google ボタン |
| 取得するもの | ID トークン（JWT 形式） |
| サーバーコード交換 | 不要（ID トークンを直接 BE に送信） |

```tsx
// ログイン画面のイメージ
<GoogleLogin
  onSuccess={(response) => {
    // response.credential に ID トークンが含まれる
    authApi.login(response.credential);
  }}
  onError={() => {
    // エラーハンドリング
  }}
/>
```

### BE 側（ID トークン検証）

FE から受け取った ID トークンを Google の公開鍵で検証する。

**検証項目**

| 項目 | 説明 |
|------|------|
| 署名検証 | Google の公開鍵（JWKS）で署名を検証 |
| iss | `https://accounts.google.com` であること |
| aud | 自アプリの Google Client ID と一致すること |
| exp | 有効期限内であること |

**ID トークンから取得するクレーム**

| クレーム | 用途 | DB カラム |
|---------|------|----------|
| sub | Google ユーザーの一意識別子 | users.google_id |
| email | メールアドレス | users.email |
| name | 表示名 | users.display_name |
| locale | 通貨コードの推定に使用 | - |

### ユーザーの検索・自動作成

ID トークン検証後、`sub` クレームで users テーブルを検索する。

| ケース | 処理 |
|--------|------|
| ユーザーが存在する | 既存ユーザーとして JWT を発行 |
| ユーザーが存在しない | 新規ユーザーを作成してから JWT を発行 |

### 通貨コードの自動設定

新規ユーザー作成時、Google ID トークンの `locale` クレームから通貨コードを推定し、`currency_code` に設定する。

**locale → 通貨コードのマッピング例**

| locale | 国 | 通貨コード |
|--------|-----|----------|
| `ja` | Japan | JPY |
| `en-US` | United States | USD |
| `en-GB` | United Kingdom | GBP |
| `de` | Germany | EUR |
| `zh-CN` | China | CNY |

- マッピングテーブルはバックエンドで管理する
- マッピングできない locale の場合は `USD` をフォールバックとする
- ユーザーが通貨を変更したい場合は設定画面から変更可能

---

## JWT 設計

### アクセストークンの構成

BE が自前で発行する JWT。Google の ID トークンとは別物。

**ヘッダー**

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**ペイロード**

```json
{
  "iss": "https://expense-tracker.example.com",
  "sub": "42",
  "email": "user@gmail.com",
  "iat": 1740300600,
  "exp": 1740387000
}
```

| クレーム | 型 | 説明 |
|---------|-----|------|
| iss | string | 発行者（`JWT_ISSUER` 環境変数で設定）。検証時に一致を確認する |
| sub | string | ユーザーの内部 ID（users.id）。文字列として格納（JWT 標準仕様） |
| email | string | メールアドレス（ログ・デバッグ用途） |
| iat | number | 発行日時（Unix タイムスタンプ） |
| exp | number | 有効期限（Unix タイムスタンプ） |

### 署名アルゴリズム

| 項目 | 値 | 理由 |
|------|-----|------|
| アルゴリズム | HS256（HMAC-SHA256） | 単一サーバー構成のため、共有秘密鍵で十分。RS256 より実装がシンプル |
| 秘密鍵 | 環境変数 `JWT_SECRET` | 最低 256 ビット（32 バイト）のランダム文字列 |

> **RS256 を採用しない理由**
> RS256（公開鍵/秘密鍵）は、複数のサービスが JWT を検証する必要がある場合に有効。
> 本アプリは単一の BE サーバーで完結するため、HS256 で十分であり、鍵管理もシンプルに保てる。

### トークンの有効期限

| 項目 | 値 | 理由 |
|------|-----|------|
| 有効期限 | 24 時間 | 個人利用アプリのため、利便性を優先。頻繁な再ログインを避ける |

> **リフレッシュトークンを採用しない理由**
> - 個人利用のアプリであり、セキュリティリスクが限定的
> - リフレッシュトークンの管理（DB 保存・ローテーション）は MVP の実装コストに見合わない
> - トークン失効時は再度 Google ログインすれば即座にトークンが再発行される
> - 将来的にセキュリティ要件が高まった場合にリフレッシュトークンを導入する

---

## FE のトークン管理

### 保存場所

| 方式 | 採用 | 理由 |
|------|------|------|
| メモリ（変数） | **採用** | XSS によるトークン窃取リスクが最も低い |
| localStorage | 不採用 | XSS で容易にアクセス可能 |
| sessionStorage | 不採用 | XSS で容易にアクセス可能 |
| Cookie（httpOnly） | 不採用 | CSRF 対策が追加で必要になる |

メモリ保持のため、ページリロード時にはトークンが失われる。
リロード時は Google の One Tap サイレント認証で再取得を試みる。サイレント認証に失敗した場合はログイン画面に遷移する。

### API リクエストへの付与

すべての認証必要 API に `Authorization` ヘッダーを付与する。

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

TanStack Query のグローバル設定または Axios / fetch のインターセプターで一元的に付与する。

### 認証エラー時の処理

| HTTP ステータス | FE の処理 |
|----------------|----------|
| 401 Unauthorized | メモリのトークンを破棄し、ログイン画面に遷移 |
| 403 Forbidden | 通常発生しない（API は 404 で返す設計）。発生した場合はエラー表示 |

---

## BE の認証・認可

### Spring Security 構成

Spring Security + OAuth2 Resource Server を使用して JWT 検証を行う。

**セキュリティフィルターチェーン**

```
リクエスト
  → CORS フィルター
  → JWT 認証フィルター（Bearer トークン検証）
  → 認可チェック
  → コントローラー
```

**エンドポイントのアクセス制御**

| パターン | アクセス |
|---------|--------|
| `POST /api/v1/auth/**` | 全員許可（`permitAll`） |
| `/api/v1/**` | 認証必要（`authenticated`） |
| `/v3/api-docs/**` | 全員許可（開発時のみ） |

### 認証情報の取得

JWT 検証成功後、`SecurityContextHolder` からユーザー情報を取得する。

```java
// コントローラーでの使用イメージ
@GetMapping("/api/v1/users/me")
public UserResponse getMe(@AuthenticationPrincipal Jwt jwt) {
    Long userId = Long.valueOf(jwt.getSubject()); // sub クレームから内部 ID を取得
    return userService.findById(userId);
}
```

### 認可ルール

| リソース | ルール | 実装方針 |
|---------|--------|---------|
| transactions | 所有ユーザーのみ操作可能 | Service 層で `userId` の一致を検証。不一致は 404 を返す |
| users/me | 自分自身のみ | JWT の `sub` から特定されるため、パス設計で担保 |
| categories | 全ユーザー共通・読み取り専用 | 認証のみで認可チェック不要 |

---

## CORS 設定

FE と BE が異なるオリジンで動作するため、CORS を適切に設定する。

| 項目 | 開発環境 | 本番環境 |
|------|---------|---------|
| Allowed Origins | `http://localhost:5173` | FE のデプロイ URL |
| Allowed Methods | GET, POST, PUT, PATCH, DELETE, OPTIONS | 同左 |
| Allowed Headers | Authorization, Content-Type | 同左 |
| Allow Credentials | false | false |

> `Allow Credentials: false` とする理由: JWT は Authorization ヘッダーで送信し、Cookie は使用しないため。

---

## 環境変数

認証に関連する環境変数の一覧。

| 変数名 | 設定先 | 説明 |
|--------|--------|------|
| `GOOGLE_CLIENT_ID` | BE / FE | Google OAuth2 クライアント ID |
| `JWT_SECRET` | BE | JWT 署名用の秘密鍵（最低 32 バイト） |
| `JWT_EXPIRATION_HOURS` | BE | JWT 有効期限（時間単位、デフォルト: 24） |

---

## セキュリティ対策

| 脅威 | 対策 |
|------|------|
| XSS によるトークン窃取 | JWT をメモリ保持（localStorage / sessionStorage 不使用） |
| CSRF | Cookie 不使用のため CSRF トークン不要 |
| ID トークン偽造 | Google の公開鍵（JWKS）で署名を検証 |
| JWT 改ざん | HS256 署名で完全性を保証 |
| IDOR（他ユーザーのリソースアクセス） | Service 層で userId の一致を検証。不一致は 404 を返す |
| トークン漏洩 | HTTPS 必須。有効期限を 24 時間に制限 |
| ブルートフォース | Google OAuth2 側でレート制限が適用される |

---

## 設計メモ

### Google 認証のみを採用した理由

- ユーザー登録・パスワード管理・メール確認・パスワードリセットなどの複雑な機能を排除できる
- Google アカウントの普及率が高く、個人利用のアプリとしてはこれで十分
- 将来的に他の認証プロバイダ（Apple、GitHub 等）を追加する場合も、同様の ID トークン検証方式で拡張可能

### サーバーサイドでのセッション管理を行わない理由

- JWT によるステートレス認証を採用し、サーバー側でセッション状態を持たない
- DB やインメモリストアでのセッション管理が不要で、実装・運用がシンプル
- Render の無料枠ではインスタンスがスリープ・再起動する可能性があり、サーバーサイドセッションは失われるリスクがある

### currency_code の初期値について

新規ユーザー作成時、Google ID トークンの `locale` クレームから通貨コードを推定し、`currency_code` に設定する。
これにより通貨選択の専用画面が不要になり、ユーザーは認証後すぐにメイン画面を利用できる。
通貨を変更したい場合は設定画面からいつでも変更可能。

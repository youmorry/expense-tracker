# ドメインモデル

## 概要

本アプリは「支出の記録・把握」に特化しているため、ドメインモデルはシンプルに保つ。
収入管理・口座管理・予算管理は対象外とし、支出記録とその分類に必要な概念のみを定義する。

---

## ドメインモデル図

```mermaid
classDiagram
    direction TB

    class User {
        <<Entity / AggregateRoot>>
        UserId id
        String googleId
        String email
        String displayName
        Currency currencyCode
        Instant createdAt
    }

    class Transaction {
        <<Entity / AggregateRoot>>
        TransactionId id
        UserId userId
        LocalDate date
        Money amount
        CategoryId categoryId
        NeedWantType needWantType
        String title
        String memo
        Instant createdAt
        Instant updatedAt
    }

    class CategoryType {
        <<Enumeration>>
        FOOD
        TRANSPORT
        HOUSING
        DAILY_GOODS
        MEDICAL
        ENTERTAINMENT
        CLOTHING
        EDUCATION
        SOCIAL
        OTHER
        UNCATEGORIZED
    }

    class Money {
        <<ValueObject>>
        BigDecimal value
    }

    class NeedWantType {
        <<Enumeration>>
        NEED
        WANT
        UNSET
    }

    User --> "java.util.Currency" : currencyCode
    Transaction --> User : userId で参照
    Transaction --> CategoryType : categoryId で参照
    Transaction --> Money : amount
    Transaction --> NeedWantType : needWantType
```

---

## エンティティ

### User（ユーザー）

Google OAuth2 で認証されたユーザーを表す。

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | UserId | ○ | 内部ID（自動採番） |
| googleId | String | ○ | Google アカウントの識別子（sub クレーム） |
| email | String | ○ | メールアドレス |
| displayName | String | ○ | 表示名 |
| currencyCode | java.util.Currency | ○ | 使用通貨（ISO 4217 コード。例: `JPY`, `USD`） |
| createdAt | Instant | ○ | 登録日時 |

**ルール**
- googleId はシステム内で一意
- 初回ログイン時に自動作成される（明示的なユーザー登録画面は持たない）
- currencyCode は初回ログイン時にブラウザの `Accept-Language` ヘッダーから通貨を推定して自動設定する
- currencyCode は後から変更可能。変更は表示記号・フォーマットの切り替えのみで、既存データの換算は行わない

---

### Transaction（支出記録）

アプリの中心となるエンティティ。ユーザーが記録する1件1件の支出。

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| id | TransactionId | ○ | 内部ID（自動採番） |
| userId | UserId | ○ | 所有ユーザー |
| date | LocalDate | ○ | 支出日 |
| amount | Money | ○ | 金額 |
| categoryId | CategoryId | ○ | カテゴリ（デフォルト: Uncategorized） |
| needWantType | NeedWantType | ○ | need / want 分類（デフォルト: UNSET） |
| title | String | - | 内容（例：「Lunch」「Train fare」） |
| memo | String | - | メモ |
| createdAt | Instant | ○ | 作成日時 |
| updatedAt | Instant | ○ | 更新日時 |

**ルール**
- 日付は未来日を許容する（立替や前払いなどのケースを考慮）
- カテゴリ未選択時は「Uncategorized」カテゴリが設定される
- need/want 未選択時は UNSET が設定される
- 支出記録は所有ユーザーのみが閲覧・編集・削除できる

---

---

## 値オブジェクト

### Money（金額）

金額を表す値オブジェクト。

| フィールド | 型 | 説明 |
|-----------|-----|------|
| value | BigDecimal | 金額の値 |

**ルール**
- null 不可
- 通貨ごとの小数桁数は ISO 4217 に準拠する（例: JPY → 0桁、USD → 2桁）
- バリデーション時はユーザーの currencyCode に基づいて許容する小数桁数を判定する

**設計判断**
- `BigDecimal` を使い浮動小数点の誤差を回避する
- 通貨情報は User.currencyCode で管理し、Money は純粋な金額値として保つ。これにより Transaction ごとに通貨を持つ必要がなく、モデルがシンプルになる
- 将来、Transaction 単位で通貨を持つ必要が生じた場合は `Money(value, currency)` へ拡張可能

---

## 列挙型

### CategoryType（カテゴリ）

支出を分類するためのカテゴリ列挙型。各定数が DB の `categories` テーブルの ID・表示名・表示順に対応する。
MVP ではプリセットのみ提供し、ユーザーによるカスタマイズは将来対応とする。

| 定数 | ID | 表示名 | 表示順 | 想定される用途 |
|------|-----|--------|-------|--------------|
| FOOD | 1 | Food | 1 | Groceries, dining out |
| TRANSPORT | 2 | Transport | 2 | Train, bus, taxi |
| HOUSING | 3 | Housing | 3 | Rent, utilities, internet |
| DAILY_GOODS | 4 | Daily Goods | 4 | Consumables, household items |
| MEDICAL | 5 | Medical | 5 | Hospital, medicine |
| ENTERTAINMENT | 6 | Entertainment | 6 | Hobbies, leisure, subscriptions |
| CLOTHING | 7 | Clothing | 7 | Apparel, dry cleaning |
| EDUCATION | 8 | Education | 8 | Books, seminars, certifications |
| SOCIAL | 9 | Social | 9 | Dining with friends, gifts |
| OTHER | 10 | Other | 10 | Anything not listed above |
| UNCATEGORIZED | 11 | Uncategorized | 11 | Default when no category is selected |

**ルール**
- カテゴリはシステム共通（全ユーザーで同じプリセットを使用）
- 「Uncategorized」は常に存在し、削除できない
- DB の `categories` テーブルの ID をソースコードに持つ（DB との整合性は Flyway マイグレーションで保証）
- 将来、ユーザー独自のカテゴリ作成を可能にする拡張を想定

---

### NeedWantType（need / want 分類）

支出を「必要」か「欲しい」かで分類する。あとから設定することも想定し、未設定状態を明示的に持つ。

| 値 | 説明 |
|----|------|
| NEED | 必要な支出（食費、家賃、通勤費など） |
| WANT | 欲しいから使った支出（趣味、外食、嗜好品など） |
| UNSET | 未設定（デフォルト） |

> 分析画面では UNSET の件数・金額を表示し、ユーザーに分類の振り返りを促す。

---

### 通貨コード（currencyCode）

ユーザーが使用する通貨を表す。Java 標準の `java.util.Currency`（ISO 4217 準拠）を使用する。

**ルール**
- 初回ログイン時にブラウザの `Accept-Language` ヘッダーから通貨を推定して自動設定する
- ユーザーは設定画面から変更可能
- JSON リクエストでは ISO 4217 通貨コード（例: `"JPY"`）で指定する
- ISO 4217 に含まれるすべての通貨を受け付ける

---

## 集約の設計

Spring Data JDBC の Aggregate / Repository パターンに沿って、集約を設計する。

### 集約の一覧

| 集約 | AggregateRoot | 含まれるオブジェクト |
|------|---------------|-------------------|
| User 集約 | User | なし（単体） |
| Transaction 集約 | Transaction | Money（値オブジェクト） |

### 集約間の参照

集約間は ID による参照とし、オブジェクト参照は持たない。
これは Spring Data JDBC の設計原則に従い、集約の独立性を保つため。

```
Transaction
  ├── userId: UserId         → User 集約への参照
  └── categoryId: CategoryId → CategoryType 列挙型の ID
```

---

## 未選択の表現方針

| フィールド | 未選択の表現 | 理由 |
|-----------|------------|------|
| categoryId | 「Uncategorized」カテゴリ（ID参照） | FK 制約が効く。集計クエリで null ハンドリングが不要になり、GROUP BY がシンプルになる |
| needWantType | UNSET（Enum値） | NOT NULL 制約が使える。分析画面で「未設定 ○件」と表示し、分類の振り返りを促せる |
| title | null | 任意の自由入力。空文字ではなく null で「未入力」を表現 |
| memo | null | 同上 |

---

## ID の型

`UserId`、`TransactionId`、`CategoryId` はプリミティブ型のラッパーとして定義する。
Java の Record を使い、型安全性を確保する。

```java
public record UserId(long value) {}
public record TransactionId(long value) {}
public record CategoryId(long value) {}
```

これにより、`findById(UserId)` と `findById(TransactionId)` のような引数の取り違えをコンパイル時に検出できる。

---

## 設計メモ

### Account（口座）を持たない理由

一般的な家計簿アプリでは「どの口座から支払ったか」を管理する Account エンティティを持つが、
本アプリは「何にいくら使ったか」の記録に特化するため、意図的に除外している。
これにより、入力項目が減り、初心者でも迷わず記録できるシンプルさを実現する。

### Budget（予算）を持たない理由

予算管理は支出記録の習慣化が定着した後のステップと位置づけ、MVP のスコープ外とした。
将来フェーズでの追加を想定し、Transaction に budget 関連のフィールドは含めていない。

### 通貨を User に持たせる理由

通貨情報を Transaction ではなく User に持たせることで、以下のメリットがある。

- Transaction のモデルがシンプルに保てる（全レコードに通貨カラムが不要）
- 「このユーザーの支出はすべて同じ通貨」という前提により、集計処理がシンプルになる
- 通貨変更は表示フォーマットの切り替えのみという要件と整合する

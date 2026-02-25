# データベーススキーマ

## 概要

[er-diagram.md](er-diagram.md) で定義したテーブル構成に対し、カラムの精度・制約・インデックスなどの詳細を定義する。
DDL は Flyway のマイグレーションスクリプトとして管理する。

---

## テーブル定義

### users

Google OAuth2 で認証されたユーザーを管理する。

| カラム | 型 | NULL | デフォルト | 制約 | 説明 |
|--------|-----|------|-----------|------|------|
| id | BIGINT | NO | GENERATED ALWAYS AS IDENTITY | PK | 内部ID |
| google_id | VARCHAR(255) | NO | - | UNIQUE | Google sub クレーム |
| email | VARCHAR(255) | NO | - | - | メールアドレス |
| display_name | VARCHAR(100) | NO | - | - | 表示名 |
| currency_code | CHAR(3) | NO | - | - | ISO 4217 通貨コード |
| created_at | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | - | 登録日時（UTC） |

**補足**
- google_id: Google の sub クレームは最大255文字の文字列
- email: RFC 5321 の実用的な上限として255文字を設定
- display_name: Google アカウントの表示名。100文字で実用上十分
- currency_code: ISO 4217 は3文字固定のため CHAR(3)。新規ユーザー作成時に Google ID トークンの locale から自動設定する

---

### categories

支出カテゴリのマスタデータ。MVP ではシステム共通のプリセットのみ。

| カラム | 型 | NULL | デフォルト | 制約 | 説明 |
|--------|-----|------|-----------|------|------|
| id | BIGINT | NO | GENERATED ALWAYS AS IDENTITY | PK | 内部ID |
| name | VARCHAR(50) | NO | - | UNIQUE | カテゴリ名 |
| display_order | INTEGER | NO | - | - | 表示順 |

**補足**
- name: カテゴリ名は短い英単語を想定。50文字で十分（将来のユーザー定義カテゴリにも対応可能）
- name に UNIQUE 制約を設定し、同名カテゴリの重複を防ぐ

---

### transactions

支出記録の本体。アプリの中心テーブル。

| カラム | 型 | NULL | デフォルト | 制約 | 説明 |
|--------|-----|------|-----------|------|------|
| id | BIGINT | NO | GENERATED ALWAYS AS IDENTITY | PK | 内部ID |
| user_id | BIGINT | NO | - | FK → users.id | 所有ユーザー |
| date | DATE | NO | - | - | 支出日 |
| amount | DECIMAL(12,4) | NO | - | CHECK (amount > 0) | 金額 |
| category_id | BIGINT | NO | - | FK → categories.id | カテゴリ |
| need_want_type | VARCHAR(5) | NO | 'UNSET' | CHECK (need_want_type IN ('NEED', 'WANT', 'UNSET')) | need/want 分類 |
| title | VARCHAR(200) | YES | NULL | - | 内容 |
| memo | TEXT | YES | NULL | - | メモ |
| created_at | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | - | 作成日時（UTC） |
| updated_at | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | - | 更新日時（UTC） |

**補足**

- **amount の精度 DECIMAL(12,4)**
  - 12桁の精度、4桁のスケールにより、整数部8桁（最大 99,999,999）まで対応
  - スケール4桁は ISO 4217 の最大小数桁数（例: CLF は4桁）に対応するため
  - 主要通貨の例: JPY → 0桁、USD/EUR → 2桁、BHD/KWD → 3桁、CLF → 4桁
  - アプリケーション側で通貨ごとの小数桁数バリデーションを行い、DB は最大桁数で受け入れる
- **need_want_type の VARCHAR(5)**
  - 最長の値は `UNSET` の5文字。VARCHAR(5) で過不足なく格納できる
- **title の VARCHAR(200)**
  - 支出の内容を簡潔に記録する用途。200文字で実用上十分
- **memo の TEXT**
  - 自由記述のため文字数制限を設けない。アプリケーション側で上限を設ける場合は別途バリデーションで対応

---

## 外部キー制約と ON DELETE

### ON DELETE とは

ON DELETE は「親テーブルのレコードが削除されたとき、それを参照している子テーブルのレコードをどう扱うか」を定義するルールである。

たとえば `transactions.user_id` は `users.id` を参照している。ここで users からあるユーザーを削除しようとすると、そのユーザーの支出記録が transactions に残っている場合、参照先が消えてデータの整合性が崩れる。ON DELETE はこの挙動を制御する。

| ルール | 挙動 |
|--------|------|
| CASCADE（連鎖削除） | 親を削除すると、子も自動的に削除される |
| RESTRICT（削除拒否） | 子が存在する限り、親の削除を拒否する |

### 本プロジェクトの設定

| カラム | 参照先 | ON DELETE | 理由 |
|--------|--------|-----------|------|
| transactions.user_id | users.id | CASCADE | ユーザー削除時に関連する支出記録もすべて削除する。孤立データを防ぐ |
| transactions.category_id | categories.id | RESTRICT | カテゴリに紐づく支出が存在する場合は削除を禁止する。データの整合性を保護する |

**設計判断**
- ユーザー削除は GDPR 等のデータ削除要求を想定し、CASCADE で関連データを一括削除する
- カテゴリ削除は RESTRICT で保護する。MVP ではプリセットのみで削除操作自体を想定しないが、将来のカスタムカテゴリ対応時にデータ不整合を防ぐ安全策として設定する

---

## インデックス設計

### 主キー・ユニークキー（自動作成）

| テーブル | インデックス | 種類 | 自動作成元 |
|---------|------------|------|-----------|
| users | users_pkey | PK | PRIMARY KEY (id) |
| users | users_google_id_key | UNIQUE | UNIQUE (google_id) |
| categories | categories_pkey | PK | PRIMARY KEY (id) |
| categories | categories_name_key | UNIQUE | UNIQUE (name) |
| transactions | transactions_pkey | PK | PRIMARY KEY (id) |

### 追加インデックス

| テーブル | インデックス名 | カラム | 用途 |
|---------|--------------|--------|------|
| transactions | idx_transactions_user_id_date | (user_id, date DESC) | 支出一覧の表示（ユーザーごとに日付の新しい順で取得） |
| transactions | idx_transactions_user_id_date_category | (user_id, date DESC, category_id) | カテゴリ別の集計・フィルタリング（期間指定 + カテゴリ） |

**設計判断**

- `idx_transactions_user_id_date`: 支出一覧画面のメインクエリ（ユーザーの支出を日付順で取得）を高速化する。ほぼすべてのクエリで user_id が条件に含まれるため、複合インデックスの先頭に配置
- `idx_transactions_user_id_date_category`: 分析画面のカテゴリ別集計を高速化する。分析画面は月/年の期間指定で使うケースが主であるため、date を category_id の前に配置し、期間絞り込み + カテゴリ集計を効率的に処理できるようにした
- need_want_type のインデックスは見送り: カーディナリティが3値と低く、user_id との複合インデックスの選択性向上効果が限定的。データ量が増えてパフォーマンス問題が発生した場合に再検討する
- created_at のインデックスは見送り: 支出一覧の表示順は `ORDER BY date DESC, created_at DESC`（同一日付内は登録の新しいもの順）だが、1日あたりの支出件数は数件〜十数件を想定しており、同一日付内のソートはインメモリで十分処理できる。インデックスに created_at を追加する効果は限定的なため見送る

**期間指定なし（「すべて」表示）の場合**

`(user_id, date DESC, category_id)` のインデックスで期間指定なしのカテゴリ別集計（`WHERE user_id = ? GROUP BY category_id`）を行う場合、date を飛び越えて category_id を活用することはできない。そのユーザーの全レコードを読んでからグループ化する動きになる。

ただし、本アプリは個人利用で1ユーザーのデータ量は年間数千件程度を想定している。user_id で絞り込んだ時点で対象レコードは十分に少ないため、期間指定なしのケースでも実用上のパフォーマンス問題にはならない。月/年の期間指定が主な利用パターンであることを優先し、この構成を採用した。

---

## Flyway マイグレーション

### ファイル構成

```
backend/src/main/resources/db/migration/
├── V1__create_users.sql
├── V2__create_categories.sql
├── V3__create_transactions.sql
└── V4__insert_preset_categories.sql
```

### V1__create_users.sql

```sql
CREATE TABLE users (
    id            BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    google_id     VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    currency_code CHAR(3)      NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_google_id UNIQUE (google_id)
);
```

### V2__create_categories.sql

```sql
CREATE TABLE categories (
    id            BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          VARCHAR(50) NOT NULL,
    display_order INTEGER     NOT NULL,

    CONSTRAINT uk_categories_name UNIQUE (name)
);
```

### V3__create_transactions.sql

```sql
CREATE TABLE transactions (
    id             BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id        BIGINT        NOT NULL,
    date           DATE          NOT NULL,
    amount         DECIMAL(12,4) NOT NULL,
    category_id    BIGINT        NOT NULL,
    need_want_type VARCHAR(5)    NOT NULL DEFAULT 'UNSET',
    title          VARCHAR(200),
    memo           TEXT,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transactions_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_category_id
        FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT,
    CONSTRAINT ck_transactions_amount_positive
        CHECK (amount > 0),
    CONSTRAINT ck_transactions_need_want_type
        CHECK (need_want_type IN ('NEED', 'WANT', 'UNSET'))
);

CREATE INDEX idx_transactions_user_id_date
    ON transactions (user_id, date DESC);

CREATE INDEX idx_transactions_user_id_date_category
    ON transactions (user_id, date DESC, category_id);
```

### V4__insert_preset_categories.sql

```sql
INSERT INTO categories (name, display_order) VALUES
    ('Food',           1),
    ('Transport',      2),
    ('Housing',        3),
    ('Daily Goods',    4),
    ('Medical',        5),
    ('Entertainment',  6),
    ('Clothing',       7),
    ('Education',      8),
    ('Social',         9),
    ('Other',         10),
    ('Uncategorized', 11);
```

---

## 設計メモ

### amount の精度について

DECIMAL(12,4) の選定根拠:

| 観点 | 値 | 説明 |
|------|-----|------|
| スケール（小数桁） | 4 | ISO 4217 の最大小数桁数に対応（CLF: 4桁） |
| 精度（全体桁数） | 12 | 整数部8桁で最大 99,999,999。個人の家計管理として十分 |

通貨ごとの小数桁数バリデーションはアプリケーション層で実施する。DB 側は最大桁数で統一的に受け入れることで、スキーマの通貨依存を排除する。

### TIMESTAMPTZ の使い方

すべてのタイムスタンプは TIMESTAMPTZ（timestamp with time zone）で保存する。PostgreSQL は入力値を UTC に変換して格納し、取得時にセッションのタイムゾーンに変換する。アプリケーション側では常に UTC で操作し、表示時にユーザーのタイムゾーンに変換する。

### need_want_type を VARCHAR にした理由

PostgreSQL の ENUM 型ではなく VARCHAR + CHECK 制約を採用した理由:
- ENUM 型は値の追加に `ALTER TYPE ... ADD VALUE` が必要で、トランザクション内で実行できないなどの制約がある
- VARCHAR + CHECK 制約は Flyway のマイグレーションで値の追加・変更が容易
- ER図で決定した方針を踏襲（er-diagram.md 参照）

### 論理削除を採用しない理由

論理削除（`deleted_at` カラムを追加し、DELETE ではなくフラグで「削除済み」とする方式）は不採用とし、物理削除（DELETE 文による実削除）を採用する。

- 個人利用のアプリで、データの所有者と操作者が同一人物。「誰かが勝手に消した」というケースがない
- 支出記録の削除は本人の意思による操作であり、復元ニーズは UX レベル（取り消し確認ダイアログ等）で対応する方が自然
- 全クエリに `WHERE deleted_at IS NULL` の条件を入れる運用コストが、個人アプリの規模に見合わない

### 監査カラム（created_by / updated_by）を持たない理由

「誰が操作したか」を記録する監査カラムは、複数ユーザーが同じデータを操作するシステムで有効だが、本アプリでは不採用とする。

- 支出記録の所有者（user_id）と操作者が常に同一であり、追加の価値がない
- `created_at` / `updated_at` は定義済みで、「いつ記録・更新したか」の時系列は追跡可能

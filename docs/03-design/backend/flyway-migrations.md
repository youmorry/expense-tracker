# Flyway マイグレーション

## 概要

データベーススキーマの変更は Flyway のマイグレーションスクリプトとして管理する。
テーブル定義の詳細は [database-schema.md](database-schema.md) を参照。

---

## ファイル構成

```
backend/src/main/resources/db/migration/
├── V1__create_users.sql
├── V2__create_categories.sql
├── V3__create_transactions.sql
├── V4__insert_preset_categories.sql
├── V5__drop_amount_positive_check.sql
├── V6__drop_currency_code_from_users.sql
└── V7__add_display_name_default.sql
```

---

## マイグレーション一覧

### V1__create_users.sql

```sql
CREATE TABLE users (
    id            BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    google_id     VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
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

### V5__drop_amount_positive_check.sql

amount の正値チェック制約を削除。バリデーションはアプリケーション層で行う方針に変更したため。

```sql
ALTER TABLE transactions DROP CONSTRAINT ck_transactions_amount_positive;
```

### V6__drop_currency_code_from_users.sql

通貨コードをフロントエンドで管理する方針に変更したため、users テーブルから削除。

```sql
ALTER TABLE users DROP COLUMN currency_code;
```

### V7__add_display_name_default.sql

display_name にデフォルト値を追加。Google アカウントに表示名が設定されていないケースに対応。

```sql
ALTER TABLE users ALTER COLUMN display_name SET DEFAULT 'USER';
```

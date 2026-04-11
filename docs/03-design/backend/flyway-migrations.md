# Flyway マイグレーション

## 概要

データベーススキーマの変更は Flyway のマイグレーションスクリプトとして管理する。
テーブル定義の詳細は [database-schema.md](database-schema.md) を参照。

## 格納先

```
backend/src/main/resources/db/migration/
```

## マイグレーション一覧

| No | 追加日 | ファイル名 | 概要 |
|----|--------|-----------|------|
| V1 | 2026-03-05 | [V1__create_users.sql](../../../backend/src/main/resources/db/migration/V1__create_users.sql) | users テーブルを作成 |
| V2 | 2026-03-05 | [V2__create_categories.sql](../../../backend/src/main/resources/db/migration/V2__create_categories.sql) | categories テーブルを作成 |
| V3 | 2026-03-05 | [V3__create_transactions.sql](../../../backend/src/main/resources/db/migration/V3__create_transactions.sql) | transactions テーブル・インデックスを作成 |
| V4 | 2026-03-05 | [V4__insert_preset_categories.sql](../../../backend/src/main/resources/db/migration/V4__insert_preset_categories.sql) | プリセットカテゴリ（11件）を投入 |
| V5 | 2026-03-29 | [V5__drop_amount_positive_check.sql](../../../backend/src/main/resources/db/migration/V5__drop_amount_positive_check.sql) | amount の正値チェック制約を削除（アプリ層でバリデーションする方針に変更） |
| V6 | 2026-04-03 | [V6__drop_currency_code_from_users.sql](../../../backend/src/main/resources/db/migration/V6__drop_currency_code_from_users.sql) | users から currency_code カラムを削除（フロントエンドで管理する方針に変更） |
| V7 | 2026-04-08 | [V7__add_display_name_default.sql](../../../backend/src/main/resources/db/migration/V7__add_display_name_default.sql) | display_name にデフォルト値 'USER' を追加 |

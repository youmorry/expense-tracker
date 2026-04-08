-- 開発用シードデータ（local プロファイル専用）
-- Repeatable Migration: 内容を変更すると次回起動時に再実行される

-- 開発用ユーザー（application-local.yml の subject と一致）
INSERT INTO users (google_id, email, display_name)
VALUES ('dev-user-001', 'dev@example.com', 'Dev User')
ON CONFLICT (google_id) DO NOTHING;

-- 開発用トランザクション（直近3ヶ月分、約80件）
INSERT INTO transactions (user_id, date, amount, category_id, need_want_type, title)
SELECT
    u.id,
    CURRENT_DATE - (random() * 90)::int,
    round((random() * 9900 + 100)::numeric, 0),
    (floor(random() * 10) + 1)::bigint,
    (ARRAY['NEED', 'WANT', 'UNSET'])[floor(random() * 3 + 1)::int],
    (ARRAY[
        'コンビニ', 'スーパー', '電車', 'ランチ', '書籍',
        'サブスク', '病院', 'カフェ', '映画', '日用品'
    ])[floor(random() * 10 + 1)::int]
FROM users u
CROSS JOIN generate_series(1, 80)
WHERE u.google_id = 'dev-user-001'
  AND NOT EXISTS (SELECT 1 FROM transactions WHERE user_id = u.id LIMIT 1);

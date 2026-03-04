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

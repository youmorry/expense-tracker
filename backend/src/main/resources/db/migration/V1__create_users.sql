CREATE TABLE users (
    id            BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    google_id     VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    currency_code CHAR(3)      NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_google_id UNIQUE (google_id)
);

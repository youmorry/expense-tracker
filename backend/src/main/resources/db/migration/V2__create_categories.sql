CREATE TABLE categories (
    id            BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          VARCHAR(50) NOT NULL,
    display_order INTEGER     NOT NULL,

    CONSTRAINT uk_categories_name UNIQUE (name)
);

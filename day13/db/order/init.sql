CREATE TABLE IF NOT EXISTS orders (
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    qty        INT    NOT NULL
);
